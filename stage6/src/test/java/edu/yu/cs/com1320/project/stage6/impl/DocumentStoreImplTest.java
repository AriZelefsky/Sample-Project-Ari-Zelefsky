package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.stage6.DocumentStore.DocumentFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.lang.System.nanoTime;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreImplTest {
    DocumentStoreImpl docStore;
    DocumentStoreImpl docStoreCustomBaseDir;
    private File baseDir = new File(System.getProperty("user.dir"));
    private File customBaseDir = new File("CustomBaseDir/allInHere");


    URI uri1;
    URI uri2;
    URI uri3;
    URI uri4;
    URI blankUri;
    URI uria;
    URI urib;
    URI uric;
    URI urid;
    URI urie;


    ByteArrayInputStream textInput1;
    ByteArrayInputStream textInput2;
    ByteArrayInputStream textInput3;
    ByteArrayInputStream textInput4;
    ByteArrayInputStream textInput5;

    ByteArrayInputStream longtxt1;
    ByteArrayInputStream longtxt2;
    ByteArrayInputStream longtxt3;
    ByteArrayInputStream longtxt4;
    ByteArrayInputStream longtxt5;


    ByteArrayInputStream binaryInput1;
    ByteArrayInputStream binaryInput2;
    ByteArrayInputStream binaryInput3;

    File file1 = new File(System.getProperty("user.dir"), "www.xyz.com.json");
    File file2 = new File(System.getProperty("user.dir"), "www.abc123.com.json");
    File filea = new File(System.getProperty("user.dir"), "abc.com.json");
    File fileb = new File(System.getProperty("user.dir"), "bcd.net.json");
    File filec = new File(System.getProperty("user.dir"), "cde/world/com/what.json");
    File filed = new File(System.getProperty("user.dir"), "d.com.json");
    File filee = new File(System.getProperty("user.dir"), "e.json");

    File customFile1 = new File(customBaseDir, "www.xyz.com.json");
    File customFile2 = new File(customBaseDir, "www.abc123.com.json");
    File customFilea = new File(customBaseDir, "abc.com.json");
    File customFileb = new File(customBaseDir, "bcd.net.json");
    File customFilec = new File(customBaseDir, "cde/world/com/what.json");
    File customFiled = new File(customBaseDir, "d.com.json");
    File customFilee = new File(customBaseDir, "e.json");

    @BeforeEach
    void setUp() throws IOException{
        docStore = new DocumentStoreImpl();
        docStoreCustomBaseDir = new DocumentStoreImpl(customBaseDir);

        uri1 = URI.create("http://www.xyz.com");
        uri2 = URI.create("http://www.abc123.com");
        uri3 = URI.create("http://AHHHHHHHHHHH");
        uri4 = URI.create("http://lmgtfy.com");
        blankUri = URI.create("");

        byte[] arrayOfHowdy = {72, 111, 119, 100, 121}; //ASCII for "Howdy". (For testing longer strings, will useString.getBytes)
        byte[] arrayOfRandomBinary = {(byte) 001101011,(byte) 00000000, (byte)11111111,(byte) 10101010};

        ByteArrayInputStream  textInput = new ByteArrayInputStream(arrayOfHowdy, 1, 4);
        ByteArrayInputStream binaryInput = new ByteArrayInputStream(arrayOfRandomBinary,  1, 3);

        docStore.put(textInput, uri1, DocumentFormat.TXT);
        docStore.put(binaryInput, uri2, DocumentFormat.BINARY);


        textInput1 = new ByteArrayInputStream(arrayOfHowdy, 0, 2); //Ho
        textInput2 = new ByteArrayInputStream(arrayOfHowdy, 0, 3); // How
        textInput3 = new ByteArrayInputStream(arrayOfHowdy, 0, 4); //Howd
        textInput4 = new ByteArrayInputStream(arrayOfHowdy, 2, 2); //wd
        textInput5 = new ByteArrayInputStream(arrayOfHowdy, 3, 2);

        binaryInput1 = new ByteArrayInputStream(arrayOfRandomBinary,  1, 7);
        binaryInput2 = new ByteArrayInputStream(arrayOfRandomBinary,  2, 7);
        binaryInput3 = new ByteArrayInputStream(arrayOfRandomBinary,  0, 3);

        longtxt1 = new ByteArrayInputStream("illness. onetwo. not sHoRt: 333112 333112 vHow are you doing today. oneonetwo oneonetwo tapping tapper tapped .kxl. kxlll kI am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. Reading writing. Reading writing.".getBytes(StandardCharsets.UTF_8));
        longtxt2 = new ByteArrayInputStream("33s 33mn 33x 33q tim. is shorter. onetwo. : How are 333112 you doing oneonetwo today. Bazinga. tapps tapo kxll kxll kxlpq. I am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. Reading writing. Reading writing.".getBytes(StandardCharsets.UTF_8));
        longtxt3 = new ByteArrayInputStream("short. sho$rtest. This.where we wa wq we wr wt ww w w w w wt wh were 333112 333112 333112 you weighing tapo weights of weird wears. M( M^ M$. That is Five M/ M".getBytes(StandardCharsets.UTF_8));
        longtxt4 = new ByteArrayInputStream("How. 3Happy people live with purpose. M M They find joy in lasting relationships, working toward their goals, and living according to their values. The happy person is not enamored with material goods or luxury vacations. This person is fine with the simple pleasures of life—petting a dog, sitting under a tree, enjoying a cup of tea. Here are a few of the outward signs that someone is content.\n".getBytes(StandardCharsets.UTF_8));
        longtxt5 = new ByteArrayInputStream("illness. Health and happiness are .kxl. completely intertwined. m m That’s not to say that people with sickness can’t be happy, but that attending to one’s health is an important—and perhaps underappreciated—component of well-being.\n".getBytes(StandardCharsets.UTF_8));


        uria = URI.create("http://abc.com");
        urib = URI.create("http://bcd.net");
        uric = URI.create("http://cde/world/com/what");
        urid = URI.create("http://d.com");
        urie = URI.create("http://e");

        deleteJsonFiles(baseDir);
    }

    private void deleteJsonFiles(File directory) {
        if (directory == null || !directory.exists()) return;

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                deleteJsonFiles(file); // Recurse into subdirectory
            } else if (file.getName().endsWith(".json")) {
                file.delete();
            }
        }
    }

    void put5MoreDocs() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);
    }

    @Test
    void setMetadataTest() throws IOException {
        //Illegal Arguments
        assertThrows(IllegalArgumentException.class, ()-> docStore.setMetadata(uri3, "A key", "a Value"), "Should throws Exception if no doc stored at that URI");
        assertThrows(IllegalArgumentException.class, ()-> docStore.setMetadata(null, "A key", "a Value"), "Should throw Exception if URI is null");
        assertThrows(IllegalArgumentException.class, ()-> docStore.setMetadata(blankUri, "A key", "a Value"), "Should throw Exception if URI is blank");
        assertThrows(IllegalArgumentException.class, ()-> docStore.setMetadata(uri2, "", "a Value"), "Should throw Exception key is blank");
        assertThrows(IllegalArgumentException.class, ()-> docStore.setMetadata(uri2, null, "a Value"), "Should throw Exception if key is null");

        //returning null when there is no old value
        assertNull(docStore.setMetadata(uri1, "ExampleKey1", "ExampleValue1"), "Returns Null if no previous metadata exist for this doc");
        assertNull(docStore.setMetadata(uri2, "ExampleKey2", "ExampleValue2"), "Returns Null if no previous metadata exist for this doc");

        //returning old value
        assertEquals("ExampleValue1", docStore.setMetadata(uri1, "ExampleKey1", "NEWExampleValue1"), "Returns the old value");
        assertEquals("ExampleValue2", docStore.setMetadata(uri2, "ExampleKey2", "NEWExampleValue2"), "Returns the old value");
        assertEquals("NEWExampleValue2", docStore.setMetadata(uri2, "ExampleKey2", "NEWNEWExampleValue2"), "Returns the old value");
    }

    @Test
    void getMetadataTest() throws IOException{
        //Return null if no value, old value if there is one
        assertNull(docStore.getMetadata(uri1, "ExampleKey1"), "returns null if there is no value");
        docStore.setMetadata(uri1, "ExampleKey1", "ExampleValue1");
        assertEquals("ExampleValue1", docStore.getMetadata(uri1, "ExampleKey1"));

        //Illegal arguments
        assertThrows(IllegalArgumentException.class, ()-> docStore.getMetadata(blankUri, "key"), "Should throw exception when URI is blank");
        assertThrows(IllegalArgumentException.class, ()-> docStore.getMetadata(null, "key"), "Should throw exception when URI is null");
        assertThrows(IllegalArgumentException.class, ()-> docStore.getMetadata(uri3, "key"), "Should throw exception when no doc exists for such a URI");

        //Illegal keys, everything else tested to be legal
        assertThrows(IllegalArgumentException.class, ()-> docStore.getMetadata(uri1, ""), "Should throw exception when no Key is blank");
        assertThrows(IllegalArgumentException.class, ()-> docStore.getMetadata(uri1, null), "Should throw exception when no Key is Null");
    }

    @Test
    void getMetadataVisaVisHeapTest() throws IOException{
        put5MoreDocs(); //now it has uri1,uri2, uri a,b,c,d,e
        docStore.get(uri2);
        docStore.get(urid);
        docStore.get(uria);

        ensureHeapOrderingIs1bce2da();
    }

    private void ensureHeapOrderingIs1bce2da() throws IOException {
        File file1 = new File(System.getProperty("user.dir"), "www.xyz.com.json");
        File file2 = new File(System.getProperty("user.dir"), "www.abc123.com.json");
        File filea = new File(System.getProperty("user.dir"), "abc.com.json");
        File fileb = new File(System.getProperty("user.dir"), "bcd.net.json");
        File filec = new File(System.getProperty("user.dir"), "cde/world/com/what.json");
        File filed = new File(System.getProperty("user.dir"), "d.com.json");
        File filee = new File(System.getProperty("user.dir"), "e.json");

        docStore.setMaxDocumentCount(7);
        assertFalse(file1.exists());
        docStore.setMaxDocumentCount(6);
        assertTrue(file1.exists());
        assertFalse(fileb.exists());
        docStore.setMaxDocumentCount(5);
        assertTrue(fileb.exists());
        assertFalse(filec.exists());
        docStore.setMaxDocumentCount(4);
        assertTrue(filec.exists());
        assertFalse(filee.exists());
        docStore.setMaxDocumentCount(3);
        assertTrue(filee.exists());
        assertFalse(file2.exists());
        docStore.setMaxDocumentCount(2);
        assertTrue(file2.exists());
        assertFalse(filed.exists());
        docStore.setMaxDocumentCount(1);
        assertTrue(filed.exists());
        assertFalse(filea.exists());


        //delete files to avoid clutter
        file1.delete();
        file2.delete();
        file1.delete();
        fileb.delete();
        filec.delete();
        filed.delete();
        filee.delete();
    }

    private void ensureHeapOrderingIs21bceda() throws IOException {
        File file1 = new File(System.getProperty("user.dir"), "www.xyz.com.json");
        File file2 = new File(System.getProperty("user.dir"), "www.abc123.com.json");
        File filea = new File(System.getProperty("user.dir"), "abc.com.json");
        File fileb = new File(System.getProperty("user.dir"), "bcd.net.json");
        File filec = new File(System.getProperty("user.dir"), "cde/world/com/what.json");
        File filed = new File(System.getProperty("user.dir"), "d.com.json");
        File filee = new File(System.getProperty("user.dir"), "e.json");

        docStore.setMaxDocumentCount(7);
        assertFalse(file2.exists());
        docStore.setMaxDocumentCount(6);
        assertTrue(file2.exists());
        assertFalse(file1.exists());
        docStore.setMaxDocumentCount(5);
        assertTrue(file1.exists());
        assertFalse(fileb.exists());
        docStore.setMaxDocumentCount(4);
        assertTrue(fileb.exists());
        assertFalse(filec.exists());
        docStore.setMaxDocumentCount(3);
        assertTrue(filec.exists());
        assertFalse(filee.exists());
        docStore.setMaxDocumentCount(2);
        assertTrue(filee.exists());
        assertFalse(filed.exists());
        docStore.setMaxDocumentCount(1);
        assertTrue(filed.exists());
        assertFalse(filea.exists());
        //delete files to avoid clutter
        file1.delete();
        file2.delete();
        file1.delete();
        fileb.delete();
        filec.delete();
        filed.delete();
        filee.delete();
    }



    @Test
    void setMetadataTestVisaVisHeap() throws IOException{
        put5MoreDocs(); //now it has uri1,uri2, uri a,b,c,d,e

        docStore.setMetadata(uria, "key", "value");
        docStore.setMetadata(uri2, "ksdfey", "vaslue");
        docStore.setMetadata(urid, "kasey", "vaslue");
        docStore.undo(uria); //Undoing setting of MD should also make it most recent

        ensureHeapOrderingIs1bce2da();
    }

    @Test
    void getTestVisaVisHeap() throws IOException{
        put5MoreDocs(); //not it has uri1,uri2, uri a,b,c,d,e

        docStore.get(uri2);
        docStore.get(urid);
        docStore.get(uria);

        ensureHeapOrderingIs1bce2da();
    }

    @Test
    void deleteTestAndUndoDeleteVisaVisHeap() throws IOException{
        put5MoreDocs(); //not it has uri1,uri2, uri a,b,c,d,e

        docStore.delete(uri2);
        docStore.delete(uria);
        docStore.delete(urid);


        docStore.setMaxDocumentCount(4); //set it to not have room for the 3 deleted docs, which should not do anything considering they are already deleted and only exist in undo stack
        docStore.setMaxDocumentCount(7);
        docStore.undo(uri2);
        docStore.undo(); //should undo urid
        docStore.undo(uria);
        ensureHeapOrderingIs1bce2da();
    }

    @Test
    void searchTestVisaVisHeap() throws IOException{
        put5MoreDocs(); //not it has uri1,uri2, uri a,b,c,d,e

        docStore.search("oneonetwo"); //should make  uria and urib most recent

        docStore.setMaxDocumentCount(2);
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        //should be left with just a and b, as those were most recent.
    }

    @Test
    void searchByPrefixTestVisaVisHeap() throws IOException{
        put5MoreDocs(); //not it has uri1,uri2, uri a,b,c,d,e

        docStore.searchByPrefix("33"); //should make  uria and urib most recent

        docStore.setMaxDocumentCount(3);
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        assertNotNull(docStore.get(uric));
        //should be left with just a, b, and as those were most recently used
    }

    @Test
    void searchByMetaDataTestVisaVisHeap() throws IOException {
        put5MoreDocs(); //now it has uri1,uri2, uri a,b,c,d,e

        docStore.setMetadata(uria, "key", "value");
        docStore.setMetadata(uri2, "key", "value");
        docStore.setMetadata(urid, "key", "value");
        docStore.setMetadata(urid, "keyd", "valued");

        docStore.get(uri1);
        docStore.get(urib);
        docStore.get(uric);
        docStore.get(urie);

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        docStore.searchByMetadata(map);
        map.put("keyd", "valued");
        docStore.searchByMetadata(map);
        docStore.get(uria);

        ensureHeapOrderingIs1bce2da();
    }
    @Test
    void searchByKeywordAndMetaDataTestVisaVisHeap() throws IOException {
        put5MoreDocs(); //now it has uri1,uri2, uri a,b,c,d,e

        docStore.setMetadata(uria, "key", "value");
        docStore.setMetadata(uri2, "key", "value");
        docStore.setMetadata(urid, "key", "value");

        docStore.get(uri1);
        docStore.get(urib);
        docStore.get(uric);
        docStore.get(urie);

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        docStore.get(uri2);
        docStore.searchByKeywordAndMetadata("3Happy", map); //d
        docStore.searchByKeywordAndMetadata("illness", map); //a

        ensureHeapOrderingIs1bce2da();
    }

    @Test
    void searchByprefixAndMetaDataTestVisaVisHeap() throws IOException {
        put5MoreDocs(); //now it has uri1,uri2, uri a,b,c,d,e

        docStore.setMetadata(uria, "key", "value");
        docStore.setMetadata(uri2, "key", "value");
        docStore.setMetadata(urid, "key", "value"); //LRU

        docStore.get(uri1);
        docStore.get(urib);
        docStore.get(uric);
        docStore.get(urie);

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        docStore.searchByPrefixAndMetadata("T", map);
        //a and d should reset last use time, but nothing else should, despite having T as prefix xor  having "key", "value" as MD!
        docStore.get(uria);
        ensureHeapOrderingIs21bceda();
    }

    @Test
    void searchByKeywordAndMetaDataTest2VisaVisHeap() throws IOException {
        put5MoreDocs(); //now it has uri1,uri2, uri a,b,c,d,e

        docStore.setMetadata(uria, "key", "value");
        docStore.setMetadata(uri2, "key", "value");

        docStore.setMetadata(urid, "key", "value"); //LRU

        Document a = docStore.get(uria);
        Document d = docStore.get(urid);

        docStore.get(uri1);
        docStore.get(urib);
        docStore.get(uric);
        docStore.get(urie);

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        long time = a.getLastUseTime();
        docStore.searchByKeywordAndMetadata("This", map);
        //a and d should reset last use time, but nothing else should, despite having This as keyword xor  having "key", "value" as MD!
        assertTrue(a.getLastUseTime()>time); //undoing, re-putting, SHOULD change its last use time

        assertEquals(a.getLastUseTime(), d.getLastUseTime());
        //break the tie between a and d
        docStore.get(uria);
        ensureHeapOrderingIs21bceda();
    }
    @Test
    void deleteAllWithMetaDataTestVisAVisHeap() throws IOException {
        put5MoreDocs(); //now it has uri1,uri2, uri a,b,c,d,e
        assertNotNull(docStore.get(uri1));
        assertNotNull(docStore.get(uri2));
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        assertNotNull(docStore.get(uric));
        assertNotNull(docStore.get(urid));
        assertNotNull(docStore.get(urie));


        //heap order is now 1,2,a,b,c,d,e

        docStore.setMetadata(uria, "key", "value");
        docStore.setMetadata(uri2, "key", "value");


        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        docStore.deleteAllWithMetadata(map);
        //deletes a and 2, heap order is now 1,b,c,d,e



        docStore.setMaxDocumentCount(5);
        docStore.undo(uria); //should bring back uria but not uri2 and knock uri1




        assertFalse(filea.exists());
        assertFalse(file2.exists());
        assertFalse(filec.exists());
        assertFalse(filed.exists());
        assertFalse(filee.exists());
        assertTrue(file1.exists());
        assertFalse(fileb.exists());
    }

    @Test
    void deleteAllTestVisAVisHeap() throws IOException {
        put5MoreDocs(); //now it has uri1,uri2, uri a,b,c,d,e

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        docStore.deleteAll("onetwo");

        docStore.setMaxDocumentCount(5);
        docStore.undo(uria); //should bring back uria and knock out uri1

        assertFalse(filea.exists());
        assertFalse(file2.exists());
        assertFalse(filec.exists());
        assertFalse(filed.exists());
        assertFalse(filee.exists());
        assertTrue(file1.exists());
        assertFalse(fileb.exists());
    }

    @Test
    void deleteAllWithPrefixTestVisAVisHeap() throws IOException {
        put5MoreDocs(); //now it has uri1,uri2, uri a,b,c,d,e

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        Document a = docStore.get(uria);
        long time = a.getLastUseTime();

        docStore.deleteAllWithPrefix("onetw");

        assertEquals(time, a.getLastUseTime()); //Deleting should not change its last use time

        docStore.setMaxDocumentCount(5);
        docStore.undo(uria); //should bring back uria and knock out urie

        assertTrue(a.getLastUseTime()>time); //undoing, reputting, SHOULD change its last use time

        assertFalse(filea.exists());
        assertFalse(file2.exists());
        assertFalse(filec.exists());
        assertFalse(filed.exists());
        assertFalse(filee.exists());
        assertTrue(file1.exists());
        assertFalse(fileb.exists());
    }


    @Test
    void putTestVisAVisHashTable() throws IOException {
        // First making some sample input streams
        byte[] arrayOfHowdy = {72, 111, 119, 100, 121}; //ASCII for "Howdy". (For testing longer strings, will useString.getBytes)
        byte[] anotherArrayOfHowdy = {72, 119, 100, 121}; //ASCII for "Hwdy". (For testing longer strings, will useString.getBytes)
        byte[] arrayOfRandomBinary = {(byte) 001101011,(byte) 00000000, (byte)11111111,(byte) 10101010};
        ByteArrayInputStream  textInput = new ByteArrayInputStream(arrayOfHowdy, 1, 4);
        ByteArrayInputStream  anothertextInput = new ByteArrayInputStream(anotherArrayOfHowdy, 0, 3);

        ByteArrayInputStream binaryInput = new ByteArrayInputStream(arrayOfRandomBinary,  1, 7);

        //Testing Null URI's or formats (no longer testing blank/empty URI) as per change in specs.
        assertThrows(IllegalArgumentException.class, ()-> docStore.put(textInput, null, DocumentFormat.TXT)); //Null URI
        textInput.reset();
        assertThrows(IllegalArgumentException.class, ()-> docStore.put(textInput, uri1, null)); //Null Format

        //Proper Input
        textInput.reset(); //This is needed so that testInput does not get swallowed up by previous use of it, turning it into an empty string.

        assertEquals(0, docStore.put(textInput, uri3, DocumentFormat.TXT)); //if no previous doc at the given URI, return 0

        textInput.reset(); //This is needed so that testInput does not get swallowed up by previous use of it, turning it into an empty string.

        assertEquals(docStore.get(uri1).hashCode(), docStore.put(textInput, uri1, DocumentFormat.TXT)); //if there IS doc at URI, return its hashcode
        assertEquals(docStore.get(uri1).hashCode(), docStore.put(anothertextInput, uri1, DocumentFormat.TXT)); //if there IS doc at URI, return its hashcode

        assertNotNull(docStore.get(uri3)); //Ensures doc with uri3 was actually "put" in
        assertNull(docStore.get(uri4));

        //When InputStream is null, this is a delete (so long as format is not null/blank)
        assertNotNull(docStore.get(uri1)); //Ensuring doc URI1 is present prior to deletion
        assertEquals(docStore.get(uri1).hashCode(), docStore.put(null, uri1,DocumentFormat.TXT)); //Should return previous hashcode of deleted doc
        assertNull(docStore.get(uri1)); //Ensuring doc with URI uri1 has been successfully deleted.
        assertEquals(0, docStore.put(null, uri4, DocumentFormat.TXT)); //Should return 0 if attempt to delete a nonexistant doc
    }

    //DocumentStoreImpl.get() is tested in other methods

    @Test
    void deleteTestVisAVisHash() throws IOException {
        assertEquals(1, docStore.searchByPrefix("owd").size()); //checking if doc is in TRIE (prefix search, like regular keyword search, utilizes trie
        assertNotNull(docStore.get(uri1)); //Ensures doc with uri1 was actually "put" in during @BeforeEach stage
        assertTrue(docStore.delete(uri1), "should be True for a successful delete");
        assertFalse(docStore.delete(uri3), "should be False for a delete of a non-existant doc");
        assertNull((docStore.get(uri1))); //Ensures doc with uri1 has been properly deleted, is no longer in the store.
        assertEquals(0, docStore.searchByPrefix("owd").size()); //checking if deleted doc is removed FROM TRIE
    }
    ///                   !!!
    /// THE FOLLOWING SEVERAL DEDICATED UNDO TESTS FOCUS ON UNDO
    /// VIS A VIS THE hashtable NOT the trie
    ///
    @Test
    void noArgUndoTestForPuts() throws IOException {
        docStore.put(textInput1, uri1, DocumentFormat.TXT);
        assertEquals(0, docStore.put(textInput2, uri4, DocumentFormat.TXT)); //DocStore.put returns an int, so null becomes 0
        textInput1.reset();
        assertEquals(0, docStore.put(textInput1, uri3, DocumentFormat.TXT)); //DocStore.put returns an int, so null becomes 0

        assertNotNull(docStore.get(uri3));
        docStore.undo(); //Undoing most recent
        assertNull(docStore.get(uri3));

        assertNotNull(docStore.get(uri4));
        docStore.undo(); //Undoing SECOND to most recent by calling undo a SECOND TIME
        assertNull(docStore.get(uri4));

        assertEquals(0, docStore.put(textInput4, uri3, DocumentFormat.TXT)); //DocStore.put returns an int, so null becomes 0
        assertNotNull(docStore.get(uri3));
        docStore.undo(); //Undoing most recent
        assertNull(docStore.get(uri3));

        //Undoing a put that acted as a CHANGE (changing the Document at an existing uri
        assertEquals("Ho",docStore.get(uri1).getDocumentTxt()); //text of textInput1, used in put in beg. of this method
        docStore.undo(); //Undoing most recent
        assertEquals("owdy",docStore.get(uri1).getDocumentTxt()); //The text of the document put at uri1 in beforeEach method
        textInput1.reset();
        textInput2.reset();
        textInput4.reset();
        //change document at URI uri1 in table four times, undo each successively
        docStore.put(textInput1, uri2, DocumentFormat.TXT);
        docStore.put(textInput2, uri2, DocumentFormat.TXT);
        docStore.put(textInput3, uri2, DocumentFormat.TXT);
        docStore.put(textInput4, uri2, DocumentFormat.TXT);
        assertEquals("wd",docStore.get(uri2).getDocumentTxt());
        docStore.undo();
        assertEquals("Howd",docStore.get(uri2).getDocumentTxt());
        docStore.undo();
        assertEquals("How",docStore.get(uri2).getDocumentTxt());
        docStore.undo();
        assertEquals("Ho",docStore.get(uri2).getDocumentTxt());
        assertNull(docStore.get(uri2).getDocumentBinaryData()); //Should be null, it is currently a text doc
        docStore.undo();
        assertNotNull(docStore.get(uri2).getDocumentBinaryData()); //Now it is reverted to its state as a binary doc, from beforeEach
        docStore.undo();
        assertNull(docStore.get(uri2)); //Now it is not a doc at all, original pur has been undone
    }

    @Test
    void noArgUndoTestForDelete() throws IOException {
        docStore.put(textInput1, uri3, DocumentFormat.TXT);
        Document a = docStore.get(uri3);

        docStore.put(textInput3, uri4, DocumentFormat.TXT);
        Document x = docStore.get(uri4);

        docStore.put(textInput2, uri3, DocumentFormat.TXT);
        Document b = docStore.get(uri3);

        docStore.put(textInput4, uri4, DocumentFormat.TXT);
        Document y = docStore.get(uri4);

        docStore.delete(uri4);
        docStore.delete(uri3);
        assertNull(docStore.get(uri4));
        assertNull(docStore.get(uri3));

        //So far we put at 3 and 4, then reput at 3 and then 4, then deleted both. Now we will delete and check each command in backwards order.

        docStore.undo(); //undoing delete(uri3)
        assertNotNull(docStore.get(uri3));
        docStore.undo(); //undoing delete(uri4)
        assertEquals(y, docStore.get(uri4));
        assertEquals(b, docStore.get(uri3));
        docStore.undo(); //undoing reput of 4
        assertEquals(b, docStore.get(uri3));
        assertEquals(x, docStore.get(uri4)); //uri4 hasnt had its undoing of reputting yet
        docStore.undo(); //undion reput of 3
        assertEquals(x, docStore.get(uri4));
        assertEquals(a, docStore.get(uri3));
        docStore.undo(); //undoing initial put at uri 4
        assertEquals(a, docStore.get(uri3));
        assertNull(docStore.get(uri4));
    }

    @Test
    void noArgUndoTestForMetaDataManipulation() throws IOException {
        docStore.put(textInput1, uri3, DocumentFormat.TXT);
        Document a = docStore.get(uri3);
        docStore.put(textInput4, uri3, DocumentFormat.TXT);
        Document b = docStore.get(uri3);
        docStore.setMetadata(uri3, "myKey", "myValue");
        docStore.setMetadata(uri3, "myKey", "updatedValue");
        docStore.setMetadata(uri3, "someOtherKey", "otherValue");
        docStore.undo();
        assertEquals("updatedValue" ,docStore.getMetadata(uri3, "myKey"));
        docStore.undo();
        assertEquals("myValue" ,docStore.getMetadata(uri3, "myKey"));
        docStore.undo();
        assertNull(docStore.getMetadata(uri3, "myKey"));
        assertEquals(b, docStore.get(uri3)); //Ensure that undoing of delete makes it likeDocument is exactly as it was before
        docStore.undo();
        assertNull(docStore.getMetadata(uri3, "myKey"));
        assertEquals(a, docStore.get(uri3)); //Ensure that undoing of delete makes it likeDocument is exactly as it was before
    }



    @Test
    void UndoWithArgTest() throws IOException {
        //First checking undoing of delete and undoing of re-putting on same uri, including checking that metadata values are not lost in the process
        assertNotNull(docStore.get(uri1));
        assertNotNull(docStore.get(uri2));
        docStore.setMetadata(uri1, "uri1", "value1");
        docStore.setMetadata(uri1, "uri1", "value2");
        docStore.setMetadata(uri2, "uri2", "value11");
        docStore.setMetadata(uri2, "uri2", "value12");
        docStore.setMetadata(uri2, "uri2 anotherkey", "anothervalue");


        docStore.put(textInput4, uri1, DocumentFormat.TXT); //Replace doc at URI1
        docStore.delete(uri2); //delete doc at uri2
        assertNull(docStore.get(uri2));
        docStore.undo(uri2); //undoing delete
        assertNotNull(docStore.get(uri2));
        docStore.undo(uri1); //undoing replaceViaPut at uri1

        //Check that undo-ing brings back old document with all its old metadata values
        assertEquals("value2", docStore.getMetadata(uri1, "uri1"), "undid put that replaced document, old doc should have its old metadatavalues");
        assertEquals("anothervalue", docStore.getMetadata(uri2, "uri2 anotherkey"), "undeleted document should go back to having its old metadatavalues");

        //Check that it even remembers the history of metadata values of once-replaced doc
        //this also constittues the check that undoing setMetadata
        docStore.undo(uri1); //undoing the resetting of metadata value at key String "uri1" from "value2" back to "value1"
        assertEquals("value1", docStore.getMetadata(uri1, "uri1"));

        //check that undoing an initial, fresh put works as expected
        docStore.put(binaryInput2, uri4, DocumentFormat.BINARY);
        docStore.put(binaryInput1, uri3, DocumentFormat.BINARY);
        Document x = docStore.get(uri4);
        Document y = docStore.get(uri3);
        docStore.undo(uri3);
        assertNull(docStore.get(uri3));
        assertEquals(x, docStore.get(uri4));
        docStore.undo(); //Should just undo most recent thing to not be undone
        assertNull(docStore.get(uri4));

        //Check that undoing a initial setting of meta data works
        assertEquals("anothervalue", docStore.getMetadata(uri2, "uri2 anotherkey"));
        docStore.undo(uri2);
        assertNull(docStore.getMetadata(uri2, "uri2 anotherkey"));
    }

    @Test
    void searchTest() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);

        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        Document three = docStore.get(uric);
        Document four = docStore.get(urid);
        Document five = docStore.get(urie);

        List<Document> HowOrdered = List.of(one, two, four);  //One has most, then 2, then 4 has a singular How.
        assertEquals(HowOrdered, docStore.search("How"));
        List<Document> MOrdered = List.of(three, four);  //Three has 5 Ms, all of which ahve non letters touching them. FIVE has LOWERCASE m, should not be here
        assertEquals(MOrdered, docStore.search("M"));
        //Empty collection:
        assertEquals(0, docStore.search("Happy").size()); //all happy's are touching NUMBERS which are included in words, or lowercase
        //Tied--Two documents who both have a word once, both should make list (no secondary order specified):
        assertEquals(2, docStore.search("kxl").size());

        //Never return null. return empty list if no matches:
        assertEquals(List.of(), docStore.search(null));
        //Also return empty list if "" empty keyword is searched, as per piazza 190
        assertEquals(List.of(), docStore.search(""));
    }

    @Test
    void searchByPrefixTest() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);

        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        Document three = docStore.get(uric);
        Document four = docStore.get(urid);
        Document five = docStore.get(urie);

        assertEquals(List.of(two, one, five), docStore.searchByPrefix("kxl")); //two has it thrice, each with more chars following kxl. then 1 has it once has kxl once with more chars, then 5 has once with kxl
        assertEquals(List.of(three, two), docStore.searchByPrefix("short")); //Ignore ShOrT, count shor$test
        //assertEquals(List.of(one, five), docStore.searchByPrefix("kxl"));

        //Never return null. return empty list if no matches:
        assertEquals(List.of(), docStore.searchByPrefix(null));
        //Also return empty list if "" empty keyword is searched, as per piazza 190
        assertEquals(List.of(), docStore.searchByPrefix(""));
    }




    @Test
    void deleteAllAndUndoTest() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);
        docStore.setMetadata(urie, "key", "value");
        assertDoesNotThrow(()-> docStore.getMetadata(urie, "key"));

        assertEquals(5,docStore.search("is").size()); //5 docs  in TRIE have "is"
        docStore.deleteAll("illness"); //Delete 2 of 7 docs in store, 2 of the 5 IS docs
        assertEquals(3,docStore.search("is").size()); //Now only 3 docs in TRIE should have is
        assertEquals(0, docStore.search("illness").size());
        assertThrows(IllegalArgumentException.class, ()-> docStore.getMetadata(urie, "key"));

        //Undo Delete all. Once deleted Documents get their MD back:
        docStore.undo(urie);
        assertDoesNotThrow(()-> docStore.getMetadata(urie, "key"));

        //Undo last remaining delete from deleteAll command set, should pop command stack, revealing next command
        docStore.undo(uria);
        assertEquals("value", docStore.getMetadata(urie, "key"));
        docStore.undo(); //Top of command stack, to be undone, should set MD
        assertNull(docStore.getMetadata(urie, "key"));
        docStore.undo(); //Top of command stack, to be undone, should now be put urie command
        assertNull(docStore.get(urie));

        //DeleteAll that only deletes ONE DOCUMENT
        assertNotNull(docStore.get(urib));
        docStore.deleteAll("Bazinga");
        assertNull(docStore.get(urib));
        docStore.undo(urib); //Should pop command off stack, revealing put(urid). Ensure this is true in next 3 lines:
        assertNotNull(docStore.get(urid));
        docStore.undo();
        assertNull(docStore.get(urid));

        //Docstore currently contains
        //DeleteAll that deletes ZERO DOCUMENTS:
        docStore.deleteAll("aWoRdThatExIsTsInNotASingularDocumeNt");
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        assertNotNull(docStore.get(uric));
        docStore.undo(); //THIS IS SHOULD ONLY UNDO THE deleteAll OF NOTHING, as it still was a command like any other
        assertNotNull(docStore.get(uric));
        docStore.undo(); //only now should undo undo the put of uric
        assertNull(docStore.get(uric));
    }

    @Test
    void deleteAllWithPrefixAndUndoTest() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);
        docStore.setMetadata(urie, "key", "value");
        assertDoesNotThrow(()-> docStore.getMetadata(urie, "key"));

        assertEquals(5,docStore.search("is").size()); //5 docs have "is"
        docStore.deleteAllWithPrefix("illne"); //Delete 2 of 7 docs in store, 2 of the 5 IS docs
        assertEquals(3,docStore.search("is").size()); //Now only 3 docs should have is
        assertEquals(0, docStore.search("illness").size());
        assertThrows(IllegalArgumentException.class, ()-> docStore.getMetadata(urie, "key"));

        //Undo Delete all. Once deleted Documents get their MD back:
        docStore.undo(urie);
        assertDoesNotThrow(()-> docStore.getMetadata(urie, "key"));

        //Undo last remaining delete from deleteAll command set, should pop command stack, revealing next command
        docStore.undo(uria);
        assertEquals("value", docStore.getMetadata(urie, "key"));
        docStore.undo(); //Top of command stack, to be undone, should set MD
        assertNull(docStore.getMetadata(urie, "key"));
        docStore.undo(); //Top of command stack, to be undone, should now be put urie command
        assertNull(docStore.get(urie));

        //DeleteAllWithPrefix that only deletes ONE DOCUMENT
        assertNotNull(docStore.get(urib));
        docStore.deleteAllWithPrefix("Bazin");
        assertNull(docStore.get(urib));
        docStore.undo(urib); //Should pop command off stack, revealing put(urid). Ensure this is true in next 3 lines:
        assertNotNull(docStore.get(urid));
        docStore.undo();
        assertNull(docStore.get(urid));

        //Docstore currently contains
        //DeleteAllWithPrefix that deletes ZERO DOCUMENTS:
        docStore.deleteAllWithPrefix("aprefixThatExIsTsInNotASingularDocumeNt");
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        assertNotNull(docStore.get(uric));
        docStore.undo(); //THIS IS SHOULD ONLY UNDO THE deleteAll OF NOTHING, as it still was a command like any other
        assertNotNull(docStore.get(uric));
        docStore.undo(); //only now should undo undo the put of uric
        assertNull(docStore.get(uric));

    }

    @Test
    void searchByMetadataTest() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);

        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        Document three = docStore.get(uric);
        Document four = docStore.get(urid);
        Document five = docStore.get(urie);

        docStore.setMetadata(uria, "abkey1", "abvalue1");
        docStore.setMetadata(urib, "abkey1", "abvalue1");

        docStore.setMetadata(uria, "abkey2", "abvalue2");
        docStore.setMetadata(urib, "abkey2", "abvalue2");

        docStore.setMetadata(urib, "bckey", "bcvalue");
        docStore.setMetadata(uric, "bckey", "bcvalue");
        docStore.setMetadata(uric, "ckey", "cvalue");

        Map<String, String> abMap = new HashMap<>();
        abMap.put("abkey1", "abvalue1");
        abMap.put("abkey2", "abvalue2");

        Map<String, String> bMap = new HashMap<>(abMap);
        bMap.put("bckey", "bcvalue");

        Map<String, String> cMap = new HashMap<>();
        cMap.put("bckey", "bcvalue");
        cMap.put("ckey", "cvalue");

        assertEquals(List.of(one, two), docStore.searchByMetadata(abMap));
        assertEquals(List.of(two), docStore.searchByMetadata(bMap));
        assertEquals(List.of(three), docStore.searchByMetadata(cMap));

        //Should return empty list, not null, on an empty store
        DocumentStore docStoreTheSecond = new DocumentStoreImpl();
        assertEquals(List.of(), docStoreTheSecond.searchByMetadata(abMap));
    }

    @Test
    void searchByKeywordAndMetadata() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);

        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        Document three = docStore.get(uric);
        Document four = docStore.get(urid);
        Document five = docStore.get(urie);

        docStore.setMetadata(uria, "abkey1", "abvalue1");
        docStore.setMetadata(urib, "abkey1", "abvalue1");

        docStore.setMetadata(uria, "abkey2", "abvalue2");
        docStore.setMetadata(urib, "abkey2", "abvalue2");

        docStore.setMetadata(urib, "bckey", "bcvalue");
        docStore.setMetadata(uric, "bckey", "bcvalue");

        docStore.setMetadata(uric, "ckey", "cvalue");

        docStore.setMetadata(uria, "abckey", "abcvalue");
        docStore.setMetadata(urib, "abckey", "abcvalue");
        docStore.setMetadata(uric, "abckey", "abcvalue");

        Map<String, String> abMap = new HashMap<>();
        abMap.put("abkey1", "abvalue1");
        abMap.put("abkey2", "abvalue2");
        abMap.put("abckey", "abcvalue");

        Map<String, String> bMap = new HashMap<>(abMap);
        bMap.put("bckey", "bcvalue");
        bMap.put("abckey", "abcvalue");


        Map<String, String> cMap = new HashMap<>();
        cMap.put("bckey", "bcvalue");
        cMap.put("ckey", "cvalue");
        cMap.put("abckey", "abcvalue");

        Map<String, String> abcMap = new HashMap<>();
        abcMap.put("abckey", "abcvalue");

        Map<String, String> nobodyMap = new HashMap<>();
        nobodyMap.put("notakey", "notavalue");

        //ALl the docs that have MD also have keyword
        assertEquals(List.of(one, two, three), docStore.searchByKeywordAndMetadata("is" ,abcMap));
        //only 2/3 of docs with MD also have keyword
        assertEquals(List.of(one, two), docStore.searchByKeywordAndMetadata("onetwo" ,abcMap));
        //None of docs with MD also have keyword
        assertEquals(List.of(), docStore.searchByKeywordAndMetadata("notASingleDocHasSuchAWOrd" ,abcMap));

        //only 2/3 of docs with KEYWORD also have MD
        assertEquals(List.of(one, two), docStore.searchByKeywordAndMetadata("is" ,abMap));
        //None of docs with KEYWORD also have MD
        assertEquals(List.of(), docStore.searchByKeywordAndMetadata("is" ,nobodyMap));

        //Should return empty list, not null, on an empty store
        DocumentStore docStoreTheSecond = new DocumentStoreImpl();
        assertEquals(List.of(), docStoreTheSecond.searchByKeywordAndMetadata("is" ,abcMap));

        //Ensure proper ordering:
        assertEquals(List.of(three, one, two), docStore.searchByKeywordAndMetadata("333112" ,abcMap));
    }

    @Test
    void searchByPrefixAndMetadata() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);

        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        Document three = docStore.get(uric);
        Document four = docStore.get(urid);
        Document five = docStore.get(urie);

        docStore.setMetadata(uria, "abkey1", "abvalue1");
        docStore.setMetadata(urib, "abkey1", "abvalue1");

        docStore.setMetadata(uria, "abkey2", "abvalue2");
        docStore.setMetadata(urib, "abkey2", "abvalue2");

        docStore.setMetadata(urib, "bckey", "bcvalue");
        docStore.setMetadata(uric, "bckey", "bcvalue");

        docStore.setMetadata(uric, "ckey", "cvalue");

        docStore.setMetadata(uria, "abckey", "abcvalue");
        docStore.setMetadata(urib, "abckey", "abcvalue");
        docStore.setMetadata(uric, "abckey", "abcvalue");

        Map<String, String> abMap = new HashMap<>();
        abMap.put("abkey1", "abvalue1");
        abMap.put("abkey2", "abvalue2");
        abMap.put("abckey", "abcvalue");

        Map<String, String> bMap = new HashMap<>(abMap);
        bMap.put("bckey", "bcvalue");
        bMap.put("abckey", "abcvalue");


        Map<String, String> cMap = new HashMap<>();
        cMap.put("bckey", "bcvalue");
        cMap.put("ckey", "cvalue");
        cMap.put("abckey", "abcvalue");

        Map<String, String> abcMap = new HashMap<>();
        abcMap.put("abckey", "abcvalue");

        Map<String, String> nobodyMap = new HashMap<>();
        nobodyMap.put("notakey", "notavalue");

        //ALl the docs that have MD also have prefix
        assertEquals(List.of(one, two, three), docStore.searchByPrefixAndMetadata("tap" ,abcMap));
        //only 2/3 of docs with MD also have prefix (Also testing prefix that is entire word)
        assertEquals(List.of(one, two), docStore.searchByPrefixAndMetadata("onetwo" ,abcMap));
        //None of docs with MD also have prefix
        assertEquals(List.of(), docStore.searchByPrefixAndMetadata("notASingleDocHasSuchAWOrd" ,abcMap));

        //only 2/3 of docs with prefix also have MD
        assertEquals(List.of(one, two), docStore.searchByPrefixAndMetadata("tap" ,abMap));
        //None of docs with prefix also have MD
        assertEquals(List.of(), docStore.searchByPrefixAndMetadata("tap" ,nobodyMap));

        //Should return empty list, not null, on an empty store
        DocumentStore docStoreTheSecond = new DocumentStoreImpl();
        assertEquals(List.of(), docStoreTheSecond.searchByPrefixAndMetadata("is" ,abcMap));

        //Ensure proper ordering:
        assertEquals(List.of(two, three, one), docStore.searchByPrefixAndMetadata("33" ,abcMap));
    }

    @Test
    void undoDelete() throws IOException{
        assertNotNull(docStore.get(uri1));
        assertNotNull(docStore.get(uri2));
        docStore.delete(uri1);
        assertNull(docStore.get(uri1));
        docStore.undo();
    }

    @Test
    void deleteAllWithMetadataAndUndoTest() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);

        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        Document three = docStore.get(uric);
        Document four = docStore.get(urid);
        Document five = docStore.get(urie);

        docStore.setMetadata(uria, "abkey1", "abvalue1");
        docStore.setMetadata(urib, "abkey1", "abvalue1");

        docStore.setMetadata(uria, "abkey2", "abvalue2");
        docStore.setMetadata(urib, "abkey2", "abvalue2");

        docStore.setMetadata(urib, "bckey", "bcvalue");
        docStore.setMetadata(uric, "bckey", "bcvalue");

        docStore.setMetadata(uric, "ckey", "cvalue");

        docStore.setMetadata(uria, "abckey", "abcvalue");
        docStore.setMetadata(urib, "abckey", "abcvalue");
        docStore.setMetadata(uric, "abckey", "abcvalue");

        Map<String, String> abMap = new HashMap<>();
        abMap.put("abkey1", "abvalue1");
        abMap.put("abkey2", "abvalue2");
        abMap.put("abckey", "abcvalue");

        Map<String, String> bMap = new HashMap<>(abMap);
        bMap.put("bckey", "bcvalue");
        bMap.put("abckey", "abcvalue");


        Map<String, String> cMap = new HashMap<>();
        cMap.put("bckey", "bcvalue");
        cMap.put("ckey", "cvalue");
        cMap.put("abckey", "abcvalue");

        Map<String, String> abcMap = new HashMap<>();
        abcMap.put("abckey", "abcvalue");

        Map<String, String> nobodyMap = new HashMap<>();
        nobodyMap.put("notakey", "notavalue");

        ///Deleting multiple documents
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        docStore.deleteAllWithMetadata(abMap);
        assertNull(docStore.get(uria));
        assertNull(docStore.get(urib));
        //Undo BOTH AT SAME TIME
        docStore.undo();
        assertEquals("abvalue1", docStore.getMetadata(uria, "abkey1"));
        assertNotNull(docStore.get(urib));

        //Delete 3 at once and then only undo to one URI, then undo other 2 at once with .undo()
        //System.out.println(docStore.commandStack.size());

        docStore.deleteAllWithMetadata(abcMap);
        //System.out.println(docStore.commandStack.size()); //ONLY WORKS WHEN COMMAND STACK is temporarily made PUBLIC

        docStore.undo(urib);
        //System.out.println(docStore.commandStack.size());

        assertNull(docStore.get(uria));
        assertNull(docStore.get(uric));
        assertNotNull(docStore.get(urib));
        docStore.undo();
        //System.out.println(docStore.commandStack.size());

        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(uric));


        /// Deleting just one Document, undoing with both types of delete
        docStore.deleteAllWithMetadata(cMap);
        assertNull(docStore.get(uric));
        //System.out.println(docStore.commandStack.size());
        docStore.undo();
        //System.out.println(docStore.commandStack.size());

        assertNotNull(docStore.get(uric));

        assertEquals(Set.of(uric),docStore.deleteAllWithMetadata(cMap));
        //System.out.println(docStore.commandStack.size());

        assertNull(docStore.get(uric));
        docStore.undo(uric);
        //System.out.println(docStore.commandStack.size());

        assertNotNull(docStore.get(uric));

        assertEquals(Set.of(), docStore.deleteAllWithMetadata(nobodyMap));

    }
    @Test
    void deleteAllWithKeywordAndMetadataAndUndoTest() throws IOException {
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        //not yet putting in urid
        docStore.put(longtxt5, urie, DocumentFormat.TXT);

        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        Document three = docStore.get(uric);

        Document five = docStore.get(urie);

        docStore.setMetadata(uria, "abkey1", "abvalue1");
        docStore.setMetadata(urib, "abkey1", "abvalue1");

        docStore.setMetadata(uria, "abkey2", "abvalue2");
        docStore.setMetadata(urib, "abkey2", "abvalue2");

        docStore.setMetadata(urib, "bckey", "bcvalue");
        docStore.setMetadata(uric, "bckey", "bcvalue");

        docStore.setMetadata(uric, "ckey", "cvalue");

        docStore.setMetadata(uria, "abckey", "abcvalue");
        docStore.setMetadata(urib, "abckey", "abcvalue");
        docStore.setMetadata(uric, "abckey", "abcvalue");

        Map<String, String> abMap = new HashMap<>();
        abMap.put("abkey1", "abvalue1");
        abMap.put("abkey2", "abvalue2");
        abMap.put("abckey", "abcvalue");

        Map<String, String> bMap = new HashMap<>(abMap);
        bMap.put("bckey", "bcvalue");
        bMap.put("abckey", "abcvalue");


        Map<String, String> cMap = new HashMap<>();
        cMap.put("bckey", "bcvalue");
        cMap.put("ckey", "cvalue");
        cMap.put("abckey", "abcvalue");

        Map<String, String> abcMap = new HashMap<>();
        abcMap.put("abckey", "abcvalue");

        Map<String, String> nobodyMap = new HashMap<>();
        nobodyMap.put("notakey", "notavalue");

        ///Deleting multiple documents
        //Delete and undo ALl the docs that have MD also have keyword
        assertEquals(Set.of(uria, urib, uric), docStore.deleteAllWithKeywordAndMetadata("is", abcMap));

        assertNull(docStore.get(uria)); //Should be deleted, not written to disk, thus get will not immediately rehydrate them
        assertNull(docStore.get(urib));
        docStore.undo(uria);
        docStore.undo(urib);


        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));

        assertNull(docStore.get(uric));
        docStore.undo(); //bring back uric
        assertNotNull(docStore.get(uric));


        docStore.search("is");
        assertEquals(Set.of(one, two, three), new HashSet<>(docStore.searchByMetadata(abcMap))); //NO REQURIED ORDER, because there is no word/prefix to go based on wordcount

        assertEquals(List.of(one, two, three), docStore.searchByKeywordAndMetadata("is" ,abcMap));

        //only 2/3 of docs with MD also have keyword
        //assertNotNull(docStore.get(uria));
        //assertNotNull(docStore.get(urib));
        assertEquals(Set.of(uria, urib),docStore.deleteAllWithKeywordAndMetadata("onetwo", abcMap));
        //assertNull(docStore.get(uria));
        //assertNull(docStore.get(urib));
        assertNotNull(docStore.get(uric));        /*
        docStore.undo();
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        assertEquals(List.of(one, two), docStore.searchByKeywordAndMetadata("onetwo" ,abcMap));

        //None of docs with MD also have keyword
        assertEquals(List.of(), docStore.searchByKeywordAndMetadata("notASingleDocHasSuchAWOrd" ,abcMap));
        docStore.put(textInput4, urid, DocumentFormat.TXT);
        docStore.deleteAllWithKeywordAndMetadata("notasingledochas", abcMap);
        docStore.undo(); //Should undo the nondeleting delete, not put of urid
        assertNotNull(docStore.get(urid));
        docStore.undo(urie); //undoing something from way earlier, not the put of urid
        assertNotNull(docStore.get(urid));
        docStore.undo(); //finally undo put of urid
        assertNull(docStore.get(urid));

        //only 2/3 of docs with KEYWORD also have MD
        assertEquals(Set.of(uria, urib), docStore.deleteAllWithKeywordAndMetadata("is" ,abMap));
        docStore.undo(uric);
        docStore.undo(urib);
        assertEquals(Set.of(urib), docStore.deleteAllWithKeywordAndMetadata("is" ,abMap));

        //None of docs with KEYWORD also have MD
        assertEquals(Set.of(), docStore.deleteAllWithKeywordAndMetadata("is" ,nobodyMap));

        //Should return empty list, not null, on an empty store
        DocumentStore docStoreTheSecond = new DocumentStoreImpl();
        assertEquals(Set.of(), docStoreTheSecond.deleteAllWithKeywordAndMetadata("is" ,abcMap));

        //Piazza 190: return empty set for delete method on empty keyword or prefix:
        assertEquals(Set.of(), docStore.deleteAllWithKeywordAndMetadata("" ,abcMap));
        assertEquals(Set.of(), docStore.deleteAllWithKeywordAndMetadata(null ,abcMap));


         */
    }

    @Test
    void deleteAllWithPrefixAndMetadata() throws IOException {
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        //not yet putting in urid
        docStore.put(longtxt5, urie, DocumentFormat.TXT);

        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        Document three = docStore.get(uric);

        Document five = docStore.get(urie);

        docStore.setMetadata(uria, "abkey1", "abvalue1");
        docStore.setMetadata(urib, "abkey1", "abvalue1");

        docStore.setMetadata(uria, "abkey2", "abvalue2");
        docStore.setMetadata(urib, "abkey2", "abvalue2");

        docStore.setMetadata(urib, "bckey", "bcvalue");
        docStore.setMetadata(uric, "bckey", "bcvalue");

        docStore.setMetadata(uric, "ckey", "cvalue");

        docStore.setMetadata(uria, "abckey", "abcvalue");
        docStore.setMetadata(urib, "abckey", "abcvalue");
        docStore.setMetadata(uric, "abckey", "abcvalue");

        Map<String, String> abMap = new HashMap<>();
        abMap.put("abkey1", "abvalue1");
        abMap.put("abkey2", "abvalue2");
        abMap.put("abckey", "abcvalue");

        Map<String, String> bMap = new HashMap<>(abMap);
        bMap.put("bckey", "bcvalue");
        bMap.put("abckey", "abcvalue");


        Map<String, String> cMap = new HashMap<>();
        cMap.put("bckey", "bcvalue");
        cMap.put("ckey", "cvalue");
        cMap.put("abckey", "abcvalue");

        Map<String, String> abcMap = new HashMap<>();
        abcMap.put("abckey", "abcvalue");

        Map<String, String> nobodyMap = new HashMap<>();
        nobodyMap.put("notakey", "notavalue");

        ///Deleting multiple documents
        //Delete and undo ALl the docs that have MD also have keyword
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));

        assertEquals(Set.of(uria, urib, uric), docStore.deleteAllWithPrefixAndMetadata("is", abcMap));

        assertNull(docStore.get(uria));
        assertNull(docStore.get(urib));
        docStore.undo(uria);
        docStore.undo(urib);
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        docStore.undo(); //bring back uric
        assertEquals(List.of(one, two, three), docStore.searchByPrefixAndMetadata("is" ,abcMap));

        //only 2/3 of docs with MD also have keyword
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        assertEquals(Set.of(uria, urib),docStore.deleteAllWithPrefixAndMetadata("onetw", abcMap));
        assertNull(docStore.get(uria));
        assertNull(docStore.get(urib));
        assertNotNull(docStore.get(uric)); //should not have deleted uric
        docStore.undo();
        assertNotNull(docStore.get(uria));
        assertNotNull(docStore.get(urib));
        assertEquals(List.of(one, two), docStore.searchByPrefixAndMetadata("onetw" ,abcMap));

        //None of docs with MD also have keyword
        assertEquals(List.of(), docStore.searchByPrefixAndMetadata("notASingleDocHasSuchAPrefix" ,abcMap));
        docStore.put(textInput4, urid, DocumentFormat.TXT);
        docStore.deleteAllWithPrefixAndMetadata("notasingledochas", abcMap);
        docStore.undo(); //Should undo the nondeleting delete, not put of urid
        assertNotNull(docStore.get(urid));
        docStore.undo(urie); //undoing something from way earlier, not the put of urid
        assertNotNull(docStore.get(urid));
        docStore.undo(urid); //finally undo put of urid
        assertNull(docStore.get(urid));

        //only 2/3 of docs with KEYWORD also have MD RIGHTHERERIGHTHERE
        assertEquals(Set.of(uria, urib), docStore.deleteAllWithPrefixAndMetadata("i" ,abMap));
        docStore.undo(uric);
        docStore.undo(urib);
        assertEquals(Set.of(urib), docStore.deleteAllWithPrefixAndMetadata("i" ,abMap));

        //None of docs with KEYWORD also have MD
        assertEquals(Set.of(), docStore.deleteAllWithKeywordAndMetadata("i" ,nobodyMap));

        //Should return empty list, not null, on an empty store
        DocumentStore docStoreTheSecond = new DocumentStoreImpl();
        assertEquals(Set.of(), docStoreTheSecond.deleteAllWithKeywordAndMetadata("i" ,abcMap));

        //Piazza 190: return empty set for delete method on empty keyword or prefix:
        assertEquals(Set.of(), docStore.deleteAllWithPrefixAndMetadata("" ,abcMap));
        assertEquals(Set.of(), docStore.deleteAllWithPrefixAndMetadata(null ,abcMap));
    }

    @Test
    void deleteNullShouldReturnEmptySetTest() throws IOException{
        /// Every delete of a null should return an empty list, as specs were vague and this would most closely resemble overall push to return empty collections rather than null or fail or throw exception

        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);

        Document one = docStore.get(uria);
        Document two = docStore.get(urib);

        docStore.setMetadata(uria, "abkey1", "abvalue1");
        docStore.setMetadata(urib, "abkey1", "abvalue1");

        docStore.setMetadata(uria, "abkey2", "abvalue2");
        docStore.setMetadata(urib, "abkey2", "abvalue2");


        Map<String, String> abMap = new HashMap<>();
        abMap.put("abkey1", "abvalue1");
        abMap.put("abkey2", "abvalue2");
        abMap.put("abckey", "abcvalue");

        docStore.delete(uri1); //Added in beforeEach
        assertEquals(Set.of(), docStore.deleteAll(null));
        assertEquals(Set.of(), docStore.deleteAllWithPrefix(null));
        assertEquals(Set.of(), docStore.deleteAllWithKeywordAndMetadata(null, abMap));
        assertEquals(Set.of(), docStore.deleteAllWithPrefixAndMetadata(null, abMap));
        assertEquals(Set.of(), docStore.deleteAllWithPrefixAndMetadata("i", null));
        assertNull(docStore.get(uri1));
        docStore.undo();
        docStore.undo();
        docStore.undo();
        docStore.undo();
        docStore.undo();
        assertNull(docStore.get(uri1)); //Still deleted
        docStore.undo();
        assertNotNull(docStore.get(uri1));
    }

    @Test
    void PutAndUndoPutVisAVisTrie() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        Document one = docStore.get(uria);
        Document two = docStore.get(urib);

        //Put that replaces a previously put document and its undo
        docStore.put(longtxt3, uria, DocumentFormat.TXT); //Overwrite put at uria
        longtxt3.reset();
        Document three = docStore.get(uria);
        assertEquals(List.of(two), docStore.search("onetwo"));
        docStore.undo(); //Should undo overwriting put at uria
        assertEquals(List.of(one, two) , docStore.search("onetwo"));
        docStore.put(longtxt3, uria, DocumentFormat.TXT); //Overwrite put at uria
        docStore.setMetadata(urib, "x", "x"); //some command relating to uriB
        docStore.undo(uria); //Should undo overwriting put at uria
        assertEquals(List.of(one, two) ,docStore.search("onetwo"));

        //Non-Replacing put and it's undo
        longtxt3.reset();
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        Document new3 = docStore.get(uric);
        assertEquals(List.of(new3), docStore.search("Thiswhere"));
        docStore.undo();
        assertEquals(List.of() , docStore.search("Thiswhere"));
    }

    @Test
    void DeleteAndUndoTestVisAVisTrie() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        assertEquals(List.of(one, two) , docStore.search("onetwo"));

        docStore.delete(uria);
        assertEquals(List.of(two) , docStore.search("onetwo"));
        docStore.put(binaryInput3, urid, DocumentFormat.BINARY); //Add to stack
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        Document three = docStore.get(uric);
        docStore.undo(uria); //undo second to top of stack
        assertEquals(List.of(one, two) , docStore.search("onetwo"));
        assertEquals(List.of(three, one, two) , docStore.searchByPrefix("w"));
        docStore.delete(urib);
        assertEquals(List.of(three, one) , docStore.searchByPrefix("w"));
        docStore.undo();
        assertEquals(List.of(three, one, two) , docStore.searchByPrefix("w"));
    }
    @Test
    void setMaxDocumentCountTest() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);

        //Doc store has these two plus uri1 and uri2 from beforeEach

        /// Test illegal inputs
        assertThrows(IllegalArgumentException.class, () -> docStore.setMaxDocumentCount(-1));
        assertThrows(IllegalArgumentException.class, () -> docStore.setMaxDocumentCount(0));

        ///setMaxDocumentCount to fewer docs than are in store
        docStore.setMaxDocumentCount(1); //Should entirely remove uri1,then uri2, then uria, leaving urib which is MOST recent
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(filea.exists());
        assertFalse(fileb.exists());


        /// setMaxDocumentCount to fewer docs in the store after having called public method to alter what is LRU
        docStore.setMaxDocumentCount(4); //Create space for new additions
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);

        docStore.setMetadata(uric,"key","val");
        docStore.setMaxDocumentCount(1);

        assertTrue(filed.exists());
        assertTrue(filee.exists());
        assertFalse(filec.exists());


        /// Add docs to an at capacity store, automatically forcing out LRU
        longtxt4.reset();
        longtxt5.reset();
        docStore.setMaxDocumentCount(2); //It already has uriC in it
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        Document doc3 = docStore.get(uric);
        Document doc4 = docStore.get(urid);
        docStore.setMetadata(uric, "akey", "avalue"); //adding a command to command stack relating to uriC
        docStore.get(urid); //Calling public method on uriD to make it more recent, so uriC will be LRU
        docStore.put(longtxt5, urie, DocumentFormat.TXT); //Putting while store is at capacity
        Document doc5 = docStore.get(urie);
        assertTrue(filec.exists()); //uriC's doc should have been pushed out
        /// Test that undo() CAN bring back docs sent to disk for memory reasons
    }

    @Test
    void undoMetaDataonThatWhichIsNowOnDisk() throws IOException{
        this.put5MoreDocs();
        docStore.setMetadata(uria, "key", "aValue");
        docStore.setMetadata(uria, "key", "CHANGED VALUE");
        docStore.setMaxDocumentCount(1);
        docStore.undo();
        assertEquals("aValue", docStore.getMetadata(uria, "key"));
    }

    @Test
    void metaDataSetOnDocInDiskMakesItLRU() throws IOException{
        put5MoreDocs();
        docStore.get(urid);
        docStore.get(urie);
        docStore.setMaxDocumentCount(2); //kick out all but d and e
        assertTrue(fileb.exists());
        assertTrue(filec.exists());
        docStore.setMetadata(urib, "hi", "there");
        docStore.getMetadata(uric, "non-existantkey");

        //check that b and c came back into memory, kicking d and e out
        assertFalse(fileb.exists());
        assertFalse(filec.exists());
        assertTrue(filed.exists());
        assertTrue(filee.exists());


    }

    @Test
    void setMaxDocumentBytesTest() throws IOException{
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);

        /// Test illegal inputs
        assertThrows(IllegalArgumentException.class, () -> docStore.setMaxDocumentBytes(-1));
        assertThrows(IllegalArgumentException.class, () -> docStore.setMaxDocumentBytes(0));

        ///setMaxDocumentbytes to fewer Bytes than are in store
        docStore.setMaxDocumentBytes(400); //Should entirely remove uri1 (4 bytes) ,then uri2 (3 bytes), then uria(473) , leaving urib which is MOST recent
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(filea.exists());
        assertFalse(fileb.exists());


        /// setMaxDocumentBytes to fewer bytes than are in the store after having called public method to alter what is LRU
        docStore.setMaxDocumentBytes(2000); //Create space for new additions (359 bytes in store from urib so far)
        docStore.put(longtxt3, uric, DocumentFormat.TXT); //159 bytes
        docStore.put(longtxt4, urid, DocumentFormat.TXT); //399 bytes (1 per each char except for "-" which takes up 3 bytes)
        docStore.put(longtxt5, urie, DocumentFormat.TXT); //243 bytes (1 per most chars, 3 per - and ')
        docStore.get(uric);

        docStore.setMaxDocumentBytes(402); //Should irreparably delete urib (359) and urid(399), which are LRU, and leave just enough room for the uric and urie
        assertTrue(fileb.exists());
        assertTrue(filed.exists());
        assertFalse(filee.exists());
        assertFalse(filec.exists());


        /// Add docs to an at capacity store, automatically forcing out LRU
        longtxt4.reset();
        longtxt5.reset();
        docStore.setMaxDocumentBytes(645); //Should irreparably delete urib (359) and urid(399), which are LRU, and leave just enough room for the uric and urie
        docStore.setMetadata(uric, "akey", "avalue"); //adding a command to command stack relating to uriB
        docStore.get(urie); //Calling public method on uriD to make it more recent, so uriC will be LRU
        docStore.put(longtxt4, urid, DocumentFormat.TXT); //This push of 399 bytes (+402 from before) goes OVER capacity, should irreperably delete uriC
        assertTrue(filec.exists()); //uriC's doc should have been pushed out, deleting 159 bytes from store
        assertFalse(filee.exists());
    }
    /// Test putting in multiple docs, bulk deleting some of them, lower maxBytes such that only some of deleted can return, undoDelete and waatch some but not all return
    /// only works when field currentdocbyts is made public
    @Test
    void edgeCase1() throws IOException {
        //already has 2
        docStore.put(longtxt1, uria, DocumentFormat.TXT);
        docStore.put(longtxt2, urib, DocumentFormat.TXT);
        docStore.put(longtxt3, uric, DocumentFormat.TXT);
        docStore.put(longtxt4, urid, DocumentFormat.TXT);
        docStore.put(longtxt5, urie, DocumentFormat.TXT);
        //System.out.println(docStore.currentDocBytes + "Before delete!!!!!!!!!!!!!!!!!!!");
        docStore.deleteAllWithPrefix("H"); //deletes 4 of 7
        //System.out.println(docStore.currentDocBytes + "After delete!!!!!!!!!!!!!!!!!!!");
        docStore.setMaxDocumentBytes(800);
        //System.out.println(docStore.currentDocBytes + "After set!!!!!!!!!!!!!!!!!!!");
        docStore.undo();
        //System.out.println(docStore.currentDocBytes + "After undo!!!!!!!!!!!!!!!!!!!");
    }

    @Test
    void changingLastUseTimeTest() throws IOException{
        put5MoreDocs();
        Document a = docStore.get(uria);
        Document b = docStore.get(urib);
        Document c = docStore.get(uric);
        Document d = docStore.get(urid);
        Document e = docStore.get(urie);

        //Get
        long aInitialTime = a.getLastUseTime();
        long bInitialTime = b.getLastUseTime();
        long cInitialTime = c.getLastUseTime();
        long dInitialTime = d.getLastUseTime();
        long eInitialTime = e.getLastUseTime();

        assertTrue(aInitialTime < docStore.get(uria).getLastUseTime()); //The call to get a will make its time be later

        //Set MD
        docStore.setMetadata(urib, "s","d");
        assertTrue(bInitialTime < b.getLastUseTime());

        //delete
        docStore.delete(uric);
        assertEquals(cInitialTime, c.getLastUseTime()); //Delete should not change last use time
        docStore.undo(uric);
        assertTrue(c.getLastUseTime()>cInitialTime);

        //deleteALlWithPrefix
        docStore.deleteAllWithPrefix("illne"); //deletes a and e
        assertNotEquals(a.getLastUseTime(), e.getLastUseTime()); //Delete should not change last use time
        assertEquals(eInitialTime, e.getLastUseTime()); //Delete should not change last use time
        docStore.undo();
        assertEquals(a.getLastUseTime(),e.getLastUseTime()); //undo should provide both docs with identical last use time
        assertTrue(e.getLastUseTime()>eInitialTime);

        //SearchByPrefixAndMetaData
        docStore.setMetadata(uria, "key", "value");
        docStore.setMetadata(uri2, "key", "value");
        docStore.setMetadata(urid, "key", "value");

        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        assertNotEquals(a.getLastUseTime(),d.getLastUseTime());
        docStore.searchByPrefixAndMetadata("T" , map); //Should affect a and d
        assertEquals(a.getLastUseTime(),d.getLastUseTime());
    }

    @Test
    void docsOnDiskAreRestoredByMethodCallsOnThem() throws IOException{
        put5MoreDocs();
        docStore.setMaxDocumentCount(1); //only uri e remains in memory
        assertFalse(filee.exists());
        assertTrue(filea.exists());

        docStore.get(uria); //Should bring a back, kick out e
        assertFalse(filea.exists());
        assertTrue(filee.exists());
        textInput4.reset();

        docStore.put(textInput4, uric, DocumentFormat.TXT); //Overwriting uriC brings it into memory, should kick out A
        assertFalse(filec.exists());
        assertTrue(filea.exists());

        docStore.searchByPrefix("onetw"); //Should bring back a and b, kick one of them out immediately thereafter
        //Next two lines effectively test that a xor b is on disk, a xor b is in memory
        assertTrue(filea.exists() || fileb.exists());
        assertFalse(filea.exists() && fileb.exists());
        assertTrue(filec.exists());
    }

    @Test
    void deserializedDocsAreIdenticalToOriginalVersionTest() throws IOException {
        put5MoreDocs();
        Document docAOriginal = docStore.get(uria);
        Document docBOriginal = docStore.get(urib);
        Document docCOriginal = docStore.get(uric);
        docStore.get(uri1); //make uri1 the LRU

        //Set MetaDatas of docs B and C
        docBOriginal.setMetadataValue("k","v");
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        docCOriginal.setMetadata(map);

        docStore.setMaxDocumentBytes(1); //Not enough space to fit ANY doc in memory

        assertTrue(filea.exists());
        assertEquals(docAOriginal, docStore.get(uria)); //get call gets from disk, returns it there afterward
        assertTrue(filea.exists());

        assertTrue(fileb.exists());
        assertEquals(docBOriginal, docStore.get(urib)); //get call gets from disk, returns it there afterward
        assertTrue(fileb.exists());

        assertTrue(filec.exists());
        assertEquals(docCOriginal, docStore.get(uric)); //get call gets from disk, returns it there afterward
        assertTrue(filec.exists());
    }

    @Test
    void binaryDeserializedDocsAreIdenticalToOriginalVersionTest() throws IOException {
        //uri2 stores a binary doc
        Document doc2Original = docStore.get(uri2);

        docStore.setMaxDocumentBytes(1);
        assertTrue(file2.exists());
        assertEquals(doc2Original, docStore.get(uri2));
        assertTrue(file2.exists());
    }

    @Test
    void deletedDocsMayNotBeRehydratedByCallingMethodsOnThem() throws IOException {
        docStore.delete(uri1);
        assertNull(docStore.get(uri1));
        assertThrows(IllegalArgumentException.class, ()-> docStore.setMetadata(uri1, "A key", "a Value"), "Should throws Exception if no doc stored at that URI, since doc was deleted");
        assertThrows(IllegalArgumentException.class, ()-> docStore.getMetadata(uri1, "A key"), "Should throws Exception if no doc stored at that URI, since doc was deleted");
    }


    @Test
    void onlyRehydrateDocsThatMeetALLSearchConditions() throws IOException{
        put5MoreDocs();
        Document one = docStore.get(uria);
        Document two = docStore.get(urib);
        Document three = docStore.get(uric);
        Document four = docStore.get(urid);
        Document five = docStore.get(urie);

        docStore.setMetadata(uria, "abkey1", "abvalue1");
        docStore.setMetadata(urib, "abkey1", "abvalue1");


        docStore.setMetadata(uria, "allkey", "allvalue");
        docStore.setMetadata(urib, "allkey", "allvalue");
        docStore.setMetadata(uric, "allkey", "allvalue");
        docStore.setMetadata(urid, "allkey", "allvalue");
        docStore.setMetadata(urie, "allkey", "allvalue");


        Map<String, String> abMap = new HashMap<>();
        abMap.put("abkey1", "abvalue1");

        Map<String, String> abcdeMap = new HashMap<>();
        abcdeMap.put("allkey", "allvalue");

        assertNotNull(docStore.get(uri1));
        assertNotNull(docStore.get(uri2));

        docStore.setMaxDocumentCount(2); //Kick everything out to disk except for uri1 and uri2
        assertTrue(filea.exists());
        assertTrue(fileb.exists());
        assertTrue(filec.exists());
        assertTrue(filed.exists());
        assertTrue(filee.exists());

        //Some docs match the MD map, but not the prefix
        assertEquals(Set.of(one, two), new HashSet<>(docStore.searchByPrefixAndMetadata("onetw", abcdeMap)));
        assertTrue(filec.exists());
        assertTrue(filed.exists());
        assertTrue(filee.exists());

        assertFalse(filea.exists());
        assertFalse(fileb.exists());

        //Some docs match the keyword but not the md map
        assertEquals(Set.of(one, two), new HashSet<>(docStore.searchByPrefixAndMetadata("i", abMap)));
        assertTrue(filec.exists());
        assertTrue(filed.exists());
        assertTrue(filee.exists());

        assertFalse(filea.exists());
        assertFalse(fileb.exists());

        //Some docs match the MD map, but not the keyword
        assertEquals(Set.of(one, two), new HashSet<>(docStore.searchByKeywordAndMetadata("onetwo", abcdeMap)));
        assertTrue(filec.exists());
        assertTrue(filed.exists());
        assertTrue(filee.exists());

        assertFalse(filea.exists());
        assertFalse(fileb.exists());

        //Some docs match the keyword but not the md map
        assertEquals(Set.of(one, two), new HashSet<>(docStore.searchByKeywordAndMetadata("is", abMap)));
        assertTrue(filec.exists());
        assertTrue(filed.exists());
        assertTrue(filee.exists());

        assertFalse(filea.exists());
        assertFalse(fileb.exists());
    }

    @Test
    void customBaseDirTest() throws IOException {
        docStoreCustomBaseDir.put(longtxt1, uria, DocumentFormat.TXT);
        docStoreCustomBaseDir.put(longtxt2, urib, DocumentFormat.TXT);
        docStoreCustomBaseDir.put(longtxt3, uric, DocumentFormat.TXT);
        docStoreCustomBaseDir.put(longtxt4, urid, DocumentFormat.TXT);
        docStoreCustomBaseDir.put(longtxt5, urie, DocumentFormat.TXT);

        docStoreCustomBaseDir.setMaxDocumentCount(2);
        assertTrue(customFilea.exists());
        assertTrue(customFileb.exists());
        assertTrue(customFilec.exists());
        assertFalse(customFiled.exists());
        assertFalse(customFilee.exists());

        docStoreCustomBaseDir.getMetadata(uria, "k");
        docStoreCustomBaseDir.get(uric);
        assertTrue(customFiled.exists());
        assertTrue(customFilee.exists());
        assertFalse(customFilea.exists());
        assertFalse(customFilec.exists());
    }

}