package com.marklogic.mule.extension.connector.internal.result.resultset;

import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;

/**
 * Closes a {@link MarkLogicConnection} once it has been processed
 *
 * @since 1.0.1
 */

public class MarkLogicResultSetCloser {

    private final MarkLogicConnection connection;

    public MarkLogicResultSetCloser(MarkLogicConnection connection) {
        this.connection = connection;
    }

    public void closeResultSets() {
        /* It appears nothing should happen here, as the connection is automatically closed when the query has
           returned all of its results. */
    }
}
