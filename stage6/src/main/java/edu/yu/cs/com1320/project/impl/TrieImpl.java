package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 128; // Standard ASCII. To save memory, could alternatively  do 62, accounting for all numbers, uppercase letters, and lowercase letters, and can call getIndex private method to convert their ASCII values into indexes 0-61.
    private Node root;

    /**
     * In stage 5 of project, DocumentStoreImpl instantiated TrieImpl with Document as the type for Value
     * In stage 6 of project, DocumentStoreImpl instantiates TrieImpl with a comparable URI wrapper class as the type for Value
     */
    private class Node{
        //Map of every Value (URI, as of stage 6) that has this node's word in it, and how many times it does
        Map<Value, Integer> wordHits = new HashMap<>();

        @SuppressWarnings("unchecked")
        Node[] links = (Node[]) new TrieImpl<?>.Node[TrieImpl.alphabetSize];
    }


    /**
     * add the given value at the given key
     * @param key represents a singular word founded in 1+ document(s), not including punctuation.
     * @param val
     */
    @Override
    public void put(String key, Value val){
        if (val == null) this.deleteAll(key); //null value is a delete. Delete all Value (all URI indirect references to docs) previously associated with the trieNode at the end of this keyword String.
        else this.root = put(this.root, key, val, 0);
    }

    private Node put(Node x, String key, Value val, int d){
        if (x==null){ //If a given character position in the key is not present in the trie (if "he" exists, and "hello" is put, 3 new nodes are created).
            x = new Node();
        }

        //when final node is reached. Ready to put Value into node's wordHits map
        if(d == key.length()){
            x.wordHits.put(val, x.wordHits.getOrDefault(val, 0) + 1);
            return x;
        }

        //Before reaching final node, recurse toward it
        else{
            char c = key.charAt(d);
            x.links[c] = this.put(x.links[c], key, val, d+1);
            return x;
        }
    }

    /**
     * Get all exact matches for the given key, sorted in descending order, where "descending" is defined by the comparator.
     * Search is CASE SENSITIVE.
     * @param key
     * @param comparator used to sort value
     *      In DocumentStoreImpl, for a keyword search, order will be based on how many times the keyword appears in a document.
     * @return a List of matching Values. Empty List (not null) if no matches.
     */
    @Override
    public List<Value> getSorted(String key, Comparator<Value> comparator){
        List<Value> sortedValueList = new ArrayList<>();

        if(key==null) return sortedValueList; //Returns empty list (there are no matches, so specs demand empty list return)

        Node x = this.getNode(this.root, key, 0);

        if(x == null) return sortedValueList; //returns empty set

        sortedValueList.addAll(x.wordHits.keySet());
        sortedValueList.sort(comparator);
        return sortedValueList;
    }

    /**
     * Get all exact matches for the given key.
     * Search is CASE SENSITIVE.
     * @param key
     * @return a Set of matching Values. Empty set if no matches.
     */
    @Override
    public Set<Value> get(String key){
        Set<Value> valueSet = new HashSet<>();

        if(key==null || key.isEmpty()) return valueSet; //Returns empty set (there are no matches, so specs demand empty set return)
        Node x = this.getNode(this.root, key, 0);

        if(x == null) return valueSet; //returns empty set

        valueSet.addAll(x.wordHits.keySet()); //Add all Values, which are keys in Node.value, associated with this key (this word) (Confusing use of key and wordHits to maintain required method headers)
        return valueSet;
    }

    /**
     * Get the node representing the final letter of a key, allowing a public getter method to access its wordHits
     */
    private Node getNode(Node x, String key, int d){
        if(x == null) return null;

        if(d == key.length()){
            return x;
        }

        char c = key.charAt(d);
        return this.getNode(x.links[c], key, d+1); //Will eventually return x (when d==key.length()) and that x will get returned  all the way back to the calling method, OR will return null if the node doesn't exist, which will be returned all the way to the calling method
    }

    /**
     * Recursive preorder traversal down the tree starting from node representing the prefix,
     * adding all nodes of full words that exist at or below (more letters than) the prefix.
     */
    private Set<Node> getAllNodesWithPrefix(Node prefixNode, StringBuilder prefix, Set<Node> nodeSet){
        if (prefixNode==null) return nodeSet;

        if(!prefixNode.wordHits.isEmpty()){
            nodeSet.add(prefixNode);
        }

        for(int c = 0; c < alphabetSize; c++){
            if(prefixNode.links[c] != null){
                prefix.append((char)c);
                getAllNodesWithPrefix(prefixNode.links[c], prefix, nodeSet);
                prefix.deleteCharAt(prefix.length() -1); //Delete the recently added character from prefix, so that loop can restart and add an alternative child letter, running down another path.
            }
        }
        return nodeSet;
    }

    /**
     * get all matches which contain a String with the given prefix, sorted according to the provided comparator
     * Search is CASE SENSITIVE.
     * @param prefix
     * @param comparator used to sort value
     * @return a List of all matching Values containing the given prefix. Empty List if no matches.
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
        Set<Value> prefixSet = new HashSet<>();
        if(prefix==null || prefix.equals("")) return Collections.emptyList();
        Node prefixNode = getNode(root, prefix, 0);

        Set<Node> nodeSet = getAllNodesWithPrefix(prefixNode, new StringBuilder(prefix), new HashSet<>());

        for(Node node: nodeSet){
            prefixSet.addAll(node.wordHits.keySet());
        }
        List<Value> sortedPrefixList = new ArrayList<>(prefixSet); //Avoiding duplicate documents by first putting in set

        sortedPrefixList.sort(comparator);

        return sortedPrefixList;
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix){
        Set<Value> valuesInSubTreeToDelete = new HashSet<>();

        if(prefix == null || prefix.isEmpty()) return valuesInSubTreeToDelete;

        Node prefixNode = getNode(root, prefix, 0);

        Set<Node> nodeSet = getAllNodesWithPrefix(prefixNode, new StringBuilder(prefix), new HashSet<>());
        for(Node node : nodeSet){
            valuesInSubTreeToDelete.addAll(node.wordHits.keySet());
        }
        deleteSubtree(this.root, prefix, 0);
        return valuesInSubTreeToDelete;
    }


    /**
     * Delete a specific node by
     * 1: Deleting its Value (clearing its wordHits map)
     * If no non-null child-links:
     *  2. deleting node entirely by returning null to most recent caller in recursion, which will set parent node's child link--which formerly pointed to the node being deleted--to null.
     * `3. Traverse up trie, deleting parent nodes if they have no values and no non-null children.
     */
    private Node deleteNode(Node node, String key, int d){
        //This will return null if the inputted node (the root) is already null, leading to an immediate method return
        // or if a later character does not exist. If key is "happy" but trie has "hat" and not "hap", then when node.links[p] is set to = deleteNode(node.links[p]...) this will return null, correctly setting node.links[p] to be null, as it always was.
        if(node==null){
            return null;
        }

        if(d == key.length()){
            node.wordHits.clear();
        }

        else{
            char c = key.charAt(d);
            node.links[c] = this.deleteNode(node.links[c], key, d+1);
        }

        if(!node.wordHits.isEmpty()){
            return node; //Send the node back to its parent's child pointer (parent's node.links[c]) to refrain from deleting node that has word(s) in it
        }

        for (Node childLink: node.links){
            if(childLink != null){
                return node; //Send back the node back to it's parent child pointer to refrain from deleting this node that (although we may have deleted its value) has valuable children
            }
        }

        //If node in question, either the keyvalue node after being stripped of its word map, or any ancestor node,
        // has neither a value nor any non-null children, then return null to its parent's child pointer
        //effectively deleting this node from the trie
        return null;
    }

    /**
     * Deletes a subtree the same way deleteNode() functions,
     * except that it always returns null when the recursion reaches the node of root of subtree,
     * rather than merely clearing the values of said node and only returning null later if said node has no children.
     */
    private Node deleteSubtree(Node node, String key, int d){
        if(node==null){
            return null;
        }

        if(d == key.length()){
            //THIS IS THE ONLY DIFFERENCE BETWEEN SUBTREE DELETION METHOD AND REGULAR NODE DELETION METHOD (Could have used param arg boolean flag to make one method with conditional logic at this point). (Lamda functions tough, because in one case need to return, in one case need to conduct logic.
            return null;
        } else{
            char c = key.charAt(d);
            node.links[c] = this.deleteSubtree(node.links[c], key, d+1); //also changed this line, to reflect method name
        }

        if(!node.wordHits.isEmpty()){
            return node;
        }

        for (Node childLink: node.links){
            if(childLink != null){
                return node;
            }
        }
        return null;
    }

    /**
     * Delete all Value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key){
        if(key == null) return Collections.emptySet();

        Node nodeToClear = getNode(this.root, key, 0);
        if(nodeToClear == null) return Collections.emptySet();

        Set<Value> valuesBeingClearedFromNode = new HashSet<>(nodeToClear.wordHits.keySet());

        this.root = deleteNode(this.root, key, 0); //Traverses from root down to node, deletes node's value, deleted node if no children, deletes all parents of no value with no children up until reaching root or a valuable/childHaving parent.

        return valuesBeingClearedFromNode;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Value val){
        if (key==null) return null;

        Node nodeWithValueToDelete = getNode(this.root, key, 0); //Find the node related to this key word
        if(nodeWithValueToDelete==null || !contains(key, val) ) return null;

        nodeWithValueToDelete.wordHits.remove(val); //Remove this value from wordHits of this node

        //If that was the only value in the node, check if node should be deleted entirely.
        if(nodeWithValueToDelete.wordHits.isEmpty()){
            deleteNode(this.root, key,0); //If node in question has no children, it will be deleted entirely, as will non-valuable/non-childrearing ancestor nodes
        }
        return val;
    }

    private boolean contains(String key, Value val){
        return (get(key).contains(val));
    }
}