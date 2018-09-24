package com.marklogic.mule.extension.connector.internal;

import java.io.InputStream;

import java.util.*;

import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.io.*;
import com.marklogic.client.datamovement.ExportListener;
import com.marklogic.client.ext.datamovement.job.AbstractQueryBatcherJob;
import com.marklogic.client.ext.datamovement.job.ExportToFileJob;
import com.marklogic.client.ext.datamovement.job.SimpleQueryBatcherJob;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class is a container for operations, every public method in this class will be taken as an extension operation. */
public class MarkLogicOperations
{

    private final Logger logger = LoggerFactory.getLogger(MarkLogicOperations.class);
    private static final String OUTPUT_URI_TEMPLATE = "%s%s%s"; // URI Prefix + basenameUri + URI Suffix

    private ObjectMapper jsonFactory = new ObjectMapper();

    // Loading files into MarkLogic asynchronously InputStream docPayload
  @MediaType(value = APPLICATION_JSON, strict = true)
  public String importDocs(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection, InputStream docPayloads, String basenameUri, String jobName) {

        // Get a handle to the Insertion batch manager
        MarkLogicInsertionBatcher batcher = MarkLogicInsertionBatcher.getInstance(configuration,connection,jobName);

        // Determine output URI
        // If the config tells us to generate a new UUID, do that
        if (configuration.getGenerateOutputUriBasename() == Boolean.TRUE) {
            basenameUri = UUID.randomUUID().toString();
        // Also, if the basenameURI is blank for whatever reason, use a new UUID
        } else if ((basenameUri == null) || (basenameUri.length() < 1)) {
            basenameUri = UUID.randomUUID().toString();
        }

        // Assemble the output URI components
        String outURI = String.format(OUTPUT_URI_TEMPLATE, configuration.getOutputPrefix(),basenameUri,configuration.getOutputSuffix());

        // Actually do the insert and return the result
        return batcher.doInsert(outURI,docPayloads);
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
			"jobName": "test-import"
		}
	],
	"exportResults": []
}
   */
  @MediaType(value = APPLICATION_JSON, strict = true)
  public String getJobReport() {
      ObjectNode rootObj = jsonFactory.createObjectNode();
      ArrayNode imports = jsonFactory.createArrayNode();
      rootObj.set("importResults", imports);
      ArrayNode exports = jsonFactory.createArrayNode();
      rootObj.set("exportResults", exports);
      MarkLogicInsertionBatcher insertionBatcher = MarkLogicInsertionBatcher.getInstance();
      if (insertionBatcher != null) {
          imports.add(insertionBatcher.createJsonJobReport(jsonFactory));
      }

      // Add support for query jobReport here!
      String result = rootObj.toString();

      // System.out.println("RESULT: " + result);
      // Add support for query result report here!
      return result;

  }
    /* Example of an operation that uses the configuration and a connection instance to perform some action. */
    @MediaType(value = ANY, strict = false)
    public String retrieveInfo(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection)
    {
        return "Using Configuration [" + configuration.getConfigId() + "] with Connection id [" + connection.getId() + "]";
    }
}
