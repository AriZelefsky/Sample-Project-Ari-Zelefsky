package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    private URI uri;
    private String text;
    private byte[] binaryData;
    private Map<String, String> metadata = new HashMap<>();
    private Map<String, Integer> wordMap;
    private long lastUseTime; //Will be first set shortly after a document's creation, in put method of DocumentStore

    /**
     * Constructor for plaintext document
     * @throws IllegalArgumentException if either argument is null or empty/blank
     */
    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap){
        if(uri==null || uri.toString().isEmpty() || text == null || text.isEmpty()){
            throw new IllegalArgumentException("uri or text was null or blank");
        }
        this.uri = uri;
        this.text = text;
        if(wordCountMap == null){
            setWordsAndCounts(); //initializes AND populates this.wordsAndCounts
        } else{
            this.wordMap = wordCountMap;
        }

        //this.binarydata remains null
    }


    /**
     * Constructor for binary data (eg images)
     * @throws IllegalArgumentException if either argument is null or empty/blank
     */
    public DocumentImpl(URI uri, byte[] binaryData) {
        if(uri==null || uri.toString().isEmpty() || binaryData == null || binaryData.length == 0){
            throw new IllegalArgumentException("One of the arguments was null or blank");
        }
        this.uri = uri;
        this.binaryData = binaryData;

        //this.text remains null
        //this.wordMap remains null
    }


    /**
     * Note on definition of a word:
     * letters and digits count towards words, spaces separate, other characters are ignored/as if they do not exist. T*)h.e h&i is 'the' and 'hi'
     */
    private void setWordsAndCounts(){
        this.wordMap = new HashMap<>();
        for (int i = 0; i < this.text.length(); i++) {
            int j = i;
            if(Character.isLetterOrDigit(this.text.charAt(i))) {
                StringBuilder sb = new StringBuilder();
                while(j < this.text.length() && (this.text.charAt(j)!=(' '))){ //ONLY VIEWING SPACE AS A WORD SEPERATOR, NOT TAB NOR A NEW LINE. and viewing anything else as nonexistant
                    if(Character.isLetterOrDigit(this.text.charAt(j))){
                        sb.append(this.text.charAt(j));
                    }
                    j++;
                }
                this.wordMap.put(sb.toString(), this.wordMap.getOrDefault(sb.toString(), 0)+1);
            }
            i = j; //j is at a 'SPACE' char. i is now 'SPACE' and is about to i++ at end of loop, to check potential next string.
        }
    }

    /**
     * @param key key of document metadata to store a value for
     * @param value value to store
     * @return old value, or null if there was no old value
     * @throws IllegalArgumentException if the key is null or blank
     */
    @Override
    public String setMetadataValue(String key, String value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return metadata.put(key, value);
    }

    /**
     * @param key metadata key whose value we want to retrieve
     * @return corresponding value, or null if there is no such key
     * @throws IllegalArgumentException if the key is null or blank
     */
    @Override
    public String getMetadataValue(String key) {
        if (key == null || key.isEmpty()){
            throw new IllegalArgumentException();
        }
        return metadata.get(key);
    }

    /**
     * @return a COPY of the metadata saved in this document
     */
    @Override
    public HashMap<String, String> getMetadata() {
        HashMap<String, String> temp= new HashMap<>();
        for (String key : this.metadata.keySet()){
            temp.put(key, this.metadata.get(key));
        }
        return temp;
    }

    @Override
    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt() {
        return this.text;
    }

    /**
     * @return content of binary data document
     */
    @Override
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey() {
        return this.uri;
    }

    /**
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Override
    public int wordCount(String word) {
        if (wordMap == null) return 0; //It is a binary document.
        return this.wordMap.getOrDefault(word, 0);
    }

    /**
     * @return all the words that appear in the document
     */
    @Override
    public Set<String> getWords() {
        if (this.text==null) return new HashSet<>(); //Return an empty set, not null, for binary doc.
        return new HashSet<>(this.wordMap.keySet());
    }

    /**
     * return the last time this document was used, via put/get or via a search result
     */
    @Override
    public long getLastUseTime() {
        return lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUseTime = timeInNanoseconds;
    }

    /**
     * @return a copy of the word to count map so it can be serialized
     */
    @Override
    public HashMap<String, Integer> getWordMap() {
        return new HashMap<>(wordMap); //Copy of wordMap
    }

    /**
     * Sets the wordCount map during deserialization
     * @param wordMap
     */
    @Override
    public void setWordMap(HashMap<String, Integer> wordMap) {
        this.wordMap = wordMap;
    }


    @Override
    public int compareTo(Document o) {
        return Long.compare(this.getLastUseTime(), o.getLastUseTime()); //The later the lastUseTime, the 'greater' in value the Document
    }

    /**
     * Specs demand equals return true iff hashCodes are equal, not actually ensuring that the documents are identical
     */
    @Override
    public boolean equals(Object obj){
        if(obj==this) return true;
        if (!(obj instanceof Document)) return false;
        Document other = (Document) obj;
        return this.hashCode()==other.hashCode();
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }
}