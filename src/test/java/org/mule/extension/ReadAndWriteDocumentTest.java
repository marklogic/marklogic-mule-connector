package org.mule.extension;

import org.junit.Test;
import com.marklogic.mule.extension.DocumentAttributes;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.message.Message;

import static org.junit.Assert.assertEquals;

public class ReadAndWriteDocumentTest extends MuleArtifactFunctionalTestCase {

    @Override
    protected String getConfigFile() {
        return "read-and-write-document.xml";
    }

    @Test
    public void readAndWriteDocument() throws Exception {
        Message message = flowRunner("read-and-write-document").run().getMessage();

        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        assertEquals(
            "The written document is expected to be returned, and its URI is based on an expression in the flow operation for writing the document.",
            "/test/hello.json", attributes.getUri()
        );

        // TODO Don't know how to access the payload message. The below method, along with other approaches, all run
        // into the same issue, which is a "java.lang.IllegalStateException: Cannot open a new cursor on a closed stream".
//        getPayloadAsString(message);
    }
}
