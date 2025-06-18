package edu.yu.cs.com1320.project.stage6.impl;


import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.System.nanoTime;

public class DocumentStoreImpl implements DocumentStore {
    private BTreeImpl<URI, Document> documentBTree;
    private Stack<Undoable> commandStack;
    private TrieImpl<URI> uriTrie;
    //private TrieImpl<URI> metaDataTrie; // To store concatenated metadata key value pairs, allowing for searching MD's without accessing btree.get, avoiding potentially accessing disk. Problem: metadata characters not guaranteed to be from ASCII character set, so trie would have to have arrays with over a million elements in case chinese/arabic/etc characters or emojis are included in the metadata key or value strings

    private MinHeap<UriWrapper> uriWrapperHeap;
    private Map<URI, Boolean> docInMemoryMap;

    private int currentDocCount;
    private int maxDocCount = -1;

    private int currentDocBytes;
    private int maxDocBytes = -1;

    /// Creates a DocStore whose BTree's persistence manager uses the default base directory for disk writes
    public DocumentStoreImpl(){
        this(null);
    }

    /// Creates a DocStore whose BTree's persistence manager uses a custom base directory for disk writes
    public DocumentStoreImpl(File baseDir){
        commandStack = new StackImpl<>();
        uriTrie = new TrieImpl<>();
        uriWrapperHeap = new MinHeapImpl<>();
        docInMemoryMap = new HashMap<>();

        documentBTree = new BTreeImpl<>();
        documentBTree.setPersistenceManager(new DocumentPersistenceManager(baseDir));
    }

    private class UriWrapper implements Comparable<UriWrapper> {
        URI uri;

        public UriWrapper(URI uri){
            this.uri = uri;
        }

        @Override
        public int compareTo(UriWrapper o) {
            Document docOfUri = DocumentStoreImpl.this.documentBTree.get(this.uri);
            Document otherDoc = DocumentStoreImpl.this.documentBTree.get(o.uri);

            assert docOfUri != null : "URI of a doc not found in btree should not be in heap, thus should never be coompared";
            assert otherDoc != null : "URI of a doc not found in btree should not be in heap, thus should never be coompared";

            return docOfUri.compareTo(otherDoc); //This returns long.compare(the last use times of the two docs)
        }

        @Override
        public boolean equals(Object o){
            if(! (o instanceof UriWrapper)) return false;
            if(o==this) return true;

            return this.uri.equals(((UriWrapper) o).uri);
        }

        @Override
        public int hashCode(){
            return this.uri.hashCode();
        }
    }

