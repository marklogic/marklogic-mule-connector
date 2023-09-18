package org.mule.extension.internal;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.TextDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.StringQueryDefinition;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;

import java.util.ArrayList;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class BasicOperations {

    /**
     * Example of a simple operation that writes documents to MarkLogic Documents database.
     */
    @MediaType(value = ANY, strict = false)
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
    public String readSingleDoc(@Connection DatabaseClient databaseClient, String uri) {
        return databaseClient
            .newTextDocumentManager()
            .readAs(uri, String.class);
    }

    /**
     * Example of a simple operation that searches document(s) from a directory in MarkLogic database and returns the content(s).
     */
    @MediaType(value = ANY, strict = false)
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
