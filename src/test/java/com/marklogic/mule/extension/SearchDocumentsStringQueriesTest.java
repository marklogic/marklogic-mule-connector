package com.marklogic.mule.extension;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class SearchDocumentsStringQueriesTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-string-queries.xml";
    }

    @Test
    public void noQuery() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-documents-no-query");
        assertTrue(1 < documentDataList.size());
    }

    @Test
    public void noQueryWithCollection() {
        List<DocumentData> collectionDocumentDataList = runFlowAndVerifyMessageCount(
            "search-documents-no-query-with-collection",
            10,
            "For the given collection, exactly 10 documents should be returned.");
        List<DocumentData> noQueryDocumentDataList = runFlowForDocumentDataList("search-documents-no-query");
        assertTrue(collectionDocumentDataList.size() < noQueryDocumentDataList.size());
    }

    @Test
    public void queryWithNoMatches() {
        runFlowAndVerifyMessageCount(
            "search-documents-query-with-no-matches",
            0,
            "A search term with no matches should return no documents");
    }
}
