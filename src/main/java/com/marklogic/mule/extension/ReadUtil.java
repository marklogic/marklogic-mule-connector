package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;

public abstract class ReadUtil {

    static protected QueryDefinition buildQueryDefinitionFromParams(
        DatabaseClient databaseClient,
        String query,
        QueryType queryType,
        QueryFormat queryFormat
    ) {
        if (query != null) {
            if (queryType != null) {
                if ((queryType != QueryType.StringQuery) && (queryFormat == null)) {
                    throw new RuntimeException("A Query Format must be specified when using a Structured, Serialized, or Combined Query");
                }
                QueryManager queryManager = databaseClient.newQueryManager();
                switch(queryType) {
                    case StructuredQuery:
                        return queryManager.newRawStructuredQueryDefinition(
                            new StringHandle(query).withFormat(Format.valueOf(queryFormat.toString()))
                        );
                    case SerializedCtsQuery:
                        return queryManager.newRawCtsQueryDefinitionAs(Format.valueOf(queryFormat.toString()), query);
                    case CombinedQuery:
                        return queryManager.newRawCombinedQueryDefinition(
                            new StringHandle(query).withFormat(Format.valueOf(queryFormat.toString()))
                        );
                    case StringQuery:
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
