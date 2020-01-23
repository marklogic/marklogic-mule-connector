package com.marklogic.mule.extension.connector.internal.result.resultset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.io.BytesHandle;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by jkrebs on 1/19/2020.
 */
public class MarkLogicJSONRecordExtractor extends MarkLogicRecordExtractor {

    // Objects used for handling JSON documents
    private JacksonHandle jacksonHandle = new JacksonHandle();
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    protected Object extractRecord(DocumentRecord record) {
        Object content;
        JsonNode jsonNode = record.getContent(jacksonHandle).get();
        JsonNodeType nodeType = jsonNode.getNodeType();
        if (null == nodeType) {
            content = jsonMapper.convertValue(jsonNode, Map.class);
        } else switch (nodeType) {
            case ARRAY:
                content = jsonMapper.convertValue(jsonNode, List.class);
                break;
            case STRING:
                content = jsonMapper.convertValue(jsonNode, String.class);
                break;
            case NUMBER:
                content = jsonMapper.convertValue(jsonNode, Number.class);
                break;
            default:
                content = jsonMapper.convertValue(jsonNode, Map.class);
                break;
        }
        return content;
    }
}
