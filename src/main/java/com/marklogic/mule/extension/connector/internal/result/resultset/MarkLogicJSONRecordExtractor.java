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
import com.marklogic.client.io.JacksonHandle;

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
