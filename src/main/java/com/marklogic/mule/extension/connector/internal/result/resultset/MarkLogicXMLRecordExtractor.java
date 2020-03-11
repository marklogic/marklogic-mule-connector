/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2020 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
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
public class MarkLogicXMLRecordExtractor extends MarkLogicRecordExtractor {

    // Objects used for handling XML documents
    private DOMHandle xmlHandle = new DOMHandle();

    @Override
    protected Object extractRecord(DocumentRecord record) {
        Document node = record.getContent(xmlHandle).get();
        return createMapFromXML(node.getDocumentElement());
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
