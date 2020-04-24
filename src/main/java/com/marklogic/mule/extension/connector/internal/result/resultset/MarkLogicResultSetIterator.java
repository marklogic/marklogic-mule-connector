/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2020 MarkLogic Corporation.
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
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.mule.extension.connector.internal.config.MarkLogicConfiguration;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Iterates across all results returned by a synchronous {@link QueryDefinition}
 * execution.
 *
 * @since 1.0.1
 *
 */
//N.b: Support server-side transforms
public class MarkLogicResultSetIterator implements Iterator
{

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicResultSetIterator.class);

    private DocumentPage documents = null;

    private DocumentManager dm;

    private QueryDefinition query;

    private long maxResults = 0;

    private AtomicLong start = new AtomicLong(1);
    private AtomicLong resultCount = new AtomicLong(0);

    public MarkLogicResultSetIterator(MarkLogicConnection connection, MarkLogicConfiguration configuration, QueryDefinition query, Integer pageLength, Long maxResults)
    {
        this.query = query;
        DatabaseClient client = connection.getClient();
        dm = client.newDocumentManager();
        if (pageLength != null) {
            dm.setPageLength(pageLength);
        }
        if (maxResults != null)  {
            this.maxResults = maxResults;
        }
    }

    @Override
    public boolean hasNext()
    {
        boolean isFirstPageHasNext = start.longValue() == 1 || documents.hasNextPage();
        boolean notAtEnd = maxResults == 0 || resultCount.get() < maxResults;
        return isFirstPageHasNext && notAtEnd;
    }

    @Override
    public List<Object> next()
    {

        if (logger.isInfoEnabled())
        {
            logger.info("iterator query: " + query.toString());
        }

        long fetchSize = dm.getPageLength();
        documents = dm.search(query, start.getAndAdd(fetchSize));
        final List<Object> page = new ArrayList<>((int)fetchSize);
        for (int i = 0; i < fetchSize && documents.hasNext(); i++)
        {
            if ((maxResults > 0) && (resultCount.getAndIncrement() >= maxResults)) {
                logger.info("Processed the user-supplied maximum number of results, which is " + maxResults);
                break;
            }
            DocumentRecord nextRecord = documents.next();
            Object content = MarkLogicRecordExtractor.extractSingleRecord(nextRecord);
            page.add(content);
        }
        documents.close();
        return page;
    }

}
