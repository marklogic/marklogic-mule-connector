package org.mule.extension;


import com.marklogic.mule.extension.DocumentAttributes;
import org.junit.Test;
import org.mule.runtime.api.message.Message;

import java.util.List;

import static org.junit.Assert.*;

/**
 * The parent class does some stuff with the classloader that prevents us from constructing a DatabaseClient.
 * It may be possible to work around that by configuring the "applicationSharedRuntimeLibs" field in the
 * "ArtifactClassLoaderRunnerConfig" annotation, but doing so appears to require listing every single dependency of
 * the Java Client. So for now, the approach to testing what's written to MarkLogic will be to run a flow to read
 * what was written.
 */
public class WriteDocumentTest extends AbstractFlowTester {
    private final String TEXT_CONTENTS = "Hello, World!\n";
    private final String JSON_CONTENTS = "{\"hello\":\"world\"}";
    private final String XML_CONTENTS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Hello>World</Hello>";
    @Override
    protected String getConfigFile() {
        return "write-document.xml";
    }

    @Test
    public void writeTextDocumentWithAllMetadata(){
        DocumentData documentData = runFlowGetDocumentData("writeTextDocumentWithAllMetadata");
        verifyContents(TEXT_CONTENTS, documentData.getContents());
        String mimeType = "text/plain; charset=UTF-8";
        assertEquals("Expected MimeType to be "+mimeType+" but instead received "+documentData.getMimeType(),
            mimeType, documentData.getMimeType());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/writeTextDocumentWithAllMetadata/hello.txt")
            .collections(1, "writeTextDocumentWithAllMetadata")
            .includesPermissions("rest-reader","read","rest-reader","update")
            .quality(1)
            .verify();
    }

    @Test
    public void writeJsonDocumentWithAllMetadata(){
        DocumentData documentData = runFlowGetDocumentData("writeJsonDocumentWithAllMetadata");
        verifyContents(JSON_CONTENTS, documentData.getContents());
        String mimeType = "application/json; charset=UTF-8";
        assertEquals("Expected MimeType to be "+mimeType+" but instead received "+documentData.getMimeType(),
            mimeType, documentData.getMimeType());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/writeJsonDocumentWithAllMetadata/hello.json")
            .collections(1, "writeJsonDocumentWithAllMetadata")
            .includesPermissions("rest-reader","read","rest-reader","update")
            .quality(2)
            .verify();
    }

    @Test
    public void writeXmlDocumentWithAllMetadata(){
        DocumentData documentData = runFlowGetDocumentData("writeXmlDocumentWithAllMetadata");
        verifyContents(XML_CONTENTS, documentData.getContents());
        String mimeType = "application/xml; charset=UTF-8";
        assertEquals("Expected MimeType to be "+mimeType+" but instead received "+documentData.getMimeType(),
            mimeType, documentData.getMimeType());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/writeXmlDocumentWithAllMetadata/hello.xml")
            .collections(1, "writeXmlDocumentWithAllMetadata")
            .includesPermissions("rest-reader","read","rest-reader","update")
            .quality(3)
            .verify();
    }

    @Test
    public void writeBinaryDocumentWithAllMetadata(){
        DocumentData documentData = runFlowGetDocumentData("writeBinaryDocumentWithAllMetadata");
        assertTrue(documentData.getContents().contains("PNG"));
        String mimeType = "application/octet-stream; charset=UTF-8";
        assertEquals("Expected MimeType to be "+mimeType+" but instead received "+documentData.getMimeType(),
            mimeType, documentData.getMimeType());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/writeBinaryDocumentWithAllMetadata/logo.png")
            .collections(1, "writeBinaryDocumentWithAllMetadata")
            .includesPermissions("rest-reader","read","rest-reader","update")
            .quality(4)
            .verify();
    }

    @Test
    public void writeDocumentWithoutUriWithTextFormat(){
        Message message = runFlowGetMessage("writeDocumentWithoutUriWithTextFormat");
        List<Message> innerMessages = (List<Message>) message.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            assertEquals("Expected MediaType to be text/plain but instead received "+
                    docMessage.getPayload().getDataType().getMediaType().toRfcString(), "text/plain",
                docMessage.getPayload().getDataType().getMediaType().toRfcString());
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .includesCollections("writeDocumentWithoutUriWithTextFormat")
                .includesPermissions("rest-reader","read","rest-reader","update")
                .quality(5)
                .verify();
        }
    }



    @Test
    public void writeDocumentWithoutUriWithoutFormat(){
        Message message = runFlowGetMessage("writeDocumentWithoutUriWithoutFormat");
        List<Message> innerMessages = (List<Message>) message.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            assertEquals("Expected MediaType to be application/octet-stream but instead received "+
                docMessage.getPayload().getDataType().getMediaType().toRfcString(), "application/octet-stream",
                docMessage.getPayload().getDataType().getMediaType().toRfcString());
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .includesCollections("writeDocumentWithoutUriWithoutFormat")
                .includesPermissions("rest-reader","read","rest-reader","update")
                .quality(6)
                .verify();
        }
    }

    @Test
    public void writeDocumentWithoutUriWithJsonFormat(){
        Message message = runFlowGetMessage("writeDocumentWithoutUriWithJsonFormat");
        List<Message> innerMessages = (List<Message>) message.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            assertEquals("Expected MediaType to be application/json but instead received "+
                    docMessage.getPayload().getDataType().getMediaType().toRfcString(), "application/json",
                docMessage.getPayload().getDataType().getMediaType().toRfcString());
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .includesCollections("writeDocumentWithoutUriWithJsonFormat")
                .includesPermissions("rest-reader","read","rest-reader","update")
                .quality(7)
                .verify();
        }
    }

    @Test
    public void writeDocumentWithoutUriWithXmlFormat(){
        Message message = runFlowGetMessage("writeDocumentWithoutUriWithXmlFormat");
        List<Message> innerMessages = (List<Message>) message.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            assertEquals("Expected MediaType to be application/xml but instead received "+
                    docMessage.getPayload().getDataType().getMediaType().toRfcString(), "application/xml",
                docMessage.getPayload().getDataType().getMediaType().toRfcString());
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .includesCollections("writeDocumentWithoutUriWithXmlFormat")
                .includesPermissions("rest-reader","read","rest-reader","update")
                .quality(8)
                .verify();
        }
    }

    @Test
    public void writeDocumentWithoutUriWithBinaryFormat(){
        Message message = runFlowGetMessage("writeDocumentWithoutUriWithBinaryFormat");
        List<Message> innerMessages = (List<Message>) message.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            assertEquals("Expected MediaType to be aapplication/octet-stream but instead received "+
                    docMessage.getPayload().getDataType().getMediaType().toRfcString(), "application/octet-stream",
                docMessage.getPayload().getDataType().getMediaType().toRfcString());
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .includesCollections("writeDocumentWithoutUriWithBinaryFormat")
                .includesPermissions("rest-reader","read","rest-reader","update")
                .quality(9)
                .verify();
        }
    }
}
