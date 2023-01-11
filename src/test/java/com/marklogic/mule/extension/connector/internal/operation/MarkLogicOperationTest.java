/**
 * MarkLogic Mule Connector
 *
 * Copyright � 2021 MarkLogic Corporation.
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.marklogic.mule.extension.connector.api.connection.AuthenticationType;
import com.marklogic.mule.extension.connector.api.connection.MarkLogicConnectionType;
import com.marklogic.mule.extension.connector.api.operation.MarkLogicQueryFormat;
import com.marklogic.mule.extension.connector.api.operation.MarkLogicQueryStrategy;
import com.marklogic.mule.extension.connector.internal.config.MarkLogicConfiguration;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author credding
 */
public class MarkLogicOperationTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicOperations.class);
    
    private static final String CONNECTION_ID = "test-connection-id";
    private static final int PORT = 8007;
    private static final String PROPERTIES_FILE = "src/test/resources/automation-credentials.properties";
    private static final String CONFIG_ID = "testConfig-223efe";
    private static final int THREAD_COUNT = 1;
    private static final int BATCH_SIZE = 10;
    private static final Object TRANSFORM = "ml:sjsInputFlow";
    private static final Object TRANSFORM_PARAMS = "entity-name,MyEntity,flow-name,loadMyEntity";
    private static final int FLUSH_SECONDS = 2;
    private static final String JOB_NAME = "myTestJobName";
    
    MarkLogicConfiguration configuration;
    MarkLogicConnection connection;
    MarkLogicOperations operation;
    Properties prop;
    
//    @BeforeClass
//    public static void setUpClass() {
//    }
//    
//    @AfterClass
//    public static void tearDownClass() {
//    }

    @Before
    public void setUp() {
        try (InputStream input = new FileInputStream(PROPERTIES_FILE)) {
            prop = new Properties();
            prop.load(input);
        } catch (IOException ex) {
            LOGGER.error("Could not retrieve automation-credentials.properties", ex.fillInStackTrace());
        }
        configuration = new MarkLogicConfiguration();
        configuration.setConfigId(CONFIG_ID);
        configuration.setThreadCount(THREAD_COUNT);
        configuration.setBatchSize(BATCH_SIZE);
        configuration.setServerTransform(TRANSFORM.toString());
        configuration.setServerTransformParams(TRANSFORM_PARAMS.toString());
        configuration.setSecondsBeforeFlush(FLUSH_SECONDS);
        configuration.setJobName(JOB_NAME);
        connection = new MarkLogicConnection(prop.getProperty("config.hostName"), PORT, null, prop.getProperty("config.username"), prop.getProperty("config.password"), AuthenticationType.digest, MarkLogicConnectionType.DIRECT, null, null, CONNECTION_ID);
        operation = new MarkLogicOperations();
    }
   
    @After
    public void tearDown() {
        connection.invalidate();
    }

    @Test
    public void testDeleteDocs() throws IOException {
        String queryString = "{ \"query\": { \"queries\": [{ \"document-query\": {\"uri\": [ \"/mulesoft/delete-junit.json\" ] } }] } }";
        String optionsName = "default";
        MarkLogicQueryStrategy queryStrategy = MarkLogicQueryStrategy.RawStructuredQueryDefinition;
        boolean useConsistentSnapshot = false;
        MarkLogicQueryFormat fmt = MarkLogicQueryFormat.JSON;
        connection.connect();
        InputStream is = operation.deleteDocs(configuration, connection, queryString, optionsName, queryStrategy, useConsistentSnapshot, fmt);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonMap = objectMapper.readTree(is);
            JsonNode delresult = jsonMap.get("deletionResult");
            String result = delresult.textValue();
            assertEquals("0 document(s) deleted", result);
        } catch(IOException ex) {
            LOGGER.error(String.format("Exception was thrown during deleteDocs operation. Error was: %s", ex.getMessage()), ex);
        } finally {
            is.close();
        }        
    }
    
    @Test
    public void testExportDocs() throws IOException {
        Long resultCount = new Long("3");
        boolean useConsistentSnapshot = true;
        String optionsName = "employeeTest";
        String queryString = "{\"query\": {\"queries\": [{\"range-constraint-query\": {\"constraint-name\": \"department\", \"value\": \"Human Resources\"}}]}}";
        String serverTransform = "transformTestEgress";
        String serverTransformParams = "text,hello";
        connection.connect();
        PagingProvider<MarkLogicConnection, Object> export = operation.exportDocs(configuration, queryString, optionsName, MarkLogicQueryStrategy.RawStructuredQueryDefinition, MarkLogicQueryFormat.JSON, resultCount, useConsistentSnapshot, serverTransform, serverTransformParams);
        assertEquals("Optional.empty", export.getTotalResults(connection).toString());
    }
}