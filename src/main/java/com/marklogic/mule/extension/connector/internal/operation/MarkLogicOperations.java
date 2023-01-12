/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2021 MarkLogic Corporation.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.DeleteListener;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.document.ServerTransform;
import com.marklogic.client.io.*;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;

import com.marklogic.mule.extension.connector.api.operation.MarkLogicQueryFormat;
import com.marklogic.mule.extension.connector.api.operation.MarkLogicQueryStrategy;
import com.marklogic.mule.extension.connector.internal.config.MarkLogicConfiguration;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;
import com.marklogic.mule.extension.connector.internal.error.provider.MarkLogicExecuteErrorsProvider;
import com.marklogic.mule.extension.connector.internal.metadata.MarkLogicSelectMetadataResolver;
import com.marklogic.mule.extension.connector.internal.metadata.MarkLogicAnyMetadataResolver;
import com.marklogic.mule.extension.connector.internal.result.resultset.MarkLogicExportListener;
import com.marklogic.mule.extension.connector.internal.result.resultset.MarkLogicResultSetCloser;
import com.marklogic.mule.extension.connector.internal.result.resultset.MarkLogicResultSetIterator;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;
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

/**
* This class is a container for operations, every public method in this class will be taken as an extension operation for the MarkLogic MuleSoft Connector
* 
* @author Jonathan Krebs (jkrebs)
* @author Clay Redding (wattsferry)
* @author John Shingler (jshingler)
* @since 1.0.0
* @version 1.1.2
* @see <a target="_blank" href="https://github.com/marklogic-community/marklogic-mule-connector">MarkLogic MuleSoft Connector GitHub</a>
* 
*/
public class MarkLogicOperations
{

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicOperations.class);
    private static final String OUTPUT_URI_TEMPLATE = "%s%s%s"; // URI Prefix + basenameUri + URI Suffix

    private ObjectMapper jsonFactory = new ObjectMapper();

 /**
 * <p>Loads JSON, XML, text, or binary document content asynchronously into MarkLogic, via the <a target="_blank" href="https://docs.marklogic.com/guide/java/intro">MarkLogic Java API</a> <a target="_blank" href="https://docs.marklogic.com/guide/java/data-movement">Data Movement SDK (DMSDK)</a> returning the DMSDK <a target="_blank" href="https://docs.marklogic.com/javadoc/client/com/marklogic/client/datamovement/JobTicket.html">JobTicket</a> ID used to insert the contents into MarkLogic.</p>
 * @param markLogicConfiguration The MarkLogic configuration details
 * @param connection The MarkLogic connection details
 * @param docPayloads The content of the input files to be used for ingestion into MarkLogic.
 * @param outputCollections A comma-separated list of output collections used during ingestion.
 * @param outputPermissions A comma-separated list of roles and capabilities used during ingestion.
 * @param outputQuality A number indicating the quality of the persisted documents.
 * @param outputUriPrefix The URI prefix, used to prepend and concatenate basenameUri.
 * @param outputUriSuffix The URI suffix, used to append and concatenate basenameUri.
 * @param generateOutputUriBasename Creates a document basename based on an auto-generated UUID.
 * @param basenameUri File basename to be used for persistence in MarkLogic, usually payload-derived.
 * @param temporalCollection The temporal collection imported documents will be loaded into.
 * @param serverTransform The name of a deployed MarkLogic server-side Javascript, XQuery, or XSLT.
 * @param serverTransformParams A comma-separated list of alternating transform parameter names and values.
 * @return java.io.InputStream
 * @throws com.marklogic.mule.extension.connector.internal.error.provider.MarkLogicExecuteErrorsProvider
 * @since 1.0.0
 */
    @MediaType(value = APPLICATION_JSON, strict = true)
    @Throws(MarkLogicExecuteErrorsProvider.class)
    public InputStream importDocs(
            @Config MarkLogicConfiguration markLogicConfiguration,
            @Connection MarkLogicConnection connection,
            @DisplayName("Document payload")
            @Summary("The content of the input files to be used for ingestion into MarkLogic.")
            @Example("#[payload]")
            @Content InputStream docPayloads,
            @Optional(defaultValue = "null")
            @Summary("A comma-separated list of output collections used during ingestion.")
            @Example("mulesoft-test") String outputCollections,
            @Optional(defaultValue = "rest-reader,read,rest-writer,update")
            @Summary("A comma-separated list of roles and capabilities used during ingestion.")
            @Example("myRole,read,myRole,update") String outputPermissions,
            @Optional(defaultValue = "1")
            @Summary("A number indicating the quality of the persisted documents.")
            @Example("1") int outputQuality,
            @Optional(defaultValue = "/")
            @Summary("The URI prefix, used to prepend and concatenate basenameUri.")
            @Example("/mulesoft/") String outputUriPrefix,
            @Optional(defaultValue = "")
            @Summary("The URI suffix, used to append and concatenate basenameUri.")
            @Example(".json") String outputUriSuffix,
            @DisplayName("Generate output URI basename?")
            @Optional(defaultValue = "true")
            @Summary("Creates a document basename based on an auto-generated UUID.")
            @Example("false") boolean generateOutputUriBasename,
            @DisplayName("Output document basename")
            @Optional(defaultValue = "null")
            @Summary("File basename to be used for persistence in MarkLogic, usually payload-derived.")
            @Example("employee123.json") String basenameUri,
            @DisplayName("Temporal collection")
            @Optional(defaultValue = "null")
            @Summary("The temporal collection imported documents will be loaded into.")
            @Example("myTemporalCollection") String temporalCollection,
            @Summary("The name of a deployed MarkLogic server-side Javascript, XQuery, or XSLT.")
            @Optional(defaultValue = "null")
            @Example("ml:sjsInputFlow") String serverTransform,
            @Summary("A comma-separated list of alternating transform parameter names and values.")
            @Optional(defaultValue = "null")
            @Example("entity-name,MyEntity,flow-name,loadMyEntity")
            String serverTransformParams
            )
    {
        // Get a handle to the Insertion batch manager
        MarkLogicInsertionBatcher batcher = connection.getInsertionBatcher(markLogicConfiguration, outputCollections, outputPermissions, outputQuality, temporalCollection, serverTransform, serverTransformParams);
        String outURI = generateOutputUri(outputUriPrefix, outputUriSuffix, generateOutputUriBasename, basenameUri);

        // Actually do the insert and return the result
        return batcher.doInsert(outURI, docPayloads);
    }

 /**
 * <p>Retrieves a JSON representation of a <a target="_blank" href="https://docs.marklogic.com/guide/java/intro">MarkLogic Java API</a> <a target="_blank" href="https://docs.marklogic.com/guide/java/data-movement">Data Movement SDK (DMSDK)</a> <a target="_blank" href="https://docs.marklogic.com/javadoc/client/com/marklogic/client/datamovement/JobReport.html">JobReport</a> following an importDocs operation.</p>
 * @throws com.marklogic.mule.extension.connector.internal.error.provider.MarkLogicExecuteErrorsProvider
 * @since 1.0.0
 * @return java.io.InputStream
 * @deprecated Deprecated in v.1.1.1
 */
 @SuppressWarnings("java:S1133")
    @Deprecated
    @MediaType(value = APPLICATION_JSON, strict = true)
    @DisplayName("Get Job Report (deprecated)")
    public InputStream getJobReport()
    {
        InputStream targetStream = new ByteArrayInputStream(new byte[0]);
        ObjectNode rootObj = jsonFactory.createObjectNode();

        ArrayNode exports = jsonFactory.createArrayNode();
        rootObj.set("exportResults", exports);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getJobReport outcome: {}", rootObj.asText());
        }

        try {
            byte[] bin = jsonFactory.writeValueAsBytes(rootObj);
            targetStream = new ByteArrayInputStream(bin);
        } catch(IOException ex) {
            LOGGER.error(String.format("Exception was thrown during getJobReport operation. Error was: %s", ex.getMessage()), ex);
        }
        
        return targetStream;
    }

 /**
 * <p>Echoes the current MarkLogicConnector and MarkLogicConfiguration information.</p>
 * @param configuration The MarkLogic configuration details
 * @param connection The MarkLogic connection details
 * @return java.lang.String
 * @since 1.0.0
 */
    @MediaType(value = ANY, strict = false)
    public String retrieveInfo(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection)
    {
        return "Using Configuration [" + configuration.getConfigId() + "] with Connection id [" + connection.getId() + "]";
    }

 /**
 * <p>Delete query-selected document content asynchronously from MarkLogic, via the <a target="_blank" href="https://docs.marklogic.com/guide/java/intro">MarkLogic Java API</a> <a target="_blank" href="https://docs.marklogic.com/guide/java/data-movement">Data Movement SDK (DMSDK)</a> returning a JSON object detailing the outcome.</p>
 * @param configuration The MarkLogic configuration details
 * @param connection The MarkLogic connection details
 * @param queryString The serialized query XML or JSON.
 * @param optionsName The server-side Search API options file used to configure the search.
 * @param queryStrategy The Java class used to execute the serialized query.
 * @param useConsistentSnapshot Whether to use a consistent point-in-time snapshot for operations.
 * @param fmt The format of the serialized query.
 * @return java.io.InputStream
 * @throws com.marklogic.mule.extension.connector.internal.error.provider.MarkLogicExecuteErrorsProvider
 * @since 1.1.0
 */
    @MediaType(value = APPLICATION_JSON, strict = true)
    @Throws(MarkLogicExecuteErrorsProvider.class)
    public InputStream deleteDocs(
            @Config MarkLogicConfiguration configuration,
            @Connection MarkLogicConnection connection,
            @DisplayName("Serialized Query String")
            @Summary("The serialized query XML or JSON.")
            @Text String queryString,
            @DisplayName("Search API Options")
            @Optional
            @Summary("The server-side Search API options file used to configure the search.") String optionsName,
            @DisplayName("Search Strategy")
            @Summary("The Java class used to execute the serialized query.") MarkLogicQueryStrategy queryStrategy,
            @DisplayName("Use Consistent Snapshot")
            @Summary("Whether to use a consistent point-in-time snapshot for operations.") boolean useConsistentSnapshot,
            @DisplayName("Serialized Query Format")
            @Summary("The format of the serialized query.") MarkLogicQueryFormat fmt
    )
    {
        DatabaseClient client = connection.getClient();
        QueryManager qm = client.newQueryManager();
        DataMovementManager dmm = client.newDataMovementManager();
        QueryDefinition query = queryStrategy.getQueryDefinition(qm, queryString, fmt, optionsName);
        QueryBatcher batcher = queryStrategy.newQueryBatcher(dmm, query);
        SearchHandle resultsHandle = qm.search(query, new SearchHandle());
        
        if (useConsistentSnapshot)
        {
            batcher.withConsistentSnapshot();
        }
        
        batcher.withBatchSize(configuration.getBatchSize())
                .withThreadCount(configuration.getThreadCount())
                .onUrisReady(new DeleteListener())
                .onQueryFailure(throwable -> LOGGER.error("Exception thrown by an onBatchSuccess listener", throwable));
        dmm.startJob(batcher);
        batcher.awaitCompletion();
        dmm.stopJob(batcher);
        
        InputStream targetStream = new ByteArrayInputStream(new byte[0]);
        ObjectNode rootObj = jsonFactory.createObjectNode();
        rootObj.put("deletionResult", String.format("%d document(s) deleted", resultsHandle.getTotalResults()));
        rootObj.put("deletionCount", resultsHandle.getTotalResults());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("deleteDocs outcome: {}", rootObj.asText());
        }

        try {
            byte[] bin = jsonFactory.writeValueAsBytes(rootObj);
            targetStream = new ByteArrayInputStream(bin);
            targetStream.close();
        } catch(IOException ex) {
            LOGGER.error(String.format("Exception was thrown during deleteDocs operation. Error was: %s", ex.getMessage()), ex);
        }
        return targetStream;
    }

 /**
 * <p>Retrieve query-selected document content synchronously from MarkLogic, via the <a target="_blank" href="https://docs.marklogic.com/guide/java/intro">MarkLogic Java API</a>.</p>
 * @param configuration The MarkLogic configuration details
 * @param structuredQuery The serialized query XML or JSON.
 * @param optionsName The server-side Search API options file used to configure the search.
 * @param structuredQueryStrategy The Java class used to execute the serialized query
 * @param fmt The format of the serialized query.
 * @param serverTransform The name of a deployed MarkLogic server-side Javascript, XQuery, or XSLT.
 * @param serverTransformParams A comma-separated list of alternating transform parameter names and values.
 * @param streamingHelper The streaming helper.
 * @param flowListener The flow listener.
 * @return org.mule.runtime.extension.api.runtime.streaming.PagingProvider
 * @throws com.marklogic.mule.extension.connector.internal.error.provider.MarkLogicExecuteErrorsProvider
 * @deprecated Deprecated in v.1.1.0, use queryDocs instead
 * @since 1.1.0
 */
 @SuppressWarnings("java:S1133")
    @Deprecated
    @MediaType(value = ANY, strict = false)
    @OutputResolver(output = MarkLogicSelectMetadataResolver.class)
    @DisplayName("Select Documents By Structured Query (deprecated)")
    //@org.mule.runtime.extension.api.annotation.deprecated.Deprecated(message = "Use Query Docs instead", since = "1.1.0")
    @Throws(MarkLogicExecuteErrorsProvider.class)
    public PagingProvider<MarkLogicConnection, Object> selectDocsByStructuredQuery(
            @Config MarkLogicConfiguration configuration,
            @DisplayName("Serialized Query String")
            @Summary("The serialized query XML or JSON.")
            @Text String structuredQuery,
            @DisplayName("Search API Options")
            @Optional
            @Summary("The server-side Search API options file used to configure the search.") String optionsName,
            @DisplayName("Search Strategy")
            @Summary("The Java class used to execute the serialized query.") MarkLogicQueryStrategy structuredQueryStrategy,
            @DisplayName("Serialized Query Format")
            @Summary("The format of the serialized query.") MarkLogicQueryFormat fmt,
            @Summary("The name of a deployed MarkLogic server-side Javascript, XQuery, or XSLT.")
            @Optional(defaultValue = "null")
            @Example("ml:sjsInputFlow") String serverTransform,
            @Summary("A comma-separated list of alternating transform parameter names and values.")
            @Optional(defaultValue = "null")
            @Example("entity-name,MyEntity,flow-name,loadMyEntity") String serverTransformParams,
            StreamingHelper streamingHelper,
            FlowListener flowListener
    )
    {
        return queryDocs(configuration, structuredQuery, optionsName, null, null, structuredQueryStrategy, fmt, serverTransform, serverTransformParams, streamingHelper, flowListener);
    }

 /**
 * <p>Retrieve query-selected document content synchronously from MarkLogic, via the <a target="_blank" href="https://docs.marklogic.com/guide/java/intro">MarkLogic Java API</a>.</p>
 * @param configuration The MarkLogic configuration details
 * @param queryString The serialized query XML or JSON.
 * @param optionsName The server-side Search API options file used to configure the search.
 * @param pageLength Number of documents fetched at a time, defaults to the connection batch size.
 * @param maxResults Maximum total number of documents to be fetched, defaults to unlimited.
 * @param queryStrategy The Java class used to execute the serialized query
 * @param fmt The format of the serialized query.
 * @param serverTransform The name of a deployed MarkLogic server-side Javascript, XQuery, or XSLT.
 * @param serverTransformParams A comma-separated list of alternating transform parameter names and values.
 * @param streamingHelper The streaming helper.
 * @param flowListener The flow listener.
 * @return org.mule.runtime.extension.api.runtime.streaming.PagingProvider
 * @throws com.marklogic.mule.extension.connector.internal.error.provider.MarkLogicExecuteErrorsProvider
 * @since 1.1.0
 */
    @MediaType(value = ANY, strict = false)
    @OutputResolver(output = MarkLogicAnyMetadataResolver.class)
    @Throws(MarkLogicExecuteErrorsProvider.class)
    public PagingProvider<MarkLogicConnection, Object> queryDocs(
            @Config MarkLogicConfiguration configuration,
            @DisplayName("Serialized Query String")
            @Summary("The serialized query XML or JSON.")
            @Text String queryString,
            @DisplayName("Search API Options")
            @Optional
            @Summary("The server-side Search API options file used to configure the search.") String optionsName,
            @DisplayName("Page Length")
            @Optional
            @Summary("Number of documents fetched at a time, defaults to the connection batch size.") Integer pageLength,
            @DisplayName("Maximum Number of Results")
            @Optional
            @Summary("Maximum total number of documents to be fetched, defaults to unlimited.") Long maxResults,
            @DisplayName("Search Strategy")
            @Summary("The Java class used to execute the serialized query.") MarkLogicQueryStrategy queryStrategy,
            @DisplayName("Serialized Query Format")
            @Summary("The format of the serialized query.") MarkLogicQueryFormat fmt,
            @Summary("The name of a deployed MarkLogic server-side Javascript, XQuery, or XSLT.")
            @Optional(defaultValue = "null")
            @Example("ml:sjsInputFlow") String serverTransform,
            @Summary("A comma-separated list of alternating transform parameter names and values.")
            @Optional(defaultValue = "null")
            @Example("entity-name,MyEntity,flow-name,loadMyEntity") String serverTransformParams,
            StreamingHelper streamingHelper,
            FlowListener flowListener)
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
                    flowListener.onError(ex ->
                    {
                        LOGGER.error(String.format("Exception was thrown during select operation. Error was: %s", ex.getMessage()), ex);
                        try
                        {
                            close(connection);
                        }
                        catch (MuleException e)
                        {
                            LOGGER.info(String.format("Exception was found closing connection for select operation. Error was: %s", e.getMessage()), e);
                        }
                    });

                    DatabaseClient client = connection.getClient();
                    QueryManager qm = client.newQueryManager();

                    String options = MarkLogicConfiguration.isDefined(optionsName) ? optionsName : null;
                    QueryDefinition query = queryStrategy.getQueryDefinition(qm,queryString,fmt,options);

                    java.util.Optional<ServerTransform> transform = configuration.generateServerTransform(serverTransform, serverTransformParams);
                    if(transform.isPresent())
                    {
                        query.setResponseTransform(transform.get());
                    }
                    
                    if ((pageLength != null) && (pageLength < 1))
                    {
                        iterator = new MarkLogicResultSetIterator(connection, query, configuration.getBatchSize(), maxResults);
                    }
                    else
                    {
                        iterator = new MarkLogicResultSetIterator(connection, query, pageLength, maxResults);
                    }

                }
                return iterator.next();
            }

            @Override
            public java.util.Optional<Integer> getTotalResults(MarkLogicConnection markLogicConnector)
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

 /**
 * <p>Retrieve query-selected document content asynchronously from MarkLogic, via the <a target="_blank" href="https://docs.marklogic.com/guide/java/intro">MarkLogic Java API</a> <a target="_blank" href="https://docs.marklogic.com/guide/java/data-movement">Data Movement SDK (DMSDK)</a>.</p>
 * @param configuration The MarkLogic configuration details
 * @param queryString The serialized query XML or JSON.
 * @param optionsName The server-side Search API options file used to configure the search.
 * @param queryStrategy The Java class used to execute the serialized query.
 * @param fmt The format of the serialized query.
 * @param maxResults Maximum total number of documents to be fetched, defaults to unlimited.
 * @param useConsistentSnapshot Whether to use a consistent point-in-time snapshot for operations.
 * @param serverTransform The name of a deployed MarkLogic server-side Javascript, XQuery, or XSLT.
 * @param serverTransformParams A comma-separated list of alternating transform parameter names and values.
 * @return org.mule.runtime.extension.api.runtime.streaming.PagingProvider
 * @throws com.marklogic.mule.extension.connector.internal.error.provider.MarkLogicExecuteErrorsProvider
 * @since 1.1.0
 */
    @MediaType(value = ANY, strict = false)
    @OutputResolver(output = MarkLogicAnyMetadataResolver.class)
    @Throws(MarkLogicExecuteErrorsProvider.class)
    public PagingProvider<MarkLogicConnection, Object> exportDocs(
            @Config MarkLogicConfiguration configuration,
            @DisplayName("Serialized Query String")
            @Summary("The serialized query XML or JSON.")
            @Text String queryString,
            @DisplayName("Search API Options")
            @Optional
            @Summary("The server-side Search API options file used to configure the search.") String optionsName,
            @DisplayName("Search Strategy")
            @Summary("The Java class used to execute the serialized query.") MarkLogicQueryStrategy queryStrategy,
            @DisplayName("Serialized Query Format")
            @Summary("The format of the serialized query.") MarkLogicQueryFormat fmt,
            @DisplayName("Maximum Number of Results")
            @Optional
            @Summary("Maximum total number of documents to be fetched, defaults to unlimited.") Long maxResults,
            @DisplayName("Use Consistent Snapshot")
            @Summary("Whether to use a consistent point-in-time snapshot for operations.") boolean useConsistentSnapshot,
            @Summary("The name of a deployed MarkLogic server-side Javascript, XQuery, or XSLT.")
            @Optional(defaultValue = "null")
            @Example("ml:sjsInputFlow") String serverTransform,
            @Summary("A comma-separated list of alternating transform parameter names and values.")
            @Optional(defaultValue = "null")
            @Example("entity-name,MyEntity,flow-name,loadMyEntity") String serverTransformParams
    )
    {
        Long max = maxResults != null ? maxResults : 0;
        MarkLogicExportListener exportListener = new MarkLogicExportListener(max);

        return new PagingProvider<MarkLogicConnection, Object>()
        {
            private final AtomicBoolean initialised = new AtomicBoolean(false);
            private DataMovementManager dmm = null;

            @Override
            public List<Object> getPage(MarkLogicConnection markLogicConnector)
            {
                if (initialised.compareAndSet(false, true))
                {
                    DatabaseClient client = markLogicConnector.getClient();
                    QueryManager qm = client.newQueryManager();
                    dmm = client.newDataMovementManager();

                    QueryDefinition query = queryStrategy.getQueryDefinition(qm, queryString, fmt, optionsName);
                    
                    QueryBatcher batcher = queryStrategy.newQueryBatcher(dmm, query);
                    
                    java.util.Optional<ServerTransform> transform = configuration.generateServerTransform(serverTransform, serverTransformParams);
                    if (transform.isPresent()) {
                        LOGGER.info("Configuring transform for exportListener: {}", transform.get().getName());
                        exportListener.withTransform(transform.get());
                    }
                    
                    if (useConsistentSnapshot)
                    {
                        batcher.withConsistentSnapshot();
                    }
                    
                    batcher.withBatchSize(configuration.getBatchSize())
                            .withThreadCount(configuration.getThreadCount())
                            .onUrisReady(exportListener)
                            .onQueryFailure(throwable -> LOGGER.error("Exception thrown by an onBatchSuccess listener", throwable));
                    
                    dmm.startJob(batcher);
                    batcher.awaitCompletion();
                    dmm.stopJob(batcher);
                }

                if (dmm == null)
                {
                    LOGGER.warn("Data Movement Manager is null after initialization.");
                }
                List<Object> results = new ArrayList<>(exportListener.getDocs());
                exportListener.clearDocs();

                return results;
            }

            @Override
            public java.util.Optional<Integer> getTotalResults(MarkLogicConnection markLogicConnector)
            {
                return java.util.Optional.empty();
            }

            @Override
            public void close(MarkLogicConnection markLogicConnector)
            {
                LOGGER.debug("NOT Invalidating ML connection...");
            }
        };
    }

    private static String generateOutputUri(String outputUriPrefix, String outputUriSuffix, boolean generateOutputUriBasename, String basenameUri) {
        // Determine output URI
        // If the config tells us to generate a new UUID, do that
        String basename = basenameUri;
        if (generateOutputUriBasename || basenameUri == null || basenameUri.equals("null") || basenameUri.length() < 1)
        {
            basename = UUID.randomUUID().toString();
            // Also, if the basenameURI is blank for whatever reason, use a new UUID
        }
        // Assemble the output URI components
        return String.format(OUTPUT_URI_TEMPLATE, outputUriPrefix, basename, outputUriSuffix);
    }
}