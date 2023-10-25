package org.mule.extension;

import org.junit.Test;
import org.mule.runtime.api.message.Message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * The parent class does some stuff with the classloader that prevents us from constructing a DatabaseClient.
 * It may be possible to work around that by configuring the "applicationSharedRuntimeLibs" field in the
 * "ArtifactClassLoaderRunnerConfig" annotation, but doing so appears to require listing every single dependency of
 * the Java Client. So for now, the approach to testing what's written to MarkLogic will be to run a flow to read
 * what was written.
 */
public class WriteDocumentTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "write-document.xml";
    }

    @Test
    public void test() throws Exception {
        Message message = runFlowGetMessage("writeDocument");
        assertNull("Not expecting any payload value to be returned", message.getPayload().getValue());

        message = flowRunner("readDocument").run().getMessage();
        assertEquals("this is text", message.getPayload().getValue().toString());
    }
}
