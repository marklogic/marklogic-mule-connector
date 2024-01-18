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
