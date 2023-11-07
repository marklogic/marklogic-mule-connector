package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;
import org.junit.Test;

import java.util.List;

@SuppressWarnings("unchecked")
public class SearchDocumentsWithMetadataTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-with-metadata.xml";
    }

    @Test
    public void searchBatchInputDocuments_DefaultMetadata() {
        List<DocumentData> documentDataList = runFlowForDocumentDataList("search-batch-input-documents-with-metadata-default");
        for (DocumentData documentData : documentDataList) {
            DocumentAttributes documentAttributes = documentData.getAttributes();
            MetadataVerifier.assertMetadata(documentAttributes, null)
                .includesCollections("test-data", "batch-input")
                .permissions(2, "rest-reader","read","rest-writer","update")
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
                .includesPermissions("rest-reader","read")
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
                .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
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
                .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
                .properties(1, "priority", "2")
                .quality(0)
                .verify();
        }
    }
}
