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

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.JobTicket;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.document.ServerTransform;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.mule.extension.connector.internal.config.MarkLogicConfiguration;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jkrebs on 9/12/2018. Singleton class that manages inserting
 * documents into MarkLogic
 */
public class MarkLogicInsertionBatcher implements MarkLogicConnectionInvalidationListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicInsertionBatcher.class);

    // a hash used internally to uniquely identify the batcher based on its current configuration
    private final int signature;

    // Object that describes the metadata for documents being inserted
    private DocumentMetadataHandle metadataHandle;

    // How will we know when the resources are ready to be freed up and provide the results report?
    private JobTicket jobTicket;

    // The object that actually write record to ML
    private WriteBatcher batcher;

    // Handle for DMSDK Data Movement Manager
    private DataMovementManager dmm;

    // The timestamp of the last write to ML-- used to determine when the pipe to ML should be flushed
    private long lastWriteTime;

    private boolean batcherRequiresReinit;
    private Timer timer = null;

    /**
     * Creates a new insertion batcher.
     *
     * @param marklogicConfiguration Information describing how the insertion process should work
     * @param connection Information describing how to connect to MarkLogic
     * @param outputCollections A comma-separated list of output collections used during ingestion.
     * @param outputPermissions A comma-separated list of roles and capabilities used during ingestion.
     * @param outputQuality A number indicating the quality of the persisted documents.
     * @param temporalCollection The temporal collection imported documents will be loaded into.
     * @param serverTransform The name of a deployed MarkLogic server-side Javascript, XQuery, or XSLT.
     * @param serverTransformParams A comma-separated list of alternating transform parameter names and values.
     */
    public MarkLogicInsertionBatcher(MarkLogicConfiguration marklogicConfiguration, MarkLogicConnection connection, String outputCollections, String outputPermissions, int outputQuality, String jobName, String temporalCollection, String serverTransform, String serverTransformParams)
    {
        this.batcherRequiresReinit = false;
        LOGGER.debug("MarkLogicInsertionBatcher batcherRequiresReinit {}", batcherRequiresReinit);
        this.signature = computeSignature(marklogicConfiguration, connection, outputCollections, outputPermissions, outputQuality, jobName, temporalCollection, serverTransform, serverTransformParams);

        // get the object handles needed to talk to MarkLogic
        initializeBatcher(marklogicConfiguration, connection, outputCollections, outputPermissions, outputQuality, temporalCollection, serverTransform, serverTransformParams);
        LOGGER.info("MarkLogicInsertionBatcher with job name: {}", jobName);
    }

    public static int computeSignature(MarkLogicConfiguration configuration, MarkLogicConnection connection, String outputCollections, String outputPermissions, int outputQuality, String jobName, String temporalCollection, String serverTransform, String serverTransformParams) {
        return Objects.hash(configuration, connection, outputCollections, outputPermissions, outputQuality, jobName, temporalCollection, serverTransform, serverTransformParams);
    }

    private void initializeBatcher(MarkLogicConfiguration configuration, MarkLogicConnection connection, String outputCollections, String outputPermissions, int outputQuality, String temporalCollection, String serverTransform, String serverTransformParams)
    {
        connection.addMarkLogicClientInvalidationListener(this);
        DatabaseClient myClient = connection.getClient();
        dmm = myClient.newDataMovementManager();
        batcher = dmm.newWriteBatcher();
        batcher.withBatchSize(configuration.getBatchSize())
                .withThreadCount(configuration.getThreadCount())
                .onBatchSuccess(batch -> LOGGER.info("Batcher with signature {} on connection ID {} writes so far: {}",
                    getSignature(), connection.getId(), batch.getJobWritesSoFar()))
                .onBatchFailure((batch, throwable) -> LOGGER.error("Exception thrown by an onBatchSuccess listener", throwable));

        // Configure the transform to be used, if any
        // ASSUMPTION: The same transform (or lack thereof) will be used for every document to be inserted during the
        // lifetime of this object

        if ((temporalCollection != null) && !"null".equalsIgnoreCase(temporalCollection))
        {
            LOGGER.info("TEMPORAL COLLECTION: {}", temporalCollection);
            batcher.withTemporalCollection(temporalCollection);
        }

        Optional<ServerTransform> transform = configuration.generateServerTransform(serverTransform, serverTransformParams);
        if(transform.isPresent())
        {
            batcher.withTransform(transform.get());
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
        }, secondsBeforeFlush * (long) 1000, secondsBeforeFlush * (long) 1000);

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
        // Set up quality new docs should have
        metadataHandle.setQuality(outputQuality);

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
                    LOGGER.info("No additive permissions assigned");
            }
        }

        // start the batcher job
        this.jobTicket = dmm.startJob(batcher);
    }

    public void release() {
        if (timer != null)
            timer.cancel();
        if (batcher != null) {
            // finalize all writes
            batcher.flushAndWait();
            dmm.stopJob(this.jobTicket);
        }
    }

    public int getSignature() {
        return this.signature;
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

        // Return the job ticket ID so it can be used to retrieve the document in the future
        String jsonout = "\"" + jobTicket.getJobId() + "\"";
        LOGGER.debug("importDocs getJobId outcome: {}", jsonout);
        
        Charset cs = StandardCharsets.UTF_8;
        return new ByteArrayInputStream(jsonout.getBytes(cs));
    }

    @Override
    public void markLogicConnectionInvalidated()
    {
        LOGGER.info("MarkLogic connection invalidated... reinitializing insertion batcher...");
        batcherRequiresReinit = true;
    }
}