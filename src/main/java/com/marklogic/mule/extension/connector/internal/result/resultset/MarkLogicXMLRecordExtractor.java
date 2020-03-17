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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.io.DOMHandle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Created by jkrebs on 1/19/2020.
 */
public class MarkLogicXMLRecordExtractor extends MarkLogicRecordExtractor {

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicXMLRecordExtractor.class);

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

        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            List<Object> children = new ArrayList<>();
            map.put(node.getNodeName(),children);
            NamedNodeMap attributes = node.getAttributes();
            if ((attributes != null ) && (attributes.getLength() > 0)) {
                Map<String,String> attributesMap = new HashMap<>();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node item = attributes.item(i);
                    attributesMap.put(item.getNodeName(), item.getNodeValue());
                }
                children.add(attributesMap);
            }
            for (int i = 0; i < nodeList.getLength(); i++) {
                children.add(createMapFromXML(nodeList.item(i)));
            }
            return map;
        }
        else if (node.getNodeType() == Node.TEXT_NODE)
        {
            return node.getTextContent();
        }
        else if (node.getNodeType() == Node.COMMENT_NODE)
        {
            return node.getTextContent();
        }
        else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
        {
            return node.getTextContent();
        }
        else {
            logger.warn("Unhandled XML node type: " + node.getNodeType());
        }
        return map;
    }

}
