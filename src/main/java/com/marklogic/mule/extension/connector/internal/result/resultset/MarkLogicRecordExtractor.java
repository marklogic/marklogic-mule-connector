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
 * Created by jkrebs on 9/25/2019.
 */
public class MarkLogicRecordExtractor {

    // Objects used for handling JSON documents
    private static JacksonHandle jacksonHandle = new JacksonHandle();
    private static ObjectMapper jsonMapper = new ObjectMapper();

    // Objects used for handling XML documents
    private static DOMHandle xmlHandle = new DOMHandle();

    // Objects used for handling text documents
    private static StringHandle stringHandle = new StringHandle();

    // Objects used for handling binary documents
    private static BytesHandle binaryHandle = new BytesHandle();


    public static Object extractRecord(DocumentRecord record) {
        Object content;
        String type = record.getMimetype();
        if (type == null)
        { // Treat no mimetype as binary
            content = record.getContent(binaryHandle).get();
        }
        else
        {
            String lowerCaseType = type.toLowerCase();
            if (lowerCaseType.contains("xml"))
            {
                Document node = record.getContent(xmlHandle).get();
                content = createMapFromXML(node.getDocumentElement());
            }
            else if (lowerCaseType.contains("json"))
            {
                JsonNode jsonNode = record.getContent(jacksonHandle).get();
                JsonNodeType nodeType = jsonNode.getNodeType();
                if (null == nodeType)
                {
                    content = jsonMapper.convertValue(jsonNode, Map.class);
                }
                else switch (nodeType)
                {
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
            }
            else if (lowerCaseType.contains("text"))
            {
                content = record.getContent(stringHandle).get();
            }
            else
            {
                content = record.getContent(binaryHandle).get();
            }
        }
        return content;

    }

    /**
     * This recursive method creates a Map from DOM object
     *
     * @param node XML Node
     * @return an object; type depends on what type of node is being processed
     */
    private static Object createMapFromXML(Node node)
    {
        Map<String, Object> map = new HashMap<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node currentNode = nodeList.item(i);
            String name = currentNode.getNodeName();
            Object value = null;
            if (currentNode.getNodeType() == Node.ELEMENT_NODE)
            {
                value = createMapFromXML(currentNode);
            }
            else if (currentNode.getNodeType() == Node.TEXT_NODE)
            {
                return currentNode.getTextContent();
            }
            if (map.containsKey(name))
            {
                Object obj = map.get(name);
                if (obj instanceof List)
                {
                    ((List) obj).add(value);
                }
                else
                {
                    List<Object> objs = new LinkedList<>();
                    objs.add(obj);
                    objs.add(value);
                    map.put(name, objs);
                }
            }
            else
            {
                map.put(name, value);
            }
        }
        return map;
    }

}
