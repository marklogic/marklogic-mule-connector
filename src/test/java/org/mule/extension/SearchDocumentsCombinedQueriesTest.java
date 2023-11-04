package org.mule.extension;

import org.junit.Test;
import org.mule.runtime.api.message.Message;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class SearchDocumentsCombinedQueriesTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-combined-queries.xml";
    }

    @Test
    public void noQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-no-query");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertTrue(1 < innerMessages.size());
    }

    @Test
    public void combinedXmlQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-xml-combinedQuery");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals("3 docs are expected to have 'world' in them", 3, innerMessages.size());
    }

    @Test
    public void combinedJsonQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-json-combinedQuery");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals("3 docs are expected to have 'world' in them", 3, innerMessages.size());
    }

    @Test
    public void combinedXmlQueryWithNoMatches() {
        Message outerMessage = runFlowGetMessage("search-documents-xml-combinedQuery-noMatches");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(0, innerMessages.size());
    }

    @Test
    public void combinedJsonQueryWithNoMatches() {
        Message outerMessage = runFlowGetMessage("search-documents-json-combinedQuery-noMatches");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(0, innerMessages.size());
    }

    @Test(expected = RuntimeException.class)
    public void combinedJsonQueryWithBadXml() {
        runFlowGetMessage("search-documents-xml-combinedQuery-badXml");
    }

    @Test(expected = RuntimeException.class)
    public void combinedJsonQueryWithBadJson() {
        runFlowGetMessage("search-documents-json-combinedQuery-badJson");
    }
}
