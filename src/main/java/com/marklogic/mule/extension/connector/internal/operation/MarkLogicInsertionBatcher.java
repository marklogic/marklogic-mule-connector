/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2020 MarkLogic Corporation.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.JobReport;
import com.marklogic.client.datamovement.JobTicket;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.document.ServerTransform;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.mule.extension.connector.internal.config.DataHubConfiguration;
import com.marklogic.mule.extension.connector.internal.config.DataHubRunFlowOptions;
import com.marklogic.mule.extension.connector.internal.config.MarkLogicConfiguration;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.marklogic.mule.extension.connector.internal.error.exception.MarkLogicConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jkrebs on 9/12/2018. Singleton class that manages inserting
 * documents into MarkLogic
 */
public class MarkLogicInsertionBatcher implements MarkLogicConnectionInvalidationListener
{

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicInsertionBatcher.class);

    private static final DateTimeFormatter ISO8601_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    // The single instance of this class
    private static MarkLogicInsertionBatcher instance;

    // If support for multiple connection configs within a flow is required, remove the above and uncomment the below.
    // private static Map<String,MarkLogicInsertionBatcher> instances = new HashMap<>()_
    
    // Object that describes the metadata for documents being inserted
    private DocumentMetadataHandle metadataHandle;

    // How will we know when the resources are ready to be freed up and provide the results report?
    private JobTicket jobTicket;
    private final String jobName;

    // The object that actually write record to ML
    private WriteBatcher batcher;

    // Handle for DMSDK Data Movement Manager
    private DataMovementManager dmm;

    // The timestamp of the last write to ML-- used to determine when the pipe to ML should be flushed
    private long lastWriteTime;

    private boolean batcherRequiresReinit = false;
    private MarkLogicConnection connection;
    private Timer timer = null;

    /**
     * Private constructor-- enforces singleton pattern
     *
     * @param marklogicConfiguration -- information describing how the insertion process
     * should work
     * @param connection -- information describing how to connect to MarkLogic
     * @param serverTransform
     * @param serverTransformParams
     */
    private MarkLogicInsertionBatcher(MarkLogicConfiguration marklogicConfiguration, MarkLogicConnection connection, DataHubConfiguration dataHubConfiguration, DataHubRunFlowOptions dataHubRunFlowOptions, String outputCollections, String outputPermissions, int outputQuality, String jobName, String temporalCollection, String serverTransform, String serverTransformParams)
    {
        // get the object handles needed to talk to MarkLogic
        initializeBatcher(connection, marklogicConfiguration, dataHubConfiguration, dataHubRunFlowOptions, outputCollections, outputPermissions, outputQuality, temporalCollection, serverTransform, serverTransformParams);
        this.jobName = jobName;
    }

    private void initializeBatcher(MarkLogicConnection connection, MarkLogicConfiguration configuration, DataHubConfiguration dataHubConfiguration, DataHubRunFlowOptions dataHubRunFlowOptions, String outputCollections, String outputPermissions, int outputQuality, String temporalCollection, String serverTransform, String serverTransformParams)
    {
        boolean usingDataHub = dataHubConfiguration != null && dataHubRunFlowOptions != null;
        if (usingDataHub && !dataHubRunFlowOptions.getIsFirstStepIngestion())
            throw new MarkLogicConnectorException("No data hub ingestion step indicated in run flow options.");

        this.connection = connection;
        connection.addMarkLogicClientInvalidationListener(this);
        DatabaseClient myClient = connection.getClient();
        dmm = myClient.newDataMovementManager();
        batcher = dmm.newWriteBatcher();
        // Configure the batcher's behavior
        batcher.withBatchSize(configuration.getBatchSize())
                .withThreadCount(configuration.getThreadCount())
                .onBatchSuccess((batch) -> logger.debug("Writes so far: " + batch.getJobWritesSoFar()))
                .onBatchFailure((batch, throwable) -> logger.error("Exception thrown by an onBatchSuccess listener", throwable));

        if (usingDataHub) {
            // run the specified data hub flow step(s) against the documents inserted for each batch
            DataHubRunFlowBatchListener runFlowBatchListener = new DataHubRunFlowBatchListener(connection, dataHubConfiguration, dataHubRunFlowOptions);
            batcher.onBatchSuccess(runFlowBatchListener);
        }
        
        // Configure the transform to be used, if any
        // ASSUMPTION: The same transform (or lack thereof) will be used for every document to be inserted during the
        // lifetime of this object

        if ((temporalCollection != null) && !"null".equalsIgnoreCase(temporalCollection))
        {
            System.out.println("TEMPORAL COLLECTION: " + temporalCollection);
            batcher.withTemporalCollection(temporalCollection);
        }

        // Have to do this if we want to duplicate "standard" DHF ingestion (e.g. wrap data in an envelope)
        // Will still work w/o a transform, but we won't get header metadata or trigger custom hook(s)
        ServerTransform transform = usingDataHub ? dataHubRunFlowOptions.getIngestionStepTransform() : configuration.generateServerTransform(serverTransform, serverTransformParams);
        if(transform != null)
        {
            batcher.withTransform(transform);
        }

        // Set up the timer to flush the pipe to MarkLogic if it's waiting to long
        int secondsBeforeFlush = configuration.getSecondsBeforeFlush();

        if (timer != null)
        {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                // Check to see if the pipe has been inactive longer than the wait time
                if ((System.currentTimeMillis() - lastWriteTime) >= secondsBeforeFlush * 1000)
                {
                    // if it has, flush the pipe
                    batcher.flushAndWait();
                    // Set the last write time to be something well into the future, so that we don't needlessly,
                    // repeatedly flush the queue
                    lastWriteTime = System.currentTimeMillis() + 900000;
                }
            }
        }, secondsBeforeFlush * 1000, secondsBeforeFlush * 1000);

        // Set up the metadata to be used for the documents that will be inserted
        // ASSUMPTION: The same metadata will be used for every document to be inserted during the lifetime of this
        // object
        this.metadataHandle = new DocumentMetadataHandle();
        String[] configCollections = outputCollections.split(",");

        // Set up list of collections that new docs should be put into
        if (!configCollections[0].equals("null"))
        {
            metadataHandle.withCollections(configCollections);
        }

        // not sure yet, but getting XDMP-INVOPTVAL when used with data hub's "mlIngest" transform; skipping for now
        if (!usingDataHub) {
            // Set up quality new docs should have
            metadataHandle.setQuality(outputQuality);
        }

        // Set up list of permissions that new docs should be granted
        String[] permissions = outputPermissions.split(",");
        for (int i = 0; i < permissions.length - 1; i++)
        {
            String role = permissions[i];
            String capability = permissions[i + 1];
            switch (capability.toLowerCase())
            {
                case "read":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.READ);
                    break;
                case "insert":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.INSERT);
                    break;
                case "update":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.UPDATE);
                    break;
                case "execute":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.EXECUTE);
                    break;
                case "node_update":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.NODE_UPDATE);
                    break;
                default:
                    logger.info("No additive permissions assigned");
            }
        }

        // start the batcher job
        this.jobTicket = dmm.startJob(batcher);
    }

    /**
     * Creates a JSON object containing details about the batcher job
     *
     * @return Job results report
     * @param jsonFactory
     */
    ObjectNode createJsonJobReport(ObjectMapper jsonFactory)
    {
        JobReport jr = dmm.getJobReport(jobTicket);
        ObjectNode obj = jsonFactory.createObjectNode();
        obj.put("jobID", jobTicket.getJobId());
        ZonedDateTime jobStartTime = toZonedDateTime(jr.getJobStartTime());
        ZonedDateTime jobEndTime = toZonedDateTime(jr.getJobEndTime());
        ZonedDateTime jobReportTime = toZonedDateTime(jr.getReportTimestamp());
        long successBatches = jr.getSuccessBatchesCount();
        long successEvents = jr.getSuccessEventsCount();
        long failBatches = jr.getFailureBatchesCount();
        long failEvents = jr.getFailureEventsCount();
        if (failEvents > 0)
        {
            obj.put("jobOutcome", "failed");
        }
        else
        {
            obj.put("jobOutcome", "successful");
        }
        obj.put("successfulBatches", successBatches);
        obj.put("successfulEvents", successEvents);
        obj.put("failedBatches", failBatches);
        obj.put("failedEvents", failEvents);
        obj.put("jobName", jobName);
        obj.put("jobStartTime", jobStartTime.format(ISO8601_DATE_TIME_FORMATTER));
        obj.put("jobEndTime", jobEndTime.format(ISO8601_DATE_TIME_FORMATTER));
        obj.put("jobReportTime", jobReportTime.format(ISO8601_DATE_TIME_FORMATTER));
        return obj;
    }

    /**
     * getInstance-- used in lieu of a public constructor... enforces singleton
     * pattern
     *
     * @param config -- information describing how the insertion process should
     * work
     * @param connection -- information describing how to connect to MarkLogic
     * @param temporalCollection
     * @return instance of the batcher
     */
    static MarkLogicInsertionBatcher getInstance(MarkLogicConfiguration config, MarkLogicConnection connection, DataHubConfiguration dataHubConfiguration, DataHubRunFlowOptions dataHubRunFlowOptions, String outputCollections, String outputPermissions, int outputQuality, String jobName, String temporalCollection, String serverTransform, String serverTransformParams)
    {
        if (instance == null)
        {
            instance = new MarkLogicInsertionBatcher(config, connection, dataHubConfiguration, dataHubRunFlowOptions, outputCollections, outputPermissions, outputQuality, jobName, temporalCollection, serverTransform, serverTransformParams);
        }
        else if ((!(connection == null)) && (!connection.equals(instance.connection)) && (instance.batcherRequiresReinit))
        {
            instance.initializeBatcher(connection, config, dataHubConfiguration, dataHubRunFlowOptions, outputCollections, outputPermissions, outputQuality, temporalCollection, serverTransform, serverTransformParams);
            instance.batcherRequiresReinit = false;
        }
        return instance;
    }

    /**
     * getInstance method to be used when configuration objects aren't available
     *
     * @return instance of the batcher
     */
    static MarkLogicInsertionBatcher getInstance()
    {
        return instance;
    }

    /**
     * Actually does the work of passing the document on to DMSDK to do its
     * thing
     *
     * @param outURI -- the URI to be used for the document being inserted
     * @param documentStream -- the InputStream containing the document to be inserted...comes from Mule
     * @return jobTicketID
     */
    InputStream doInsert(String outURI, InputStream documentStream)
    {
        // Add the InputStream to the DMSDK WriteBatcher object
        batcher.addAs(outURI, metadataHandle, new InputStreamHandle(documentStream));
        // Update the most recent insert's timestamp
        lastWriteTime = System.currentTimeMillis();
        // Have the DMSDK WriteBatcher object sleep until it is needed again
        batcher.awaitCompletion();
        // Return the job ticket ID so it can be used to retrieve the document in the future
        String jsonout = "\"" + jobTicket.getJobId() + "\"";
        logger.debug("importDocs getJobId outcome: " + jsonout);
        
        Charset cs = StandardCharsets.UTF_8;
        return new ByteArrayInputStream(jsonout.getBytes(cs));
    }

    private ZonedDateTime toZonedDateTime(Calendar calendar)
    {
        if (calendar == null)
        {
            return ZonedDateTime.now();
        }
        TimeZone tz = calendar.getTimeZone();
        ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
        return ZonedDateTime.ofInstant(calendar.toInstant(), zid);
    }

    @Override
    public void markLogicConnectionInvalidated()
    {
        logger.info("MarkLogic connection invalidated... reinitializing insertion batcher...");
        batcherRequiresReinit = true;
    }
}