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

import static org.junit.Assert.assertEquals;

public class ReadAndWriteDocumentTest extends AbstractFlowTester {

    @Override
    protected String getFlowTestFile() {
        return "read-and-write-document.xml";
    }

    @Test
    public void readAndWriteDocument() {
        DocumentData documentData = runFlowGetDocumentData("read-and-write-document");
        assertEquals("application/json; charset=UTF-8", documentData.getMimeType());
        assertEquals("The contents of the message should match the contents of the original document",
            JSON_HELLO_WORLD, documentData.getContents());
        assertEquals(
            "The written document is expected to be returned, and its URI is based on an expression in the flow operation for writing the document.",
            "/test/metadataSamples/json/hello.json", documentData.getAttributes().getUri());
    }
}
