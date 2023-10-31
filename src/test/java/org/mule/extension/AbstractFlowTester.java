package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.message.Message;

import static org.junit.Assert.assertEquals;

public abstract class AbstractFlowTester extends MuleArtifactFunctionalTestCase {

    Message runFlowGetMessage(String flowName) {
        try {
            return flowRunner(flowName).keepStreamsOpen().run().getMessage();
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
