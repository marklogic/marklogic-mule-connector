/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2019 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.internal.operation;

import java.io.InputStream;

import java.util.*;

import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.DeleteListener;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.io.*;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.RawCtsQueryDefinition;
import com.marklogic.client.query.RawStructuredQueryDefinition;
import com.marklogic.client.query.StructuredQueryDefinition;

import com.marklogic.mule.extension.connector.internal.config.MarkLogicConfiguration;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;
import com.marklogic.mule.extension.connector.internal.error.MarkLogicExecuteErrorsProvider;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import com.marklogic.mule.extension.connector.internal.exception.MarkLogicConnectorException;
import com.marklogic.mule.extension.connector.internal.metadata.MarkLogicSelectMetadataResolver;
import com.marklogic.mule.extension.connector.internal.result.resultset.MarkLogicResultSetCloser;
import com.marklogic.mule.extension.connector.internal.result.resultset.MarkLogicResultSetIterator;

import org.apache.commons.jexl3.*;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.extension.api.runtime.operation.FlowListener;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class is a container for operations, every public method in this class will be taken as an extension operation. */
public class MarkLogicOperations
{

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicOperations.class);
    private static final String OUTPUT_URI_TEMPLATE = "%s%s%s"; // URI Prefix + basenameUri + URI Suffix

    private ObjectMapper jsonFactory = new ObjectMapper();

    // Loading files into MarkLogic asynchronously InputStream docPayload
    @MediaType(value = APPLICATION_JSON, strict = true)
    @Throws(MarkLogicExecuteErrorsProvider.class)
    public String importDocs(
            @Config MarkLogicConfiguration configuration,
            @Connection MarkLogicConnection connection,
            @DisplayName("Document payload")
            @Summary("The content of the input files to be used for ingestion into MarkLogic.")
            @Example("#[payload]")
            @Content InputStream docPayloads,
            @Optional(defaultValue = "null")
            @Summary("A comma-separated list of the collections to which persisted documents will belong after successful ingestion.")
            @Example("mulesoft-test") String outputCollections,
            @Optional(defaultValue = "rest-reader,read,rest-writer,update")
            @Summary("A comma-separated list of roles and capabilities to which persisted documents will possess after successful ingestion.")
            @Example("myRole,read,myRole,update") String outputPermissions,
            @Optional(defaultValue = "1")
            @Summary("A number indicating the quality of the persisted documents")
            @Example("1") int outputQuality,
            @Optional(defaultValue = "/")
            @Summary("The URI prefix, used to prepend and concatenate basenameUri.")
            @Example("/mulesoft/") String outputUriPrefix,
            @Optional(defaultValue = ".json")
            @Summary("The URI suffix, used to append and concatenate basenameUri.")
            @Example(".json") String outputUriSuffix,
            @DisplayName("Generate output URI basename?")
            @Optional(defaultValue = "true")
            @Summary("Creates a document basename based on a UUID, to be combined with the outputUriPrefix and outputUriSuffix. Use this if you can't programmatically assign a basename from an identifier in the document. Otherwise use basenameUri.")
            @Example("false") boolean generateOutputUriBasename,
            @DisplayName("Output document basename")
            @Optional(defaultValue = "null")
            @Summary("The file basename to be used for persistence in MarkLogic, usually derived a value from within the payload. Different than the UUID produced from generateOutputUriBasename.")
            @Example("employee123.json") String basenameUri,
            @DisplayName("Temporal collection")
            @Optional(defaultValue = "null")
            @Summary("The temporal collection imported documents will be loaded into.")
            @Example("") String temporalCollection)
    {

        // Get a handle to the Insertion batch manager
        MarkLogicInsertionBatcher batcher = MarkLogicInsertionBatcher.getInstance(configuration, connection, outputCollections, outputPermissions, outputQuality, configuration.getJobName(), temporalCollection);

        // Determine output URI
        // If the config tells us to generate a new UUID, do that
        if (generateOutputUriBasename)
        {
            basenameUri = UUID.randomUUID().toString();
            // Also, if the basenameURI is blank for whatever reason, use a new UUID
        }
        else if ((basenameUri == null) || (basenameUri.equals("null")) || (basenameUri.length() < 1))
        {
            basenameUri = UUID.randomUUID().toString();
        }

        // Assemble the output URI components
        String outURI = String.format(OUTPUT_URI_TEMPLATE, outputUriPrefix, basenameUri, outputUriSuffix);

        // Actually do the insert and return the result
        return batcher.doInsert(outURI, docPayloads);
    }

    /*
  Sample JSON created by getJobReport() :
{
	"importResults": [
		{
			"jobID": "59903224-c3db-46d8-9881-d24952131b4d",
			"jobOutcome": "successful",
			"successfulBatches": 2,
			"successfulEvents": 100,
			"failedBatches": 0,
			"failedEvents": 0,
			"jobName": "test-import",
			"jobStartTime": "2019-04-18T12:00:00Z",
			"jobEndTime": "2019-04-18T12:00:01Z",
			"jobReportTime": "2019-04-18T12:00:02Z"
		}
	],
	"exportResults": []
}
     */

    @MediaType(value = APPLICATION_JSON, strict = true)
    public String getJobReport()
    {
        ObjectNode rootObj = jsonFactory.createObjectNode();

        ArrayNode exports = jsonFactory.createArrayNode();
        rootObj.set("exportResults", exports);
        MarkLogicInsertionBatcher insertionBatcher = MarkLogicInsertionBatcher.getInstance();
        if (insertionBatcher != null)
        {
            ArrayNode imports = jsonFactory.createArrayNode();
            imports.add(insertionBatcher.createJsonJobReport(jsonFactory));
            rootObj.set("importResults", imports);
        }

        // Add support for query jobReport here!
        String result = rootObj.toString();

        // System.out.println("RESULT: " + result);
        // Add support for query result report here!
        return result;

    }

    @MediaType(value = ANY, strict = false)
    public String retrieveInfo(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection)
    {
        return "Using Configuration [" + configuration.getConfigId() + "] with Connection id [" + connection.getId() + "]";
    }

    @MediaType(value = APPLICATION_JSON, strict = true)
    @Throws(MarkLogicExecuteErrorsProvider.class)
    public String deleteDocs(
            @Config MarkLogicConfiguration configuration,
            @Connection MarkLogicConnection connection,
            @DisplayName("Serialized Query String")
            @Summary("The serialized query XML or JSON")
            @Text String queryString,
            @DisplayName("Search API Options")
            @Optional(defaultValue = "null")
            @Summary("The server-side Search API options file used to configure the search") String optionsName,
            @DisplayName("Search Strategy")
            @Summary("The Java class used to execute the serialized query") MarkLogicQueryStrategy queryStrategy,
            @DisplayName("Use Consistent Snapshot")
            @Summary("Whether to use a consistent point-in-time snapshot for operations") boolean useConsistentSnapshot,
            @DisplayName("Serialized Query Format")
            @Summary("The format of the serialized query") MarkLogicQueryFormat fmt
    )
    {
        DatabaseClient client = connection.getClient();
        QueryManager qm = client.newQueryManager();
        DataMovementManager dmm = client.newDataMovementManager();
        QueryBatcher batcher;
        QueryDefinition query;
        switch (queryStrategy)
        {
            case RawStructuredQueryDefinition:
                query = createRawStructuredQuery(qm, queryString, fmt);
                batcher = dmm.newQueryBatcher((RawStructuredQueryDefinition) query);
                break;
            case StructuredQueryBuilder:
                // Example of incoming structuredQuery string as criteria: sb.document("/mulesoft/10078.json") 
                query = createStructuredQuery(qm, queryString, optionsName);
                batcher = dmm.newQueryBatcher((StructuredQueryDefinition) query);
                break;
            case CTSQuery:
                query = createCtsQuery(qm, queryString, fmt, optionsName);
                batcher = dmm.newQueryBatcher((RawCtsQueryDefinition) query);
                break;
            default:
                logger.error(String.format("Query Strategy %s is not supported", queryStrategy));
                throw new RuntimeException("Invalid query type. Unable to create query to delete documents");
        }
        SearchHandle resultsHandle = qm.search(query, new SearchHandle());
        if (Boolean.valueOf(useConsistentSnapshot) == Boolean.TRUE)
        {
            batcher.withConsistentSnapshot();
        }
        batcher.withBatchSize(configuration.getBatchSize())
                .withThreadCount(configuration.getThreadCount())
                .onUrisReady(new DeleteListener())
                .onQueryFailure((throwable) ->
                {
                    logger.error("Exception thrown by an onBatchSuccess listener", throwable);  // For Sonar...
                });
        dmm.startJob(batcher);
        batcher.awaitCompletion();
        dmm.stopJob(batcher);
        ObjectNode rootObj = jsonFactory.createObjectNode();
        //ArrayNode imports = jsonFactory.createArrayNode();
        rootObj.put("deletionResult", String.format("%d document(s) deleted", resultsHandle.getTotalResults()));
        rootObj.put("deletionCount", resultsHandle.getTotalResults());
        return rootObj.toString();
    }

    @MediaType(value = ANY, strict = false)
    @OutputResolver(output = MarkLogicSelectMetadataResolver.class)
    @Deprecated
    public PagingProvider<MarkLogicConnection, Object> selectDocsByStructuredQuery(
            @DisplayName("Serialized Query String")
            @Summary("The serialized query XML or JSON")
            @Text String structuredQuery,
            @Config MarkLogicConfiguration configuration,
            @DisplayName("Search API Options")
            @Optional(defaultValue = "null")
            @Summary("The server-side Search API options file used to configure the search") String optionsName,
            @DisplayName("Search Strategy")
            @Summary("The Java class used to execute the serialized query") MarkLogicQueryStrategy structuredQueryStrategy,
            @DisplayName("Serialized Query Format")
            @Summary("The format of the serialized query") MarkLogicQueryFormat fmt,
            StreamingHelper streamingHelper,
            FlowListener flowListener
    ) throws MarkLogicConnectorException
    {
        return queryDocs(structuredQuery, configuration, optionsName, structuredQueryStrategy, fmt, streamingHelper, flowListener);
    }

    @MediaType(value = ANY, strict = false)
    @OutputResolver(output = MarkLogicSelectMetadataResolver.class)
    public PagingProvider<MarkLogicConnection, Object> queryDocs(
            @DisplayName("Serialized Query String")
            @Summary("The serialized query XML or JSON")
            @Text String queryString,
            @Config MarkLogicConfiguration configuration,
            @DisplayName("Search API Options")
            @Optional(defaultValue = "null")
            @Summary("The server-side Search API options file used to configure the search") String optionsName,
            @DisplayName("Search Strategy")
            @Summary("The Java class used to execute the serialized query") MarkLogicQueryStrategy queryStrategy,
            @DisplayName("Serialized Query Format")
            @Summary("The format of the serialized query") MarkLogicQueryFormat fmt,
            StreamingHelper streamingHelper,
            FlowListener flowListener)
            throws MarkLogicConnectorException
    {
        return new PagingProvider<MarkLogicConnection, Object>()
        {

            private final AtomicBoolean initialised = new AtomicBoolean(false);
            private MarkLogicResultSetCloser resultSetCloser;
            MarkLogicResultSetIterator iterator;

            @Override
            public List<Object> getPage(MarkLogicConnection connection)
            {
                if (initialised.compareAndSet(false, true))
                {
                    resultSetCloser = new MarkLogicResultSetCloser(connection);
                    flowListener.onError(e ->
                    {
                        try
                        {
                            close(connection);
                        }
                        catch (Exception t)
                        {
                            logger.warn(String.format("Exception was found closing connection for select operation. Error was: %s", t.getMessage()), e);
                        }
                    });

                    QueryDefinition query;
                    DatabaseClient client = connection.getClient();
                    QueryManager qm = client.newQueryManager();

                    String options = isDefined(optionsName) ? optionsName : null;

                    switch (queryStrategy)
                    {
                        case StructuredQueryBuilder:
                            // Example of incoming structuredQuery string as criteria: sb.document("/mulesoft/10078.json")
                            query = createStructuredQuery(qm, queryString, options);
                            break;
                        case CTSQuery:
                            query = createCtsQuery(qm, queryString, fmt, options);
                            break;
                        default: //RawStructuredQueryDefinition:
                            query = createRawStructuredQuery(qm, queryString, fmt);
                    }

                    if (configuration.hasServerTransform())
                    {
                        query.setResponseTransform(configuration.createServerTransform());
                    }
                    else
                    {
                        logger.info("Ingesting doc payload without a transform");
                    }

                    iterator = new MarkLogicResultSetIterator(connection, configuration, query);
                }

                return iterator.next();
            }

            @Override
            public java.util.Optional<Integer> getTotalResults(MarkLogicConnection markLogicConnection)
            {
                return java.util.Optional.empty();
            }

            @Override
            public void close(MarkLogicConnection connection) throws MuleException
            {
                resultSetCloser.closeResultSets();
            }

            @Override
            public boolean useStickyConnections()
            {
                return true;
            }
        };
    }

    private QueryDefinition createCtsQuery(QueryManager queryManager, String queryString, MarkLogicQueryFormat fmt, String optionsName)
    {
        return queryManager.newRawCtsQueryDefinitionAs(getMLQueryFormat(fmt), queryString, optionsName);
    }

    private static Format getMLQueryFormat(MarkLogicQueryFormat format)
    {
        switch (format)
        {
            case JSON:
                return Format.JSON;
            default:
                return Format.XML;
        }
    }

    private RawStructuredQueryDefinition createRawStructuredQuery(QueryManager qManager, String structuredQuery, MarkLogicQueryFormat fmt)
    {
        return qManager.newRawStructuredQueryDefinition(new StringHandle().withFormat(getMLQueryFormat(fmt)).with(structuredQuery));
    }

    private StructuredQueryDefinition createStructuredQuery(QueryManager qManager, String structuredQuery, String optionsName)
    {
        JexlEngine jexl = new JexlBuilder().create();
        JexlExpression e = jexl.createExpression(structuredQuery);
        JexlContext jc = new MapContext();
        if (optionsName == null)
        {
            jc.set("sb", qManager.newStructuredQueryBuilder());
        }
        else
        {
            jc.set("sb", qManager.newStructuredQueryBuilder(optionsName));
        }
        Object o = e.evaluate(jc);
        return (StructuredQueryDefinition) o;
    }

    private boolean isDefined(String str)
    {
        return str != null && !str.trim().isEmpty() && !"null".equals(str.trim());
    }
    //TODO: Make toString function for UI
//    private enum WhereMethod
//    {
//        Collections,
//        Uris,
//        UriPattern,
//        UrisQuery
//    }
}
