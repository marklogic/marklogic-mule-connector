package com.marklogic.mule.extension.connector.internal;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.JobTicket;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.document.ServerTransform;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.InputStreamHandle;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jkrebs on 9/12/2018.
 * Singleton class that manages inserting documents into MarkLogic
 */

public class MarkLogicInsertionBatcher {

    // The single instance of this class
    private static MarkLogicInsertionBatcher instance;

    // Object that describes the metadata for documents being inserted
    private final DocumentMetadataHandle metadataHandle;

    // TODO: How will we know when the resources are ready to be freed up and provide the results report?
    private final JobTicket jt;

    // The object that actually write record to ML
    private WriteBatcher batcher;

    // The timestamp of the last write to ML-- used to determine when the pipe to ML should be flushed
    private long lastWriteTime;

    // The number of seconds the pipe to ML should wait before being flushed
    private static final int SECONDS_BEFORE_FLUSH = 2;

    /**
     * Private constructor-- enforces singleton pattern
     * @param configuration -- information describing how the insertion process should work
     * @param connection -- information describing how to connect to MarkLogic
     */
    private MarkLogicInsertionBatcher(MarkLogicConfiguration configuration, MarkLogicConnection connection) {

        // get the object handles needed to talk to MarkLogic
        DatabaseClient myClient = connection.getClient();
        DataMovementManager dmm = myClient.newDataMovementManager();
        batcher = dmm.newWriteBatcher();
        // Configure the batcher's behavior
        batcher.withBatchSize(configuration.getBatchSize())
                .withThreadCount(configuration.getThreadCount())
                .onBatchSuccess(batch-> {
                    String successMsg = batch.getTimestamp().getTime() + " documents written: " + batch.getJobWritesSoFar();
                    System.out.println(successMsg);
                })
                .onBatchFailure((batch,throwable) -> {
                    throwable.printStackTrace();
                });
        // Configure the transform to be used, if any
        // ASSUMPTION: The same transform (or lack thereof) will be used for every document to be inserted during the
        // lifetime of this object
        String configTransform = configuration.getServerTransform();
        if ((configTransform == null) || (configTransform.equals("null"))) {
            System.out.println("Ingesting doc payload without a transform");
        } else {
            ServerTransform thistransform = new ServerTransform(configTransform);
            String[] configTransformParams = configuration.getServerTransformParams();
            if (!configTransformParams[0].equals("null") && configTransformParams.length % 2 == 0) {
                for (int i = 0; i < configTransformParams.length - 1; i++) {
                    String paramName = configTransformParams[i];
                    String paramValue = configTransformParams[i + 1];
                    thistransform.addParameter(paramName, paramValue);
                }
            }
            batcher.withTransform(thistransform);
            System.out.println("Transforming input doc payload with transform: " + thistransform.getName());
        }

        // Set up the timer to flush the pipe to MarkLogic if it's waiting to long
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Check to see if the pipe has been inactive longer than the wait time
                if ((System.currentTimeMillis() - lastWriteTime) >= SECONDS_BEFORE_FLUSH * 1000) {
                    // if it has, flush the pipe
                    batcher.flushAndWait();
                    // Set the last write time to be something well into the future, so that we don't needlessly,
                    // repeatedly flush the queue
                    lastWriteTime = System.currentTimeMillis() + 900000;
                }
            }
        }, SECONDS_BEFORE_FLUSH * 1000, SECONDS_BEFORE_FLUSH *1000);

        // Set up the metadata to be used for the documents that will be inserted
        // ASSUMPTION: The same metadata will be used for every document to be inserted during the lifetime of this
        // object
        this.metadataHandle = new DocumentMetadataHandle();
        String[] configCollections = configuration.getOutputCollections();

        // Set up list of collections that new docs should be put into
        if (!configCollections[0].equals("null")) {
            metadataHandle.withCollections(configCollections);
        }
        // Set up quality new docs should have
        metadataHandle.setQuality(configuration.getOutputQuality());

        // Set up list of permissions that new docs should be granted
        String[] permissions = configuration.getOutputPermissions();
        for (int i = 0; i < permissions.length - 1; i++) {
            String role = permissions[i];
            String capability = permissions[i + 1];
            switch(capability.toLowerCase()) {
                case "read" :
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.READ);
                    break;
                case "insert" :
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.INSERT);
                    break;
                case "update" :
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.UPDATE);
                    break;
                case "execute" :
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.EXECUTE);
                    break;
                case "node_update" :
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.NODE_UPDATE);
                    break;
                default :
                    System.out.println("No additive permissions assigned");
            }
        }

        // start the batcher job
        this.jt = dmm.startJob(batcher);
    }

    /**
     * getInstance-- used in lieu of a public constructor... enforces singleton pattern
     * @param config -- information describing how the insertion process should work
     * @param connection -- information describing how to connect to MarkLogic
     * @return
     */
    public static MarkLogicInsertionBatcher getInstance(MarkLogicConfiguration config, MarkLogicConnection connection) {
        if (instance == null) {
            instance = new MarkLogicInsertionBatcher(config,connection);
        }
        return instance;
    }

    /**
     * Actually does the work of passing the document on to DMSDK to do its thing
     * @param outURI -- the URI to be used for the document being inserted
     * @param documentStream -- the InputStream containing the document to be inserted... comes from mule
     */
    public void doInsert(String outURI, InputStream documentStream){
        batcher.addAs(outURI, metadataHandle, new InputStreamHandle(documentStream));
        lastWriteTime = System.currentTimeMillis();
        batcher.awaitCompletion();
    }
}
