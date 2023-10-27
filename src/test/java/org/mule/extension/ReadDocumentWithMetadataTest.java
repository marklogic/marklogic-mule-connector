package org.mule.extension;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReadDocumentWithMetadataTest extends AbstractFlowTester {

    private final String JSON_URI = "/metadataSamples/json/hello.json";
    private final String JSON_CONTENTS = "{\"hello\":\"world\"}";
    private final String XML_URI = "/metadataSamples/xml/hello.xml";
    private final String XML_CONTENTS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Hello>World</Hello>";
    private final String TEXT_URI = "/metadataSamples/text/hello.text";
    private final String TEXT_CONTENTS = "Hello, World!\n";
    private final String BINARY_URI = "/metadataSamples/binary/logo.png";

    @Override
    protected String getConfigFile() {
        return "read-document-with-metadata.xml";
    }

    @Test
    public void readJsonDocument_OnlyPermissionsAndCollections() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-permissions-collections");
        verifyDocumentContents(JSON_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, JSON_URI)
            .collections(2, "test-data", "json-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonDocument_OnlyCollections() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-collections");
        verifyDocumentContents(JSON_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, JSON_URI)
            .collections(2, "test-data", "json-data")
            .permissions(0)
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonQualityDocument_OnlyPermissions() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-permissions");
        verifyDocumentContents(JSON_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, JSON_URI)
            .collections(0)
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonQualityDocument_OnlyQuality() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-quality");
        verifyDocumentContents(JSON_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, JSON_URI)
            .collections(0)
            .permissions(0)
            .quality(17)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonQualityDocument_All() {
        DocumentData documentData = runFlowGetDocumentData("read-json-document-with-metadata-all");
        verifyDocumentContents(JSON_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, JSON_URI)
            .collections(2, "test-data", "json-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(17)
            .properties(0)
            .verify();
    }


    @Test
    public void readXmlDocument_OnlyPermissionsAndCollections() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-permissions-collections");
        verifyDocumentContents(XML_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, XML_URI)
            .collections(2, "test-data", "xml-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlDocument_OnlyCollections() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-collections");
        verifyDocumentContents(XML_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, XML_URI)
            .collections(2, "test-data", "xml-data")
            .permissions(0)
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlQualityDocument_OnlyPermissions() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-permissions");
        verifyDocumentContents(XML_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, XML_URI)
            .collections(0)
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlQualityDocument_OnlyQuality() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-quality");
        verifyDocumentContents(XML_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, XML_URI)
            .collections(0)
            .permissions(0)
            .quality(27)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlQualityDocument_All() {
        DocumentData documentData = runFlowGetDocumentData("read-xml-document-with-metadata-all");
        verifyDocumentContents(XML_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, XML_URI)
            .collections(2, "test-data", "xml-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(27)
            .properties(0)
            .verify();
    }

    @Test
    public void readTextQualityDocument_All() {
        DocumentData documentData = runFlowGetDocumentData("read-text-document-with-metadata-all");
        verifyDocumentContents(TEXT_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, TEXT_URI)
            .collections(1, "text-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(95)
            .properties(1, "priority", "2")
            .verify();
    }

    @Test
    public void readTextQualityDocument_Metadata() {
        DocumentData documentData = runFlowGetDocumentData("read-text-document-with-metadata-metadata");
        verifyDocumentContents(TEXT_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, TEXT_URI)
            .collections(1, "text-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(95)
            .properties(1, "priority", "2")
            .verify();
    }

    @Test
    public void readTextQualityDocument_Properties() {
        DocumentData documentData = runFlowGetDocumentData("read-text-document-with-metadata-properties");
        verifyDocumentContents(TEXT_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, TEXT_URI)
            .collections(0)
            .permissions(0)
            .quality(0)
            .properties(1, "priority", "2")
            .verify();
    }

    @Test
    // Not passing in a category parameter seems to default to "ALL"
    public void readTextQualityDocument_Content() {
        DocumentData documentData = runFlowGetDocumentData("read-text-document-with-metadata-content");
        verifyDocumentContents(TEXT_CONTENTS, documentData.documentContents);
        MetadataVerifier.assertMetadata(documentData.documentAttributes, TEXT_URI)
            .collections(1, "text-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(95)
            .properties(1, "priority", "2")
            .verify();
    }

    @Test
    public void readBinaryQualityDocument_All() {
        DocumentData documentData = runFlowGetDocumentData("read-binary-document-with-metadata-all");
        assertTrue(documentData.documentContents.contains("PNG"));
        MetadataVerifier.assertMetadata(documentData.documentAttributes, BINARY_URI)
            .collections(2, "test-data", "binary-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readBinaryQualityDocument_Content() {
        DocumentData documentData = runFlowGetDocumentData("read-binary-document-with-metadata-content");
        assertTrue(documentData.documentContents.contains("PNG"));
        MetadataVerifier.assertMetadata(documentData.documentAttributes, BINARY_URI)
            .collections(2, "test-data", "binary-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    private void verifyDocumentContents(String expectedContents, String messageContents) {
        assertEquals("The contents of the message should match the contents of the original document",
            expectedContents, messageContents);
    }
}
