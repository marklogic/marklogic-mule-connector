package com.marklogic.mule.extension.connector.internal.result.resultset;

import com.marklogic.client.document.DocumentRecord;
import com.marklogic.mule.extension.connector.api.operation.MarkLogicMimeType;

/**
 * Created by jkrebs on 9/25/2019.
 */
public abstract class MarkLogicRecordExtractor {

    protected abstract Object extractRecord(DocumentRecord record);

    public static Object extractSingleRecord(DocumentRecord record) {
        MarkLogicRecordExtractor re = MarkLogicMimeType.fromString(record.getMimetype()).getRecordExtractor();
        return re.extractRecord(record);
    }
}
