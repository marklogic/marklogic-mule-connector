package com.marklogic.mule.extension.connector.internal.result.resultset;

import com.marklogic.client.datamovement.ExportListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkrebs on 9/27/2019.
 * The purpose of this class is to support paging of results from MarkLogic DMSDK to MuleSoft PagingProvider
 */
public class MarkLogicExportListener extends ExportListener {
    private List<Object> docs = new ArrayList<>();

    public MarkLogicExportListener() {
        super();
        this.onDocumentReady(doc-> {
            try {
                docs.add(MarkLogicRecordExtractor.extractRecord(doc));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public List<Object> getDocs() {
        return docs;
    }

    public void clearDocs() {
        docs.clear();
    }


}
