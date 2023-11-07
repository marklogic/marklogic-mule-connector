package org.mule.extension;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class SearchDocumentsCombinedQueriesTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-combined-queries.xml";
    }

    @Test
    public void noQuery() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-documents-no-query");
        assertTrue(1 < documentDataList.size());
    }

    @Test
    public void combinedXmlQuery() {
        runFlowAndVerifyMessageCount(
            "search-documents-xml-combinedQuery",
            3,
            "3 docs are expected to have 'world' in them");
    }

    @Test
    public void combinedJsonQuery() {
        runFlowAndVerifyMessageCount(
            "search-documents-json-combinedQuery",
            3,
            "3 docs are expected to have 'world' in them");
    }

    @Test
    public void combinedXmlQueryWithNoMatches() {
        runFlowAndVerifyMessageCount(
            "search-documents-xml-combinedQuery-noMatches",
            0,
            "A search term with no matches should return no documents");
    }

    @Test
    public void combinedJsonQueryWithNoMatches() {
        runFlowAndVerifyMessageCount(
            "search-documents-json-combinedQuery-noMatches",
            0,
            "A search term with no matches should return no documents");
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
