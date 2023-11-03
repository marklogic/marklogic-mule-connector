package org.mule.extension;

import org.junit.Test;
import org.mule.runtime.api.message.Message;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class SearchDocumentsSerializedCtsQueriesTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-serialized-cts-queries.xml";
    }

    @Test
    public void noQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-no-query");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertTrue(1 < innerMessages.size());
    }

    @Test
    public void serializedXmlQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-xml-serializedCtsQuery");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals("3 docs are expected to have 'world' in them", 3, innerMessages.size());
    }

    @Test
    public void serializedJsonQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-json-serializedCtsQuery");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals("3 docs are expected to have 'world' in them", 3, innerMessages.size());
    }

    @Test
    public void serializedJsonQueryWithNoMatches() {
        Message outerMessage = runFlowGetMessage("search-documents-serializedCtsQuery-noMatches");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(0, innerMessages.size());
    }

    @Test(expected = RuntimeException.class)
    public void serializedJsonQueryWithBadJson() {
        runFlowGetMessage("search-documents-serializedCtsQuery-badJson");
    }

    @Test(expected = RuntimeException.class)
    public void serializedXmlQueryWithBadXml() {
        runFlowGetMessage("search-documents-serializedCtsQuery-badXml");
    }
}
