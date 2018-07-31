package com.marklogic.mule.extension.connector.internal;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

import com.marklogic.client.io.*;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.datamovement.DataMovementManager;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class is a container for operations, every public method in this class will be taken as an extension operation. */
public class MarkLogicOperations {

  private final Logger logger = LoggerFactory.getLogger(MarkLogicOperations.class);

  // Loading files into MarkLogic asynchronously byte[] docPayload
  @MediaType(value = ANY, strict = false)
  public String importDocs(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection, InputStream docPayloads, String basenameUri) {
        
        /* Grab the input docPayload Object array that Mule Batch sends and convert it to a String array */
        InputStreamHandle handle = new InputStreamHandle(docPayloads);
        //String payload = docPayload;
        //String stringArray[] = docPayloads.stream().toArray(String[]::new);
        //String stringArray[] = docPayloads;
        //System.out.println(payload);
        //System.out.println(Arrays.toString(docPayloads));
        System.out.println(handle.toString());
        
        DocumentMetadataHandle metah = new DocumentMetadataHandle();

        // Collections, quality, and permissions.  
        // Permissions are additive to the rest-reader,read and rest-writer,update.
        metah.withCollections(configuration.getOutputCollections());
        metah.setQuality(configuration.getOutputQuality());
        String[] permissions = configuration.getOutputPermissions();
        for( int i = 0; i < permissions.length - 1; i++) {
            String role = permissions[i];
            String capability = permissions[i + 1];
            switch(capability.toLowerCase()) {
                case "read" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.READ);
                    break;
                case "insert" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.INSERT);
                    break;
                case "update" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.UPDATE);
                    break;
                case "execute" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.EXECUTE);
                    break;
                case "node_update" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.NODE_UPDATE);
                    break;
                default :
                    System.out.println("No additive permissions assigned");
            }
        }
        
        // determine output URI
        String outURI;
        if (basenameUri.length() > 0 && configuration.getGenerateOutputUriBasename() == Boolean.FALSE) {
            outURI = configuration.getOutputPrefix() + basenameUri + configuration.getOutputSuffix();
        } else {
            String uuid = UUID.randomUUID().toString();
            outURI = configuration.getOutputPrefix() + uuid + configuration.getOutputSuffix();
        }
        
        // create and configure the job
        DatabaseClient myClient = connection.getClient();
        DataMovementManager dmm = myClient.newDataMovementManager();
        WriteBatcher batcher = dmm.newWriteBatcher();
        batcher.withBatchSize(configuration.getBatchSize())
        .withThreadCount(configuration.getThreadCount())
        .onBatchSuccess(batch-> {
            System.out.println(batch.getTimestamp().getTime() + " documents written: " + batch.getJobWritesSoFar());
        })
        .onBatchFailure((batch,throwable) -> {
            throwable.printStackTrace();
        });
        
        // start the job and feed input to the batcher
        dmm.startJob(batcher);
        try {
            batcher.add(outURI, metah, handle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*for (int i = 0; i < stringArray.length; i++) {
            try {
                //batcher.add(outURI, metah, new StringHandle(payload));
                batcher.add(outURI, metah, new StringHandle(stringArray[i]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        
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
