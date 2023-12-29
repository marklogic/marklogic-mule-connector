package com.marklogic.mule.internal;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;
import com.marklogic.mule.internal.api.QueryFormat;
import com.marklogic.mule.internal.api.QueryType;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
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
    @NullSafe
    @Summary("Specify one or more document URIs to read. If specified, all other parameters for querying will be ignored.")
    List<String> uris;

    @Parameter
    @DisplayName("Query")
    @Summary("A MarkLogic query that corresponds to the type defined by 'Query Type', which must be set if a query is provided here.")
    @Text
    @Optional
    String query;

    @Parameter
    @DisplayName("Query Type")
    @Summary("The type of MarkLogic query to execute. Please see the connector user guide for more information on each type of query.")
    @Optional
    QueryType queryType;

    @Parameter
    @DisplayName("Query Format")
    @Summary("Whether a query is represented as JSON or XML. Only applies when 'Query Type' is either a structured query, " +
        "a serialized CTS query, or a combined query.")
    @Optional(defaultValue = "JSON")
    QueryFormat queryFormat;

    @Parameter
    @DisplayName("Collections")
    @Optional
    @Summary("Comma-delimited collections to filter results.")
    @Example("myCollection1,myCollection2")
    String collections;

    @Parameter
    @DisplayName("Directory")
    @Summary("Filters results to only include documents in the specified database directory.")
    @Optional
    @Example("/customerData")
    String directory;

    @Parameter
    @DisplayName("Search Options")
    @Summary("The name of the REST search options to apply to the query defined in 'Query'. " +
        "Please see the connector user guide for more information.")
    @Optional
    String searchOptions;

    @Parameter
    @DisplayName("Document Metadata")
    @Summary("Comma-delimited list of the types of metadata to include for each matching document. Allowable values are: " +
        "all, collections, permissions, properties, quality, and metadatavalues.")
    @Optional
    @Example("all,collections,permissions,properties,quality,metadatavalues")
    String categories;

    @Parameter
    @DisplayName("Max Results")
    @Summary("Maximum number of documents to retrieve.")
    @Optional
    Integer maxResults;

    @Parameter
    @DisplayName("Page Length")
    @Summary("Number of documents to retrieve in each page, which corresponds to a call to MarkLogic.")
    @Optional
    @Example("100")
    Integer pageLength;

    @Parameter
    @DisplayName("Transform")
    @Summary("Name of a REST transform to apply to each matching document.")
    @Optional
    String transform;

    @Parameter
    @DisplayName("Transform Parameters")
    @Summary("Comma-delimited parameters to pass to the REST transform.")
    @Optional
    @Example("param1,value1,param2,value2")
    String transformParameters;

    @Parameter
    @DisplayName("Transform Parameters Delimiter")
    @Summary("Delimiter to use for defining 'Transform Parameters'.")
    @Optional(defaultValue = ",")
    String transformParametersDelimiter;

    @Parameter
    @DisplayName("Consistent Snapshot")
    @Summary("Whether to constrain each page of results to the same MarkLogic server timestamp.")
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
            try {
                transformedCategories[index++] = DocumentManager.Metadata.valueOf(category.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(String.format("Invalid document metadata category: %s", category));
            }
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
                    throw new IllegalArgumentException("'Query Format' must be specified when using a Structured, Serialized, or Combined Query.");
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
