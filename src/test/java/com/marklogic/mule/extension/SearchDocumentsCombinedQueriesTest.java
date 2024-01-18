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
