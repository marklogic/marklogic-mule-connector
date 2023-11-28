package com.marklogic.mule.extension;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchDocumentsStringQueriesTest extends AbstractFlowTester {

    @Override
    protected String getFlowTestFile() {
        return "search-documents-string-queries.xml";
    }

    @Test
    public void noQuery() {
        assertTrue("Expecting all documents to be returned, which is far greater than 1",
            1 < runFlowForDocumentCount("search-documents-no-query"));
    }

    @Test
    public void noQueryWithCollection() {
        List<DocumentData> collectionDocumentDataList = runFlowAndVerifyMessageCount(
            "search-documents-no-query-with-collection",
            10,
            "For the given collection, exactly 10 documents should be returned.");
        assertTrue(collectionDocumentDataList.size() < runFlowForDocumentCount("search-documents-no-query"));
    }

    @Test
    public void queryWithNoMatches() {
        assertEquals("A search term with no matches should return no documents",
            0, runFlowForDocumentCount("search-documents-query-with-no-matches"));
    }
}
