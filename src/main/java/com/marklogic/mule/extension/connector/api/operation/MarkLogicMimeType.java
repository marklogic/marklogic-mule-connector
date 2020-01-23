package com.marklogic.mule.extension.connector.api.operation;

import com.marklogic.mule.extension.connector.internal.result.resultset.*;

public enum MarkLogicMimeType {

    XML{
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            if (xmlRecordExtractor == null) {
                xmlRecordExtractor = new MarkLogicXMLRecordExtractor();
            }
            return xmlRecordExtractor;
        }
    },
    JSON{
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            if (jsonRecordExtractor == null) {
                jsonRecordExtractor = new MarkLogicJSONRecordExtractor();
            }
            return jsonRecordExtractor;
        }
    },
    TEXT{
        @Override
        public MarkLogicRecordExtractor getRecordExtractor() {
            if (textRecordExtractor == null) {
                textRecordExtractor = new MarkLogicTextRecordExtractor();
            }
            return textRecordExtractor;
        }
    },
    BINARY{
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

    public static MarkLogicMimeType fromString(String mimeString) {
        MarkLogicMimeType mimeObj = MarkLogicMimeType.BINARY;
        if (mimeString == null) {
            mimeObj = Enum.valueOf(MarkLogicMimeType.class,mimeString);
        }
        return mimeObj;
    }

    public abstract MarkLogicRecordExtractor getRecordExtractor();
}
