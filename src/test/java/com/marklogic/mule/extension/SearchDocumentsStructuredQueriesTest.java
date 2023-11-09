package com.marklogic.mule.extension;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class SearchDocumentsStructuredQueriesTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-structured-queries.xml";
    }

    @Test
    public void noQuery() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-documents-no-query");
        assertTrue(1 < documentDataList.size());
    }

    @Test
    public void structuredXmlQuery() {
        runFlowAndVerifyMessageCount(
            "search-documents-xml-structuredQuery",
            3,
            "3 docs are expected to have 'world' in them");
    }

    @Test
    public void structuredJsonQuery() {
        runFlowAndVerifyMessageCount(
            "search-documents-json-structuredQuery",
            3,
            "3 docs are expected to have 'world' in them");
    }

    @Test
    public void structuredJsonQueryWithNoMatches() {
        runFlowAndVerifyMessageCount(
            "search-documents-structuredQuery-noMatches",
            0,
            "A search term with no matches should return no documents");
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
