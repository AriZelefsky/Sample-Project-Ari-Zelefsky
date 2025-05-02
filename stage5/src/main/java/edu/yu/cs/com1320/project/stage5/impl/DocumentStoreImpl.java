package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import static java.lang.System.nanoTime;


public class DocumentStoreImpl implements DocumentStore{
    private HashTable<URI, Document> documents;
    private Stack<Undoable> commandStack;
    private TrieImpl<Document> documentTrie;
    private MinHeap<Document> documentheap;

    private int currentDocCount;
    private int maxDocCount = -1;

    private int currentDocBytes;
    private int maxDocBytes = -1;


    public DocumentStoreImpl(){
        documents = new HashTableImpl<>();
        commandStack = new StackImpl<>();
        documentTrie = new TrieImpl<>();
        documentheap = new MinHeapImpl<>();
    }
    /**
     * set the given key-value metadata pair for the document at the given uri
     * @param uri
     * @param key
     * @param value
     * @return the old value, or null if there was no previous value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    @Override
    public String setMetadata(URI uri, String key, String value){
        if(uri == null || uri.toString().isEmpty()) throw new IllegalArgumentException("Null/empty URI provided!");
        if(this.documents.get(uri) == null) throw new IllegalArgumentException("No such URI storing a document");
        if (key == null || key.isEmpty()) throw new IllegalArgumentException("Empty or Null Key provided!");

        //prep for a potential future undo:
        String oldValue = this.documents.get(uri).getMetadataValue(key); //will be null if there was no value, some String otherwise
        Consumer<URI> undoSetMDvalue = theUri -> {
            this.documents.get(theUri).setMetadataValue(key, oldValue); //resets value to prev value or null, null understood by HashTableImpl as delete
            
            // Update Heap
            makeMinOfHeap(theUri); //Sets Last Use time to now and Reheapifies
        };
        GenericCommand<URI> setMDValueCommand = new GenericCommand<>(uri, undoSetMDvalue);
        commandStack.push(setMDValueCommand);

        //Update Heap
        makeMinOfHeap(uri);

        //Actually set the metadata value
        return this.documents.get(uri).setMetadataValue(key, value); //get(uri) pulls up the Document object at this uri, on which we set metadatavalue
    }

    /**
     * @param uri is uri of doc that is to become max of heap
     */
    private void makeMinOfHeap(URI uri) {
        Document doc = documents.get(uri);
        doc.setLastUseTime(nanoTime());
        this.documentheap.reHeapify(doc);
    }

    /**
     * @param doc is the actual doc that is to become max of heap
     */
    private void makeMinOfHeap(Document doc) {
        doc.setLastUseTime(nanoTime());
        this.documentheap.reHeapify(doc);
    }

    /**
     * get the value corresponding to the given metadata key for the document at the given uri
     * @param uri
     * @param key
     * @return the value, or null if there was no value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */

    @Override
    public String getMetadata(URI uri, String key){
        if(uri == null || uri.toString().isEmpty()) throw new IllegalArgumentException("Null/empty URI provided!");
        if(this.documents.get(uri) == null) throw new IllegalArgumentException("No such URI storing a document");
        if (key == null || key.isEmpty()) throw new IllegalArgumentException("Empty or Null Key provided!");

        Document doc = documents.get(uri);

        makeMinOfHeap(doc);

        return doc.getMetadataValue(key);
    }


