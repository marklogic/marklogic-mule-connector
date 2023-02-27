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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MarkLogicExportListener extends ExportListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicExportListener.class);

    private List<Object> docs = new ArrayList<>();

    private AtomicLong resultCount = new AtomicLong(0);
    private AtomicBoolean maxDocsReached = new AtomicBoolean(false);
    private final RecordExtractor recordExtractor = new RecordExtractor();

    public MarkLogicExportListener(long maxDocs) {
        super();
        this.onDocumentReady(doc -> {
            if (!maxDocsReached.get()) {
                if ((maxDocs > 0) && (resultCount.getAndIncrement() >= maxDocs)) {
                    maxDocsReached.set(true);
                    LOGGER.info("Processed the user-supplied maximum number of results, which is {}", maxDocs);
                } else {
                    docs.add(recordExtractor.extractRecord(doc));
                }
            }
        });
        this.onFailure((batch, throwable) -> LOGGER.error("Unable to process batch; URIs: {}; cause: {}",
            Arrays.asList(batch.getItems()), throwable.getMessage())
        );
    }

    public List<Object> getDocs() {
        return docs;
    }
}
