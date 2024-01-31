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

import static org.junit.Assert.*;

public class ReadDocumentWithMetadataTest extends AbstractFlowTester {

    private final String JSON_URI = "/metadataSamples/json/hello.json";
    private final String XML_URI = "/metadataSamples/xml/hello.xml";
    private final String XML_CONTENTS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Hello>World</Hello>";
    private final String TEXT_URI = "/metadataSamples/text/hello.text";
    private final String BINARY_URI = "/metadataSamples/binary/logo.png";

    @Override
    protected String getFlowTestFile() {
        return "read-document-with-metadata.xml";
    }

    @Test
    public void readJsonDocument_OnlyPermissionsAndCollections() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-permissions-collections");
        assertEquals("application/json; charset=UTF-8", documentData.getMimeType());
        assertEquals(JSON_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), JSON_URI)
            .collections(2, "test-data", "json-data")
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonDocument_OnlyCollections() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-collections");
        assertEquals(JSON_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), JSON_URI)
            .collections(2, "test-data", "json-data")
            .permissions(0)
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonQualityDocument_OnlyPermissions() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-permissions");
        assertEquals(JSON_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), JSON_URI)
            .collections(0)
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonQualityDocument_OnlyQuality() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-quality");
        assertEquals(JSON_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), JSON_URI)
            .collections(0)
            .permissions(0)
            .quality(17)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonQualityDocument_All() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-all");
        assertEquals(JSON_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), JSON_URI)
            .collections(2, "test-data", "json-data")
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(17)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonDocumentNoMetadata() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-no-metadata");
        assertEquals(JSON_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), JSON_URI)
            .collections(0)
            .permissions(0)
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonDocumentInvalidMetadata() {
        try {
            runFlowGetDocumentData("read-json-document-invalid-metadata");
            fail("Expected the invalid metadata value to throw an error");
        } catch (RuntimeException ex) {
            assertEquals("Invalid document metadata category: not-valid", ex.getCause().getMessage());
        }
    }

    @Test
    public void readXmlDocument_OnlyPermissionsAndCollections() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-permissions-collections");
        assertEquals("application/xml; charset=UTF-8", documentData.getMimeType());
        assertEquals(XML_CONTENTS, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), XML_URI)
            .collections(2, "test-data", "xml-data")
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlDocument_OnlyCollections() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-collections");
        assertEquals(XML_CONTENTS, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), XML_URI)
            .collections(2, "test-data", "xml-data")
            .permissions(0)
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlQualityDocument_OnlyPermissions() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-permissions");
        assertEquals(XML_CONTENTS, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), XML_URI)
            .collections(0)
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlQualityDocument_OnlyQuality() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-quality");
        assertEquals(XML_CONTENTS, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), XML_URI)
            .collections(0)
            .permissions(0)
            .quality(27)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlQualityDocument_All() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-all");
        assertEquals(XML_CONTENTS, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), XML_URI)
            .collections(2, "test-data", "xml-data")
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(27)
            .properties(0)
            .verify();
    }

    @Test
    public void readTextQualityDocument_All() {
        DocumentData documentData = runFlowGetDocumentData("read-text-document-with-metadata-all");
        assertEquals("text/plain; charset=UTF-8", documentData.getMimeType());
        assertEquals(TEXT_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), TEXT_URI)
            .collections(2, "text-data", "test-data")
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(95)
            .properties(3, "{org:example}priority", "<hello>world</hello>", "complexity", "2", "{org:example}anotherProp", "PropValue")
            .verify();
    }

    @Test
    public void readTextQualityDocument_Metadata() {
        DocumentData documentData = runFlowGetDocumentData("read-text-document-with-metadata-metadata");
        assertEquals(TEXT_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), TEXT_URI)
            .collections(2, "text-data", "test-data")
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(95)
            .properties(3, "{org:example}priority", "<hello>world</hello>", "complexity", "2", "{org:example}anotherProp", "PropValue")
            .verify();
    }

    @Test
    public void readTextQualityDocument_Properties() {
        DocumentData documentData = runFlowGetDocumentData("read-text-document-with-metadata-properties");
        assertEquals(TEXT_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), TEXT_URI)
            .collections(0)
            .permissions(0)
            .quality(0)
            .properties(3, "{org:example}priority", "<hello>world</hello>", "complexity", "2", "{org:example}anotherProp", "PropValue")
            .verify();
    }

    @Test
    public void readTextQualityDocument_Content() {
        DocumentData documentData = runFlowGetDocumentData("read-text-document-with-metadata-content");
        assertEquals(TEXT_HELLO_WORLD, documentData.getContents());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), TEXT_URI)
            .collections(2, "text-data", "test-data")
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(95)
            .properties(3, "{org:example}priority", "<hello>world</hello>", "complexity", "2", "{org:example}anotherProp", "PropValue")
            .verify();
    }

    @Test
    public void readBinaryQualityDocument_All() {
        DocumentData documentData = runFlowGetDocumentData("read-binary-document-with-metadata-all");
        assertEquals("application/octet-stream; charset=UTF-8", documentData.getMimeType());
        assertTrue(documentData.getContents().contains("PNG"));
        MetadataVerifier.assertMetadata(documentData.getAttributes(), BINARY_URI)
            .collections(2, "test-data", "binary-data")
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readBinaryQualityDocument_Content() {
        DocumentData documentData = runFlowGetDocumentData("read-binary-document-with-metadata-content");
        assertTrue(documentData.getContents().contains("PNG"));
        MetadataVerifier.assertMetadata(documentData.getAttributes(), BINARY_URI)
            .collections(2, "test-data", "binary-data")
            .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
            .quality(0)
            .properties(0)
            .verify();
    }
}
