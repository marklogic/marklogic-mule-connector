package org.mule.extension;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.message.Message;

public abstract class AbstractFlowTester extends MuleArtifactFunctionalTestCase {

    // TODO - Change to catch and rethrow a RuntimeException,
    //  thus preventing every test method from having to declare "throws Exception"
    Message runFlowGetMessage(String flowName) throws Exception {
        Event event = flowRunner(flowName).keepStreamsOpen().run();
        return event.getMessage();
    }
}
