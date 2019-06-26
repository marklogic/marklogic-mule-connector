/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2019 MarkLogic Corporation.
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
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.BytesHandle;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.mule.extension.connector.internal.config.MarkLogicConfiguration;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Iterates across all results returned by a synchronous {@link QueryDefinition}
 * execution.
 *
 * @since 1.0.1
 *
 */
//N.b: Support server-side transforms
public class MarkLogicResultSetIterator implements Iterator
{

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicResultSetIterator.class);

    private final MarkLogicConfiguration configuration;

    private DocumentPage documents = null;

    // Objects used for handling JSON documents
    private JacksonHandle jacksonHandle = new JacksonHandle();
    private ObjectMapper jsonMapper = new ObjectMapper();
    private DocumentManager dm;

    // Objects used for handling XML documents
    private DOMHandle xmlHandle = new DOMHandle();

    // Objects used for handling text documents
    private StringHandle stringHandle = new StringHandle();

    // Objects used for handling binary documents
    private BytesHandle binaryHandle = new BytesHandle();

    private int start = 1;
    private QueryDefinition query;

    public MarkLogicResultSetIterator(MarkLogicConnection connection, MarkLogicConfiguration configuration, QueryDefinition query)
    {
        this.configuration = configuration;
        this.query = query;
        DatabaseClient client = connection.getClient();
        dm = client.newDocumentManager();
        dm.setPageLength(configuration.getBatchSize());
    }

    @Override
    public boolean hasNext()
    {
        return (start == 1 || documents.hasNextPage());
    }

    @Override
    public List<Object> next()
    {

        if (logger.isInfoEnabled())
        {
            logger.info("iterator query: " + query.toString());
        }

        documents = dm.search(query, start);
        int fetchSize = configuration.getBatchSize();
        final List<Object> page = new ArrayList<>(fetchSize);
        for (int i = 0; i < fetchSize && documents.hasNext(); i++)
        {
            DocumentRecord nextRecord = documents.next();
            Object content;
            String type = nextRecord.getMimetype();
            if (type == null)
            { // Treat no mimetype as binary
                content = nextRecord.getContent(binaryHandle).get();
            }
            else
            {
                String lowerCaseType = type.toLowerCase();
                if (lowerCaseType.contains("xml"))
                {
                    Document node = nextRecord.getContent(xmlHandle).get();
                    content = createMapFromXML(node.getDocumentElement());
                }
                else if (lowerCaseType.contains("json"))
                {
                    JsonNode jsonNode = nextRecord.getContent(jacksonHandle).get();
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
                    content = nextRecord.getContent(stringHandle).get();
                }
                else
                {
                    content = nextRecord.getContent(binaryHandle).get();
                }
            }

            page.add(content);
        }
        start += fetchSize;
        return page;
    }

    /**
     * This recursive method creates a Map from DOM object
     *
     * @param node XML Node
     * @return an object; type depends on what type of node is being processed
     */
    private Object createMapFromXML(Node node)
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
