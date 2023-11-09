package com.marklogic.mule.extension;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.message.Message;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public abstract class AbstractFlowTester extends MuleArtifactFunctionalTestCase {

    protected final static String JSON_HELLO_WORLD = "{\"hello\":\"world\"}";
    protected final static String TEXT_HELLO_WORLD = "Hello, World!\n";
    protected final static String XML_HELLO_WORLD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Hello>World</Hello>";


    // Use this to delete all non-test-data documents (i.e. anything not loaded by the test-app) at the start
    // of a test.
    protected final static String DELETE_QUERY = "declareUpdate();\n" +
        "for (var uri of cts.uris(null, null, cts.notQuery(cts.collectionQuery('test-data')))) {\n" +
        "  xdmp.documentDelete(uri);\n" +
        "}";

    Message runFlowGetMessage(String flowName) {
        try {
            return flowRunner(flowName)
                .withVariable("DELETE_QUERY", DELETE_QUERY)
                .keepStreamsOpen().run().getMessage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    List<DocumentData> runFlowAndVerifyMessageCount(String flowName, long expectedCount, String assertionMessage) {
        Message outerMessage = runFlowGetMessage(flowName);
        List<Message> innerMessages = (List<Message>) outerMessage.getPayload().getValue();
        assertEquals(assertionMessage, expectedCount, innerMessages.size());
        return toDocumentDataList(innerMessages);
    }

    DocumentData runFlowGetDocumentData(String flowName) {
        return toDocumentData(runFlowGetMessage(flowName));
    }

    List<DocumentData> runFlowForDocumentDataList(String flowName) {
        return toDocumentDataList((List<Message>) runFlowGetMessage(flowName).getPayload().getValue());
    }

    private DocumentData toDocumentData(Message message) {
        String content;
        try {
            content = getPayloadAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get payload as string", e);
        }
        return new DocumentData(
            content,
            (DocumentAttributes) message.getAttributes().getValue(),
            message.getPayload().getDataType().getMediaType().toRfcString()
        );

    }

    private List<DocumentData> toDocumentDataList(List<Message> innerMessages) {
        List<DocumentData> documentDataList = new ArrayList<>();
        for (Message docMessage : innerMessages) {
            documentDataList.add(toDocumentData(docMessage));
        }
        return documentDataList;
    }

}
