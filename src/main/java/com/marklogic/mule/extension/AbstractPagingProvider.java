package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.impl.GenericDocumentImpl;
import com.marklogic.client.io.JacksonParserHandle;
import com.marklogic.client.query.QueryDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Defines the common logic for reading and exporting documents. Subclasses provide the read/export-specific logic,
 * which boils down to what is returned for each document.
 */
abstract class AbstractPagingProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int totalDocumentsDelivered = 0;
    private Integer currentPage = -1;
    private Long serverTimestamp = null;

    private final int pageLength;

    protected final QueryParameters queryParameters;

    protected AbstractPagingProvider(QueryParameters params) {
        this.pageLength = params.pageLength != null ? params.pageLength : 100;
        this.queryParameters = params;
    }

    protected final void handlePage(DatabaseClient databaseClient, Consumer<DocumentRecord> documentHandler) {
        currentPage++;

        GenericDocumentManager documentManager = databaseClient.newDocumentManager();
        if (Utilities.hasText(queryParameters.categories)) {
            documentManager.setMetadataCategories(queryParameters.buildMetadataCategories());
        }

        final QueryDefinition queryDefinition = queryParameters.makeQueryDefinition(databaseClient);

        initializeServerTimestampIfNecessary(documentManager, queryDefinition);

        int startingPosition = (currentPage * pageLength) + 1;
        documentManager.setPageLength(pageLength);
        DocumentPage documentPage = serverTimestamp != null ?
            ((GenericDocumentImpl) documentManager).search(queryDefinition, startingPosition, serverTimestamp) :
            documentManager.search(queryDefinition, startingPosition);

        while (documentPage.hasNext()) {
            DocumentRecord document = documentPage.next();
            if (queryParameters.maxResults != null) {
                if (totalDocumentsDelivered < queryParameters.maxResults) {
                    documentHandler.accept(document);
                    totalDocumentsDelivered++;
                } else {
                    break;
                }
            } else {
                documentHandler.accept(document);
                totalDocumentsDelivered++;
            }
        }
    }

    private void initializeServerTimestampIfNecessary(GenericDocumentManager documentManager, QueryDefinition queryDefinition) {
        if (queryParameters.consistentSnapshot && serverTimestamp == null) {
            JacksonParserHandle searchHandle = new JacksonParserHandle();
            documentManager.setPageLength(1);
            documentManager.search(queryDefinition, 1, searchHandle);
            serverTimestamp = searchHandle.getServerTimestamp();
            logger.info("Using consistent snapshot with server timestamp: {}", serverTimestamp);
        }
    }
}
