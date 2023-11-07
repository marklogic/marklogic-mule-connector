package org.mule.extension;

import org.junit.Test;
import com.marklogic.mule.extension.DocumentAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;

import static org.junit.Assert.assertEquals;

public class ReadAndWriteDocumentTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "read-and-write-document.xml";
    }

    @Test
    public void readAndWriteDocument() throws Exception {
        DocumentData documentData = runFlowGetDocumentData("read-and-write-document");
        assertEquals("application/json; charset=UTF-8", documentData.getMimeType());
        assertEquals("The contents of the message should match the contents of the original document",
            JSON_HELLO_WORLD, documentData.getContents());
        assertEquals(
                "The written document is expected to be returned, and its URI is based on an expression in the flow operation for writing the document.",
                "/test/metadataSamples/json/hello.json", documentData.getAttributes().getUri());
    }
}
