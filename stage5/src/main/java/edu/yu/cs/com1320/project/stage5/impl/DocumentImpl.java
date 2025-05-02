package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.*;


public class DocumentImpl implements Document{
    private URI uri;
    private String text;
    private byte[] binaryData;
    private HashTable<String, String> metadata = new HashTableImpl<>();
    private HashMap<String, Integer> wordsAndCounts;
    private long lastUseTime; //Will be first set shortly after a document's creation, in put method of DocumentStore

    /**
     * Constructor for plaintext document
     * @throws IllegalArgumentException if either argument is null or empty/blank
     */
    public DocumentImpl(URI uri, String txt){
        if(uri==null || uri.toString().isEmpty() || txt == null || txt.isEmpty()){
            throw new IllegalArgumentException("One of the arguments was null or blank");
        }
        this.uri = uri;
        this.text = txt;

        setWordsAndCounts(); //initializes AND populates this.wordsAndCounts
        //this.binarydata remains null
    }


    /**
     * Constructor for binary data (e.g images)
     * @throws IllegalArgumentException if either argument is null or empty/blank
     */
    public DocumentImpl(URI uri, byte[] binaryData) {
        if(uri==null || uri.toString().isEmpty() || binaryData == null || binaryData.length == 0){
            throw new IllegalArgumentException("One of the arguments was null or blank");
        }
        this.uri = uri;
        this.binaryData = binaryData;


        //this.text remains null
        //this.words remains null
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    /**
     * Specs demand equals return true iff hashCodes are equal
     */
    @Override
    public boolean equals(Object obj){
        if(obj==this) return true;
        if (!(obj instanceof Document)) return false;
        Document other = (Document) obj;
        return this.hashCode()==other.hashCode();
    }

    public int compareTo(Document other){
        return Long.compare(this.getLastUseTime(), other.getLastUseTime()); //DO NOT do (int) this.getlast -other.get, as that is flawed in case of integer overflow
        //returns a negative if other is later in time. This will create an order from earliest time to latest, most recent time. First in list will be least recent, most likely to get rid of .
    }


    /**
     * @param key key of document metadata to store a value for
     * @param value value to store
     * @return old value, or null if there was no old value
     * @throws IllegalArgumentException if the key is null or blank
     */
    @Override
    public String setMetadataValue(String key, String value){
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
    public String getMetadataValue(String key){
        if (key == null || key.isEmpty()){
            throw new IllegalArgumentException();
        }
        return metadata.get(key);
    }

    /**
     * @return a COPY of the metadata saved in this document
     */
    @Override
    public HashTable<String, String> getMetadata(){
        HashTableImpl<String, String> temp= new HashTableImpl<>();
        for (String key : this.metadata.keySet()){
            temp.put(key, this.metadata.get(key));
        }
        return temp;
    }
    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt(){
        return this.text;
    }

    /**
     * @return content of binary data document
     */
    @Override
    public byte[] getDocumentBinaryData(){
        return this.binaryData;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey(){
        return this.uri;
    }

    //***************STAGE 4 ADDITIONS

    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Override
    public int wordCount(String word){
        if (wordsAndCounts == null) return 0; //It is a binary document.
        return this.wordsAndCounts.getOrDefault(word, 0);
    }

    /**
     * @return all the words that appear in the document
     */
    @Override
    public Set<String> getWords(){
        if (this.text==null) return new HashSet<>(); //Return EMPTY COLLECTION not null for binary doc, as per specs
        return new HashSet<>(this.wordsAndCounts.keySet());
    }

    /**
     * Letters/digits count towards words, spaces separate, other things are ignored/as if they do not exist. T*)h.e h&i is 'the' and 'hi'
     * */
    private void setWordsAndCounts(){
        this.wordsAndCounts = new HashMap<>();
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
                this.wordsAndCounts.put(sb.toString(), this.wordsAndCounts.getOrDefault(sb.toString(), 0)+1);
            }
            i = j; //j is at a 'SPACE' char. i is now 'SPACE' and is about to i++ at end of loop, to check potential next string.
        }
    }

    /**
     * return the last time this document was used, via put/get or via a search result
     * (for stage 4 of project)
     */
    @Override
    public long getLastUseTime(){
        return lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds){
        this.lastUseTime = timeInNanoseconds;
    }
}

