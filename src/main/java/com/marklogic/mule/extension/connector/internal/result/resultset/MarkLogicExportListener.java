/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2023 MarkLogic Corporation.
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

import com.marklogic.client.datamovement.ExportListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MarkLogicExportListener extends ExportListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicExportListener.class);

    private List<Object> docs;

    private final RecordExtractor recordExtractor = new RecordExtractor();

    private int resultCount;

    public MarkLogicExportListener(long maxDocs) {
        super();
        if (maxDocs > 0) {
            addDocsToListUntilMax(maxDocs);
        } else {
            addAllDocsToList();
        }
        this.onFailure((batch, throwable) -> LOGGER.error("Unable to process batch; URIs: {}; cause: {}",
            Arrays.asList(batch.getItems()), throwable.getMessage())
        );
    }

    private void addDocsToListUntilMax(long maxDocs) {
        this.docs = new ArrayList<>();
        // Must synchronize entire operation so that both the check on the number of documents and the addition to
        // the list are threadsafe.
        this.onDocumentReady(doc -> {
            synchronized (docs) {
                if (resultCount < maxDocs) {
                    resultCount++;
                    docs.add(recordExtractor.extractRecord(doc));
                }
            }
        });
    }

    private void addAllDocsToList() {
        // If no limit is set, just need threadsafe access to the list.
        this.docs = Collections.synchronizedList(new ArrayList<>());
        this.onDocumentReady(doc -> docs.add(recordExtractor.extractRecord(doc)));
    }

    public List<Object> getDocs() {
        return docs;
    }
}
