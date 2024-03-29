/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2024 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.mule.extension;

import com.marklogic.mule.connector.api.types.DocumentAttributes;
import org.junit.Before;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.streaming.object.CursorIteratorProvider;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public abstract class AbstractFlowTester extends MuleArtifactFunctionalTestCase {

    protected final static String JSON_HELLO_WORLD = "{\"hello\":\"world\"}";
    protected final static String TEXT_HELLO_WORLD = "Hello, World!";
    protected final static String XML_HELLO_WORLD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Hello>World</Hello>";

    protected abstract String getFlowTestFile();

    @Before
    public void prepareDatabase() throws Exception {
        flowRunner("prepare-database").run();
    }

    @Override
    final protected String[] getConfigFiles() {
        return new String[]{"prepare-database-flow.xml", getFlowTestFile()};
    }

    Message runFlowGetMessage(String flowName) {
        try {
            return flowRunner(flowName).keepStreamsOpen().run().getMessage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    List<DocumentData> runFlowAndVerifyMessageCount(String flowName, long expectedCount, String assertionMessage) {
        List<DocumentData> resultSet = runFlowForDocumentDataList(flowName);
        assertEquals(assertionMessage, expectedCount, resultSet.size());
        return resultSet;
    }

    DocumentData runFlowGetDocumentData(String flowName) {
        return runFlowForDocumentDataList(flowName).get(0);
    }

    List<DocumentData> runFlowForDocumentDataList(String flowName) {
        CursorIteratorProvider prov = (CursorIteratorProvider) runFlowGetMessage(flowName).getPayload().getValue();
        List<DocumentData> result = new ArrayList<>();
        prov.openCursor().forEachRemaining(message -> result.add(toDocumentData((Message) message)));
        return result;
    }

    int runFlowForDocumentCount(String flowName) {
        return runFlowForDocumentDataList(flowName).size();
    }

    private DocumentData toDocumentData(Message message) {
        String content;
        try {
            content = getPayloadAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get payload as string", e);
        }
        return new DocumentData(
            content,
            (DocumentAttributes) message.getAttributes().getValue(),
            message.getPayload().getDataType().getMediaType().toRfcString()
        );

    }
}
