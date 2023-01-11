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

import com.marklogic.mule.extension.connector.internal.result.resultset.*;

import java.util.Arrays;
import java.util.List;

// sonarqube wants these to be uppercase, but cannot change them in the 1.x timeline since they're part of the
// public API
@SuppressWarnings("java:S115")
public enum MarkLogicMimeType {

    xml {
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            return xmlRecordExtractor;
        }
    },
    json {
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            return jsonRecordExtractor;
        }
    },
    text {
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            return textRecordExtractor;
        }
    },
    binary {
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            return binaryRecordExtractor;
        }


    };

    private static MarkLogicBinaryRecordExtractor binaryRecordExtractor = new MarkLogicBinaryRecordExtractor();
    private static MarkLogicXMLRecordExtractor xmlRecordExtractor = new MarkLogicXMLRecordExtractor();
    private static MarkLogicJSONRecordExtractor jsonRecordExtractor = new MarkLogicJSONRecordExtractor();
    private static MarkLogicTextRecordExtractor textRecordExtractor = new MarkLogicTextRecordExtractor();

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

}
