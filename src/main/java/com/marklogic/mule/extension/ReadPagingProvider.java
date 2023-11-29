package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.mule.extension.api.DocumentAttributes;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ReadPagingProvider extends AbstractPagingProvider implements PagingProvider<DatabaseClient, Result<InputStream, DocumentAttributes>> {

    ReadPagingProvider(QueryParameters params) {
        super(params);
    }

    @Override
    public List<Result<InputStream, DocumentAttributes>> getPage(DatabaseClient databaseClient) {
        List<Result<InputStream, DocumentAttributes>> results = new ArrayList<>();
        super.handlePage(databaseClient, documentRecord -> {
            InputStreamHandle contentHandle = new InputStreamHandle();
            InputStream contentStream = documentRecord.getContent(contentHandle).get();
            DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
            if (Utilities.hasText(queryParameters.categories)) {
                documentRecord.getMetadata(metadataHandle);
            }
            Result<InputStream, DocumentAttributes> result = Result.<InputStream, DocumentAttributes>builder()
                .output(contentStream)
                .attributes(new DocumentAttributes(documentRecord.getUri(), metadataHandle))
                .mediaType(makeMediaType(contentHandle.getFormat()))
                .attributesMediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA)
                .build();
            results.add(result);
        });
        return results;
    }

    @Override
    public Optional<Integer> getTotalResults(DatabaseClient databaseClient) {
        return java.util.Optional.empty();
    }

    @Override
    public void close(DatabaseClient databaseClient) {
        // We don't want to call release here, as the client will no longer be usable by other operations.
    }

    private org.mule.runtime.api.metadata.MediaType makeMediaType(Format format) {
        if (Format.JSON.equals(format)) {
            return org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;
        } else if (Format.XML.equals(format)) {
            return org.mule.runtime.api.metadata.MediaType.APPLICATION_XML;
        } else if (Format.TEXT.equals(format)) {
            return org.mule.runtime.api.metadata.MediaType.TEXT;
        }
        return org.mule.runtime.api.metadata.MediaType.BINARY;
    }
}
