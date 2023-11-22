package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.ServerTransform;
import com.marklogic.client.impl.JSONDocumentImpl;
import com.marklogic.client.io.JacksonParserHandle;
import com.marklogic.client.io.marker.JSONReadHandle;
import com.marklogic.client.io.marker.JSONWriteHandle;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.mule.extension.api.QueryFormat;
import com.marklogic.mule.extension.api.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Defines the common logic for reading and exporting documents. Subclasses provide the read/export-specific logic,
 * which boils down to what is returned for each document.
 */
abstract class AbstractPagingProvider {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int totalDocumentsDelivered = 0;
    private Integer currentPage = -1;
    private Long serverTimestamp = -1L;

    private final int pageLength;
    private final List<String> uris;
    private final String collection;
    private final String query;
    private final QueryType queryType;
    private final QueryFormat queryFormat;
    private final String categories;
    private final Integer maxResults;
    private final String searchOptions;
    private final String directory;
    private final String restTransform;
    private final String restTransformParameters;
    private final String restTransformParametersDelimiter;
    private final boolean consistentSnapshot;

    protected AbstractPagingProvider(List<String> uris, String collection, String query, QueryType queryType, QueryFormat queryFormat,
                                     String categories, Integer maxResults, Integer pageLength, String searchOptions,
                                     String directory, String restTransform, String restTransformParameters,
                                     String restTransformParametersDelimiter, boolean consistentSnapshot) {

        this.pageLength = pageLength != null ? pageLength : 100;
        this.uris = uris;
        this.collection = collection;
        this.query = query;
        this.queryType = queryType;
        this.queryFormat = queryFormat;
        this.categories = categories;
        this.maxResults = maxResults;
        this.searchOptions = searchOptions;
        this.directory = directory;
        this.restTransform = restTransform;
        this.restTransformParameters = restTransformParameters;
        this.restTransformParametersDelimiter = restTransformParametersDelimiter;
        this.consistentSnapshot = consistentSnapshot;
    }

    protected final void handlePage(DatabaseClient databaseClient, Consumer<DocumentRecord> documentHandler) {
        currentPage++;

        DocumentManager<JSONReadHandle, JSONWriteHandle> documentManager = databaseClient.newJSONDocumentManager();
        if (Utilities.hasText(categories)) {
            documentManager.setMetadataCategories(buildMetadataCategories(categories));
        }

        final QueryDefinition queryDefinition = uris != null && !uris.isEmpty() ?
            databaseClient.newQueryManager().newStructuredQueryBuilder().document(uris.toArray(new String[]{})) :
            ReadUtil.buildQueryDefinitionFromParams(databaseClient, query, queryType, queryFormat);

        if (Utilities.hasText(collection)) {
            queryDefinition.setCollections(collection);
        }
        if (Utilities.hasText(searchOptions)) {
            queryDefinition.setOptionsName(searchOptions);
        }
        if (Utilities.hasText(directory)) {
            queryDefinition.setDirectory(directory);
        }
        if (Utilities.hasText(restTransform)) {
            ServerTransform serverTransform = new ServerTransform(restTransform);
            if (Utilities.hasText(restTransformParameters)) {
                String[] parametersArray = restTransformParameters.split(restTransformParametersDelimiter);
                for (int i = 0; i < parametersArray.length; i = i + 2) {
                    serverTransform.addParameter(parametersArray[i], parametersArray[i + 1]);
                }
            }
            queryDefinition.setResponseTransform(serverTransform);
        }

        if ((consistentSnapshot) && (serverTimestamp == -1)) {
            JacksonParserHandle searchHandle = new JacksonParserHandle();
            documentManager.setPageLength(1);
            documentManager.search(queryDefinition, 1, searchHandle);
            serverTimestamp = searchHandle.getServerTimestamp();
            logger.debug("Server timestamp for PagingProvider search: {}", serverTimestamp);
        }

        int startingDocument = (currentPage * pageLength) + 1;
        documentManager.setPageLength(pageLength);
        logger.debug("Server timestamp for current Page: {}", serverTimestamp);
        DocumentPage documentPage = ((JSONDocumentImpl) documentManager).search(queryDefinition, startingDocument, serverTimestamp);
        while (documentPage.hasNext()) {
            DocumentRecord document = documentPage.next();
            if (maxResults != null) {
                if (totalDocumentsDelivered < maxResults) {
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
