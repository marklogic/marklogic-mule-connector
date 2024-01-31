/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2024 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.mule.extension;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchDocumentsWithOptionsTest extends AbstractFlowTester {

    @Override
    protected String getFlowTestFile() {
        return "search-documents-with-options.xml";
    }

    @Test
    public void searchDocuments_DefaultMetadata() {
        assertEquals("With maxResults set, only that many documents should be returned.",
            9, runFlowForDocumentCount("search-documents-with-maxResults"));
    }

    @Test
    public void searchDocuments_WithoutConsistentSnapshot() {
        assertEquals("This only verifies that setting the consistentSnapshot parameter to false does not break anything.",
            9, runFlowForDocumentCount("search-documents-without-consistent-snapshot"));
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
        assertEquals("Without search options, the search string should not have any matches",
            0, runFlowForDocumentCount("search-documents-search-term-without-options"));
    }

    @Test
    public void searchDocuments_withinDirectory() {
        assertEquals("Only the documents in the specified directory should be returned.",
            10, runFlowForDocumentCount("search-documents-within-directory"));
    }

    @Test
    public void searchDocuments_withTransform() {
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
