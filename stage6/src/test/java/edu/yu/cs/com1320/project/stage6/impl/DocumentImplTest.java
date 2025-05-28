package edu.yu.cs.com1320.project.stage6.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentImplTest {
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
    void constructorsTest(){
        byte[] byteArray = {34, 66};
        byte[] emptyByteArray = {};
        URI blankUri = URI.create("");
        //First param null or blank with proper second param
        assertThrows(IllegalArgumentException.class, ()-> new DocumentImpl(null, "Hi", null));
        assertThrows(IllegalArgumentException.class, ()-> new DocumentImpl(null, byteArray));
        assertThrows(IllegalArgumentException.class, ()-> new DocumentImpl(blankUri, "Hi", null));
        assertThrows(IllegalArgumentException.class, ()-> new DocumentImpl(blankUri, byteArray));

        //Second para null or blank with proper first param
        assertThrows(IllegalArgumentException.class, ()-> new DocumentImpl(uri, (String) null, null)); //Specifically calling the txt constructor with null text
        assertThrows(IllegalArgumentException.class, ()-> new DocumentImpl(uri, (byte[]) null)); //Specifically calling the binary data document constructor with null binary array
        assertThrows(IllegalArgumentException.class, ()-> new DocumentImpl(uri, "", null));
        assertThrows(IllegalArgumentException.class, ()-> new DocumentImpl(uri, emptyByteArray));
    }

    @Test
    void setMetadataValueTest(){
        String A = new String("HI");
        String B = new String("HI"); // use of new keyword avoids string interning rendering A equivalent, even in memory location, to B
        //This test will only pass if string A, string B, and "HI" are treated the same.
        // Ensuring that .equals rather than == was used in HashTable.Put method
        assertNull(textDoc.getMetadataValue(A));
        assertNull(textDoc.setMetadataValue(B,"yoyoyo"), "Should Return null if there was no old value");
        assertEquals(textDoc.getMetadataValue(B), "yoyoyo");
        assertEquals("yoyoyo", textDoc.setMetadataValue(A,"valueTake2"), "Should return the old value");
        assertEquals("valueTake2", textDoc.setMetadataValue("HI","valueTake3"));

        assertNull(binaryDoc.setMetadataValue("HI","val1"));
        assertEquals("val1", binaryDoc.setMetadataValue("HI","valueTake2"));

        assertThrows(IllegalArgumentException.class, () -> textDoc.setMetadataValue("", "someValue"));
        assertThrows(IllegalArgumentException.class, () -> binaryDoc.setMetadataValue(null, "someValue"));
    }
    @Test
    void getMetadataValueTest(){
        assertNull(textDoc.getMetadataValue("hi"));
        assertNull(binaryDoc.getMetadataValue("datakey"));
        textDoc.setMetadataValue("datakey", "ProperValue");
        binaryDoc.setMetadataValue("aKey", "exampleValue");
        assertEquals("ProperValue", textDoc.getMetadataValue("datakey"));
        assertEquals("exampleValue", binaryDoc.getMetadataValue("aKey"));

        assertThrows(IllegalArgumentException.class, ()->textDoc.getMetadataValue(""));
        assertThrows(IllegalArgumentException.class, ()->textDoc.getMetadataValue(null));
        assertThrows(IllegalArgumentException.class, ()->binaryDoc.getMetadataValue(""));
        assertThrows(IllegalArgumentException.class, ()->binaryDoc.getMetadataValue(null));
    }
    @Test
    void getMetadataTest(){
        assertEquals(0, textDoc.getMetadata().size());
        assertEquals(0, binaryDoc.getMetadata().size());
        textDoc.setMetadataValue("datapointA", "valueA");
        textDoc.setMetadataValue("datapointB", "valueB");
        binaryDoc.setMetadataValue("datapointA", "valueA");
        binaryDoc.setMetadataValue("datapointB", "valueB");
        assertTrue(textDoc.getMetadata().size() == 2 && binaryDoc.getMetadata().size() == 2);
        assertEquals("valueB",textDoc.getMetadata().get("datapointB"));
        assertEquals("valueA",binaryDoc.getMetadata().get("datapointA"));
    }

    @Test
    void getDocumentTxtTest(){
        assertEquals("The text of the document \n \n the end.", textDoc.getDocumentTxt());
        assertNull(binaryDoc.getDocumentTxt(), "Binary documents should not have text");
    }

    @Test
    void getDocumentBinaryDataTest(){
        assertNull(textDoc.getDocumentBinaryData(), "Text docs should not have any binary data stored in the BinaryData field");
        byte b1 = 34;
        byte b2 = -44;
        byte b3 = 55;
        byte b4 = 94;
        byte b5 = 100;
        byte b6 = 7;
        byte[] byteArray = {b1,b2,b3,b4,b5,b6};
        assertArrayEquals(byteArray,binaryDoc.getDocumentBinaryData());
    }
    @Test
    void getKeyTest(){
        assertEquals(this.uri, textDoc.getKey());
        assertEquals(this.uri, binaryDoc.getKey());
    }

    @Test
    void hashCodeTest(){
        assertEquals(binaryDoc.hashCode(), binaryDocCopy.hashCode());
        assertNotEquals(binaryDoc.hashCode(), textDoc.hashCode());
        assertNotEquals(binaryDoc.hashCode(), differentBinaryDoc.hashCode());

        //testing of hashcode is  LIMITED considering that prof. provided the hashcode method
    }

    @Test
    void equalsTest(){
        assertEquals(binaryDoc, binaryDocCopy, "these documents have different memory addresses, but are otherwise identical");
        assertNotEquals(binaryDoc, textDoc, "these documents are totally different types");
        assertNotEquals(binaryDoc, differentBinaryDoc, "These documents contain different binary data");
    }

    @Test
    void getWordsTest(){
        //Check that only letters and digits count toward words
        DocumentImpl textDoc2 = new DocumentImpl(uri, ".i i i i i i i @#$i ()(i)(", null); //Should be considered a singular word
        //check that only SPACES seperate words, not tabs nor new lines
        DocumentImpl textDoc3 = new DocumentImpl(uri, " 1\ndoesnot\tcount\nbconly\nspaces\tnottabs\nornewlines \n \n 2 3 4 5 6 7 8 \n \n 9 ", null);
        DocumentImpl textDoc4 = new DocumentImpl(uri, "che*$ck1   m---y2 e.x.a.c.t3 !w@or#d$s4", null);
        DocumentImpl textDoc5 = new DocumentImpl(uri, "words4 exact3 my2 check1", null); //Regardless of order and nonletter/digit chars, should be same as above
        DocumentImpl textDoc6 = new DocumentImpl(uri, "hi theRe how Are you!!!", null);
        DocumentImpl textDoc7 = new DocumentImpl(uri, "hi there how are you!!!", null);
        DocumentImpl textDoc8 = new DocumentImpl(uri, "!@#$$%^^&&**()_+=-{][]:',./?>,.", null);

        assertEquals(9, textDoc3.getWords().size());
        assertEquals(1, textDoc2.getWords().size());
        assertEquals(textDoc4.getWords(), textDoc5.getWords());
        assertNotNull(binaryDoc.getWords());
        assertTrue(binaryDoc.getWords().isEmpty());

        //test exact contents:
        Set<String> words = Set.of("hi", "theRe", "how", "Are", "you");
        assertEquals(words, textDoc6.getWords());

        //Test Case Sensitivity
        assertNotEquals(textDoc6.getWords(), textDoc7.getWords());

        //Not a single Letter/Digit
        assertEquals(0, textDoc8.getWords().size());
    }

    @Test
    void wordCountTest(){
        DocumentImpl textDoc5 = new DocumentImpl(uri, "hi .hi h/i that is three his)", null);
        assertEquals(3, textDoc5.wordCount("hi"));
        assertEquals(1, textDoc5.wordCount("is"));
        assertEquals(0, textDoc5.wordCount(""));

        //BinaryDoc should have 0 no matter what
        assertEquals(0, binaryDoc.wordCount("is"));
        assertEquals(0, binaryDoc.wordCount(""));
    }

    /// TESTING OF GET AND SET LAST USE TIME IS DONE EXTENSIVELY IN DOCSTOREIMPLTEST class

}
