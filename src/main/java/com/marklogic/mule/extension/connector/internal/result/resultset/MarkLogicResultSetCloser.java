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

import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Closes a {@link MarkLogicConnection} once it has been processed
 *
 * @since 1.0.1
 */
public class MarkLogicResultSetCloser
{
    private static final Logger logger = LoggerFactory.getLogger(MarkLogicResultSetCloser.class);
    private final MarkLogicConnection connection;

    public MarkLogicResultSetCloser(MarkLogicConnection connection)
    {
        this.connection = connection;
    }

    public void closeResultSets()
    {
        /* It appears nothing should happen here, as the connection is automatically closed when the query has
           returned all of its results. */
        logger.debug("Closing results in MarkLogicResultSetCloser");
    }
}
