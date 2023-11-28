/*
 * Copyright (c) 2023 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.Format;
import com.marklogic.mule.extension.api.DocumentAttributes;
import com.marklogic.mule.extension.api.DocumentFormat;
import com.marklogic.mule.extension.api.QueryFormat;
import com.marklogic.mule.extension.api.QueryType;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;

import java.io.InputStream;
import java.util.List;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

public class Operations {

    @Summary("Write one or more documents to MarkLogic, typically from within a Mule 'Batch Aggregator'.")
    public void writeDocuments(
        @Connection DatabaseClient databaseClient,
        @Content InputStream[] contents,
        @Summary("Format of documents to write to MarkLogic. Selecting 'UNKNOWN' will result in MarkLogic determining the format based on the URI extension.") @Optional(defaultValue = "JSON")
        DocumentFormat format,
        @Summary("Comma-delimited collections to assign to each document.") @Optional @Example("collection1,collection2")
        String collections,
        @Summary("Comma-delimited permissions to assign to each document; format is role,capability,role,capability.") @Optional @Example("rest-reader,read,rest-writer,update")
        String permissions,
        @Summary("Temporal collection to assign each document to.") @DisplayName("Temporal Collection") @Optional @Example("temporal-collection")
        String temporalCollection,
        @Summary("Quality score to assign to each document.") @Optional(defaultValue = "0")
        int quality,
        @Summary("String to prepend to each document URI.") @DisplayName("URI Prefix") @Optional @Example("/example/")
        String uriPrefix,
        @Summary("String to append to each document URI.") @DisplayName("URI Suffix") @Optional @Example(".json")
        String uriSuffix,
        @Summary("Whether to include a UUID in each document URI.") @DisplayName("Generate UUID") @Optional(defaultValue = "True")
        boolean generateUUID,
        @Summary("Name of a REST server transform to apply to each document.") @DisplayName("Transform") @Optional
        String transform,
        @Summary("Comma-delimited parameters to pass to the REST server transform.") @DisplayName("Transform Parameters") @Optional @Example("param1,value1,param2,value2")
        String transformParameters,
        @Summary("Delimiter to use for defining 'Transform Parameters'.") @DisplayName("Transform Parameters Delimiter") @Optional(defaultValue = ",")
        String transformParametersDelimiter
    ) {
        new WriteOperations().writeDocuments(databaseClient, contents,
            format != null ? format.getFormat() : Format.UNKNOWN, permissions, quality, collections,
            uriPrefix, uriSuffix, generateUUID, temporalCollection, transform, transformParameters, transformParametersDelimiter);
    }

    @MediaType(value = ANY, strict = false)
    @Summary("Returns the content, URI, and optional metadata for each document matching the query criteria.")
    public PagingProvider<DatabaseClient, Result<InputStream, DocumentAttributes>> readDocuments(
        @DisplayName("Document URIs") @Optional
        @Summary("Specify one or more document URIs to read. If specified, all other parameters for querying will be ignored.") List<String> uris,
        @DisplayName("Collections") @Optional(defaultValue = "") @Example("myCollection1,myCollection2") String collections,
        @DisplayName("Query") @Text @Optional @Example("searchTerm") String query,
        @DisplayName("Query Type") @Optional QueryType queryType,
        @DisplayName("Query Format") @Optional QueryFormat queryFormat,
        @DisplayName("Metadata Category List") @Optional(defaultValue = "all") @Example("COLLECTIONS,PERMISSIONS") String categories,
        @DisplayName("Max Results") @Optional @Example("10") Integer maxResults,
        @DisplayName("Page Length") @Optional @Example("10") Integer pageLength,
        @DisplayName("Search Options") @Optional @Example("appSearchOptions") String searchOptions,
        @DisplayName("Directory") @Optional @Example("/customerData") String directory,
        @DisplayName("REST Transform") @Optional String restTransform,
        @DisplayName("REST Transform Parameters") @Optional String restTransformParameters,
        @DisplayName("REST Transform Parameters Delimiter") @Optional @Example(",") String restTransformParametersDelimiter,
        @DisplayName("Consistent Snapshot") @Optional(defaultValue = "True") boolean consistentSnapshot
    ) {
        return new ReadPagingProvider(uris, collections, query, queryType, queryFormat, categories,
            maxResults, pageLength, searchOptions, directory, restTransform, restTransformParameters,
            restTransformParametersDelimiter, consistentSnapshot);
    }

    @MediaType(value = ANY, strict = false)
    @Summary("Returns the content of documents matching the query criteria.")
    public PagingProvider<DatabaseClient, InputStream> exportDocuments(
        @DisplayName("Document URIs") @Optional
        @Summary("Specify one or more document URIs to read. If specified, all other parameters for querying will be ignored.") List<String> uris,
        @DisplayName("Collections") @Optional @Example("myCollection1,myCollection2") String collections,
        @DisplayName("Query") @Text @Optional @Example("searchTerm") String query,
        @DisplayName("Query Type") @Optional QueryType queryType,
        @DisplayName("Query Format") @Optional QueryFormat queryFormat,
        @DisplayName("Metadata Category List") @Optional(defaultValue = "all") @Example("COLLECTIONS,PERMISSIONS") String categories,
        @DisplayName("Max Results") @Optional @Example("10") Integer maxResults,
        @DisplayName("Page Length") @Optional @Example("10") Integer pageLength,
        @DisplayName("Search Options") @Optional @Example("appSearchOptions") String searchOptions,
        @DisplayName("Directory") @Optional @Example("/customerData") String directory,
        @DisplayName("REST Transform") @Optional String restTransform,
        @DisplayName("REST Transform Parameters") @Optional String restTransformParameters,
        @DisplayName("REST Transform Parameters Delimiter") @Optional @Example(",") String restTransformParametersDelimiter,
        @DisplayName("Consistent Snapshot") @Optional(defaultValue = "True") boolean consistentSnapshot
    ) {
        return new ExportPagingProvider(uris, collections, query, queryType, queryFormat, categories,
            maxResults, pageLength, searchOptions, directory, restTransform, restTransformParameters,
            restTransformParametersDelimiter, consistentSnapshot);
    }
}
