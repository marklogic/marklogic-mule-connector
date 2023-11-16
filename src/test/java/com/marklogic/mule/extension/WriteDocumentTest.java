package com.marklogic.mule.extension;


import com.marklogic.mule.extension.api.DocumentAttributes;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The parent class does some stuff with the classloader that prevents us from constructing a DatabaseClient.
 * It may be possible to work around that by configuring the "applicationSharedRuntimeLibs" field in the
 * "ArtifactClassLoaderRunnerConfig" annotation, but doing so appears to require listing every single dependency of
 * the Java Client. So for now, the approach to testing what's written to MarkLogic will be to run a flow to read
 * what was written.
 */
public class WriteDocumentTest extends AbstractFlowTester {
    @Override
    protected String getFlowTestFile() {
        return "write-document.xml";
    }

    @Test
    public void writeTextDocumentWithAllMetadata() {
        DocumentData documentData = runFlowGetDocumentData("writeTextDocumentWithAllMetadata");
        assertEquals(TEXT_HELLO_WORLD, documentData.getContents());
        assertTrue(documentData.isText());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/writeTextDocumentWithAllMetadata/hello.txt")
            .collections(1, "writeTextDocumentWithAllMetadata")
            .includesPermissions("rest-reader", "read", "rest-reader", "update")
            .quality(1)
            .verify();
    }

    @Test
    public void writeJsonDocumentWithAllMetadata() {
        DocumentData documentData = runFlowGetDocumentData("writeJsonDocumentWithAllMetadata");
        assertEquals(JSON_HELLO_WORLD, documentData.getContents());
        assertTrue(documentData.isJSON());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/writeJsonDocumentWithAllMetadata/hello.json")
            .collections(1, "writeJsonDocumentWithAllMetadata")
            .includesPermissions("rest-reader", "read", "rest-reader", "update")
            .quality(2)
            .verify();
    }

    @Test
    public void writeXmlDocumentWithAllMetadata() {
        DocumentData documentData = runFlowGetDocumentData("writeXmlDocumentWithAllMetadata");
        assertEquals(XML_HELLO_WORLD, documentData.getContents());
        assertTrue(documentData.isXML());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/writeXmlDocumentWithAllMetadata/hello.xml")
            .collections(1, "writeXmlDocumentWithAllMetadata")
            .includesPermissions("rest-reader", "read", "rest-reader", "update")
            .quality(3)
            .verify();
    }

    @Test
    public void writeBinaryDocumentWithAllMetadata() {
        DocumentData documentData = runFlowGetDocumentData("writeBinaryDocumentWithAllMetadata");
        assertTrue(documentData.getContents().contains("PNG"));
        assertTrue(documentData.isBinary());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/writeBinaryDocumentWithAllMetadata/logo.png")
            .collections(1, "writeBinaryDocumentWithAllMetadata")
            .includesPermissions("rest-reader", "read", "rest-reader", "update")
            .quality(4)
            .verify();
    }

    @Test
    public void writeDocumentWithoutUriWithTextFormat() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("writeDocumentWithoutUriWithTextFormat");
        for (DocumentData documentData : documentDataList) {
            assertTrue(documentData.isText());
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("writeDocumentWithoutUriWithTextFormat")
                .includesPermissions("rest-reader", "read", "rest-reader", "update")
                .quality(5)
                .verify();
        }
    }


    @Test
    public void writeDocumentWithoutUriWithoutFormat() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("writeDocumentWithoutUriWithoutFormat");
        for (DocumentData documentData : documentDataList) {
            assertTrue(documentData.isBinary());
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("writeDocumentWithoutUriWithoutFormat")
                .includesPermissions("rest-reader", "read", "rest-reader", "update")
                .quality(6)
                .verify();
        }
    }

    @Test
    public void writeDocumentWithoutUriWithJsonFormat() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("writeDocumentWithoutUriWithJsonFormat");
        for (DocumentData documentData : documentDataList) {
            assertTrue(documentData.isJSON());
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("writeDocumentWithoutUriWithJsonFormat")
                .includesPermissions("rest-reader", "read", "rest-reader", "update")
                .quality(7)
                .verify();
        }
    }

    @Test
    public void writeDocumentWithoutUriWithXmlFormat() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("writeDocumentWithoutUriWithXmlFormat");
        for (DocumentData documentData : documentDataList) {
            assertTrue(documentData.isXML());
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("writeDocumentWithoutUriWithXmlFormat")
                .includesPermissions("rest-reader", "read", "rest-reader", "update")
                .quality(8)
                .verify();
        }
    }

    @Test
    public void writeDocumentWithoutUriWithBinaryFormat() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("writeDocumentWithoutUriWithBinaryFormat");
        for (DocumentData documentData : documentDataList) {
            assertTrue(documentData.isBinary());
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("writeDocumentWithoutUriWithBinaryFormat")
                .includesPermissions("rest-reader", "read", "rest-reader", "update")
                .quality(9)
                .verify();
        }
    }

    @Test
    public void writeDocumentWithPrefixWithoutUuid() {
        DocumentData documentData = runFlowGetDocumentData("writeDocumentWithPrefixWithoutUuid");
        assertEquals(TEXT_HELLO_WORLD, documentData.getContents());
        assertTrue(documentData.isText());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "writeDocumentWithPrefixWithoutUuid")
            .collections(1, "writeDocumentWithPrefixWithoutUuid")
            .includesPermissions("rest-reader", "read", "rest-reader", "update")
            .quality(10)
            .verify();
    }

    @Test
    public void writeDocumentWithPrefixAndSuffix() {
        DocumentData documentData = runFlowGetDocumentData("writeDocumentWithPrefixAndSuffix");
        assertEquals(JSON_HELLO_WORLD, documentData.getContents());
        assertTrue(documentData.isJSON());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "writeDocumentWithPrefixAndSuffix.json")
            .collections(1, "writeDocumentWithPrefixAndSuffix")
            .includesPermissions("rest-reader", "read", "rest-reader", "update")
            .quality(11)
            .verify();
    }

    @Test
    public void writeDocumentWithUuid() {

        List<DocumentData> documentDataList = runFlowForDocumentDataList("writeDocumentWithUuid");
        for (DocumentData documentData : documentDataList) {
            assertTrue(documentData.isJSON());
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("writeDocumentWithUuid")
                .includesPermissions("rest-reader", "read", "rest-reader", "update")
                .quality(12)
                .verify();
        }
    }

    @Test
    public void writeDocumentWithTemporalCollection() {
        DocumentData documentData = runFlowGetDocumentData("writeDocumentWithTemporalCollection");
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/documentWithTemporalCollection.xml")
            .includesPermissions("rest-reader", "read", "rest-reader", "update")
            .collections(3, "mule-temporal-collection","/documentWithTemporalCollection.xml","latest")
            .verify();
    }

    @Test
    public void writeDocumentWithTransform() {

        DocumentData documentData = runFlowGetDocumentData("writeDocumentWithTransform");
        assertEquals("The contents of the message should match the transformed contents of the test3 document.",
            "{\"key\":\"value\"}", documentData.getContents());
        assertTrue(documentData.isJSON());
        MetadataVerifier.assertMetadata(documentData.getAttributes(), "/writeDocumentWithTransform")
            .collections(1, "test-data")
            .includesPermissions("rest-reader", "read", "rest-reader", "update")
            .quality(14)
            .verify();
    }
}
