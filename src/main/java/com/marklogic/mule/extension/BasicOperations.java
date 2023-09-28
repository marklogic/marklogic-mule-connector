package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.TextDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.StringQueryDefinition;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class BasicOperations {

    /**
     * Read a single document as a Result object.
     *
     * @param databaseClient
     * @param uri
     * @return
     */
    @MediaType(value = ANY, strict = false)
    public Result<InputStream, DocumentAttributes> readDocument(@Connection DatabaseClient databaseClient, String uri) {
        InputStreamHandle handle = new InputStreamHandle();
        DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
        InputStream content = databaseClient.newJSONDocumentManager().read(uri, metadataHandle, handle).get();
        return Result.<InputStream, DocumentAttributes>builder()
            .output(content)
            .attributes(new DocumentAttributes(uri, metadataHandle))
            .mediaType(makeMediaType(handle.getMimetype()))
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
        String uri
    ) {
        databaseClient
            .newDocumentManager()
            .write(uri,
                new DocumentMetadataHandle().withPermission("rest-reader", DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE),
                new InputStreamHandle(myContent)
            );
    }

    /**
     * Search for documents, returning the documents but not yet any metadata for them.
     * <p>
     * Will eventually support many parameters here for searching.
     *
     * @param databaseClient
     * @param collection
     * @return
     */
    @MediaType(value = ANY, strict = false)
    public List<Result<InputStream, Void>> searchDocuments(@Connection DatabaseClient databaseClient, String collection) {
        DocumentPage page = databaseClient.newDocumentManager().search(
            databaseClient.newQueryManager().newStructuredQueryBuilder().collection(collection), 1
        );
        List<Result<InputStream, Void>> results = new ArrayList<>();
        while (page.hasNext()) {
            InputStreamHandle handle = new InputStreamHandle();
            InputStream content = page.nextContent(handle).get();
            results.add(Result.<InputStream, Void>builder()
                .output(content)
                .mediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON)
                .build());
        }
        return results;
    }

    /**
     * Write a batch of documents.
     * <p>
     * TODO This will eventually have parameters for controlling how each document is written.
     *
     * @param databaseClient
     * @param myContents
     */
    public void writeBatch(
        @Connection DatabaseClient databaseClient,
        @Content InputStream[] myContents
    ) {
        System.out.println("CONTENT COUNT: " + myContents.length);
        DocumentManager mgr = databaseClient.newDocumentManager();
        DocumentWriteSet writeSet = mgr.newWriteSet();
        DocumentMetadataHandle metadata = new DocumentMetadataHandle()
            .withPermission("rest-reader", DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE)
            .withCollections("batch-output");
        for (InputStream content : myContents) {
            String uri = "/batch-output/" + UUID.randomUUID().toString() + ".json";
            writeSet.add(uri, metadata, new InputStreamHandle(content));
        }
        mgr.write(writeSet);
    }

    private org.mule.runtime.api.metadata.MediaType makeMediaType(String mimetype) {
        // This is likely not comprehensive and may need to handle more scenarios.
        if (mimetype.contains("json")) {
            return org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;
        } else if (mimetype.contains("xml")) {
            return org.mule.runtime.api.metadata.MediaType.APPLICATION_XML;
        } else if (mimetype.contains("text")) {
            return org.mule.runtime.api.metadata.MediaType.TEXT;
        }
        return org.mule.runtime.api.metadata.MediaType.BINARY;
    }

    /**
     * Example of a simple operation that writes documents to MarkLogic Documents database.
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
     * Example of a simple operation that writes a single text document to MarkLogic database.
     */
    @MediaType(value = ANY, strict = false)
    @Deprecated
    public void writeSingledoc(@Connection DatabaseClient databaseClient, String content, String uri) {
        databaseClient
            .newTextDocumentManager()
            .write(uri,
                new DocumentMetadataHandle().withPermission("rest-reader", DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE),
                new StringHandle(content)
            );
    }

    /**
     * Example of a simple operation that writes text documents to MarkLogic database when given content.
     */
    @MediaType(value = ANY, strict = false)
    @Deprecated
    public void writeDocuments(@Connection DatabaseClient databaseClient, String[] contents, String uriPrefix) {
        TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
        DocumentWriteSet batch = textDocumentManager.newWriteSet();
        for (int i = 0; i < contents.length; i++) {
            batch.add(uriPrefix + Math.random() + "_i_value_is_" + i + ".txt", new StringHandle(contents[i]).withFormat(Format.TEXT));
        }
        textDocumentManager.write(batch);
    }

    /**
     * Example of a simple operation that reads documents from MarkLogic Documents database.
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
     * Example of a simple operation that reads a single document from MarkLogic database.
     */
    @MediaType(value = ANY, strict = false)
    @Deprecated
    public String readSingleDoc(@Connection DatabaseClient databaseClient, String uri) {
        return databaseClient
            .newTextDocumentManager()
            .readAs(uri, String.class);
    }

    /**
     * Example of a simple operation that searches document(s) from a directory in MarkLogic database and returns the content(s).
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
}
