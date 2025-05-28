package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage6.impl.DocumentPersistenceManager;
import edu.yu.cs.com1320.project.undo.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class BTreeImplTest {
    private DocumentImpl textDoc;
    private DocumentImpl binaryDoc;
    private DocumentImpl binaryDocCopy;
    private DocumentImpl differentBinaryDoc;
    private URI uri;
    @BeforeEach
    void beforeEach(){ //make an instances of documentImpl for text and binaryInfo docs
        uri = null;
        try {
            uri = new URI("https://www.ari.zelefsky/random/uri/?query=parameter#fragment");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        textDoc = new DocumentImpl(uri, "The text of the document \n \n the end.", null);


        byte b1 = 34;
        byte b2 = -44;
        byte b3 = 55;
        byte b4 = 94;
        byte b5 = 100;
        byte b6 = 7;
        byte[] byteArray = {b1,b2,b3,b4,b5,b6};
        byte[] differentByteArray = {b2, b1, b3,b4,b5,b6};
        binaryDoc = new DocumentImpl(uri, byteArray);
        binaryDocCopy = new DocumentImpl(uri, byteArray);
        differentBinaryDoc = new DocumentImpl(uri, differentByteArray);
    }




    @Test
    void putTest() {
        BTreeImpl<Integer, String> btree = new BTreeImpl<>();
        URI uria = URI.create("https://abc.com");

        btree.put(1, "one");
        btree.put(2, "two");
        btree.put(6, "six");
        btree.put(7, "seven");
        btree.put(8, "eight");
        btree.put(9, "nine");
        btree.put(3, "three");
        btree.put(4, "four");
        btree.put(5, "five");


        assertEquals("one", btree.get(1));
        assertEquals("two", btree.get(2));
        assertEquals("three", btree.get(3));
        assertEquals("four", btree.get(4));
        assertEquals("five", btree.get(5));
        assertEquals("six", btree.get(6));
        assertEquals("seven", btree.get(7));
        assertEquals("eight", btree.get(8));
        assertEquals("nine", btree.get(9));
    }

    @Test
    void putGetTestWithInts(){
        BTreeImpl<Integer, String> btree = new BTreeImpl<>();

        assertNull(btree.put(1, "one"));
        assertNull(btree.put(3, "three"));
        assertNull(btree.put(4, "four"));
        assertNull(btree.put(7, "seven"));
        assertNull(btree.put(8, "eight"));
        assertNull(btree.put(9, "nine"));
        assertNull(btree.put(10, "ten"));
        assertNull(btree.put(11, "eleven"));
        assertNull(btree.put(12, "twelve"));
        assertNull(btree.put(13, "thirteen"));
        assertNull(btree.put(5, "five"));
        assertNull(btree.put(6, "six"));
        assertNull(btree.put(2, "two"));


        assertEquals("one", btree.get(1));
        assertEquals("two", btree.get(2));
        assertEquals("three", btree.get(3));
        assertEquals("four", btree.get(4));
        assertEquals("five", btree.get(5));
        assertEquals("six", btree.get(6));
        assertEquals("seven", btree.get(7));
        assertEquals("eight", btree.get(8));
        assertEquals("nine", btree.get(9));
        assertEquals("ten", btree.get(10));
        assertEquals("eleven", btree.get(11));
        assertEquals("twelve", btree.get(12));
        assertEquals("thirteen", btree.get(13));

        assertEquals("six", btree.put(6, "six2"));
        assertEquals("three", btree.put(3, "three2"));
        assertEquals("seven", btree.put(7, "seven2"));
        assertEquals("ten", btree.put(10, "ten2"));
        assertEquals("one", btree.put(1, "one2"));

        //Deletion
        assertEquals("three2", btree.put(3, null));
        assertNull(btree.get(3));
        assertEquals("ten2", btree.put(10, null));
        assertNull(btree.get(3));
        assertEquals("two", btree.put(2, null));
        assertNull(btree.get(3));
        //Put back after Delete
        assertNull(btree.put(3, "three"));
        assertEquals("three", btree.get(3));


        assertEquals("one2", btree.get(1));
        assertEquals("four", btree.get(4));
        assertEquals("five", btree.get(5));
        assertEquals("six2", btree.get(6));
        assertEquals("seven2", btree.get(7));
        assertEquals("eight", btree.get(8));
        assertEquals("nine", btree.get(9));
        assertEquals("eleven", btree.get(11));
        assertEquals("twelve", btree.get(12));
        assertEquals("thirteen", btree.get(13));

        assertNull(btree.get(1000));
    }

    @Test
void putGetWithDocs(){
        BTreeImpl<URI, Document> btree = new BTreeImpl<>();

        Document[] docs = new Document[9];
        URI[] uris = new URI[9];

        // Create and insert 9 documents
        for (int i = 0; i < 9; i++) {
            uris[i] = URI.create("https://az/net.com/doc" + (i + 1));
            docs[i] = new DocumentImpl(uris[i], "text for document " + (i + 1), null);
            assertNull(btree.put(uris[i], docs[i]));
        }

        for (int i = 0; i < 9; i++) {
            assertEquals(docs[i], btree.get(uris[i]), "Failed at get for doc" + (i + 1));
        }

        /// Replace some docs
        for (int i = 0; i < 3; i++) {
            Document updated = new DocumentImpl(uris[i], "updated text for document " + (i + 1), null);
            assertEquals(docs[i], btree.put(uris[i], updated));
            assertEquals(updated, btree.get(uris[i]));
        }

        // Delete a few documents
        for (int i = 6; i < 9; i++) {
            assertEquals(docs[i], btree.put(uris[i], null));
            assertNull(btree.get(uris[i]));
        }
    }

    @Test
    void moveToDiskTest() throws IOException {
        BTreeImpl<URI, Document> btree = new BTreeImpl<>();

        Document[] docs = new Document[9];
        URI[] uris = new URI[9];

        //create and put 9 docs
        for (int i = 0; i < 9; i++) {
            uris[i] = URI.create("http://www.yu.edu/documents/doc" + (i + 1));
            docs[i] = new DocumentImpl(uris[i], "text for the document " + (i + 1), null);
            //assertNull(btree.put(uris[i], docs[i]));
        }
        Map<String, String> map = new HashMap<>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        docs[8].setMetadata((HashMap<String, String>) map);
        for (int i = 0; i < 9; i++) {
            assertNull(btree.put(uris[i], docs[i]));
        }

        btree.setPersistenceManager(new DocumentPersistenceManager(null));

        for (int i = 0; i < 9; i++) {
            btree.moveToDisk(uris[i]);
        }

        //Bring back into memory
        for (int i = 0; i < 9; i++) {
            assertEquals(docs[i], btree.get(uris[i]), "Failed at get for doc" + (i + 1));
        }

        for (int i = 0; i < 6; i++) {
            btree.pm.delete(uris[i]);
        }
    }



}
