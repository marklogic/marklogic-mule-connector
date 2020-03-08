package com.marklogic.mule.extension.connector.api.operation;

import com.marklogic.mule.extension.connector.internal.result.resultset.*;

import java.util.Arrays;
import java.util.List;

public enum MarkLogicMimeType {

    xml{
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            if (xmlRecordExtractor == null) {
                xmlRecordExtractor = new MarkLogicXMLRecordExtractor();
            }
            return xmlRecordExtractor;
        }
    },
    json{
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            if (jsonRecordExtractor == null) {
                jsonRecordExtractor = new MarkLogicJSONRecordExtractor();
            }
            return jsonRecordExtractor;
        }
    },
    text{
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            if (textRecordExtractor == null) {
                textRecordExtractor = new MarkLogicTextRecordExtractor();
            }
            return textRecordExtractor;
        }
    },
    binary{
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            if (binaryRecordExtractor == null) {
                binaryRecordExtractor = new MarkLogicBinaryRecordExtractor();
            }
            return binaryRecordExtractor;
        }


    };

    private static MarkLogicBinaryRecordExtractor binaryRecordExtractor;
    private static MarkLogicXMLRecordExtractor xmlRecordExtractor;
    private static MarkLogicJSONRecordExtractor jsonRecordExtractor;
    private static MarkLogicTextRecordExtractor textRecordExtractor;

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
