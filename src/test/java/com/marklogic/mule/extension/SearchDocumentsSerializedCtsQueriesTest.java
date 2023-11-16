package com.marklogic.mule.extension;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class SearchDocumentsSerializedCtsQueriesTest extends AbstractFlowTester {

    @Override
    protected String getFlowTestFile() {
        return "search-documents-serialized-cts-queries.xml";
    }

    @Test
    public void noQuery() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-documents-no-query");
        assertTrue(1 < documentDataList.size());
    }

    @Test
    public void serializedXmlQuery() {
        runFlowAndVerifyMessageCount(
            "search-documents-xml-serializedCtsQuery",
            3,
            "3 docs are expected to have 'world' in them");
    }

    @Test
    public void serializedJsonQuery() {
        runFlowAndVerifyMessageCount(
            "search-documents-json-serializedCtsQuery",
            3,
            "3 docs are expected to have 'world' in them");
    }

    @Test
    public void serializedJsonQueryWithNoMatches() {
        runFlowAndVerifyMessageCount(
            "search-documents-serializedCtsQuery-noMatches",
            0,
            "A search term with no matches should return no documents");
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
