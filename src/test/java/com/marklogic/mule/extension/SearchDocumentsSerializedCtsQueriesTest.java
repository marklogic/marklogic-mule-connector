package com.marklogic.mule.extension;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class SearchDocumentsSerializedCtsQueriesTest extends AbstractFlowTester {

    @Override
    protected String getFlowTestFile() {
        return "search-documents-serialized-cts-queries.xml";
    }

    @Test
    public void noQuery() {
        assertTrue(1 < runFlowForDocumentCount("search-documents-no-query"));
    }

    @Test
    public void serializedXmlQuery() {
        assertEquals("3 docs are expected to have 'world' in them",
            3, runFlowForDocumentCount("search-documents-xml-serializedCtsQuery"));
    }

    @Test
    public void serializedJsonQuery() {
        assertEquals("3 docs are expected to have 'world' in them",
            3, runFlowForDocumentCount("search-documents-json-serializedCtsQuery"));
    }

    @Test
    public void serializedJsonQueryWithNoMatches() {
        assertEquals("A search term with no matches should return no documents",
            0, runFlowForDocumentCount("search-documents-serializedCtsQuery-noMatches"));
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