    /**
     * @param input the document being put
     * @param url unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException if there is an issue reading input
     * @throws IllegalArgumentException if url is null or empty, or format is null,
     *      OR IF THE MEMORY FOOTPRINT OF THE DOCUMENT IS > MAX DOCUMENT BYTES   (because even if we would remove all former docs, there would still be no room)
     */
    @Override
    public int put(InputStream input, URI url, DocumentStore.DocumentFormat format) throws IOException{
        if(url == null || url.toString().isEmpty()) throw new IllegalArgumentException("Null/empty URI provided!");
        if(format == null) throw new IllegalArgumentException("Null Format Provided");
        //in putTextDoc/putBinary, in updateCurrentMemoryCounts(), throws exception if Memory footprint of Doc is > MAX DOC BYTES

        boolean urlAlreadyHasDoc = documents.containsKey(url);
        int returnValue = urlAlreadyHasDoc ? documents.get(url).hashCode() : 0;
        
        //Deleting:
        if(input == null){ //if input is null, delete a doc and return its hashcode, or return 0 if there was no such doc to delete
            delete(url); //delete method contains undo logic
            return returnValue;
        }

        byte[] dataInBinary = input.readAllBytes(); //automatically throws IO Exception if issue reading input
        
        //Create binary doc, put it in hashtable and heap, prepping for potential undo
        if(format == DocumentStore.DocumentFormat.BINARY){
            putBinaryDoc(url, dataInBinary);
        }

        //Create New text doc, put it in hashtable, trie, and heap, prepping for potential undo
        if(format == DocumentStore.DocumentFormat.TXT){
            putTextDoc(url, dataInBinary);
        }
        return returnValue;
    }

    /**
     * Update the current counts of bytes and docs in memory to reflect an impending put
     * @throws IllegalArgumentException if memory footprint of the document is larger than this.maxDocumentBytes
     */
    private void updateMemoryCounters(int bytesToAdd){
        if(maxDocBytes != -1 && bytesToAdd > maxDocBytes){
            throw new IllegalArgumentException("MEMORY FOOTPRINT OF THE DOCUMENT IS > MAX DOCUMENT BYTES");
        }
        currentDocBytes += bytesToAdd;
        currentDocCount++;

        //Loop condition inside this method checks if it should remove anything at all
        removeLeastRecentlyUsedDocs();

    }

    private void removeLeastRecentlyUsedDocs(){
        while((maxDocCount != -1) && currentDocCount > maxDocCount || (maxDocBytes != -1) && (currentDocBytes > maxDocBytes) ){
            decrementMemoryCounts(documentheap.peek());
            removeAllTraces(documentheap.remove());
        }
    }

    private int decrementMemoryCounts(Document lru) {
        int bytesOfMemoryRemoved = 0;

        if (lru == null) return 0; //if a null doc was passed in, do nothing, return that 0 bytes were removed


        if(lru.getDocumentTxt() != null){ //If least recently used doc is Text:
            bytesOfMemoryRemoved = lru.getDocumentTxt().getBytes().length;
        } else{ // If Binary doc:
            bytesOfMemoryRemoved = lru.getDocumentBinaryData().length;
        }

        currentDocBytes -= bytesOfMemoryRemoved;
        currentDocCount--;

        return bytesOfMemoryRemoved; //so that the undo in a calling method can easily put back the amount of bytes removed
    }

    /// Does not remove from heap. Calling method should call this method on minheap.remove
    private void removeAllTraces(Document doc){
        URI uri = doc.getKey();

        // Hashtable removal
        if(documents.get(uri) == doc){ //If doc is in Hashtable (not some other doc at this uri)
            documents.put(uri, null); //Delete from hashtable (will not affect a different d
        }

        // Trie removal
        deleteDocFromTrie(doc); //For each word in the doc, this doc will be removed (if currently there) from that word node in trie

        // CommandStack removal
        expungeCommandStackOfReferencesTo(uri);
    }

    private void expungeCommandStackOfReferencesTo(URI uri) {
        Stack<Undoable> helperStack = new StackImpl<>();

        while(commandStack.peek() != null) {
            if (commandStack.peek() instanceof GenericCommand && ((GenericCommand<?>) commandStack.peek()).getTarget().equals(uri)) {
                commandStack.pop(); //Pop off command Stack and DON'T add to helper stack
            } else if (commandStack.peek() instanceof CommandSet && ((CommandSet<URI>) commandStack.peek()).containsTarget(uri)) {
                CommandSet<URI> replacementCommandSet = new CommandSet<>();
                CommandSet<URI> oldCommandSet = (CommandSet<URI>) commandStack.peek();

                for (GenericCommand<URI> cmd : oldCommandSet) {
                    if (!cmd.getTarget().equals(uri)) {
                        replacementCommandSet.addCommand(cmd);
                    }
                }
                commandStack.pop(); //Remove the old commandSet and DON'T push to helper stack
                if (!replacementCommandSet.isEmpty()) {
                    helperStack.push(replacementCommandSet);
                }
            } else { //This command does not include the Doc being expunged from the store
                helperStack.push(commandStack.pop());
            }
        }

        //Push everything that was not uriToBeExpunged-related back onto command stack
        while(helperStack.peek() != null){
            commandStack.push(helperStack.pop());
        }
    }

