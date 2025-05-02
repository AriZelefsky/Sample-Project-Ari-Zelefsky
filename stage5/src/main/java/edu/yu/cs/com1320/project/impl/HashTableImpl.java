package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

import java.util.*;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    /**
     * Private class representing an Entry, including a key, a value.
     * Also includes a "nextEntry" instance variable, of type entry, representing the nextEntry in a linked list of entries
     *
     * Note: If future stages demand a bifurcation of Entries and Nodes in the linked list,
     *     I can remove the NextEntry field and instead create a second nested class called Node, containing Entry entry and Entry nextEntry.
     */
    private class Entry{
        Key key;
        Value value;
        Entry nextEntry;

        Entry(Key key, Value value){
            this.key = key;
            this.value = value;
        }
    }

    private Entry[] table;


    /**
     * Constructor MUST NOT take any arguments
     */
    public HashTableImpl(){
        //set the array (of Key Value entries) to its initial length of 5
        this.table = (Entry[]) new HashTableImpl<?,?>.Entry[5];
    }


    private int hashFunction(Key key){
        return (key.hashCode() & 0x7fffffff) % this.table.length;
    }


    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    @Override
    public Value get(Key k){
        if(k == null) return null; 

        int index = this.hashFunction(k);
        Entry current = this.table[index];

        while(current != null){
            if(current.key.equals(k)){
                return current.value;
            }
            current = current.nextEntry;
        }

        return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store
     *          To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    @Override
    public Value put(Key k, Value v){
        if(k == null) return null;
        if(v == null) return deleteEntry(k);

        //If there are 4 times as many total entries as there are spots in this.table, resize:
        if(this.table.length <= this.size()/4) resizeAndRehash();
        return addEntry(k, v);
    }

    private void resizeAndRehash() {
        int m = this.table.length;
        int n = this.size();
        if(m <= n/4) {
            HashSet<Entry> tempEntrySet = new HashSet<>(this.entrySet()); //copy all keys into temp

            this.table = (Entry[]) new HashTableImpl<?,?>.Entry[this.table.length * 2]; //Array doubling this.table

            for (Entry entry : tempEntrySet){
                if(entry != null){
                    addEntry(entry.key, entry.value);
                }
            }
        }
    }

    private Value addEntry(Key k, Value v) {
        int index = this.hashFunction(k);
        //When NO other keys with same hash value exist:
        if(this.table[index] == null){
            this.table[index] = new Entry(k, v);
            return null;
        }
        //When other keys DO have same hash value as Key k. Key k itself may or may not already exist:
        Entry current = this.table[index];
        while(current.nextEntry != null){
            if(k.equals(current.key)){ // The key being put already exists.
                return this.changeValue(current, v);
            }
            current = current.nextEntry;
        }
        if(current.key.equals(k)){ //Edge case of last entry in list (whose nextEntry == null) having k as its key:
            return this.changeValue(current, v);
        }
        // Key K does not already exist, PUT it in:
        Entry newlyPutEntry = new Entry(k, v);
        current.nextEntry = newlyPutEntry;
        return null;
    }

    /**
     * @param k the key (and its value) to be deleted
     * @return if the key was present in the HashTable, return its old value. If absent, return null.
     */
    private Value deleteEntry(Key k){
        int index = this.hashFunction(k);
        Entry firstEntry = this.table[index];

        if(firstEntry == null) return null; //There were no entries to traverse, much less delete.

        if(firstEntry.key.equals(k)){ //First entry's key is a match
            Value oldValue = firstEntry.value;
            this.table[index] = firstEntry.nextEntry; //the nextEntry could be null or another entry. Either way, array index will now point to that.
            return oldValue;
        }

        Entry current = firstEntry; //renaming firstEntry (prior to iteratively changing its value during entry traversal)

        while(current.nextEntry != null){
            if(current.nextEntry.key.equals(k)){
                Value oldValue = current.nextEntry.value;
                current.nextEntry = current.nextEntry.nextEntry; //Current's next is now what used to be two ahead, skipping and effectively deleting the former current.next
                return oldValue;
            }
            current = current.nextEntry;
        }
        return null; //traversed entire list, reached the null at the end without finding key to delete.
    }

    /**
     * @param entry the <key,value> entry whose value is being changed
     * @return the previous value stored for the key in this entry
     */
    private Value changeValue(Entry entry, Value newValue){
        Value oldValue = entry.value;
        entry.value = newValue;
        return oldValue;
    }

    /**
     * @param key the key whose presence in the hashtable we are inquiring about
     * @return true if the given key is present in the hashtable as a key, false if not
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean containsKey(Key key){
        if(key == null){
            throw new NullPointerException("Null key inputted");
        }

        int index = this.hashFunction(key);
        Entry current = this.table[index];

        while(current != null){
            if(current.key.equals(key)) return true;
            current = current.nextEntry;
        }
        return false;
    }

    /**
     * @return an unmodifiable set of all the keys in this HashTable
     * @see Collections#unmodifiableSet(Set)
     */
    @Override
    public Set<Key> keySet(){
        Set<Key> myKeySet = new HashSet<>();

        for (Entry entry : this.table) {
            Entry current = entry;

            while (current != null) {
                myKeySet.add(current.key);
                current = current.nextEntry;
            }
        }
        return Collections.unmodifiableSet(myKeySet);
    }

    private Set<Entry> entrySet(){
        Set<Entry> myEntrySet = new HashSet<>();

        for (Entry entry : this.table) {
            Entry current = entry;

            while (current != null) {
                myEntrySet.add(current);
                current = current.nextEntry;
            }
        }
        return Collections.unmodifiableSet(myEntrySet);
    }


    /**
     * @return an unmodifiable collection of all the values in this HashTable
     * @see Collections#unmodifiableCollection(Collection)
     */
    @Override
    public Collection<Value> values(){
        Collection<Value> valueCollection = new ArrayList<>(); //Will not actually use functionality of list, just a way to instantiate a collection.

        for (Entry entry : this.table) {
            Entry current = entry;

            while (current != null) {
                valueCollection.add(current.value);
                current = current.nextEntry;
            }
        }
        return Collections.unmodifiableCollection(valueCollection);
    }

    /**
     * @return how many entries there currently are in the HashTable
     *
     * Note to self: If need to save on time, loop through all the entries incrementing size++
     * which avoids actually creating a new set, which this.keySet() does.
     */
    @Override
    public int size(){
        return this.keySet().size();
    }
}
