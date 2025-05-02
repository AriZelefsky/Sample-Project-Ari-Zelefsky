package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class HashTableImplTest {
    HashTable<Integer, Boolean> intBooltable = new HashTableImpl<>();
    HashTable<Integer, String> intStringtable = new HashTableImpl<>();
    HashTable<String,String> metadataTable = new HashTableImpl<>();
    HashTable<URI, Document> docstoreTable = new HashTableImpl<>();

    private DocumentImpl textDoc;
    private DocumentImpl textDoc2;
    private URI uri;

    @BeforeEach
    void beforeEach(){
        uri = null;
        try {
            uri = new URI("https://www.ari.zelefsky/random/uri/?query=parameter#fragment");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        textDoc = new DocumentImpl(uri, "The text of the document \n \n the end.");
        textDoc2 = new DocumentImpl(uri, "The22 2t2ex2t2 2o2f t2h2e2 d2o2c2u2m2ent \n \n the end.");
    }

    @Test
    void putTest(){
        //Should return null if key was not already there:
        assertNull(intBooltable.put(7, true));
        assertNull(intBooltable.put(3093, false));
        assertNull(metadataTable.put("key1", "value1"));
        assertNull(docstoreTable.put(uri, textDoc));

        //Should return former value if key is already present
        assertEquals(true,intBooltable.put(7, false));
        assertEquals("value1", metadataTable.put("key1", "valueTWO"));
        assertEquals(textDoc, docstoreTable.put(uri, textDoc2));

        //To delete an entry, put a null value. Still return former value.
        assertEquals(false,intBooltable.put(7, null));
        assertEquals("valueTWO", metadataTable.put("key1", null));
        assertEquals(textDoc2, docstoreTable.put(uri, null));

        //check that they were actually deleted:
        assertNull(intBooltable.put(7, false));
        assertNotNull(intBooltable.put(3093, false)); //This one was not deleted before, so it should not be null
        assertNull(metadataTable.put("key1", "value3"));

        //return null if delete a non-existent key
        assertNull(intBooltable.put(10, null));
        assertNull(metadataTable.put("KeyNEVERSEENBEFORE", null));
        assertNull(docstoreTable.put(uri, null)); //this was deleted before and never put back

        //Per Piazza, if a null key is inputted, return NULL (no exception)
        assertNull(intBooltable.put(null, false));
        assertNull(intBooltable.put(null, false));
        assertNull(intBooltable.put(null, false));

        //COLLISION HANDLING PUT TESTS:

        //testing putting when multiple keys COLLIDE (like abs value of the int being 4 plus/minus a multiple of 5, which will hashfunction into same bucket)
        assertNull(intBooltable.put(4, true));
        assertNull(intBooltable.put(9, false));
        assertNull(intBooltable.put(-14, true));
        assertNull(intBooltable.put(19, true));

        assertEquals(true ,intBooltable.put(4, false) );
        assertEquals(false ,intBooltable.put(9, false) );
        assertEquals(true ,intBooltable.put(-14, true) );
        assertEquals(true ,intBooltable.put(19, true) );

        assertEquals(6, intBooltable.size()); //7 and 3093 from before plus the above 4

        //Deleting the final of 4 entry in the linked list of a hash bucket
        assertEquals(true,intBooltable.put(19, null));
        //Deleting entry from the middle of the linked list
        assertEquals(false,intBooltable.put(9, null));
        //Quick add in middle of deletions to see if I can throw HashTableImpl off its game
        assertEquals(null, intBooltable.put(24, true)); //It was not. It's game was NOT thrown off.
        //Checking they actually deleted
        assertEquals(5, intBooltable.size()); //6, deleted 2, added 1.
        //Deleting from beginning of linked list
        assertEquals(false,intBooltable.put(4, null));
        assertEquals(true,intBooltable.put(-14, null));
        assertEquals(true,intBooltable.put(24, null));
        assertEquals(false,intBooltable.put(7, null));
        assertEquals(1, intBooltable.size()); //7 and 3093 from before plus the above 4

    }


    //Testing Array Doubling propery when put many values
    @Test
    void putTest2(){
        for (int i = 0; i < 800; i++) {
            assertEquals(null, intStringtable.put(i, "hi"));
            assertEquals("hi", intStringtable.put(i, "second"));
            if(i==10) assertEquals("second", intStringtable.put(10, "ten"));
            if(i==378) assertEquals("second", intStringtable.put(378, "378"));
        }

        assertEquals("ten", intStringtable.get(10));
        assertEquals("second", intStringtable.get(5));
        assertEquals("second", intStringtable.get(6));
        assertEquals("second", intStringtable.get(25));
        assertEquals("378", intStringtable.get(378));
    }

    @Test
    void getTest(){
        //Return null if there is no such key
        assertNull(intStringtable.get(5));
        assertNull(intStringtable.get(null));

        //Return the key's value
        intStringtable.put(5, "val1");
        assertEquals("val1" ,intStringtable.get(5));
        intStringtable.put(5, "val2");
        assertEquals("val2" ,intStringtable.get(5));
        intStringtable.put(5, null); //Delete via putting null value
        assertNull(intStringtable.get(5));

        //Collision testing:
        intStringtable.put(5, "valfor5");
        intStringtable.put(10, "valfor10");
        intStringtable.put(15, "valfor15");
        intStringtable.put(275, "valfor275");

        assertEquals("valfor15" ,intStringtable.get(15));
        assertEquals("valfor275" ,intStringtable.get(275));
        assertEquals("valfor5" ,intStringtable.get(5));
        assertEquals("valfor10" ,intStringtable.get(10));

        intStringtable.put(60, "valfor60");
        intStringtable.put(61, "valfor61");
        intStringtable.put(70, "valfor70");

        assertEquals("valfor61" ,intStringtable.get(61));
        assertEquals("valfor60" ,intStringtable.get(60));
        assertEquals("valfor70" ,intStringtable.get(70));
    }

    @Test
    void containsKeyTest(){
        intStringtable.put(5, "valfor5");
        intStringtable.put(10, "valfor10");
        intStringtable.put(15, "valfor15");
        intStringtable.put(275, "valfor275");

        assertTrue(intStringtable.containsKey(10));
        assertTrue(intStringtable.containsKey(275));

        assertFalse(intStringtable.containsKey(20)); //Key that would have hashed to same bucket, but is not actually present
        assertFalse(intStringtable.containsKey(280)); //Key that would have hashed to same bucket, but is not actually present
        assertFalse(intStringtable.containsKey(7));

        metadataTable.put("keyA", "valueA");
        metadataTable.put("keyX", "valueX");

        assertTrue(metadataTable.containsKey("keyA"));
        assertTrue(metadataTable.containsKey("keyX"));
        assertFalse(metadataTable.containsKey("KeyW"));


        //contains null throws error
        assertThrows(NullPointerException.class, ()-> intStringtable.containsKey(null));
    }

    @Test
    void keySetTest(){
        intStringtable.put(5, "valfor5");
        intStringtable.put(5, "valfor5");
        intStringtable.put(5, "valfor5AGAIN");
        intStringtable.put(10, "valfor10");
        intStringtable.put(15, "valfor15");
        intStringtable.put(275, "valfor275");
        intStringtable.put(3, "valfor5");
        intStringtable.put(8, "valfor10");

        Set<Integer> returnValue = intStringtable.keySet();

        assertEquals(6, returnValue.size());
        assertTrue(returnValue.contains(10));
        assertTrue(returnValue.contains(3));
        assertTrue(returnValue.contains(5));
        assertTrue(returnValue.contains(15));
        assertTrue(returnValue.contains(8));

        //Empty keyset:
        assertEquals(0, intBooltable.keySet().size());
        assertTrue(metadataTable.keySet().isEmpty());

    }

    @Test
    void valuesTest(){
        intStringtable.put(5, "valfor5");
        intStringtable.put(10, "valfor10");
        intStringtable.put(15, "valfor15");
        intStringtable.put(275, "valfor275");
        intStringtable.put(3, "valfor5");
        intStringtable.put(8, "valfor10");

        Collection<String> returnValue = intStringtable.values();
        assertTrue(returnValue.contains("valfor15"));
        assertTrue(returnValue.contains("valfor275"));
        assertTrue(returnValue.contains("valfor5"));
        assertTrue(returnValue.contains("valfor10"));

        assertTrue(returnValue.size() == 6); //Should be 6 even though two sets of values are repeated

        //Empty Value Collection:
        assertTrue(metadataTable.values().isEmpty());
    }

    @Test
    void size(){
        assertEquals(0, docstoreTable.size());
        docstoreTable.put(uri, this.textDoc);
        docstoreTable.put(uri, this.textDoc2);
        assertEquals(1, docstoreTable.size());

        assertEquals(0, intStringtable.size());
        intStringtable.put(5, "hi");
        intStringtable.put(10, "hi");
        intStringtable.put(11, "hi");
        assertEquals(3, intStringtable.size());
    }
}

