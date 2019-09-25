/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2019 MarkLogic Corporation.
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

    private final MarkLogicConfiguration configuration;

    private DocumentPage documents = null;

    private DocumentManager dm;

    private int start = 1;
    private QueryDefinition query;

    public MarkLogicResultSetIterator(MarkLogicConnection connection, MarkLogicConfiguration configuration, QueryDefinition query)
    {
        this.configuration = configuration;
        this.query = query;
        DatabaseClient client = connection.getClient();
        dm = client.newDocumentManager();
        dm.setPageLength(configuration.getBatchSize());
    }

    @Override
    public boolean hasNext()
    {
        return (start == 1 || documents.hasNextPage());
    }

    @Override
    public List<Object> next()
    {

        if (logger.isInfoEnabled())
        {
            logger.info("iterator query: " + query.toString());
        }

        documents = dm.search(query, start);
        int fetchSize = configuration.getBatchSize();
        final List<Object> page = new ArrayList<>(fetchSize);
        for (int i = 0; i < fetchSize && documents.hasNext(); i++)
        {
            DocumentRecord nextRecord = documents.next();
            Object content = MarkLogicRecordExtractor.extractRecord(nextRecord);
            page.add(content);
        }
        start += fetchSize;
        return page;
    }

}
