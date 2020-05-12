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
package com.marklogic.mule.extension.connector;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
//import org.mule.runtime.api.streaming.object.CursorIterator;
//import org.mule.runtime.core.internal.streaming.object.ManagedCursorIteratorProvider;

public class MarkLogicOperationsTestCase extends MuleArtifactFunctionalTestCase
{
    private static final Logger logger = Logger.getLogger(MarkLogicOperationsTestCase.class.getName());
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final String CURRENT_DATE = new SimpleDateFormat("yyyy-MM-dd").format(new GregorianCalendar().getTime());

    /**
     * Specifies the mule config xml with the flows that are going to be
     * executed in the tests, this file lives in the test resources.
     */
    @Override
    protected String getConfigFile()
    {
        return "test-mule-config.xml";
    }

    @Test
    public void executeRetrieveInfoOperation() throws Exception
    {
        String payloadValue = ((String) flowRunner("retrieveInfoFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());
        assertThat(payloadValue, is("Using Configuration [configId] with Connection id [testConfig-223efe]"));
    }

    @Test
    public void executeImportDocsOperation() throws Exception
    {
        Object isValue = (flowRunner("importDocsFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());
        if (isValue instanceof InputStream) {
            InputStream is = (InputStream) isValue;
            String payloadValue = inputStreamToString(is);
            assertThat(payloadValue, payloadValue.matches('"' + UUID_REGEX + '"'));
        }
    }

    @Test
    public void executeQueryTemporalOperation() throws Exception
    {
        Object payloadValue = (flowRunner("querytemporalFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());
        assertThat(payloadValue, notNullValue());
    }

    @Test
    public void executeExportDocsOperation() throws Exception
    {
        Object payloadValue = (flowRunner("exportDocsFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());
        assertThat(payloadValue, notNullValue());
    }

    @Test
    public void executeGetJobReportOperation() throws Exception
    {
        Object isValue = (flowRunner("getJobReportFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());
        if (isValue instanceof InputStream) {
            InputStream is = (InputStream) isValue;
            String payloadValue = inputStreamToString(is);
            assertThat(payloadValue, containsString("\"importResults\":[{\"jobID\":\""));
            assertThat(payloadValue, containsString("\"jobStartTime\":\"" + CURRENT_DATE + "T"));
            assertThat(payloadValue, containsString("\"jobEndTime\":\"" + CURRENT_DATE + "T"));
            assertThat(payloadValue, containsString("\"jobReportTime\":\"" + CURRENT_DATE + "T"));
        }
    }

    @Test
    public void executeDeleteDocsStructuredQueryFlow() throws Exception
    {
        Object isValue = (flowRunner("deleteDocsStructuredQueryFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());
        if (isValue instanceof InputStream) {
            InputStream is = (InputStream) isValue;
            String payloadValue = inputStreamToString(is);
            assertThat(payloadValue, containsString(" document(s) deleted"));
        }
    }

    @Test
    public void executeQueryDocsTransformFlow() throws Exception
    {
        Object payloadValue = (flowRunner("queryDocsTransformFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, notNullValue());

        // At some point in the future this test should actually test the results.  For whatever reason the following lines
        // throw java.lang.NoClassDefFoundError: org/mule/runtime/core/internal/streaming/object/ManagedCursorIteratorProvider
        // even though there is a jar file in surefire.test.class.path that contains that class.
        //CursorIterator cursor = ((ManagedCursorIteratorProvider) payloadValue).openCursor();
        //while(cursor.hasNext()) {
        //    assertThat(cursor.next().toString(), containsString("\"transformer\": \"transformTestEgress\""));
        //}
        //isA(ManagedCursorIteratorProvider.class);
    }
    
    @Test
    public void executeQueryDocsMaxResults() throws Exception
    {
        Object payloadValue = (flowRunner("queryDocsMaxResultsFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        //logger.info("Returned from flow - " + payloadValue);
        //assertThat(payloadValue, containsString("mlw: 5"));
        assertThat(payloadValue, notNullValue());
    }
    
    private String inputStreamToString(InputStream inputStream) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        String out = null;
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            out = result.toString("UTF-8");
        } catch(IOException ex) {
            logger.info(String.format("Exception was thrown during MarkLogicOperationsTestCase. Error was: %s", ex));
        }
        return out;    
    }
}