    /// Called by public PUT method to put text document into hashtable, trie, and heap, while prepping for future undoing
    private void putTextDoc(URI url, byte[] dataInBinary) {
        Document oldDoc = documents.get(url); //will be null if there is no former Document

        //Free up space in memory counter by first decrementing by amount of bytes of old doc
        int bytesRemovedFromMemory = (oldDoc == null) ? 0: decrementMemoryCounts(oldDoc);

        String stringOfTheText = new String(dataInBinary);

        //Throw exception if it takes up too much memory, increments memoryCounters prior to putting if not.
        this.updateMemoryCounters(stringOfTheText.getBytes().length);

        // Create the doc
        Document newDoc = new DocumentImpl(url, stringOfTheText);

        //Undo prep:
        Consumer<URI> undoTextDocPut = uriParam -> {
            //Decrement memory count to reflect removal of newlyput do
            this.decrementMemoryCounts(newDoc);

            //Remove newly put doc from trie
            deleteDocFromTrie(newDoc);

            //Remove newly put doc from heap
            removeFromHeap(newDoc);

            //If there was an old Doc, add back to Trie and Heap and update memory counters
            if(oldDoc != null){
                this.updateMemoryCounters(bytesRemovedFromMemory);

                putDocIntoTrie(oldDoc);

                oldDoc.setLastUseTime(nanoTime()); //Will be called when lamda is invoked, not when putTextDoc is first called
                documentheap.insert(oldDoc); //Will insert at bottom/max because last use time was just made most recent
            }

            //Replace newly put doc with old doc in hashtable. If old doc == null, this will delete newDoc without putting anything in its place
            documents.put(uriParam, oldDoc);

        };

        // Put newDoc into HashTable
        documents.put(url, newDoc);

        // Put newDoc into Trie
        putDocIntoTrie(newDoc);

        // Remove oldDoc from trie
        deleteDocFromTrie(oldDoc);

        //Add putCommand to Command Stack
        GenericCommand<URI> putTextDocCommand = new GenericCommand<>(url, undoTextDocPut);
        commandStack.push(putTextDocCommand);

        //remove old doc from heap
        if(oldDoc != null){
            removeFromHeap(oldDoc);
        }

        //add new doc to Heap
        newDoc.setLastUseTime(nanoTime());
        documentheap.insert(newDoc);
    }

    private void removeFromHeap(Document doc) {
        doc.setLastUseTime(Long.MIN_VALUE); //Make the formerly put's lastUseTime least recent.
        documentheap.reHeapify(doc);
        documentheap.remove();
    }


    /// Deletes each word of a Document from DocumentTrie
    private void deleteDocFromTrie(Document oldDoc) {
        if(oldDoc != null) {
            for (String word : oldDoc.getWords()) { //For each Word String in Doc being added to Store
                for (int i = 0; i < oldDoc.wordCount(word); i++) { //For each occurrence of said word
                    documentTrie.delete(word, oldDoc); //Remove Document from Word's node in trie
                }
            }
        }
    }


    ///  Puts every word of a Document into DocumentTrie
    private void putDocIntoTrie(Document doc) {
        if(doc == null) return; //This should never be the case. Never call on null.
        for (String word : doc.getWords()) { //For each Word String in Doc being added to Store
            for (int i = 0; i < doc.wordCount(word); i++) { //For each occurrence of said word
                documentTrie.put(word, doc); //Add Document to Word's node in trie
            }
        }
    }

