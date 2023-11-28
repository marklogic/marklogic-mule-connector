package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;
import com.marklogic.mule.extension.api.QueryFormat;
import com.marklogic.mule.extension.api.QueryType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.param.display.Text;

import java.util.List;

/**
 * See https://docs.mulesoft.com/mule-sdk/latest/define-parameters#using-pojo-parameters for more information
 * on parameter groups.
 */
public class QueryParameters {

    @Parameter
    @DisplayName("Document URIs")
    @Optional
    @Summary("Specify one or more document URIs to read. If specified, all other parameters for querying will be ignored.")
    List<String> uris;

    @Parameter
    @DisplayName("Collections")
    @Optional
    @Example("myCollection1,myCollection2")
    String collections;

    @Parameter
    @DisplayName("Query")
    @Text
    @Optional
    @Example("searchTerm")
    String query;

    @Parameter
    @DisplayName("Query Type")
    @Optional
    QueryType queryType;

    @Parameter
    @DisplayName("Query Format")
    @Optional
    QueryFormat queryFormat;

    @Parameter
    @DisplayName("Metadata Category List")
    @Optional(defaultValue = "all")
    @Example("COLLECTIONS;PERMISSIONS")
    String categories;

    @Parameter
    @DisplayName("Max Results")
    @Optional
    @Example("10")
    Integer maxResults;

    @Parameter
    @DisplayName("Page Length")
    @Optional
    @Example("10")
    Integer pageLength;

    @Parameter
    @DisplayName("Search Options")
    @Optional
    @Example("appSearchOptions")
    String searchOptions;

    @Parameter
    @DisplayName("Directory")
    @Optional
    @Example("/customerData")
    String directory;

    @Parameter
    @DisplayName("Transform")
    @Optional
    String transform;

    @Parameter
    @DisplayName("Transform Parameters")
    @Optional
    String transformParameters;

    @Parameter
    @DisplayName("Transform Parameters Delimiter")
    @Optional(defaultValue = ",")
    String transformParametersDelimiter;

    @Parameter
    @DisplayName("Consistent Snapshot")
    @Optional(defaultValue = "True")
    boolean consistentSnapshot;

    QueryDefinition makeQueryDefinition(DatabaseClient databaseClient) {
        final QueryDefinition queryDefinition = uris != null && !uris.isEmpty() ?
            databaseClient.newQueryManager().newStructuredQueryBuilder().document(uris.toArray(new String[]{})) :
            buildQueryDefinitionFromParams(databaseClient, query, queryType, queryFormat);

        if (Utilities.hasText(collections)) {
            queryDefinition.setCollections(collections.split(","));
        }
        if (Utilities.hasText(searchOptions)) {
            queryDefinition.setOptionsName(searchOptions);
        }
        if (Utilities.hasText(directory)) {
            queryDefinition.setDirectory(directory);
        }
        if (Utilities.hasText(transform)) {
            queryDefinition.setResponseTransform(Utilities.makeServerTransform(transform, transformParameters, transformParametersDelimiter));
        }
        return queryDefinition;
    }

    DocumentManager.Metadata[] buildMetadataCategories() {
        String[] categoriesArray = categories.split(",");
        DocumentManager.Metadata[] transformedCategories = new DocumentManager.Metadata[categoriesArray.length];
        int index = 0;
        for (String category : categoriesArray) {
            transformedCategories[index++] = DocumentManager.Metadata.valueOf(category.toUpperCase());
        }
        return transformedCategories;
    }

    private QueryDefinition buildQueryDefinitionFromParams(
        DatabaseClient databaseClient,
        String query,
        QueryType queryType,
        QueryFormat queryFormat
    ) {
        if (query != null) {
            if (queryType != null) {
                if ((queryType != QueryType.STRING_QUERY) && (queryFormat == null)) {
                    throw new IllegalArgumentException("A Query Format must be specified when using a Structured, Serialized, or Combined Query");
                }
                QueryManager queryManager = databaseClient.newQueryManager();
                switch (queryType) {
                    case STRUCTURED_QUERY:
                        return queryManager.newRawStructuredQueryDefinition(
                            new StringHandle(query).withFormat(Format.valueOf(queryFormat.toString()))
                        );
                    case SERIALIZED_CTS_QUERY:
                        return queryManager.newRawCtsQueryDefinitionAs(Format.valueOf(queryFormat.toString()), query);
                    case COMBINED_QUERY:
                        return queryManager.newRawCombinedQueryDefinition(
                            new StringHandle(query).withFormat(Format.valueOf(queryFormat.toString()))
                        );
                    case STRING_QUERY:
                    default:
                        return queryManager.newStringDefinition().withCriteria(query);
                }
            } else {
                return databaseClient.newQueryManager().newStringDefinition().withCriteria(query);
            }
        } else {
            return databaseClient.newQueryManager().newStringDefinition();
        }
    }
}
