package org.mule.extension;

import org.junit.Test;
import com.marklogic.mule.extension.DocumentAttributes;
import org.mule.runtime.api.message.Message;

import static org.junit.Assert.assertEquals;

public class ReadAndWriteDocumentTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "read-and-write-document.xml";
    }

    @Test
    public void readAndWriteDocument() throws Exception {
        Message message = runFlowGetMessage("read-and-write-document");
        String messageString = getPayloadAsString(message);
        assertEquals("The contents of the message should match the contents of the original document",
                "{\"hello\":\"world\"}", messageString);

        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        assertEquals(
                "The written document is expected to be returned, and its URI is based on an expression in the flow operation for writing the document.",
                "/test/metadataSamples/json/hello.json", attributes.getUri());
    }
}