    /// Called by public PUT method to put BINARY document into hashtable, prepping for future undoing
    private void putBinaryDoc(URI url, byte[] dataInBinary) {
        //First reduce memory counters by old doc, if there is one
        Document oldDoc = documents.get(url); //null if there is no former Document.
        int bytesRemovedFromMemory = (oldDoc == null) ? 0: decrementMemoryCounts(oldDoc);

        Document newDoc = new DocumentImpl(url, dataInBinary);

        //Throws exception if, even after having reduced memory count by oldDoc's bytes, it takes up too much memory
        // Updates memoryCounters prior to putting if not (including removing LRU docs to make room, if needed)
        this.updateMemoryCounters(dataInBinary.length);

        // Undo prep
        Consumer<URI> undoBinaryPut = uriParam -> {
            //Remove newDoc from memory counters (must happen before calling update memory count, lest we unnecessarily remove LRU docs)
            decrementMemoryCounts(newDoc);

            //Add oldDoc back to Heap, update memory count, (it is added back to hashtable above)
            if(oldDoc != null){
                this.updateMemoryCounters(bytesRemovedFromMemory);

                oldDoc.setLastUseTime(nanoTime());
                documentheap.insert(oldDoc);
            }

            //Add old doc back to hashtable, remove newDoc from hashtable
            documents.put(uriParam, oldDoc); //if oldDoc is null, HashTableImpl.put treats null value as a delete

            //remove newDoc from heap
            removeFromHeap(newDoc);
        };

        GenericCommand<URI> putBinaryDocCommand = new GenericCommand<>(url, undoBinaryPut);

        documents.put(url, newDoc);

        commandStack.push(putBinaryDocCommand);

        //remove old doc from heap
        if(oldDoc != null){
            removeFromHeap(oldDoc);
        }

        newDoc.setLastUseTime(nanoTime()); //set last use time
        documentheap.insert(newDoc);
    }


    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     * Returns null if doc is not in store
     */
    @Override
    public Document get(URI url){
        Document doc = documents.get(url);
        if(doc != null) makeMinOfHeap(doc); //sets last use time to now & reheapifies
        return doc;
    }


    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean delete(URI url){
        boolean docWasThere = documents.containsKey(url);
        long originalLastUseTime = docWasThere ? documents.get(url).getLastUseTime() : 0;
        GenericCommand<URI> deleteCommand = deleteAndReturnCommand(url, new IdenticalTimeStampProvider(), originalLastUseTime);
        commandStack.push(deleteCommand);
        return docWasThere;
    }

    ///A local class that can be called during deletes and undoDeletes of each doc, returning identical timeStamp each time
    private class IdenticalTimeStampProvider {
        Long timeStampForUndo;
        //Long timeStampForDelete;

        public Long getTimeStampForUndo() {
            if (timeStampForUndo == null){
                timeStampForUndo = nanoTime(); //Sets time equal to the nanotime at moment of undoing of first doc.
            }
            return timeStampForUndo;
        }
        /* public Long getTimeStampForDelete() { //Method removed based on specs update to not adjust last use time after delete
            if (timeStampForDelete == null){
                timeStampForDelete = nanoTime(); //Sets time equal to the nanotime at deletion of first doc
            }
            return timeStampForDelete;
        }*/
    }


    /**
     * @param url the unique identifier of the document to delete
     * Returns the Command containing the Undo Consumer that can undo delete
     * Actually deleted the document from Hashtable and Trie
     */
    private GenericCommand<URI> deleteAndReturnCommand(URI url, IdenticalTimeStampProvider timeStampProvider, long formerLastUseTime) {
        Document docToDelete = documents.get(url); //Null if doc was not there

        //If docToDelete is null, set variable to 0 for undo. If not null, decrement memory count and return int of bytes removed from bytes in memory count
        int bytesRemovedFromMemory = (docToDelete == null) ? 0 : decrementMemoryCounts(docToDelete);

        if(docToDelete != null) {
            documents.put(url, null); //sets last use time to right now (will change later)

            deleteDocFromTrie(docToDelete);

            removeFromHeap(docToDelete); //sets lastusetime to Long.MIN_VALUE, removes from heap
            docToDelete.setLastUseTime(formerLastUseTime); //ensuring that deletes do not adjust last use time
        }


        Consumer<URI> toUndoDelete = uri -> {
            if(docToDelete != null){ //But if the doc we are deleting was not even there to delete, do NOTHING to undo the so called "delete"
                //Add doc back to Hashtable, Trie, and Heap, and reflect this addition in memory counts:
                updateMemoryCounters(bytesRemovedFromMemory); //Put back all the bytes that were removed in the delete

                this.documents.put(uri, docToDelete);

                putDocIntoTrie(docToDelete);

                docToDelete.setLastUseTime(timeStampProvider.getTimeStampForUndo()); //Each doc in a bulk delete will have SAME EXACT TIMESTAMP.
                documentheap.insert(docToDelete);
            }
        };

        GenericCommand<URI> deleteCommand = new GenericCommand<>(url, toUndoDelete);

        return deleteCommand;
    }

