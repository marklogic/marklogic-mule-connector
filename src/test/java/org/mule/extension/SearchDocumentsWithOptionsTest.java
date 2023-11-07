package org.mule.extension;

import org.junit.Test;
import org.mule.runtime.api.message.Message;

import java.util.List;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class SearchDocumentsWithOptionsTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-with-options.xml";
    }

    @Test
    public void searchDocuments_DefaultMetadata() {
        Message outerMessage = runFlowGetMessage("search-documents-with-maxResults");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(5, innerMessages.size());
    }

    @Test
    public void searchDocuments_SearchTermWithOptions() throws Exception {
        Message outerMessage = runFlowGetMessage("search-documents-search-term-with-options");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(1, innerMessages.size());
        String messageString = getPayloadAsString(innerMessages.get(0));
        assertEquals("The contents of the message should match the contents of the test3 document.",
            "{\"test\":3}", messageString);
    }

    @Test
    public void searchDocuments_SearchTermWithoutOptions() {
        Message outerMessage = runFlowGetMessage("search-documents-search-term-without-options");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals("Without search options, the search string should not have any matches",
            0, innerMessages.size());
    }

    @Test
    public void searchDocuments_withinDirectory() {
        Message outerMessage = runFlowGetMessage("search-documents-within-directory");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(10, innerMessages.size());
    }

    @Test
    public void searchDocuments_withTransform() throws Exception {
        Message outerMessage = runFlowGetMessage("search-documents-with-transform");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(1, innerMessages.size());
        String messageString = getPayloadAsString(innerMessages.get(0));
        assertEquals("The contents of the message should match the contents of the test3 document.",
            "{\"Hola\":\"Mundo\"}", messageString);
    }


}
