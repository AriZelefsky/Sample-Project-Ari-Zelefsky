package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TrieImplTest {
    Trie<Integer> intTrie = new TrieImpl<>();
    Trie<Boolean> boolTrie = new TrieImpl<>();
    Comparator<Integer> intSorter;

    @BeforeEach
    void setUp(){
        intTrie.put("r", 1);
        intTrie.put("r", 9);
        intTrie.put("r", 7);
        intTrie.put("r", 5);
        intTrie.put("r", 3);
        intTrie.put("r", 2);
        intTrie.put("r", 4);
        intTrie.put("r", 6);
        intTrie.put("r", 8);
        intTrie.put("r", 44);
        intTrie.put("r", 100);

        intSorter = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if(o2!= 44 && o1==44) return 1; //put 44 at the end for fun
                return o2-o1; //Descending order
            }
        };
    }

    @Test
    void putTest(){
        assertEquals(Set.of(), intTrie.get("a"));
        intTrie.put("a", 1);
        assertEquals(Set.of(1), intTrie.get("a"));

        assertEquals(Set.of(), boolTrie.get("a"));
        boolTrie.put("a", false);
        assertEquals(Set.of(false), boolTrie.get("a"));

        //Ensure all letters and digits can make up words:
        boolTrie.put("qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM", true);
        assertEquals(Set.of(true), boolTrie.get("qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM"));

    }
    @Test
    void getSortedTest(){
        assertEquals(List.of(100,9,8,7,6,5,4,3,2,1,44), intTrie.getSorted("r", intSorter));

        //No duplicates
        intTrie.put("r", 100);
        intTrie.put("r", 99);
        intTrie.put("R", 37); //CASE SENSITIVITY SHOULD ENSURE THIS DOES NOT affect next line:
        assertEquals(List.of(100,99,9,8,7,6,5,4,3,2,1,44), intTrie.getSorted("r", intSorter));

        //Empty list if no matches
        assertEquals(List.of(), intTrie.getSorted("nonexistant", intSorter));
        assertEquals(List.of(), intTrie.getSorted("", intSorter));
        assertEquals(List.of(), intTrie.getSorted(null, intSorter));
    }

    @Test
    void getTest(){
        assertEquals(Set.of(100,9,8,7,6,5,4,3,2,1,44), intTrie.get("r"));
        intTrie.put("read", 100);
        intTrie.put("reading", 57);

        //Empty set if no matches
        assertEquals(Set.of(), intTrie.get("re")); //re is in trie, but no values
        assertEquals(Set.of(), intTrie.get("xz")); //xz not in trie at all
        assertEquals(Set.of(), intTrie.get(null));

        //Ensure "reading" does not steal the values of its predecessor "read"
        assertEquals(Set.of(57), intTrie.get("reading"));
    }

    @Test
    void getAllWithPrefixSortedTest(){
        intTrie.put("read", 101);
        intTrie.put("readi", 102);
        intTrie.put("reading", 103);
        intTrie.put("readingsauce", 104);
        intTrie.put("readingsauceAgain", 104); //Repeat value should NOT appear twice in getALl's list

        //Get sorted list of all with prefix
        assertEquals(List.of(104, 103,102), intTrie.getAllWithPrefixSorted("readi", intSorter));

        //Empty list if no matches
        assertEquals(List.of(), intTrie.getAllWithPrefixSorted("rez", intSorter));
        assertEquals(List.of(), intTrie.getAllWithPrefixSorted("", intSorter));
    }

    @Test
    void deleteAllWithPrefixTest(){
        intTrie.put("a", 1);
        intTrie.put("ap", 2); //"ap"'s prefix can be it itself: "ap"
        intTrie.put("aptly", 4);
        intTrie.put("aptly", 5); //Same key different value IS recorded twice
        intTrie.put("apportioned", 8);
        intTrie.put("applied", 16);
        intTrie.put("ab", 32);
        intTrie.put("aa", 100);
        intTrie.put("actors", 555);

        assertEquals(9, intTrie.getAllWithPrefixSorted("a", intSorter).size());
        assertEquals(Set.of(2,4,5,8, 16),intTrie.deleteAllWithPrefix("ap"));
        assertEquals(4, intTrie.getAllWithPrefixSorted("a", intSorter).size());

        //Return emptyset of nothing to delete
        assertEquals(Set.of(),intTrie.deleteAllWithPrefix("nonexistantPrefix"));
        assertEquals(Set.of(),intTrie.deleteAllWithPrefix(null));
    }

    @Test
    void deleteAllTest(){
        intTrie.put("a", 1);
        intTrie.put("ap", 2); //"ap"'s prefix can be it itself: "ap"
        intTrie.put("aptly", 4);
        intTrie.put("aptly", 5); //Same key different value IS recorded twice
        intTrie.put("aptly", 6); //Same key different value IS recorded twice
        intTrie.put("apportioned", 8);
        intTrie.put("applied", 16);
        intTrie.put("aptlyCHILDNODE", 2937);



        assertEquals(8, intTrie.getAllWithPrefixSorted("a", intSorter).size());
        assertEquals(Set.of(4,5,6),intTrie.deleteAll("aptly"));
        assertEquals(5, intTrie.getAllWithPrefixSorted("a", intSorter).size()); //Ensures that everything but aptly, including its child, remain

        //Return emptyset of nothing to delete
        assertEquals(Set.of(),intTrie.deleteAll("nonexistant"));
        assertEquals(Set.of(),intTrie.deleteAll(null));


    }

    @Test
    void deleteTest(){
        intTrie.put("a", 1);
        intTrie.put("ap", 2); //"ap"'s prefix can be it itself: "ap"
        intTrie.put("aptly", 4);
        intTrie.put("aptly", 5);
        intTrie.put("aptly", 6);
        intTrie.put("aptly", 7);


        assertEquals(Set.of(4,5,6,7), intTrie.get("aptly"));
        assertEquals(4, intTrie.delete("aptly", 4)); //Return the value that was deleted
        assertEquals(7, intTrie.delete("aptly", 7)); //Return the value that was deleted
        assertEquals(Set.of(5,6), intTrie.get("aptly"));

        //If they key did not contain the value (or key did not exist, so it certainly did not contain the value)
        // then return NULL:
        assertNull(intTrie.delete("nonexistantword", 2));
        assertNull(intTrie.delete("aptly", 613613)); //Yes such key, NO SUCH VALUE
        assertNull(intTrie.delete(null, 2));
    }
}