    //**********STAGE 3 ADDITIONS

    /**
     * undoes the last command (put/delete/setmetadata)
     * (If last command was a deleteAll, it will undo all of the generic commands in that COmmandSet)
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     *
     * Undo is supposed to reset last use time. this is done in the individual lamda functions across
     *      ...the various methods, so as to ensure the correct URI gets its last use time updates in the trickier cases of overwriting puts etc
     */
    @Override
    public void undo() throws IllegalStateException{
        if(commandStack.peek() == null) throw new IllegalStateException("No action to be undone");

        //Either calls CommandSet.undo() or GenericCommand.undo(). Either gets the job done.
        commandStack.pop().undo();
    }

    /**
     * undo the last command that was done with the given URI as its key
     * If most recent command involving this uri was a deleteAll of a group, this will only undo
     *      the delete of this specific uri
     *      if this is the LAST command in the commandset, only then would the entire CommandSet be POPPED off stack
     * @param url
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
    */
    @Override
    public void undo(URI url) throws IllegalStateException{
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

    //**********STAGE 4 ADDITIONS
    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) {
        List<Document> sortedSearchResults = getDocsWithKeywordSorted(keyword);

        bulkSetLastUseTime(sortedSearchResults);

        return sortedSearchResults;
    }

    private List<Document> getDocsWithKeywordSorted(String keyword) {
        if(keyword == null || keyword.isEmpty()) return Collections.emptyList();

        Comparator<Document> wordCountSorter = (doc1, doc2) -> doc2.wordCount(keyword) - doc1.wordCount(keyword);
        return this.documentTrie.getSorted(keyword, wordCountSorter);
    }

    private void bulkSetLastUseTime(Collection<Document> docCollection){
        long timeStampForAll = nanoTime();
        for (Document doc : docCollection){
            doc.setLastUseTime(timeStampForAll); //Ensure that every doc gets exact same timestamp, despite technically being dealt with one at a time (hence not calling makemaxofheap in a loop on each
            documentheap.reHeapify(doc);
        }
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix){
        List<Document> sortedSearchResults = getDocsWithPrefixSorted(keywordPrefix); //Empty list if null/empty keyword

        bulkSetLastUseTime(sortedSearchResults);

        return sortedSearchResults;
    }

    private List<Document> getDocsWithPrefixSorted(String keywordPrefix) {
        if(keywordPrefix == null || keywordPrefix.isEmpty()) return Collections.emptyList();

        return documentTrie.getAllWithPrefixSorted(keywordPrefix, getPrefixSortComparator(keywordPrefix));
    }

