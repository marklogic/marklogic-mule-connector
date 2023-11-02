package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.*;
import com.marklogic.client.io.*;
import com.marklogic.client.io.marker.JSONReadHandle;
import com.marklogic.client.io.marker.JSONWriteHandle;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.StringQueryDefinition;

import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.util.*;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

/**
 * This class is a container for operations, every public method in this class
 * will be taken as an extension operation.
 */
public class BasicOperations {

    @MediaType(value = ANY, strict = false)
    @DisplayName("Read document")
    public Result<InputStream, DocumentAttributes> readDocument(
        @Connection DatabaseClient databaseClient,
        @DisplayName("Document URI") @Example("/data/customer.json") String uri,
        @DisplayName("Metadata Category List") @Optional(defaultValue = "ALL") @Example("COLLECTIONS,PERMISSIONS") String categories
    ) {
        DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
        GenericDocumentManager documentManager = databaseClient.newDocumentManager();

        if ((categories != null) && (!categories.isEmpty())) {
            documentManager.setMetadataCategories(buildMetadataCategories(categories));
        }

        InputStreamHandle handle = documentManager.read(uri, metadataHandle, new InputStreamHandle());
        return Result.<InputStream, DocumentAttributes>builder()
            .output(handle.get())
            .attributes(new DocumentAttributes(uri, metadataHandle))
            .mediaType(makeMediaType(handle.getFormat()))
            .attributesMediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA)
            .build();
    }

    /**
     * Likely a temporary method for writing a single document.
     *
     * @param databaseClient
     * @param myContent
     * @param uri
     */
    public void writeDocument(
        @Connection DatabaseClient databaseClient,
        @Content InputStream myContent,
        String uri) {
        databaseClient
            .newDocumentManager()
            .write(uri,
                new DocumentMetadataHandle().withPermission("rest-reader",
                    DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE),
                new InputStreamHandle(myContent));
    }

    /**
     * Search for documents, returning the documents but not yet any metadata for them.
     * Will eventually support many parameters here for searching.
     */
    @MediaType(value = ANY, strict = false)
    @DisplayName("Search Documents")
    public List<Result<InputStream, DocumentAttributes>> searchDocuments(
        @Connection DatabaseClient databaseClient,
        @DisplayName("Collection") @Optional(defaultValue = "") @Example("myCollection") String collection,
        @DisplayName("Query") @Text @Optional @Example("searchTerm") String query,
        @DisplayName("Query Type") @Optional QueryType queryType,
        @DisplayName("Query Format") @Optional QueryFormat queryFormat,
        @DisplayName("Metadata Category List") @Optional(defaultValue = "all") @Example("COLLECTIONS,PERMISSIONS") String categories
    ) {
        DocumentManager<JSONReadHandle, JSONWriteHandle> documentManager = databaseClient.newJSONDocumentManager();
        if ((categories != null) && !categories.isEmpty()) {
            documentManager.setMetadataCategories(buildMetadataCategories(categories));
        }

        QueryDefinition structuredQueryDefinition = ReadUtil.buildQueryDefinitionFromParams(databaseClient, query, queryType, queryFormat);
        if (!collection.isEmpty()) {
            structuredQueryDefinition.setCollections(collection);
        }

        DocumentPage page = documentManager.search(structuredQueryDefinition, 1);
        List<Result<InputStream, DocumentAttributes>> results = new ArrayList<>();
        while (page.hasNext()) {
            InputStreamHandle handle = new InputStreamHandle();
            DocumentRecord documentRecord = page.next();
            InputStream content = documentRecord.getContent(handle).get();
            DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
            documentRecord.getMetadata(metadataHandle);
            Result<InputStream, DocumentAttributes> resultDoc = Result.<InputStream, DocumentAttributes>builder()
                .output(content)
                .attributes(new DocumentAttributes(documentRecord.getUri(), metadataHandle))
                .mediaType(makeMediaType(handle.getFormat()))
                .attributesMediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA)
                .build();
            results.add(resultDoc);
        }
        return results;
    }

    /**
     * Write a batch of documents.
     * <p>
     * TODO This will eventually have parameters for controlling how each document
     * is written.
     *
     * @param databaseClient
     * @param myContents
     */
    public void writeBatch(
        @Connection DatabaseClient databaseClient,
        @Content InputStream[] myContents) {
        System.out.println("CONTENT COUNT: " + myContents.length);
        DocumentManager mgr = databaseClient.newDocumentManager();
        DocumentWriteSet writeSet = mgr.newWriteSet();
        DocumentMetadataHandle metadata = new DocumentMetadataHandle()
            .withPermission("rest-reader", DocumentMetadataHandle.Capability.READ,
                DocumentMetadataHandle.Capability.UPDATE)
            .withCollections("batch-output");
        for (InputStream content : myContents) {
            String uri = "/batch-output/" + UUID.randomUUID().toString() + ".json";
            writeSet.add(uri, metadata, new InputStreamHandle(content));
        }
        mgr.write(writeSet);
    }

    private org.mule.runtime.api.metadata.MediaType makeMediaType(Format format) {
        if (Format.JSON.equals(format)) {
            return org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;
        } else if (Format.XML.equals(format)) {
            return org.mule.runtime.api.metadata.MediaType.APPLICATION_XML;
        } else if (Format.TEXT.equals(format)) {
            return org.mule.runtime.api.metadata.MediaType.TEXT;
        }
        return org.mule.runtime.api.metadata.MediaType.BINARY;
    }

    /**
     * Example of a simple operation that writes documents to MarkLogic Documents
     * database.
     */
    @MediaType(value = ANY, strict = false)
    @Deprecated
    public void writeDocs(@Connection DatabaseClient databaseClient) {
        TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
        DocumentWriteSet batch = textDocumentManager.newWriteSet();

        batch.add("doc1.txt", new StringHandle(
            "Document - 1").withFormat(Format.TEXT));
        batch.add("doc2.txt", new StringHandle(
            "Document - 2").withFormat(Format.TEXT));
        textDocumentManager.write(batch);
    }

    /**
     * Example of a simple operation that writes a single text document to MarkLogic
     * database.
     */
    @MediaType(value = ANY, strict = false)
    @Deprecated
    public void writeSingledoc(@Connection DatabaseClient databaseClient, String content, String uri) {
        databaseClient
            .newTextDocumentManager()
            .write(uri,
                new DocumentMetadataHandle().withPermission("rest-reader",
                    DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE),
                new StringHandle(content));
    }

    /**
     * Example of a simple operation that writes text documents to MarkLogic
     * database when given content.
     */
    @MediaType(value = ANY, strict = false)
    @Deprecated
    public void writeDocuments(@Connection DatabaseClient databaseClient, String[] contents, String uriPrefix) {
        TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
        DocumentWriteSet batch = textDocumentManager.newWriteSet();
        for (int i = 0; i < contents.length; i++) {
            batch.add(uriPrefix + Math.random() + "_i_value_is_" + i + ".txt",
                new StringHandle(contents[i]).withFormat(Format.TEXT));
        }
        textDocumentManager.write(batch);
    }

    /**
     * Example of a simple operation that reads documents from MarkLogic Documents
     * database.
     */
    @MediaType(value = ANY, strict = false)
    @Deprecated
    public String readDocs(@Connection DatabaseClient databaseClient) {
        TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
        StringBuffer str = new StringBuffer();
        for (DocumentRecord record : textDocumentManager.read("doc1.txt", "doc2.txt")) {
            String content = record.getContentAs(String.class);
            str.append(content);
            str.append("\n");
        }
        return str.toString();
    }

    /**
     * Example of a simple operation that reads a single document from MarkLogic
     * database.
     */
    @MediaType(value = ANY, strict = false)
    @Deprecated
    public String readSingleDoc(@Connection DatabaseClient databaseClient, String uri) {
        return databaseClient
            .newTextDocumentManager()
            .readAs(uri, String.class);
    }

    /**
     * Example of a simple operation that searches document(s) from a directory in
     * MarkLogic database and returns the content(s).
     */
    @MediaType(value = ANY, strict = false)
    @Deprecated
    public String[] searchDocs(@Connection DatabaseClient databaseClient, String directory, int pageLength) {
        TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
        textDocumentManager.setPageLength(pageLength);
        ArrayList<String> arrayList = new ArrayList<>();
        StringQueryDefinition query = databaseClient.newQueryManager().newStringDefinition();
        query.setDirectory(directory);
        try (DocumentPage page = textDocumentManager.search(query, 1)) {
            while (page.hasNext()) {
                arrayList.add(page.next().getContent(new StringHandle()).toString());
            }
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

    private DocumentManager.Metadata[] buildMetadataCategories(String categories) {
        String[] categoriesArray = categories.split(",");
        DocumentManager.Metadata[] transformedCategories = new DocumentManager.Metadata[categoriesArray.length];
        int index = 0;
        for (String category : categoriesArray) {
            transformedCategories[index++] = DocumentManager.Metadata.valueOf(category.toUpperCase());
        }
        return transformedCategories;
    }
}
