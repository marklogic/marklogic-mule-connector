package com.marklogic.mule.extension.connector.internal.result.resultset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.io.BytesHandle;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.mule.extension.connector.api.operation.MarkLogicMimeType;
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
public abstract class MarkLogicRecordExtractor {

    protected abstract Object extractRecord(DocumentRecord record);

    public static Object extractSingleRecord(DocumentRecord record) {
        MarkLogicRecordExtractor re = MarkLogicMimeType.fromString(record.getMimetype()).getRecordExtractor();
        return re.extractRecord(record);
    }
}
