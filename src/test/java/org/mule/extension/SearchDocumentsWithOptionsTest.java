package org.mule.extension;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchDocumentsWithOptionsTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-with-options.xml";
    }

    @Test
    public void searchDocuments_DefaultMetadata() {
        runFlowAndVerifyMessageCount(
            "search-documents-with-maxResults",
            5,
            "With maxResults set, only that many documents should be returned.");
    }

    @Test
    public void searchDocuments_SearchTermWithOptions() throws Exception {
        List<DocumentData> documentDataList = runFlowAndVerifyMessageCount(
            "search-documents-search-term-with-options",
            1,
            "With the constraint defined, exactly 1 document should be returned.");
        assertEquals("{\"test\":3}", documentDataList.get(0).getContents());
    }

    @Test
    public void searchDocuments_SearchTermWithoutOptions() {
        runFlowAndVerifyMessageCount(
            "search-documents-search-term-without-options",
            0,
            "Without search options, the search string should not have any matches");
    }

    @Test
    public void searchDocuments_withinDirectory() {
        runFlowAndVerifyMessageCount(
            "search-documents-within-directory",
            10,
            "Only the documents in the specified directory should be returned.");
    }

    @Test
    public void searchDocuments_withTransform() throws Exception {
        List<DocumentData> documentDataList = runFlowAndVerifyMessageCount(
            "search-documents-with-transform",
            1,
            "Only a single document should be returned with this criteria.");
        assertEquals(
            "The contents of the message should match the transformed contents of the test3 document.",
            "{\"Hola\":\"Mundo\"}",
            documentDataList.get(0).getContents());
    }


}
