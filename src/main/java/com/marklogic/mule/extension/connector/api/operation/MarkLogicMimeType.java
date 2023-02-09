/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2021 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.api.operation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.io.BytesHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// sonarqube wants these to be uppercase, but cannot change them in the 1.x timeline since they're part of the
// public API
@SuppressWarnings("java:S115")
public enum MarkLogicMimeType {

    xml {
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            return doc -> doc.getContent(new StringHandle()).withMimetype("application/xml").withFormat(Format.XML).get();
        }
    },
    json {
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            return new JSONRecordExtractor();
        }
    },
    text {
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            return doc -> doc.getContent(new StringHandle()).get();
        }
    },
    binary {
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            return doc -> doc.getContent(new BytesHandle()).get();
        }
    };

    public static Object extractSingleRecord(DocumentRecord documentRecord) {
        return MarkLogicMimeType.fromString(documentRecord.getMimetype()).getRecordExtractor().extractRecord(documentRecord);
    }

    public static MarkLogicMimeType fromString(String mimeString)
    {
        MarkLogicMimeType mimeObj = MarkLogicMimeType.binary;

        if (mimeString != null)
        {
            List<String> typeString = Arrays.asList(mimeString.split("/"));
            if (typeString.contains("xml")) {
                mimeObj = MarkLogicMimeType.xml;
            }
            else if (typeString.contains("json")) {
                mimeObj =  MarkLogicMimeType.json;
            }
            else if (typeString.contains("text")) {
                mimeObj =  MarkLogicMimeType.text;
            }
        }

        return mimeObj;
    }

    public abstract MarkLogicRecordExtractor getRecordExtractor();

    private static class JSONRecordExtractor implements MarkLogicRecordExtractor {
        private static ObjectMapper jsonMapper = new ObjectMapper();

        @Override
        public Object extractRecord(DocumentRecord documentRecord) {
            Object content;
            JsonNode jsonNode = documentRecord.getContent(new JacksonHandle()).get();
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

}
