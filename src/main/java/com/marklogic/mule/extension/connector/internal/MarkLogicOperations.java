package com.marklogic.mule.extension.connector.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.marklogic.client.io.*;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class is a container for operations, every public method in this class will be taken as an extension operation. */
public class MarkLogicOperations {

  private final Logger LOGGER = LoggerFactory.getLogger(MarkLogicOperations.class);

  // Loading files into MarkLogic asynchronously
  @MediaType(value = ANY, strict = false)
  public String importDocs(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection) {
      
        DocumentMetadataHandle metah = new DocumentMetadataHandle();

        // Collections and permissions.  Permissions are additive to the rest-reader:read and rest-writer:update.
        metah.withCollections("mulesoft-dmsdk-test-clay");
        metah.getPermissions().add("xpl-content-read", DocumentMetadataHandle.Capability.READ);
        metah.getPermissions().add("xpl-content-write", DocumentMetadataHandle.Capability.UPDATE);
        
        // create and configure the job
        DatabaseClient myClient = connection.getClient();
        DataMovementManager dmm = myClient.newDataMovementManager();
        WriteBatcher batcher = dmm.newWriteBatcher();
        batcher.withBatchSize(5)
        .withThreadCount(3)
        .onBatchSuccess(batch-> {
            System.out.println(batch.getTimestamp().getTime() + " documents written: " + batch.getJobWritesSoFar());
        })
        .onBatchFailure((batch,throwable) -> {
            throwable.printStackTrace();
        });
        
        // start the job and feed input to the batcher
        dmm.startJob(batcher);
        try {
            batcher.add("/mulesoft/doc1.txt", metah, new StringHandle("doc1 contents"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start any partial batches waiting for more input, then wait
        // for all batches to complete. This call will block.
        batcher.flushAndWait();
        dmm.stopJob(batcher);
        return "Success";
  }
  
  /* Example of an operation that uses the configuration and a connection instance to perform some action. */
  @MediaType(value = ANY, strict = false)
  public String retrieveInfo(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection) {
    return "Using Configuration [" + configuration.getConfigId() + "] with Connection id [" + connection.getId() + "]";
  }

  /* Example of a simple operation that receives a string parameter and returns a new string message that will be set on the payload. */
  /*@MediaType(value = ANY, strict = false)
  public String sayHi(String person) {
    return buildHelloMessage(person);
  }*/

  /* Private Methods are not exposed as operations */
  /*private String buildHelloMessage(String person) {
    return "Hello " + person + "!!!";
  }*/

}