    private Comparator<Document> getPrefixSortComparator(String keywordPrefix){
        //If word has this prefix
        //Increment prefix count as many times as word with prefix exists in document
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
     * Completely remove any trace, from Hashtable and Trie, of any document in the provided list
     * @return a Set of URIs of the documents that were deleted.
     * A list of a singular document will result in a commandSet of a singular generic command
     * An empty list will result in a commandSet that is empty. Use will call undo() to pop that off the stack, not actually undoing anything of significance (but for all they know the deleteAll did something, so they will think that they have to undo it) and then the next undo call will undo previous command
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
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword){
        List<Document> docsWithKeyword = getDocsWithKeywordSorted(keyword);
        return deleteAllDocsInList(docsWithKeyword); //Undo logic taken care of there
    }


    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix){
        List<Document> docsWithprefix = getDocsWithPrefixSorted(keywordPrefix);
        return deleteAllDocsInList(docsWithprefix); //Undo logic taken care of there
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains ALL OF the given values for the given keys. If no documents contain all the given key-value pairs, return an empty list.
     */
    @Override
    public List<Document> searchByMetadata(Map<String,String> keysValues){
        List<Document> docsWithMetadata = getDocsWithMetaDataSorted(keysValues);

        bulkSetLastUseTime(docsWithMetadata);
        return docsWithMetadata;
    }

    private List<Document> getDocsWithMetaDataSorted(Map<String, String> keysValues) {
        if(keysValues == null) return Collections.emptyList();

        List<Document> docsWithMetadata = new ArrayList<>();
        for (Document doc : this.documents.values()){ //All the Documents in the <URI, Document> hashmap
            boolean hasAll = true;
            for (String key: keysValues.keySet()){
                if(doc.getMetadataValue(key)==null || !doc.getMetadataValue(key).equals(keysValues.get(key))){ //if doc in question does not have one of of these keys, or has the wrong value for one of these keys, it will not be added to list
                    hasAll = false;
                }
            }
            if(hasAll){
                docsWithMetadata.add(doc);
            }
        }
        return docsWithMetadata;
    }

    /**
     * Retrieve all documents whose text contains the given keyword AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String,String> keysValues){
        List<Document> docsWithBoth = getDocsWithKeywordAndMetaData(keyword, keysValues);

        bulkSetLastUseTime(docsWithBoth);

        return docsWithBoth;
    }

    private List<Document> getDocsWithKeywordAndMetaData(String keyword, Map<String, String> keysValues) {
        List<Document> docsWithMetaData = getDocsWithMetaDataSorted(keysValues);
        List<Document> docsWithKeyword = getDocsWithKeywordSorted(keyword);

        List<Document> docsWithBoth = new ArrayList<>();

        for (Document doc: docsWithMetaData){
            if(docsWithKeyword.contains(doc)){
                docsWithBoth.add(doc);
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
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix,Map<String,String> keysValues){
        List<Document> docsWithBoth = getDocsWithPrefixAndMetaDataSorted(keywordPrefix, keysValues);

        bulkSetLastUseTime(docsWithBoth);

        return docsWithBoth;
    }

    private List<Document> getDocsWithPrefixAndMetaDataSorted(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> docsWithMetaData = this.getDocsWithMetaDataSorted(keysValues);
        List<Document> docsWithPrefix = this.getDocsWithPrefixSorted(keywordPrefix);

        List<Document> docsWithBoth = new ArrayList<>();

        for (Document doc : docsWithMetaData){
            if(docsWithPrefix.contains(doc)){
                docsWithBoth.add(doc);
            }
        }

        docsWithBoth.sort(getPrefixSortComparator(keywordPrefix));
        return docsWithBoth;
    }

    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithMetadata(Map<String,String> keysValues){
        List<Document> docsWithMD = getDocsWithMetaDataSorted(keysValues);
        return deleteAllDocsInList(docsWithMD); //Undo logic taken care of there
    }

    /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword,Map<String,String> keysValues){
        List<Document> docsWithKeywordAndMD = getDocsWithKeywordAndMetaData(keyword, keysValues);
        return deleteAllDocsInList(docsWithKeywordAndMD); //Undo logic taken care of there
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix,Map<String,String> keysValues){
        List<Document> docsWithPrefixAndMD = getDocsWithPrefixAndMetaDataSorted(keywordPrefix, keysValues);
        return deleteAllDocsInList(docsWithPrefixAndMD); //Undo logic taken care of there
    }

    //**********STAGE 5 ADDITIONS

    /**
     * set maximum number of documents that may be stored
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentCount(int limit){
        if(limit<1) throw new IllegalArgumentException("Memory limit can't be less than 1");

        maxDocCount = limit;

        removeLeastRecentlyUsedDocs(); // This first checks if it needs to remove anything at all
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentBytes(int limit){
        if(limit<1) throw new IllegalArgumentException("Memory limit can't be less than 1");

        maxDocBytes = limit;

        removeLeastRecentlyUsedDocs(); //This method first checks if it needs to remove anything at all
    }

}