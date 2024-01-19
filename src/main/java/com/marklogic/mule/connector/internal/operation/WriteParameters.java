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
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.ServerTransform;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.mule.connector.internal.Utilities;
import com.marklogic.mule.connector.api.types.DocumentFormat;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.UUID;

public class WriteParameters {
    private static final Logger logger = LoggerFactory.getLogger(WriteParameters.class);

    @Parameter
    @Summary("Format of documents to write to MarkLogic. Selecting 'UNKNOWN' will result in MarkLogic determining the format based on the URI extension.")
    @Optional(defaultValue = "JSON")
    DocumentFormat documentFormat;

    @Parameter
    @Summary("Comma-delimited collections to assign to each document.")
    @Optional
    @Example("collection1,collection2")
    String collections;

    @Parameter
    @Summary("Comma-delimited permissions to assign to each document; format is role,capability,role,capability.")
    @Optional
    @Example("rest-reader,read,rest-writer,update")
    String permissions;

    @Parameter
    @Summary("Temporal collection to assign each document to.")
    @DisplayName("Temporal Collection")
    @Optional
    @Example("temporal-collection")
    String temporalCollection;

    @Parameter
    @Summary("Quality score to assign to each document.")
    @Optional(defaultValue = "0")
    int quality;

    @Parameter
    @Summary("String to prepend to each document URI.")
    @DisplayName("URI Prefix")
    @Optional
    @Example("/example/")
    String uriPrefix;

    @Parameter
    @Summary("String to append to each document URI.")
    @DisplayName("URI Suffix")
    @Optional
    @Example(".json")
    String uriSuffix;

    @Parameter
    @Summary("Whether to include a UUID in each document URI.")
    @DisplayName("Generate UUID")
    @Optional(defaultValue = "True")
    boolean generateUUID;

    @Parameter
    @Summary("Name of a REST transform to apply to each document.")
    @DisplayName("Transform")
    @Optional
    String transform;

    @Parameter
    @Summary("Comma-delimited parameters to pass to the REST transform.")
    @DisplayName("Transform Parameters")
    @Optional
    @Example("param1,value1,param2,value2")
    String transformParameters;

    @Parameter
    @Summary("Delimiter to use for defining 'Transform Parameters'.")
    @DisplayName("Transform Parameters Delimiter")
    @Optional(defaultValue = ",")
    String transformParametersDelimiter;

    void writeDocuments(DatabaseClient databaseClient, InputStream[] contents) {
        final DocumentMetadataHandle metadata = makeMetadata();

        final DocumentWriteSet writeSet = databaseClient.newDocumentManager().newWriteSet();
        for (InputStream inputStream : contents) {
            writeSet.add(makeUri(), metadata,
                new InputStreamHandle(inputStream)
                    .withFormat(documentFormat != null ? documentFormat.getFormat() : Format.UNKNOWN));
        }

        final ServerTransform serverTransform = Utilities.hasText(transform) ?
            Utilities.makeServerTransform(transform, transformParameters, transformParametersDelimiter) :
            null;

        if (Utilities.hasText(temporalCollection)) {
            if (documentFormat != null && Format.XML.equals(documentFormat.getFormat())) {
                databaseClient.newXMLDocumentManager().write(writeSet, serverTransform, null, temporalCollection);
            } else {
                databaseClient.newJSONDocumentManager().write(writeSet, serverTransform, null, temporalCollection);
            }
        } else {
            databaseClient.newDocumentManager().write(writeSet, serverTransform);
        }
        logger.debug("Wrote {} documents to the database.", contents.length);
    }

    private DocumentMetadataHandle makeMetadata() {
        DocumentMetadataHandle metadata = new DocumentMetadataHandle().withQuality(quality);
        if (Utilities.hasText(collections)) {
            metadata.withCollections(collections.split(","));
        }
        if (Utilities.hasText(permissions)) {
            metadata.getPermissions().addFromDelimitedString(permissions);
        }
        return metadata;
    }

    private String makeUri() {
        StringBuilder uri = new StringBuilder();
        if (Utilities.hasText(uriPrefix)) {
            uri.append(uriPrefix);
        }
        if (generateUUID) {
            uri.append(UUID.randomUUID());
        }
        if (Utilities.hasText(uriSuffix)) {
            uri.append(uriSuffix);
        }
        return uri.toString();
    }
}
