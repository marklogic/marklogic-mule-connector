package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;
import org.junit.Test;
import org.mule.runtime.api.message.Message;

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
    public void readJsonDocument_OnlyPermissionsAndCollections() throws Exception {
        Message message = runFlowGetMessage("read-json-document-with-metadata-permissions-collections");
        verifyDocumentContents(JSON_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, JSON_URI)
            .collections(2, "test-data", "json-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonDocument_OnlyCollections() throws Exception {
        Message message = runFlowGetMessage("read-json-document-with-metadata-collections");
        verifyDocumentContents(JSON_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, JSON_URI)
            .collections(2, "test-data", "json-data")
            .permissions(0)
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonQualityDocument_OnlyPermissions() throws Exception {
        Message message = runFlowGetMessage("read-json-document-with-metadata-permissions");
        verifyDocumentContents(JSON_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, JSON_URI)
            .collections(0)
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonQualityDocument_OnlyQuality() throws Exception {
        Message message = runFlowGetMessage("read-json-document-with-metadata-quality");
        verifyDocumentContents(JSON_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, JSON_URI)
            .collections(0)
            .permissions(0)
            .quality(17)
            .properties(0)
            .verify();
    }

    @Test
    public void readJsonQualityDocument_All() throws Exception {
        Message message = runFlowGetMessage("read-json-document-with-metadata-all");
        verifyDocumentContents(JSON_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, JSON_URI)
            .collections(2, "test-data", "json-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(17)
            .properties(0)
            .verify();
    }


    @Test
    public void readXmlDocument_OnlyPermissionsAndCollections() throws Exception {
        Message message = runFlowGetMessage("read-xml-document-with-metadata-permissions-collections");
        verifyDocumentContents(XML_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, XML_URI)
            .collections(2, "test-data", "xml-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlDocument_OnlyCollections() throws Exception {
        Message message = runFlowGetMessage("read-xml-document-with-metadata-collections");
        verifyDocumentContents(XML_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, XML_URI)
            .collections(2, "test-data", "xml-data")
            .permissions(0)
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlQualityDocument_OnlyPermissions() throws Exception {
        Message message = runFlowGetMessage("read-xml-document-with-metadata-permissions");
        verifyDocumentContents(XML_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, XML_URI)
            .collections(0)
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlQualityDocument_OnlyQuality() throws Exception {
        Message message = runFlowGetMessage("read-xml-document-with-metadata-quality");
        verifyDocumentContents(XML_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, XML_URI)
            .collections(0)
            .permissions(0)
            .quality(27)
            .properties(0)
            .verify();
    }

    @Test
    public void readXmlQualityDocument_All() throws Exception {
        Message message = runFlowGetMessage("read-xml-document-with-metadata-all");
        verifyDocumentContents(XML_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, XML_URI)
            .collections(2, "test-data", "xml-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(27)
            .properties(0)
            .verify();
    }

    @Test
    public void readTextQualityDocument_All() throws Exception {
        Message message = runFlowGetMessage("read-text-document-with-metadata-all");
        verifyDocumentContents(TEXT_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, TEXT_URI)
            .collections(1, "text-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(95)
            .properties(1, "priority", "2")
            .verify();
    }

    @Test
    public void readTextQualityDocument_Metadata() throws Exception {
        Message message = runFlowGetMessage("read-text-document-with-metadata-metadata");
        verifyDocumentContents(TEXT_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, TEXT_URI)
            .collections(1, "text-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(95)
            .properties(1, "priority", "2")
            .verify();
    }

    @Test
    public void readTextQualityDocument_Properties() throws Exception {
        Message message = runFlowGetMessage("read-text-document-with-metadata-properties");
        verifyDocumentContents(TEXT_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, TEXT_URI)
            .collections(0)
            .permissions(0)
            .quality(0)
            .properties(1, "priority", "2")
            .verify();
    }

    @Test
    // Not passing in a category parameter seems to default to "ALL"
    public void readTextQualityDocument_Content() throws Exception {
        Message message = runFlowGetMessage("read-text-document-with-metadata-content");
        verifyDocumentContents(TEXT_CONTENTS, getPayloadAsString(message));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, TEXT_URI)
            .collections(1, "text-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(95)
            .properties(1, "priority", "2")
            .verify();
    }

    @Test
    public void readBinaryQualityDocument_All() throws Exception {
        Message message = runFlowGetMessage("read-binary-document-with-metadata-all");
        assertTrue(getPayloadAsString(message).contains("PNG"));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, BINARY_URI)
            .collections(2, "test-data", "binary-data")
            .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
            .quality(0)
            .properties(0)
            .verify();
    }

    @Test
    public void readBinaryQualityDocument_Content() throws Exception {
        Message message = runFlowGetMessage("read-binary-document-with-metadata-content");
        assertTrue(getPayloadAsString(message).contains("PNG"));
        DocumentAttributes attributes = (DocumentAttributes) message.getAttributes().getValue();
        MetadataVerifier.assertMetadata(attributes, BINARY_URI)
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
