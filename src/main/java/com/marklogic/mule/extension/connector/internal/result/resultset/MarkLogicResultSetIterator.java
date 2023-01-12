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
package com.marklogic.mule.extension.connector.internal.result.resultset;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Iterates across all results returned by a synchronous {@link QueryDefinition}
 * execution.
 *
 * @since 1.0.1
 *
 */
//N.B.: Support server-side transforms
public class MarkLogicResultSetIterator implements Iterator<Object>
{

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicResultSetIterator.class);
    private DocumentPage documentPage = null;
    private final GenericDocumentManager documentManager;
    private final QueryDefinition query;
    private long maxResults = 0;
    private final AtomicLong start = new AtomicLong(1);
    private final AtomicLong resultCount = new AtomicLong(0);

    public MarkLogicResultSetIterator(MarkLogicConnection connection, QueryDefinition query, Integer pageLength, Long maxResults)
    {
        this.query = query;
        DatabaseClient client = connection.getClient();
        documentManager = client.newDocumentManager();
        if (pageLength != null) {
            documentManager.setPageLength(pageLength);
        }
        if (maxResults != null)  {
            this.maxResults = maxResults;
        }
    }

    @Override
    public boolean hasNext()
    {
        boolean isFirstPageHasNext = start.longValue() == 1 || documentPage.hasNextPage();
        boolean notAtEnd = maxResults == 0 || resultCount.get() < maxResults;
        return isFirstPageHasNext && notAtEnd;
    }

    @Override
    public List<Object> next()
    {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("iterator query: {}", query);
        }

        long fetchSize = documentManager.getPageLength();
        documentPage = documentManager.search(query, start.getAndAdd(fetchSize));
        final List<Object> page = new ArrayList<>((int)fetchSize);
        for (int i = 0; i < fetchSize && documentPage.hasNext(); i++)
        {
            if ((maxResults > 0) && (resultCount.getAndIncrement() >= maxResults)) {
                LOGGER.info("Processed the user-supplied maximum number of results, which is {}", maxResults);
                break;
            }
            DocumentRecord nextRecord = documentPage.next();
            Object content = MarkLogicRecordExtractor.extractSingleRecord(nextRecord);
            page.add(content);
        }
        documentPage.close();
        return page;
    }
}