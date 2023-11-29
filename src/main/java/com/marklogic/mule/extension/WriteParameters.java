package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.ServerTransform;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.mule.extension.api.DocumentFormat;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.io.InputStream;
import java.util.UUID;

public class WriteParameters {

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
    @Summary("Name of a REST server transform to apply to each document.")
    @DisplayName("Transform")
    @Optional
    String transform;

    @Parameter
    @Summary("Comma-delimited parameters to pass to the REST server transform.")
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