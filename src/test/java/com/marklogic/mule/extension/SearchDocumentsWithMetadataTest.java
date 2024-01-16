package com.marklogic.mule.extension;

import com.marklogic.mule.connector.internal.provider.DocumentAttributes;
import org.junit.Test;

import java.util.List;

public class SearchDocumentsWithMetadataTest extends AbstractFlowTester {

    @Override
    protected String getFlowTestFile() {
        return "search-documents-with-metadata.xml";
    }

    @Test
    public void searchBatchInputDocuments_DefaultMetadata() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-batch-input-documents-with-metadata-default");
        for (DocumentData documentData : documentDataList) {
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("test-data", "batch-input")
                .permissions(2, "rest-reader", "read", "admin", "update")
                .quality(42)
                .properties(2, "uriProperty", documentAttributes.getUri(), "widgets", "2")
                .verify();
        }
    }

    @Test
    public void searchTestDataDocuments_AllMetadata() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-test-data-documents-with-metadata-all");
        for (DocumentData documentData : documentDataList) {
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("test-data")
                .includesPermissions("rest-reader", "read")
                .verify();
        }
    }

    @Test
    public void searchBinaryDataDocuments_CollectionsMetadata() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-binary-documents-with-metadata-collections");
        for (DocumentData documentData : documentDataList) {
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("binary-data", "test-data")
                .permissions(0)
                .properties(0)
                .verify();
        }
    }

    @Test
    public void queryDataDocuments_PermissionsMetadata() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-text-documents-with-metadata-permissions");
        for (DocumentData documentData : documentDataList) {
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .collections(0)
                .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
                .properties(0)
                .quality(0)
                .verify();
        }
    }

    @Test
    public void queryDataDocuments_PermissionsPropertiesMetadata() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-text-documents-with-metadata-permissionsProperties");
        for (DocumentData documentData : documentDataList) {
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .collections(0)
                .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
                .properties(3, "{org:example}priority", "<hello>world</hello>", "complexity", "2", "{org:example}anotherProp", "PropValue")
                .quality(0)
                .verify();
        }
    }

    @Test
    public void queryDataDocuments_AllMetadata() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-text-documents-with-metadata-all");
        for (DocumentData documentData : documentDataList) {
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .collections(2)
                .permissions(3, "rest-reader", "read", "rest-admin", "read", "rest-admin", "update", "rest-extension-user", "execute")
                .properties(3, "{org:example}priority", "<hello>world</hello>", "complexity", "2", "{org:example}anotherProp", "PropValue")
                .metadataValues(1, "hello", "world")
                .quality(95)
                .verify();
        }
    }

    @Test
    public void queryDataDocuments_MetadataValuesOnly() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-text-data-documents-with-metadata-values");
        for (DocumentData documentData : documentDataList) {
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .collections(0)
                .permissions(0)
                .properties(0)
                .metadataValues(1, "hello", "world")
                .quality(0)
                .verify();
        }
    }
}
