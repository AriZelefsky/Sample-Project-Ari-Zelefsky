package edu.yu.cs.com1320.project.stage6.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.nanoTime;

public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    File baseDir; //Leave as FILE not STRING bc specs say that docimpl will accept FILE BASEDIR as arg

    /// As per Piazza 241, must have 1 constructor
    public DocumentPersistenceManager(File baseDir){
        if(baseDir == null) this.baseDir = new File(System.getProperty("user.dir"));
        else this.baseDir = baseDir;
    }

    /**
     * @param uri
     * @param val
     * @throws IOException
     */
    @Override
    public void serialize(URI uri, Document val) throws IOException {
        DocumentImpl docAsDocImpl = (DocumentImpl) val;

        //Create Java File object for the json file and for its parent path
        File jsonFile = new File(baseDir, uri.toString().substring(7) + ".json"); //Leaving %20 as is, and given that all URIs will be provided as http://...
        File parent = jsonFile.getParentFile(); //Will be the path represented by baseDir up until right before the json file

        // Actually create said path and then said file on disk
        if(parent != null) parent.mkdirs();
            //Create the actual file on disk, as well as the ability to write to it
        try (FileWriter jsonWriter = new FileWriter(jsonFile)) { //try-with-resources to automatically close everything at end
            //Instantiate a Gson to serialize the Document
            Gson gson = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, new DocumentImplSerializer()).create();
            String jsonString = gson.toJson(docAsDocImpl);
            jsonWriter.write(jsonString);
        }
    }

    /**
     * Specs: You must serialize/deserialize:
     *  1. the contents of the document (String or binary)
     *  2. any metadata key-value pairs
     *  3. the URI
     *  4. the wordcount map.
     */
    private class DocumentImplSerializer implements JsonSerializer<DocumentImpl> {
        @Override
        public JsonElement serialize(DocumentImpl src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("metadataPairsMap", context.serialize(src.getMetadata()));
            jsonObject.addProperty("uriString", src.getKey().toString()); //Deserialization will have to make a new URI.create("uriStringRep)

            //For Binary docs ONLY. (for text docs, the property will be null, not jsonNUll (jsonNull is when the proerpty is set, but set to null)
            if(src.getDocumentBinaryData() != null){
                String stringEncodingOfBytes = DatatypeConverter.printBase64Binary(src.getDocumentBinaryData());
                jsonObject.addProperty("stringRepOfBytes", stringEncodingOfBytes);
            }
            // For Text docs Only:
            else{
                jsonObject.addProperty("stringText",  src.getDocumentTxt());
                jsonObject.add("wordCountMap", context.serialize(src.getWordMap()));
            }
            return jsonObject;
        }
    }

    private class DocumentImplDeserializer implements JsonDeserializer<DocumentImpl> {
        public DocumentImpl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            String uriInString = jsonObject.get("uriString").getAsString();
            URI docUri = URI.create(uriInString);

            TypeToken<Map<String, Integer>> wordMapType = new TypeToken<Map<String, Integer>>(){};
            Map<String, Integer> docWordCountMap = context.deserialize(jsonObject.get("wordCountMap"), wordMapType.getType());

            /// For a text document
            if(jsonObject.get("stringText") != null && !jsonObject.get("stringText").isJsonNull()){
                String docText = jsonObject.get("stringText").getAsString(); //(Without getAsString it would return a json element

                TypeToken<HashMap<String, String>> metaDataMapType = new TypeToken<HashMap<String, String>>(){};
                HashMap<String, String> docMetadataMap = context.deserialize(jsonObject.get("metadataPairsMap"), metaDataMapType.getType());

                DocumentImpl doc =  new DocumentImpl(docUri, docText, docWordCountMap);
                doc.setMetadata(docMetadataMap);
                return doc;
            }

            /// For a binary document
            byte[] docBinaryData = DatatypeConverter.parseBase64Binary(jsonObject.get("stringRepOfBytes").getAsString());
            return new DocumentImpl(docUri, docBinaryData);
        }
    }

    /**
     * @param uri
     * @return
     * @throws IOException
     */
    @Override
    public Document deserialize(URI uri) throws IOException {
        File jsonFile = new File(baseDir, uri.toString().substring(7) + ".json");
        //assert jsonFile.exists() : "File does not exist, but it SHOULD considering that deserialize is only called after an earlier serialization: " + jsonFile.getAbsolutePath();

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(jsonFile))){
            StringBuilder jsonStringBuilder = new StringBuilder();

            while(true){
                char[] chunk = new char[8192];
                // Line below copies up to 8192 characters of the file into the char[],
                // returns len: the amount (8192, or less if end of file was reached in middle,
                //      or -1 if up to end of file before reading any chars)
                int len = bufferedReader.read(chunk);
                if(len == -1) break;
                jsonStringBuilder.append(chunk,0, len);
            }

            Gson gson = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, new DocumentImplDeserializer()).create();
            DocumentImpl recoveredDoc = gson.fromJson(jsonStringBuilder.toString(), DocumentImpl.class);
            this.delete(uri); //Added as per Piazza 312
            return recoveredDoc;
            //ensure that method in docstore that brings back from disk sets last use time to now
        }
    }

    /**
     * delete the file stored on disk that corresponds to the given key
     *
     * @param uri
     * @return true or false to indicate if deletion occured or not
     * @throws IOException
     */
    @Override
    public boolean delete(URI uri) throws IOException {
        File jsonFile = new File(baseDir, uri.toString().substring(7) + ".json"); //Alternatively use uri.getHost() and uri.getRawPath(), if piazza would not have told us all URIs will start with http://
        return jsonFile.delete();
    }
}