    /**
     * set the given key-value metadata pair for the document at the given uri
     *
     * @param uri
     * @param key
     * @param value
     * @return the old value, or null if there was no previous value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    @Override
    public String setMetadata(URI uri, String key, String value) throws IOException {
        if(uri == null || uri.toString().isEmpty()) throw new IllegalArgumentException("Null/empty URI provided!");
        if(this.documentBTree.get(uri) == null) throw new IllegalArgumentException("No such URI storing a document");
        if (key == null || key.isEmpty()) throw new IllegalArgumentException("Empty or Null Key provided!");

        //prep for a potential future undo:
        String oldValue = this.documentBTree.get(uri).getMetadataValue(key); //will be null if there was no value, some String otherwise
        Consumer<URI> undoSetMDvalue = theUri -> {
            this.documentBTree.get(theUri).setMetadataValue(key, oldValue); //resets value to prev value or null, null understood by BTreeImpl as delete
            boolean didAnything = nextStepsUponBringingFromDisk(theUri); //In case btree.get took from disk,
            if(!didAnything) moveToBottomOfHeap(theUri); //Sets Last Use time to now and Reheapifies, not needed if doc was just brought from disk, which would have inserted into heap at bottom
        };

        GenericCommand<URI> setMDValueCommand = new GenericCommand<>(uri, undoSetMDvalue);
        commandStack.push(setMDValueCommand);

        //Actually set the metadata value
        String results =  this.documentBTree.get(uri).setMetadataValue(key, value);

        nextStepsUponBringingFromDisk(uri);

        moveToBottomOfHeap(uri);

        return results;
    }

    /// Called when Btree.get() may have been called on a document on disk, automatically serializing it and bringing it into memory
    private boolean nextStepsUponBringingFromDisk(URI uri){
        if(docInMemoryMap.get(uri) == false){ //If document was previously on disk, not in memory
            putIntoMemory(documentBTree.get(uri));
            return true;
        }

        return false; //Doc was not just brought from disk, but in memory all along, do nothing.
    }

    private boolean moveBackToDiskIfWasThereBefore(URI uri){
        if(docInMemoryMap.get(uri) == false){ //ie uri used to be on disk
            try {
                documentBTree.moveToDisk(uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false; //Method did not need to do anything, document was in memory originally, no need to send to disk
    }

    /**
     * For docs that formerly did no exist, or were on disk
     *  1. Updates counts of bytes and docs in memory
     *  2. Sets value to true in docInMemoryMap for this doc's uri
     *  3. Makes lastUseTime of Doc right now (can be changed again later to synchronize last use times in bulk actions)
     *  4. Inserts doc into heap (and heapifies it to bottom/max, given that last use time is right now
     * @throws IllegalArgumentException if memory footprint of the document is larger than this.maxDocumentBytes
     */
    private void putIntoMemory(Document doc){
        int bytesOfMemoryToAdd = 0;

        if(doc.getDocumentTxt() != null){ //If least recently used doc is Text:
            bytesOfMemoryToAdd = doc.getDocumentTxt().getBytes().length;
        } else{ // If Binary doc:
            bytesOfMemoryToAdd = doc.getDocumentBinaryData().length;
        }

        currentDocBytes += bytesOfMemoryToAdd;
        currentDocCount++;


        docInMemoryMap.put(doc.getKey(), true);
        doc.setLastUseTime(nanoTime()); //This allows removal to not remove that which was just put into memory,
        //However, for bulk actions demanding that last use times of all involved docs are equivalent, calling method
        //will have to setlastusetime of all docs AGAIN, even after this method has been called
        uriWrapperHeap.insert(new UriWrapper(doc.getKey()));

        removeLeastRecentlyUsedDocs();
    }

