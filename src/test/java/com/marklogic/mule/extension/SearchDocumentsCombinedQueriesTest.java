package com.marklogic.mule.extension;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchDocumentsCombinedQueriesTest extends AbstractFlowTester {

    @Override
    protected String getFlowTestFile() {
        return "search-documents-combined-queries.xml";
    }

    @Test
    public void noQuery() {
        assertTrue(1 < runFlowForDocumentCount("search-documents-no-query"));
    }

    @Test
    public void combinedXmlQuery() {
        assertEquals("3 docs are expected to have 'world' in them",
            3, runFlowForDocumentCount("search-documents-xml-combinedQuery"));
    }

    @Test
    public void combinedJsonQuery() {
        assertEquals("3 docs are expected to have 'world' in them",
            3, runFlowForDocumentCount("search-documents-json-combinedQuery"));
    }

    @Test
    public void combinedXmlQueryWithNoMatches() {
        assertEquals("A search term with no matches should return no documents",
            0, runFlowForDocumentCount("search-documents-xml-combinedQuery-noMatches"));
    }

    @Test
    public void combinedJsonQueryWithNoMatches() {
        assertEquals("A search term with no matches should return no documents",
            0, runFlowForDocumentCount("search-documents-json-combinedQuery-noMatches"));
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
