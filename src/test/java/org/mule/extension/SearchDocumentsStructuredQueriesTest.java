package org.mule.extension;

import org.junit.Test;
import org.mule.runtime.api.message.Message;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class SearchDocumentsStructuredQueriesTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-structured-queries.xml";
    }

    @Test
    public void noQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-no-query");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertTrue(1 < innerMessages.size());
    }

    @Test
    public void structuredXmlQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-xml-structuredQuery");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(2, innerMessages.size());
    }

    @Test
    public void structuredJsonQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-json-structuredQuery");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(2, innerMessages.size());
    }

    @Test
    public void structuredJsonQueryWithNoMatches() {
        Message outerMessage = runFlowGetMessage("search-documents-structuredQuery-noMatches");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(0, innerMessages.size());
    }

    @Test(expected = RuntimeException.class)
    public void structuredJsonQueryWithBadJson() {
        runFlowGetMessage("search-documents-structuredQuery-badJson");
    }

    @Test(expected = RuntimeException.class)
    public void structuredJsonQueryWithBadXml() {
        runFlowGetMessage("search-documents-structuredQuery-badXml");
    }
}
