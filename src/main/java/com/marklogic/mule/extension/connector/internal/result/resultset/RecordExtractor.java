package com.marklogic.mule.extension.connector.internal.result.resultset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.io.BytesHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.mule.extension.connector.api.operation.MarkLogicMimeType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Intent is for a dependent to hold onto an instance of this to avoid re-instantiation of the Jackson ObjectMapper.
 * Mulesoft does not want static instances. 
 */
public class RecordExtractor {

    private ObjectMapper objectMapper = new ObjectMapper();

    public Object extractRecord(DocumentRecord doc) {
        MarkLogicMimeType mimeType = fromString(doc.getMimetype());
        if (MarkLogicMimeType.xml.equals(mimeType)) {
            return doc.getContent(new StringHandle()).withMimetype("application/xml").withFormat(Format.XML).get();
        } else if (MarkLogicMimeType.json.equals(mimeType)) {
            Object content;
            JsonNode jsonNode = doc.getContent(new JacksonHandle()).get();
            JsonNodeType nodeType = jsonNode.getNodeType();
            if (null == nodeType) {
                content = objectMapper.convertValue(jsonNode, Map.class);
            } else switch (nodeType) {
                case ARRAY:
                    content = objectMapper.convertValue(jsonNode, List.class);
                    break;
                case STRING:
                    content = objectMapper.convertValue(jsonNode, String.class);
                    break;
                case NUMBER:
                    content = objectMapper.convertValue(jsonNode, Number.class);
                    break;
                default:
                    content = objectMapper.convertValue(jsonNode, Map.class);
                    break;
            }
            return content;
        } else if (MarkLogicMimeType.text.equals(mimeType)) {
            return doc.getContent(new StringHandle()).get();
        }
        return doc.getContent(new BytesHandle()).get();
    }

    private MarkLogicMimeType fromString(String mimeString) {
        if (mimeString != null) {
            List<String> typeString = Arrays.asList(mimeString.split("/"));
            if (typeString.contains("xml")) {
                return MarkLogicMimeType.xml;
            } else if (typeString.contains("json")) {
                return MarkLogicMimeType.json;
            } else if (typeString.contains("text")) {
                return MarkLogicMimeType.text;
            }
        }
        return MarkLogicMimeType.binary;
    }
}
