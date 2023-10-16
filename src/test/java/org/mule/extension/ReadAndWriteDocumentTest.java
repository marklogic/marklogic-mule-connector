package org.mule.extension;

import org.junit.Test;
import com.marklogic.mule.extension.DocumentAttributes;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.message.Message;

import static org.junit.Assert.assertEquals;

public class ReadAndWriteDocumentTest extends MuleArtifactFunctionalTestCase {

    @Override
    protected String getConfigFile() {
        return "read-and-write-document.xml";
    }

    @Test
    public void readAndWriteDocument() throws Exception {
        Event event = flowRunner("read-and-write-document").keepStreamsOpen().run();
        Message message = event.getMessage();
        String messageString = getPayloadAsString(message);
        assertEquals("The contents of the message should match the contents of the original document",
                "{\"hello\":\"world\"}", messageString);

        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        assertEquals(
                "The written document is expected to be returned, and its URI is based on an expression in the flow operation for writing the document.",
                "/test/hello.json", attributes.getUri());
    }
}
