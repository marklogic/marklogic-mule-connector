package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;
import org.junit.Test;
import org.mule.runtime.api.message.Message;

import java.util.List;

@SuppressWarnings("unchecked")
public class SearchDocumentsWithMetadataTest extends AbstractFlowTester {

    @Override
    protected String getConfigFile() {
        return "search-documents-with-metadata.xml";
    }

    @Test
    public void searchBatchInputDocuments_DefaultMetadata() throws Exception {
        Message outerMessage = runFlowGetMessage("search-batch-input-documents-with-metadata-default");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .includesCollections("test-data", "batch-input")
                .permissions(2, "rest-reader","read","rest-writer","update")
                .quality(42)
                .properties(2, "uriProperty", attributes.getUri(), "widgets", "2")
                .verify();
        }
    }

    @Test
    public void searchTestDataDocuments_AllMetadata() throws Exception {
        Message outerMessage = runFlowGetMessage("search-test-data-documents-with-metadata-all");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .includesCollections("test-data")
                .includesPermissions("rest-reader","read")
                .verify();
        }
    }

    @Test
    public void searchBinaryDataDocuments_CollectionsMetadata() throws Exception {
        Message outerMessage = runFlowGetMessage("search-binary-documents-with-metadata-collections");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .includesCollections("binary-data", "test-data")
                .permissions(0)
                .properties(0)
                .verify();
        }
    }

    @Test
    public void searchTextDataDocuments_PermissionsMetadata() throws Exception {
        Message outerMessage = runFlowGetMessage("search-text-documents-with-metadata-permissions");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .collections(0)
                .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
                .properties(0)
                .quality(0)
                .verify();
        }
    }

    @Test
    public void searchTextDataDocuments_PermissionsPropertiesMetadata() throws Exception {
        Message outerMessage = runFlowGetMessage("search-text-documents-with-metadata-permissionsProperties");
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        for (Message docMessage : innerMessages) {
            DocumentAttributes attributes = (DocumentAttributes) docMessage.getAttributes().getValue();
            MetadataVerifier.assertMetadata(attributes, null)
                .collections(0)
                .permissions(3, "rest-reader","read","rest-admin","read","rest-admin","update","rest-extension-user","execute")
                .properties(1, "priority", "2")
                .quality(0)
                .verify();
        }
    }
}