    private void removeLeastRecentlyUsedDocs(){
        while((maxDocCount != -1) && currentDocCount > maxDocCount || (maxDocBytes != -1) && (currentDocBytes > maxDocBytes) ){
            //Remove from heap
            URI leastRecentUri = uriWrapperHeap.remove().uri;

            //Decrement Memory counters
            Document lru = documentBTree.get(leastRecentUri);

            decrementMemoryCounts(lru);

            //Send to disk vis a vis BTree
            assert docInMemoryMap.containsKey(leastRecentUri) : "this uri should certainly exist in either memory or disk";
            docInMemoryMap.put(leastRecentUri, false);
            try {
                documentBTree.moveToDisk(leastRecentUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Leave trie and commandStack as they are. They do not reference the document, just the URI, which takes up minimal memory.
        }
    }

    /**
     * @param uri is to become max of heap
     */
    private void moveToBottomOfHeap(URI uri) {
        Document doc = documentBTree.get(uri);
        doc.setLastUseTime(nanoTime());
        this.uriWrapperHeap.reHeapify(new UriWrapper(uri));
    }

    /**
     * get the value corresponding to the given metadata key for the document at the given uri
     *
     * @param uri
     * @param key
     * @return the value, or null if there was no value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    @Override
    public String getMetadata(URI uri, String key) throws IOException {
        if(uri == null || uri.toString().isEmpty()) throw new IllegalArgumentException("Null/empty URI provided!");
        if(this.documentBTree.get(uri) == null) throw new IllegalArgumentException("No such URI storing a document");
        if (key == null || key.isEmpty()) throw new IllegalArgumentException("Empty or Null Key provided!");

        Document doc = documentBTree.get(uri);

        boolean broughtFromDisk = nextStepsUponBringingFromDisk(uri);
        if(!broughtFromDisk) moveToBottomOfHeap(uri);

        return doc.getMetadataValue(key);
    }

    /**
     * @param input the document being put
     * @param url unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException if there is an issue reading input
     * @throws IllegalArgumentException if uri is null or empty, or format is null
     */
    @Override
    public int put(InputStream input, URI url, DocumentFormat format) throws IOException {
        if(url == null || url.toString().isEmpty()) throw new IllegalArgumentException("Null/empty URI provided!");
        if(format == null) throw new IllegalArgumentException("Null Format Provided");

        Document oldDoc = documentBTree.get(url);
        int returnValue = oldDoc != null ? oldDoc.hashCode() : 0;

        //Deleting:
        if(input == null){
            delete(url); //delete method contains its own undo logic
            return returnValue;
        }

        byte[] dataInBinary = input.readAllBytes();

        //Create binary doc, put it in BTree and its URI in heap, prepping for potential undo
        if(format == DocumentStore.DocumentFormat.BINARY){
            putBinaryDoc(url, dataInBinary);
        }

        //Create New text doc, put it in BTree, its URI in trie and heap, prepping for potential undo
        if(format == DocumentStore.DocumentFormat.TXT){
            putTextDoc(url, dataInBinary);
        }
        return returnValue;
    }

    private void decrementMemoryCounts(Document lru) {
        int bytesOfMemoryRemoved;

        if (lru == null) return;


        if(lru.getDocumentTxt() != null){ //If least recently used doc is Text:
            bytesOfMemoryRemoved = lru.getDocumentTxt().getBytes().length;
        } else{ // If Binary doc:
            bytesOfMemoryRemoved = lru.getDocumentBinaryData().length;
        }

        currentDocBytes -= bytesOfMemoryRemoved;
        currentDocCount--;
    }

    private void putTextDoc(URI url, byte[] dataInBinary) {
        Document oldDoc = documentBTree.get(url); // Null if there is no former Document

        if(oldDoc != null && docInMemoryMap.get(url).equals(true)){
            decrementMemoryCounts(oldDoc);
        }

        String stringOfTheText = new String(dataInBinary);

        // Create the doc (increment memory counts later, immediately after putting)
        Document newDoc = new DocumentImpl(url, stringOfTheText, null);

        //Undo prep:
        Consumer<URI> undoTextDocPut = uriParam -> {
            //Decrement memory count to reflect removal of newly put doc
            if(docInMemoryMap.get(uriParam).equals(true)){
                decrementMemoryCounts(newDoc); //Decrement memory count if newDoc is in memory when it is overwritten
            }

            deleteDocFromTrie(newDoc);

            //Remove newly put doc from heap (Note: this must be done before putting oldDoc back into btree)
            removeFromHeap(newDoc);

            //Replace newly put doc with old doc in BTree. If old doc == null, this will delete newDoc without putting anything in its place
            documentBTree.put(uriParam, oldDoc);

            //If there was an old Doc, add uri back to Trie and Heap and update memory counters
            if(oldDoc != null){
                putIntoMemory(oldDoc);
                putDocIntoTrie(oldDoc);
            } else{
                docInMemoryMap.remove(uriParam); //No need for this if oldDoc!=null, because putIntoMemory will put TRUE into this map on account of the newly put back oldDoc
            }

        };
        deleteDocFromTrie(oldDoc);

        if(oldDoc != null && docInMemoryMap.get(url).equals(true)){
            removeFromHeap(oldDoc);
        }

        documentBTree.put(url, newDoc);

        //Increment memory counts, put into docsinmemoryMap, insert (back) into heap
        putIntoMemory(newDoc);

        putDocIntoTrie(newDoc);

        GenericCommand<URI> putTextDocCommand = new GenericCommand<>(url, undoTextDocPut);
        commandStack.push(putTextDocCommand);
    }

    private void removeFromHeap(Document doc) {
        doc.setLastUseTime(Long.MIN_VALUE); //Make the formerly put's lastUseTime least recent.
        uriWrapperHeap.reHeapify(new UriWrapper(doc.getKey()));
        uriWrapperHeap.remove();
    }

    /// Deletes each word of a Document from DocumentTrie
    private void deleteDocFromTrie(Document oldDoc) {
        if(oldDoc != null) {
            for (String word : oldDoc.getWords()) {
                for (int i = 0; i < oldDoc.wordCount(word); i++) {
                    uriTrie.delete(word, oldDoc.getKey());
                }
            }
        }
    }

    ///  Puts every word of a Document into DocumentTrie
    private void putDocIntoTrie(Document doc) {
        if(doc == null) return;
        for (String word : doc.getWords()) {
            for (int i = 0; i < doc.wordCount(word); i++) {
                uriTrie.put(word, doc.getKey());
            }
        }
    }

    private void putBinaryDoc(URI url, byte[] dataInBinary) {
        Document oldDoc = documentBTree.get(url);
        if(oldDoc != null && docInMemoryMap.get(url).equals(true)){
            decrementMemoryCounts(oldDoc);
        }

        Document newDoc = new DocumentImpl(url, dataInBinary);

        // Undo prep
        Consumer<URI> undoBinaryPut = uriParam -> {
            if(docInMemoryMap.get(uriParam).equals(true)){
                decrementMemoryCounts(newDoc);
            }

            removeFromHeap(newDoc);

            documentBTree.put(uriParam, oldDoc);

            if(oldDoc != null){
                putIntoMemory(oldDoc);
            } else{
                docInMemoryMap.remove(uriParam);            }
        };

        GenericCommand<URI> putBinaryDocCommand = new GenericCommand<>(url, undoBinaryPut);

        if(oldDoc != null && docInMemoryMap.get(url).equals(true)){
            removeFromHeap(oldDoc);
        }

        documentBTree.put(url, newDoc);

        putIntoMemory(newDoc);

        commandStack.push(putBinaryDocCommand);
    }

    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document get(URI url) throws IOException {
        Document doc = documentBTree.get(url);
        if(doc != null){
            boolean broughtFromDisk = nextStepsUponBringingFromDisk(url); //for docs gotten from disk, includes logic to increment memory counts and insert into heap in the first place
            if(!broughtFromDisk) moveToBottomOfHeap(url);
        }
        return doc;
    }

    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean delete(URI url) {
        boolean docWasThere = documentBTree.get(url) != null;
        long originalLastUseTime = docWasThere ? documentBTree.get(url).getLastUseTime() : 0;
        GenericCommand<URI> deleteCommand = deleteAndReturnCommand(url, new IdenticalTimeStampProvider(), originalLastUseTime);
        commandStack.push(deleteCommand);
        return docWasThere;
    }

    ///A local class that can be called during deletes and undoDeletes of each doc, returning identical timeStamp each time
    private class IdenticalTimeStampProvider {
        Long timeStampForUndo;
        Long timeStampForDelete;

        public Long getTimeStampForUndo() {
            if (timeStampForUndo == null){
                timeStampForUndo = nanoTime(); //Sets time equal to the nanotime at moment of undoing of first doc.
            }
            return timeStampForUndo;
        }
        public Long getTimeStampForDelete() {
            if (timeStampForDelete == null){
                timeStampForDelete = nanoTime(); //Sets time equal to the nanotime at deletion of first doc
            }
            return timeStampForDelete;
        }
    }

    /**
     * @param url the unique identifier of the document to delete
     * Returns the Command containing the Undo Consumer that can undo delete
     * Actually deleted the document from Hashtable and Trie
     */
    private GenericCommand<URI> deleteAndReturnCommand(URI url, IdenticalTimeStampProvider timeStampProvider, long formerLastUseTime) {
        Document docToDelete = documentBTree.get(url);

        if(docToDelete != null) {
            if(docInMemoryMap.get(url).equals(true)){
                decrementMemoryCounts(docToDelete); //Decrement memory count if docToDelete was in memory previously, not just now taken off disk
                removeFromHeap(docToDelete);
            }

            documentBTree.put(url, null);

            assert docInMemoryMap.get(url).equals(true) : "When compiling list of docs to delete, even those on disk should have been brought to memory via docBTree.get";
            docInMemoryMap.remove(url);

            deleteDocFromTrie(docToDelete);

            docToDelete.setLastUseTime(formerLastUseTime); //Ensuring, as per update to specs, that deletes do NOT make any change to last use time. (Alternative would be to use the timeStampProvider, not only adjusting lastUseTime on a delete, but ensuring that multiple docs deleted in a bulk delete end up with the same last use time)
        }

        Consumer<URI> toUndoDelete = uri -> {
            if(docToDelete != null){ //But if the doc we are deleting was not even there to delete, do NOTHING to undo the so called "delete"
                Document doc = documentBTree.put(uri, docToDelete);

                putIntoMemory(docToDelete);

                putDocIntoTrie(docToDelete);

                docToDelete.setLastUseTime(timeStampProvider.getTimeStampForUndo()); //Each doc in a bulk delete will have SAME EXACT TIMESTAMP
                uriWrapperHeap.reHeapify(new UriWrapper(uri));
            }
        };

        GenericCommand<URI> deleteCommand = new GenericCommand<>(url, toUndoDelete);

        return deleteCommand;
    }

    /**
     * undo the last put, delete, or setMetaData command
     *
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        if(commandStack.peek() == null) throw new IllegalStateException("No action to be undone");

        //Either calls CommandSet.undo() or GenericCommand.undo(). Either gets the job done.
        commandStack.pop().undo();
    }

    /**
     * undo the last put, delete, or setMetaData that was done with the given URI as its key
     *
     * If the most recent command involving this uri was a deleteAll of a group, this will only undo
     * the delete of this specific uri. If this is the only remaining command in a commandset,
     * the entire CommandSet be POPPED off stack
     * @param url
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI url) throws IllegalStateException {
        Stack<Undoable> helperStack = new StackImpl<>();
        boolean notYetFoundUri = true;
        while(commandStack.peek() != null && notYetFoundUri) {
            if(commandStack.peek() instanceof GenericCommand && ((GenericCommand<?>) commandStack.peek()).getTarget().equals(url)){
                notYetFoundUri = false;
                break; //Found the right command. Break out of search loop
            }
            if(commandStack.peek() instanceof CommandSet && ((CommandSet<URI>) commandStack.peek()).containsTarget(url)){ //
                notYetFoundUri = false;
                break; //Found the right command. Break out of search loop
            }
            //We have peeked at next command and confirmed it does NOT have correct URI, so move it aside:
            helperStack.push(commandStack.pop());
        }

        //loop either stopped because it got to bottom of command stack or found URI:
        if(commandStack.peek() == null) throw new IllegalStateException("No command of given URI to undo");
        // Else, commandStack.peek() equals Command of correct URI. Undo said command:
        if(commandStack.peek() instanceof GenericCommand<?>){
            commandStack.pop().undo(); //No need to cast, undoable has no Param undo() in its API
        } else if (commandStack.peek() instanceof CommandSet<?>){ //We have a commandSet including a generic command on this uri
            ((CommandSet<URI>) commandStack.peek()).undo(url);
            if(((CommandSet<URI>) commandStack.peek()).size() == 0){
                commandStack.pop();
            }
        }

        //Push everything that was not undone back onto command stack
        while(helperStack.peek() != null){
            commandStack.push(helperStack.pop());
        }
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) throws IOException {
        List<URI> dummySortedSearchResults = getURIsWithKeywordUnsorted(keyword);
        List<Document> docs = new ArrayList<>();

        for(URI uri : dummySortedSearchResults){
            Document doc = documentBTree.get(uri);
            nextStepsUponBringingFromDisk(uri);
            docs.add(doc);
        }
        Comparator<Document> wordCountSorter = (doc1, doc2) ->doc2.wordCount(keyword) - doc1.wordCount(keyword);
        docs.sort(wordCountSorter);
        bulkSetLastUseTime(docs);

        return docs;
    }
    /**
     * As of stage6, no longer sorting via trie.getsorted(). Instead, passing trie.getSorted
     * a dummy comparator, such that all sorting can wait until list of URIs is whittled down
     * to only include URIs that ALSO match additional search requirements (for keyword and metadata or prefix and metadata)
     * searches. This way, the comparator, whose search involved calling btree.get.getwordcount, which rehydrates docs from disk,
     * is only used on the docs that are search results, and not on results of half the query.
     */
    private List<URI> getURIsWithKeywordUnsorted(String keyword) {
        if(keyword == null || keyword.isEmpty()) return Collections.emptyList();

        //Comparator<URI> wordCountSorter = (uri1, uri2) -> DocumentStoreImpl.this.documentBTree.get(uri2).wordCount(keyword) - DocumentStoreImpl.this.documentBTree.get(uri1).wordCount(keyword);
        Comparator<URI> wordCountDUMMYSorter = (uri1, uri2) -> 0;
        List<URI> unSortedUris = this.uriTrie.getSorted(keyword, wordCountDUMMYSorter);

        return unSortedUris;
    }

    private void bulkSetLastUseTime(Collection<Document> docCollection){
        long timeStampForAll = nanoTime();
        for (Document doc : docCollection){
            doc.setLastUseTime(timeStampForAll);
            if(docInMemoryMap.get(doc.getKey()).equals(true)){
                uriWrapperHeap.reHeapify(new UriWrapper(doc.getKey()));
            }
        }
    }

    /**
     * Retrieve all documents containing a word that starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException {

        List<URI> dummySortedSearchResults = getURIsWithPrefixSorted(keywordPrefix); //Empty list if null/empty keyword

        List<Document> trulySortedDocs = new ArrayList<>();
        for (URI uri : dummySortedSearchResults){
            Document doc = documentBTree.get(uri);
            nextStepsUponBringingFromDisk(uri);
            trulySortedDocs.add(documentBTree.get(uri));
        }
        trulySortedDocs.sort(getPrefixSortComparatorForDocs(keywordPrefix));
        bulkSetLastUseTime(trulySortedDocs);

        return trulySortedDocs;
    }

    private List<URI> getURIsWithPrefixSorted(String keywordPrefix) {
        if(keywordPrefix == null || keywordPrefix.isEmpty()) return Collections.emptyList();

        Comparator<URI> wordCountDUMMYSorter = (uri1, uri2) -> 0;
        List<URI> dummySortedUris =  uriTrie.getAllWithPrefixSorted(keywordPrefix, wordCountDUMMYSorter);

        return dummySortedUris;
    }

    private Comparator<Document> getPrefixSortComparatorForDocs(String keywordPrefix){
        return new Comparator<Document>() {
            @Override
            public int compare(Document doc1, Document doc2) {
                int doc1Count = 0;
                int doc2Count = 0;
                for (String word : doc1.getWords()){
                    if(word.startsWith(keywordPrefix)){ //If word has this prefix
                        doc1Count += doc1.wordCount(word); //Increment prefix count as many times as word with prefix exists in document
                    }
                }
                for (String word : doc2.getWords()){
                    if(word.startsWith(keywordPrefix)){
                        doc2Count += doc2.wordCount(word);
                    }
                }
                return doc2Count - doc1Count;
            }
        };
    }

    /**
     * Completely remove any trace of any document in the provided list
     * @return a Set of URIs of the documents that were deleted.
     * A list of a singular document will result in a commandSet of a singular generic command
     * An empty list will result in a commandSet that is empty. User will call undo() to pop that off the stack, not actually undoing anything of significance (but for all they know the deleteAll did something, so they will think that they have to undo it) and then the next undo call will undo previous command
     */
    private Set<URI> deleteAllDocsInList(List<Document> docsToDelete) {
        Set<URI> deletedURIs = new HashSet<>();

        //Prep for Undo:
        CommandSet<URI> deleteAllCommand= new CommandSet<>();

        //Instantiate class that will provide the same timestamps (one for deletes and one for undos) for each of the deleted docs
        IdenticalTimeStampProvider timeStampProvider = new IdenticalTimeStampProvider();

        //Actually deletes and further preps for undo:
        for (Document doc : docsToDelete){
            URI uri = doc.getKey();
            deleteAllCommand.addCommand(this.deleteAndReturnCommand(uri, timeStampProvider, doc.getLastUseTime()));
            deletedURIs.add(uri);
        }
        commandStack.push(deleteAllCommand);

        return deletedURIs;
    }



    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        List<URI> URIsWithKeyword = getURIsWithKeywordUnsorted(keyword); //Not actually sorted, comparator had no logical ordering
        List<Document> docsWithKeyword = new ArrayList<>();
        for(URI uri : URIsWithKeyword){
            docsWithKeyword.add(documentBTree.get(uri)); //Not calling nextSteps...(),
        }

        return deleteAllDocsInList(docsWithKeyword); //Undo logic taken care of there
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        List<URI> URIsWithPrefix = getURIsWithPrefixSorted(keywordPrefix);

        List<Document> docsWithPrefix = new ArrayList<>();
        for(URI uri : URIsWithPrefix){
            docsWithPrefix.add(documentBTree.get(uri)); //Not calling nextStepsUponBringingIntoMemory(), which would reflect that the document has now technically been brought into memory, so that delete method called in deleteAllDocsInList method can know whether the doc being deleted originates from disk or memory
        }

        return deleteAllDocsInList(docsWithPrefix); //Undo logic taken care of there
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains ALL OF the given values for the given keys. If no documents contain all the given key-value pairs, return an empty list.
     */
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) throws IOException {
        List<Document> docsWithMetadata = getDocsWithMetaDataSorted(keysValues);

        for(Document doc: docsWithMetadata){
            URI uriBroughtToMemory = doc.getKey();
            nextStepsUponBringingFromDisk(uriBroughtToMemory);
        }
        bulkSetLastUseTime(docsWithMetadata);
        return docsWithMetadata;
    }

    private List<Document> getDocsWithMetaDataSorted(Map<String, String> keysValues) {
        if(keysValues == null) return Collections.emptyList();

        List<Document> docsWithMetadata = new ArrayList<>();

        for (URI uri : this.docInMemoryMap.keySet()){ //All the Documents in the <URI, Document> hashmap, including those on disk
            Document doc = documentBTree.get(uri);
            boolean hasAll = true;
            for (String key: keysValues.keySet()){
                if(doc.getMetadataValue(key)==null || !doc.getMetadataValue(key).equals(keysValues.get(key))){
                    hasAll = false;
                }
            }
            if(hasAll){
                docsWithMetadata.add(doc);
                // calling methods will call nextStepsUpon...() or movebacktodisk() on each uri, depending on whether further search criteria are met
            } else{
                moveBackToDiskIfWasThereBefore(uri);
            }
        }
        return docsWithMetadata;
    }

    /**
     * Retrieve all documents whose text contains the given keyword AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        List<Document> docsWithBoth = getDocsWithKeywordAndMetaData(keyword, keysValues);

        bulkSetLastUseTime(docsWithBoth);

        return docsWithBoth;
    }


    private List<Document> getDocsWithKeywordAndMetaData(String keyword, Map<String, String> keysValues) {
        List<Document> docsWithMetaData = getDocsWithMetaDataSorted(keysValues);
        List<URI> dummySortedURIsWithKeyword = getURIsWithKeywordUnsorted(keyword);

        List<Document> docsWithBoth = new ArrayList<>();

        for (Document doc: docsWithMetaData){ //If any of these docs were formerly on disk, they were brought into memory during getDocsWithMetaDataSorted()
            URI uriWithMetaData = doc.getKey();
            if(dummySortedURIsWithKeyword.contains(uriWithMetaData)){
                docsWithBoth.add(doc);
                nextStepsUponBringingFromDisk(uriWithMetaData);
            } else{
                moveBackToDiskIfWasThereBefore(uriWithMetaData);
            }
        }

        Comparator<Document> wordCountSorter = (doc1, doc2) -> doc2.wordCount(keyword) - doc1.wordCount(keyword); //Descending order
        docsWithBoth.sort(wordCountSorter);
        return docsWithBoth;
    }


    /**
     * Same as getDocsWithKeywordAndMetaData method, but without calling nextStepsUponBringingFromDisk method,
     * as even if doc was brought from disk to memory, adjusting memory counters accordingly would potentially kick other documents out of the memory to make room,
     * when really we should just wait for the conclusion of the deletion method, which will delete this doc that was temporarily brought into memory.
     */
    private List<Document> forDeleteGetDocsWithKeywordAndMetaData(String keyword, Map<String, String> keysValues) {
        List<Document> docsWithMetaData = getDocsWithMetaDataSorted(keysValues);
        List<URI> dummySortedURIsWithKeyword = getURIsWithKeywordUnsorted(keyword);

        List<Document> docsWithBoth = new ArrayList<>();

        for (Document doc: docsWithMetaData){
            URI uriWithMetaData = doc.getKey(); //getDocsWithMetaDataSorted called docBTree.get to get each doc, hence bringing disk docs to memory

            if(dummySortedURIsWithKeyword.contains(uriWithMetaData)){
                docsWithBoth.add(doc);
                //DOES NOT CALL NEXT STEPS METHOD, such that delete method to be called imminently can work properly, checking if that which is being deleted was just now on memory to decide if it will decrement (and i cannot just increment now, before deleting, and decrement right after during delete, because the increment may erroneously knock other docs out of memory if memory limits are reached, and deleting docs on disk should not be knocking other docs out of memory
            } else{
                moveBackToDiskIfWasThereBefore(uriWithMetaData);
            }
        }

        Comparator<Document> wordCountSorter = (doc1, doc2) -> doc2.wordCount(keyword) - doc1.wordCount(keyword); //Descending order
        docsWithBoth.sort(wordCountSorter);
        return docsWithBoth;
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
        List<Document> docsWithBoth = getDocsWithPrefixAndMetaDataSorted(keywordPrefix, keysValues);

        bulkSetLastUseTime(docsWithBoth);

        return docsWithBoth;
    }


    private List<Document> getDocsWithPrefixAndMetaDataSorted(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> docsWithMetaData = this.getDocsWithMetaDataSorted(keysValues);
        List<URI> URIsWithPrefix = this.getURIsWithPrefixSorted(keywordPrefix);

        List<Document> docsWithBoth = new ArrayList<>();

        for (Document doc : docsWithMetaData){
            URI uriWithMetaData = doc.getKey(); //getDocsWithMetaDataSorted called docBTree.get to get each doc, hence bringing disk docs to memory

            if(URIsWithPrefix.contains(uriWithMetaData)){
                docsWithBoth.add(doc);
                nextStepsUponBringingFromDisk(uriWithMetaData);
            } else{
                moveBackToDiskIfWasThereBefore(uriWithMetaData);
            }
        }

        docsWithBoth.sort(getPrefixSortComparatorForDocs(keywordPrefix));
        return docsWithBoth;
    }

    private List<Document> forDeleteGetDocsWithPrefixAndMetaDataSorted(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> docsWithMetaData = this.getDocsWithMetaDataSorted(keysValues);
        List<URI> URIsWithPrefix = this.getURIsWithPrefixSorted(keywordPrefix);

        List<Document> docsWithBoth = new ArrayList<>();

        for (Document doc : docsWithMetaData){
            URI uriWithMetaData = doc.getKey(); //getDocsWithMetaDataSorted called docBTree.get to get each doc, hence bringing disk docs to memory

            if(URIsWithPrefix.contains(uriWithMetaData)){
                docsWithBoth.add(doc);
                //DOES NOT CALL next steps method, in order for subsequent deleting method to function properly
            } else{
                moveBackToDiskIfWasThereBefore(uriWithMetaData);
            }
        }

        docsWithBoth.sort(getPrefixSortComparatorForDocs(keywordPrefix));
        return docsWithBoth;
    }

    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) throws IOException {
        List<Document> docsWithMD = getDocsWithMetaDataSorted(keysValues); //not calling nextSteps...()
        return deleteAllDocsInList(docsWithMD);
    }

    /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        List<Document> docsWithKeywordAndMD = forDeleteGetDocsWithKeywordAndMetaData(keyword, keysValues);
        return deleteAllDocsInList(docsWithKeywordAndMD);
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
        List<Document> docsWithPrefixAndMD = forDeleteGetDocsWithPrefixAndMetaDataSorted(keywordPrefix, keysValues);
        return deleteAllDocsInList(docsWithPrefixAndMD);
    }

    /**
     * set maximum number of documents that may be stored
     *
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentCount(int limit) {
        if(limit<1) throw new IllegalArgumentException("Memory limit can't be less than 1");

        maxDocCount = limit;

        removeLeastRecentlyUsedDocs(); // First checks if it needs to remove anything at all
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     *
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentBytes(int limit) {
        if(limit<1) throw new IllegalArgumentException("Memory limit can't be less than 1");

        maxDocBytes = limit;

        removeLeastRecentlyUsedDocs(); //First checks if it needs to remove anything at all
    }
}
