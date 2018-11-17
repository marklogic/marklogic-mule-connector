/**
 * MarkLogic Connector
 *
 * Copyright © 2018 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
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

import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

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
    @Config
        MarkLogicConfiguration configuration, 
    @Connection
        MarkLogicConnection connection, 
    @DisplayName("Document payload")
    @Summary("The content of the input files to be used for ingestion into MarkLogic.")
    @Example("#[payload]")
        InputStream docPayloads, 
    @Optional(defaultValue="null")
    @Summary("A comma-separated list of the collections to which persisted documents will belong after successful ingestion.")
    @Example("mulesoft-test")
        String outputCollections,
    @Optional(defaultValue="rest-reader,read,rest-writer,update")
    @Summary("A comma-separated list of roles and capabilities to which persisted documents will possess after successful ingestion.")
    @Example("myRole,read,myRole,update")
        String outputPermissions,
    @Optional(defaultValue="1")
    @Summary("A number indicating the quality of the persisted documents")
    @Example("1")
        String outputQuality,
    @Optional(defaultValue="/")
    @Summary("The URI prefix, used to prepend and concatenate basenameUri.")
    @Example("/mulesoft/")
        String outputUriPrefix,
    @Optional(defaultValue=".json")
    @Summary("The URI suffix, used to append and concatenate basenameUri.")
    @Example(".json")
        String outputUriSuffix,
    @DisplayName("Generate output URI basename?")
    @Optional(defaultValue="true")
    @Summary("Creates a document basename based on a UUID, to be combined with the outputUriPrefix and outputUriSuffix. Use this if you can't programmatically assign a basename from an identifier in the document. Otherwise use basenameUri.")
    @Example("false")
        String generateOutputUriBasename,
    @DisplayName("Output document basename")
    @Optional(defaultValue="null")
    @Summary("The file basename to be used for persistence in MarkLogic, usually derived a value from within the payload. Different than the UUID produced from generateOutputUriBasename.")
    @Example("employee123.json")
        String basenameUri) {

        // Get a handle to the Insertion batch manager
        MarkLogicInsertionBatcher batcher = MarkLogicInsertionBatcher.getInstance(configuration, connection, outputCollections, outputPermissions, outputQuality, outputUriPrefix, outputUriSuffix, generateOutputUriBasename, basenameUri, configuration.getJobName());

        // Determine output URI
        // If the config tells us to generate a new UUID, do that
        if (Boolean.valueOf(generateOutputUriBasename) == Boolean.TRUE) {
            basenameUri = UUID.randomUUID().toString();
        // Also, if the basenameURI is blank for whatever reason, use a new UUID
        } else if ((basenameUri == null) || (basenameUri.equals("null")) || (basenameUri.length() < 1)) {
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
