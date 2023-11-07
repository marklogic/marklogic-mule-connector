package org.mule.extension;

import org.junit.Test;
import org.mule.runtime.api.message.Message;

import static org.junit.Assert.*;

public class EvalJavaScriptTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "eval-javascript.xml";
    }

    @Test
    public void simplestEvalTest() {
        Message message = runFlowGetMessage("simple-math");
        assertEquals("3", message.getPayload().getValue());
    }

    @Test
    public void writeAndDeleteDocument() {
        DocumentData data = runFlowGetDocumentData("writeAndDeleteDocument");
        assertEquals(JSON_HELLO_WORLD, data.getContents());

        runFlowGetMessage("deleteDocument");

        try {
            runFlowGetMessage("readDocumentThatShouldHaveBeenDeleted");
            fail("This message should cause the exception because the document should not exist any longer.");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Local message: Could not read non-existent document."));
        }
    }
}
