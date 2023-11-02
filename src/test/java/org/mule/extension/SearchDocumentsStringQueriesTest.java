package org.mule.extension;

import org.junit.Test;
import org.mule.runtime.api.message.Message;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class SearchDocumentsStringQueriesTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-string-queries.xml";
    }

    @Test
    public void noQuery() {
        Message outerMessage = runFlowGetMessage("search-documents-no-query");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertTrue(1 < innerMessages.size());
    }

    @Test
    public void noQueryWithCollection() {
        Message collectionOuterMessage = runFlowGetMessage("search-documents-no-query-with-collection");
        List<Message> collectionInnerMessages = (List<Message>) collectionOuterMessage.getPayload().getValue();
        assertEquals(10, collectionInnerMessages.size());
        Message outerMessage = runFlowGetMessage("search-documents-no-query");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertTrue(collectionInnerMessages.size() < innerMessages.size());
    }

    @Test
    public void queryWithNoMatches() {
        Message outerMessage = runFlowGetMessage("search-documents-query-with-no-matches");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(0, innerMessages.size());
    }
}
