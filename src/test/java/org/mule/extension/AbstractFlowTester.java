package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.message.Message;

import static org.junit.Assert.assertEquals;

public abstract class AbstractFlowTester extends MuleArtifactFunctionalTestCase {

    Message runFlowGetMessage(String flowName) throws RuntimeException {
        try {
            Event event = flowRunner(flowName).keepStreamsOpen().run();
            return event.getMessage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    DocumentData runFlowGetDocumentData(String flowName) throws RuntimeException {
        try {
            Event event = flowRunner(flowName).keepStreamsOpen().run();
            return new DocumentData(
                getPayloadAsString(event.getMessage()),
                (DocumentAttributes) event.getMessage().getAttributes().getValue()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void verifyContents(String expectedContents, String messageContents) {
        assertEquals("The contents of the message should match the contents of the original document",
            expectedContents, messageContents);
    }

}
