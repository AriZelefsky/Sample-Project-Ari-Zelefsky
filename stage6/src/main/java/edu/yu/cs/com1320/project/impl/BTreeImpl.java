package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;

import java.io.IOException;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    private final int MAX = 6; //max entries per Node. Nodes split if all 6 fill up.
    private Node root; //root of the B-tree
    private int height; //height of the entire B-tree

    PersistenceManager<Key, Value> pm;

    private class Node{
        private int entryCount; // number of filled in entries
        @SuppressWarnings("unchecked")
        private Entry[] entries = (Entry[]) new BTreeImpl<?,?>.Entry[MAX];

        private Node(int entryCount){
            this.entryCount = entryCount;
        }
    }

    private class Entry{
        private KeyWrapper wrappedKey;
        private Object val;
        boolean isDeleted = false;

        private Entry(KeyWrapper kw, Object val){
            this.wrappedKey = kw;
            this.val = val;
            this.isDeleted = val==null; //If creating an entry with a null value,
        }

        //only to be called on entries of internal nodes
        @SuppressWarnings("unchecked")
        private Node getChild(){
            assert ((this.val == null) || this.val instanceof BTreeImpl<?,?>.Node) : "getChild called on external node!";
            return (Node) this.val;
        }

        //Only to be called on entries of external nodes
        @SuppressWarnings("unchecked")
        private Value getValue(){
            return (Value) this.val;
        }
    }


    /// Key wrapper class to allow for creation of a sentinel key that automatically comes before every other key
    /// Rather than adding new URI("A") in docStore when it creates a new BTreeImpl, this solution works no matter
    /// what test code is used, including if this class is instantiated with a type other than URI, being used apart from docStore.
    private class KeyWrapper implements Comparable<KeyWrapper>{
        Key key;
        boolean isSentinel = false;

        @Override
        public int compareTo (KeyWrapper o){
            if(this.isSentinel && o.isSentinel) return 0;
            if(this.isSentinel) return -1;
            if(o.isSentinel) return 1;

            return this.key.compareTo(o.key);
        }

        public KeyWrapper(Key key){
            this.key = key;
        }

        // I chose not to limit to one param, the String or Boolean, in case Key is itself a String a Boolean in some use cases (though this will never happen in this project, I am allowing for this B-Tree class to be used independently for other types)
        public KeyWrapper(String indicateSentinel, Boolean b){ //Caller will use null for key.
            isSentinel = b;
        }
    }
    /// Not being used at the moment. Alternative way of keeping track of deleted versus written to disk
    private class ValueWrapper{
        Value value;
        boolean isDeleted;

        public ValueWrapper(Value value){
            this.value = value;
        }
    }


    public BTreeImpl(){
        //Make root node, add sentinel entry to said node. Which will automatically end up on left most entry at each height
        this.root = new Node(0);
        //KeyWrapper sentinel = new KeyWrapper("Sentinel", true);
        //this.put(root, sentinel,null,0); //Will increment entryCount of root node to 1
    }

    @Override
    public Value get(Key k) {
        if (k == null) throw new IllegalArgumentException("Can't get() null key");
        KeyWrapper kw = new KeyWrapper(k);

        Entry gottenEntry = get(this.root, kw, this.height);
        if(gottenEntry == null) return null; // Meaning that no such key exists, not even on disk

        if(gottenEntry.val != null) return gottenEntry.getValue(); // Meaning the value is in memory already

        if(gottenEntry.isDeleted) return null;

        // Else, value was written to disk
        try {
            Value val = (Value) pm.deserialize(k);
            //deserialize should, itself, call pm.delete to delete file from disk
            gottenEntry.val = val; // This Value is now accessible in memory.
            //pm.delete(k); //No longer needed, now that pm.deserialize itself calls pm.delete, as per piazza 312
            return val;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Entry get(Node currentNode, KeyWrapper kw , int height){
        Entry[] entries = currentNode.entries;

        // If at external node:
        if(height == 0){
            for(int i=0; i < currentNode.entryCount; i++){
                if(isEqual(entries[i].wrappedKey, (kw))) return entries[i]; //Checks compareTo==0, not .equals. For URI there is no inconsistency declared in javadocs.
            }
            return null; //Did not find entry
        }

        //Internal node code:
        for (int i = 0; i < currentNode.entryCount; i++) {
            if(i+1 == currentNode.entryCount || isLess(kw, entries[i+1].wrappedKey)){
                return get(entries[i].getChild(), kw, height - 1);
            }
        }
        assert false : "get() method should have returned before reaching the end";
        return null; //This line will never be reached
    }

    private boolean isLess(KeyWrapper kw1, KeyWrapper kw2) {
        assert !(kw1.isSentinel && kw2.isSentinel) : "We never compare two sentinels to each other";
        return kw1.compareTo(kw2) < 0;
    }
    private boolean isEqual(KeyWrapper kw1, KeyWrapper kw2) {
        assert !(kw1.isSentinel && kw2.isSentinel) : "We never compare two sentinels to each other";
        return kw1.compareTo(kw2) == 0;
    }

    @Override
    public Value put(Key k, Value v) {
        if (k == null) throw new IllegalArgumentException("key to put() is null");
        KeyWrapper kw = new KeyWrapper(k);

        Value oldValue = null;

        Entry alreadyExists = get(root, kw, this.height);
        if(alreadyExists != null) { //Former value is either in memory, deleted, or written to disk
            if (alreadyExists.val == null && !alreadyExists.isDeleted) { //If was written to disk
                try {
                    //deserialize calls pm.delete to delete old value from file on disk
                    oldValue = pm.deserialize(k);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                oldValue = alreadyExists.getValue(); //Either value from memory, or null if was deleted
            }
            alreadyExists.val = v;
            alreadyExists.isDeleted = (v == null);
            return oldValue;
        }

        //else, this key has never existed before
        if(v==null) return null; //Deleting a non-existant key does nothing

        Node newNode = this.put(this.root, kw, v, this.height);

        // If no need to split root, because no new node was created at height 0 during the put
        if(newNode == null){
            return null;
        }
        // Otherwise, split the root:
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(root.entries[0].wrappedKey, root); //First param == Sentinel
        newRoot.entries[1] = new Entry (newNode.entries[0].wrappedKey, newNode);
        this.root = newRoot;
        //a split at the root always increases the tree height by 1
        this.height++;

        return oldValue; //Either null or a Value
    }

    private Node put(Node currentNode , KeyWrapper kw, Value v, int height){
        int j;
        Entry newEntry = new Entry(kw, v); //if v is null, newEntry.isDeleted is automatically set to true


        //External Node Code
        if(height == 0){
            for (j = 0; j < currentNode.entryCount; j++) {
                if(isLess(kw, currentNode.entries[j].wrappedKey)){
                    break;
                }
            }
        }

        //Internal Node Code
        else{
            for (j = 0; j < currentNode.entryCount; j++){
                if(j+1 == currentNode.entryCount || isLess(kw, currentNode.entries[j+1].wrappedKey)){
                    Node newNode = this.put(currentNode.entries[j++].getChild(), kw, v, height - 1);
                    if(newNode == null){ //ie nothing was split into two nodes, only a new ENTRY was added lowed down the B-Tree
                        return null;
                    }

                    // If newNode!=null, a new node was split off immediately below currentNode.
                    // currentNode needs a new Entry to point to this newNode:
                    newEntry.wrappedKey = newNode.entries[0].wrappedKey;
                    newEntry.val = newNode;
                    break;
                }
            }
        }

        // Code for adding new Entry to external node
        // OR adding new Entry (pointing to newNode) to internal node if newNode != null

        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i-1];
        }
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;

        if(currentNode.entryCount < MAX) return null;
        return split(currentNode, height);
    }

    private Node split (Node currentNode, int height){
        Node newNode = new Node(MAX/2);
        currentNode.entryCount = MAX/2;
        for (int i = 0; i < MAX/2; i++) {
            newNode.entries[i] = currentNode.entries[MAX/2 + i];
            currentNode.entries[MAX/2 +i] = null; //preventing memory leaks
        }
        return newNode;
    }

    @Override
    public void moveToDisk(Key k) throws IOException {
        Value val = get(k);
        if(val==null) throw new IllegalArgumentException();
        this.pm.serialize(k, val);
        KeyWrapper kw = new KeyWrapper(k);
        Entry entryWithValToBeNulledOut = get(root, kw , height);
        assert entryWithValToBeNulledOut != null : "Should not be writing a null/deleted/alreadyOnDisk value to disk";
        entryWithValToBeNulledOut.val = null;
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        this.pm = pm;
    }
}
