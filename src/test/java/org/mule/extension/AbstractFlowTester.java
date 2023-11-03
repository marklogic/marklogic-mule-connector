package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;
import org.mule.functional.api.flow.FlowRunner;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.message.Message;

import static org.junit.Assert.assertEquals;

public abstract class AbstractFlowTester extends MuleArtifactFunctionalTestCase {

    // Use this to delete all non-test-data documents (i.e. anything not loaded by the test-app) at the start
    // of a test.
    protected final static String DELETE_QUERY = "declareUpdate();\n" +
        "for (var uri of cts.uris(null, null, cts.notQuery(cts.collectionQuery('test-data')))) {\n" +
        "  xdmp.documentDelete(uri);\n" +
        "}";

    Message runFlowGetMessage(String flowName) {
        try {
            return flowRunner(flowName)
                .withVariable("DELETE_QUERY", DELETE_QUERY)
                .keepStreamsOpen().run().getMessage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    DocumentData runFlowGetDocumentData(String flowName) {
        Message message = runFlowGetMessage(flowName);
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

    void verifyContents(String expectedContents, String messageContents) {
        assertEquals("The contents of the message should match the contents of the original document",
            expectedContents, messageContents);
    }

}
