/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2024 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.mule.connector.internal.operation;

import com.marklogic.client.DatabaseClient;
import com.marklogic.mule.connector.api.types.DocumentAttributes;
import com.marklogic.mule.connector.internal.error.provider.ExecuteErrorsProvider;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;

import java.io.InputStream;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

public class Operations {

    @Summary("Write one or more documents to MarkLogic, typically from within a Mule 'Batch Aggregator'.")
    @Throws(ExecuteErrorsProvider.class)
    public void writeDocuments(
        @Connection DatabaseClient databaseClient,
        @Content InputStream[] contents,
        // "Settings" seems to be the preferred term in the Anypoint UI.
        @ParameterGroup(name = "Write Settings") WriteParameters writeParameters
    ) {
        writeParameters.writeDocuments(databaseClient, contents);
    }

    @MediaType(value = ANY, strict = false)
    @Summary("Returns the content, URI, and optional metadata for each document matching the query criteria.")
    @Throws(ExecuteErrorsProvider.class)
    public PagingProvider<DatabaseClient, Result<InputStream, DocumentAttributes>> readDocuments(
        @ParameterGroup(name = "Query Settings") QueryParameters queryParameters
    ) {
        return new ReadPagingProvider(queryParameters);
    }
}
