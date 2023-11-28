package com.marklogic.mule.extension;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchDocumentsStructuredQueriesTest extends AbstractFlowTester {

    @Override
    protected String getFlowTestFile() {
        return "search-documents-structured-queries.xml";
    }

    @Test
    public void noQuery() {
        assertTrue(1 < runFlowForDocumentCount("search-documents-no-query"));
    }

    @Test
    public void structuredXmlQuery() {
        assertEquals("3 docs are expected to have 'world' in them",
            3, runFlowForDocumentCount("search-documents-xml-structuredQuery"));
    }

    @Test
    public void structuredJsonQuery() {
        assertEquals("3 docs are expected to have 'world' in them",
            3, runFlowForDocumentCount("search-documents-json-structuredQuery"));
    }

    @Test
    public void structuredJsonQueryWithNoMatches() {
        assertEquals("A search term with no matches should return no documents",
            0, runFlowForDocumentCount("search-documents-structuredQuery-noMatches"));
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
