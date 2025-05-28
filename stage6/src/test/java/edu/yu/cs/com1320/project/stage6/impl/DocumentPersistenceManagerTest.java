package edu.yu.cs.com1320.project.stage6.impl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentPersistenceManagerTest {
    //This class is primarily tested via btree test code methods involving documents
    DocumentPersistenceManager pm = new DocumentPersistenceManager(new File("Custom/Example"));

    @Test
    void serializeAndDeserializeAndDeleteTest() throws IOException {
        URI urib1 = URI.create("http://www.yu.edu/documents/binarydoc1");
        URI urib2 = URI.create("http://www.yu.edu/documents/binarydoc2");
        URI uriT = URI.create("http://www.yu.edu/documents/textdoc1");
        ByteArrayInputStream longtxt1 = new ByteArrayInputStream("illness. onetwo. not sHoRt: 333112 333112 vHow are you doing today. oneonetwo oneonetwo tapping tapper tapped .kxl. kxlll kI am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. How are you doing today. I am doing well. This is great. Reading writing. Reading writing.".getBytes(StandardCharsets.UTF_8));

        byte[] arrayOfHowdy = {72, 111, 119, 100, 121};
        byte[] arrayOfRandomBinary = {(byte) 001101011,(byte) 00000000, (byte)11111111,(byte) 10101010};
        DocumentImpl textDoc1 = new DocumentImpl(uriT, longtxt1.toString(), null);
        DocumentImpl binaryDoc1 = new DocumentImpl(urib1, arrayOfRandomBinary);
        DocumentImpl binaryDoc2 = new DocumentImpl(urib1, arrayOfHowdy);

        pm.serialize(uriT, textDoc1);
        pm.serialize(urib1, binaryDoc1);
        pm.serialize(urib2, binaryDoc2);

        File fileT = new File(System.getProperty(System.getProperty("user.dir")),"Custom/Example/www.yu.edu/documents/textdoc1.json");
        File fileb1 = new File(System.getProperty("user.dir"),"Custom/Example/www.yu.edu/documents/binarydoc1.json");
        File fileb2 = new File(System.getProperty("user.dir"),"Custom/Example/www.yu.edu/documents/binarydoc2.json");

        assertTrue(fileT.exists());
        assertTrue(fileb1.exists());
        assertTrue(fileb2.exists());

        assertEquals(textDoc1, pm.deserialize(uriT));
        assertEquals(binaryDoc1, pm.deserialize(urib1));
        assertEquals(binaryDoc2, pm.deserialize(urib2));

        assertFalse(fileT.exists());
        assertFalse(fileb1.exists());
        assertFalse(fileb2.exists());

        assertThrows(IOException.class, () -> pm.deserialize(URI.create("urinotactuallyondisk")));
    }

}
