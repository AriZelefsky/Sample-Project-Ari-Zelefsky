package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

//URGENT URGENT this class should not be instantiated with URI rather than DOcument as the Value type
//HENCE< change all of the comparators to compare based on the .get(uri) rather than directly discussing documents iT iT iT



/**
 * FOR STAGE 3
 * AZ: Above must be typo for STAGE 4
 * @param <Value>
 */
public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 128; // Standard ASCII. Alternatively could do 62, because just need to store Numbers, uppercase letters, and lowercase letters, and can call getIndex private method to convert those into indeces 0-61.
    private Node root; // root of trie

    /**
     * AZ: For Document Store, TrieImpl will be instantiated with Value as Document
     * AZ: The list<Value> represents all Docs with the word whose last letter is this node.
     * AZ: Repeats of the same Doc indicate that the word comes up multiple times in said doc.
     */
    private class Node{
        //Map of every Value (URI, as of stage 6) that has this node's word in it, and how many times it does
        Map<Value, Integer> wordHits = new HashMap<>();

        @SuppressWarnings("unchecked") //Array creation below involves an unchecked cast
        //Node[] links = (Node[]) new Node[TrieImpl.alphabetSize];
        Node[] links = (Node[]) new TrieImpl<?>.Node[TrieImpl.alphabetSize];
    }
    /**
     * Constructor MUST NOT take any arguments
     */
    public TrieImpl(){
        // pass Prof's test for constructor.
    }

    /**
     * add the given value at the given key
     * @param key AZ: the key represents a singular word founded in 1+ document, not including punctuation.
     * @param val AZ: Value will represent a URI, which will be added to a List<Value> of URI's of Documents in a node field.

    AZ: Piazza instructor approved post about put(null): "I think you would just delete all links to that key, not 100% sure though"
     */
    @Override
    public void put(String key, Value val){
        if (val == null) this.deleteAll(key); //null value is a delete. Delete ALL VALUES (all URI indirect references to docs) previously associated with the trieNode at the end of this keyword String.
        else this.root = put(this.root, key, val, 0); //Reassignment of root only needed when putting first ever node. And acceptable elsewhere, because as the recursive calls unwind, put keeps return progressively earlier nodes, until it returns the root.
    }

    private Node put(Node x, String key, Value val, int d){
        if (x==null){ //If a given character position in the key is not present in the trie (if "he" exists, and "hello" is put, 3 new nodes are created).
            x = new Node();
        }

        //when final node is reached. Ready to put Value into node's wordHits map
        if(d == key.length()){
            //If this value already exists at this key (at the node at the end of this string)
            //then update the counter of how many times this specific word appears in this specific Value
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
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     *        AZ: the above is informs as to what comparator will be inputted into this method by calling method in ...stage4.impl.DocumentStore
     * Search is CASE SENSITIVE.
     * @param key
     * @param comparator used to sort value
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
     * get all exact matches for the given key.
     * Search is CASE SENSITIVE. (AZ: this is taken care of by the trie putting differently depending on case. 'a' and 'A' are totally different letters in ASCII)
     * @param key
     * @return a Set of matching Values. Empty set if no matches.
     *
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
        //If root, or any node needed along the path to reaching the end of the key is null, this will return null
        if(x == null) return null; //do not let a public method end up returning null rather an empty list

        //when final node is reached, get its wordHits
        if(d == key.length()){
            return x;
        }

        char c = key.charAt(d);
        return this.getNode(x.links[c], key, d+1); //Will eventually return x (when d==key.length()) and that x will get returned  all the way back to the calling method, OR will return null if the node doesn't exist, which will be returned all the way to the calling method
    }


    /**
     * AZ: This method recursively preorder traversals down the tree starting from node representing the prefix,
     * adding all nodes of full words that exist at or below (more letters than) the prefix.
     * NOTE: does NOT start traversal from root of trie to find prefixNode. prefixNode is provided.
     */
    private Set<Node> getAllNodesWithPrefix(Node prefixNode, StringBuilder prefix, Set<Node> nodeSet){
        if (prefixNode==null) return nodeSet; //If method originally took in null root node, immediately spits back inputted nodeSet.

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
     * get all matches which contain a String with the given prefix, sorted in descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     *      AZ: what this means is for the purposes of a prefix search, comparator is defined as how many times a word with said prefix arises. A Document with "hi" "hi" and "hike" counts as 3 matches for prefix "hi", comes earlier than a Document containing just "hiya" and "hippo"
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE SENSITIVE.
     * @param prefix
     * @param comparator used to sort value
     * @return a List of all matching Values containing the given prefix, in descending order. Empty List if no matches.
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
        Set<Value> prefixSet = new HashSet<>();
        if(prefix==null || prefix.equals("")) return Collections.emptyList(); //Piazza 190 on "" empty strings
        Node prefixNode = getNode(root, prefix, 0);

        Set<Node> nodeSet = getAllNodesWithPrefix(prefixNode, new StringBuilder(prefix), new HashSet<>()); //Will keep modifying the new set provided, return a set

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
     *
    AZ: NOTE it only says to delete the subtree. Not to delete Documents themselves, just the nodes in general associated with a generic trieImpl
     */
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix){
        Set<Value> valuesInSubTreeToDelete = new HashSet<>();

        if(prefix == null || prefix.equals("")) return valuesInSubTreeToDelete; //See piazza 190 on empty string


        Node prefixNode = getNode(root, prefix, 0);

        Set<Node> nodeSet = getAllNodesWithPrefix(prefixNode, new StringBuilder(prefix), new HashSet<>()); //Will keep modifying the new set provided, return a set

        for(Node node : nodeSet){
            valuesInSubTreeToDelete.addAll(node.wordHits.keySet());
        }
        deleteSubtree(this.root, prefix, 0); //This will delete the node even though it has children, deleting the entire subtree rooted at prefixNode. Will also delete any valueless ancestors of prefixNode.

        return valuesInSubTreeToDelete;
    }


    /**
     * AZ:
     * Delete a specific node by
     * 1: Deleting all its Value
     * IF has no children, deleting node entirely by returning null to most recent caller in recursion, which will set parent node's child link--which formerly pointed to the node to delete--to null.
     *  Traverse up trie, deleting parent nodes if they already have no values AND no non-null children. Stop at root.
     *
     */
    private Node deleteNode(Node node, String key, int d){
        //This will return null if the inputted node (the root) is already null, leading to an immediate method return
        // or  if a later character does not exist. If key is "happy" but trie has "hat" and not "hap", then when node.links[p] is set to = deleteNode(node.links[p]...) this will return null, correctly setting node.links[p] to be null, as it always was.
        if(node==null){
            return null;
        }

        //If we have reached the node of the keyword. This part of the method can only be once.
        // It is the finale step in deletion prior to the recursive calls unwinding
        if(d == key.length()){
            node.wordHits.clear(); //Remove ALL Values, and their corresponding integer counts, from node we are deleting. Potentially, we will also delete the reference to node itself later.
        }
        //
        //Traverse down to keyword's node
        else{
            char c = key.charAt(d);
            node.links[c] = this.deleteNode(node.links[c], key, d+1); //This will send us down to the next node. When that method eventually returns, node.links[c] will either be set to the node already sitting below it or to null, based on following logic
        }
        //
        //Remainder of method will be seen on the final recursive call if the keyword existed (unlike "hat" "hap" case described beginning of method),
        //And will also be reached on the entire traversal back up the trie, the returnings of all the recursive calls
        if(!node.wordHits.isEmpty()){
            return node; //Send the node back to its parent's child pointer (parent's node.links[c]) to refrain from deleting node that has word(s) in it
        }

        for (Node childLink: node.links){
            if(childLink != null){
                return node; //Send back the node back to it's parent child pointer to refrain from deleting this node that (although we may have deleted its value) has valuable children
            }
        }
        //
        //If node in question, either the keyvalue node after being stripped of its word map, or any ancestor node,
        // has neither a value nor any non-null children, then return null to its parent's child pointer
        //effectively deleting this node from the trie
        return null;
    }

    /** AZ:
     * Deletes a subtree the same way deleteNode() functions,
     * except that it returns null when the recursion reaches the node of root of subtree,
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
            return node;         }

        for (Node childLink: node.links){
            if(childLink != null){
                return node;
            }
        }

        return null;
    }

    /**
     * Delete all value from the node of the given key (do not remove the value from other nodes in the Trie)
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
        if(nodeWithValueToDelete==null || !contains(key, val) ) return null; //If node does not exist, the keyword is not in the trie

        nodeWithValueToDelete.wordHits.remove(val); //Delete the val key and its integer pair

        //If that was the only value in the node, be sure to check if node should be deleted entirely.
        if(nodeWithValueToDelete.wordHits.isEmpty()){ //If node has no OTHER values
            deleteNode(this.root, key,0); //AND node in question has no children, it will be deleted entirely, as will non-valuable/non-childrearing ancestor nodes
        }
        return val;
    }

    private boolean contains(String key, Value val){
        return (get(key).contains(val));
    }
}