package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.mule.extension.api.DocumentAttributes;
import com.marklogic.mule.extension.api.ErrorType;
import org.jetbrains.annotations.NotNull;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.transform.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ReadPagingProvider extends AbstractPagingProvider implements PagingProvider<DatabaseClient, Result<InputStream, InputStream>> {

    ReadPagingProvider(QueryParameters params) {
        super(params);
    }

    @Override
    public List<Result<InputStream, InputStream>> getPage(DatabaseClient databaseClient) {
        ObjectMapper objectMapper = new ObjectMapper();
        final Transformer transformer = newTransformer();

        List<Result<InputStream, InputStream>> results = new ArrayList<>();
        super.handlePage(databaseClient, documentRecord -> {
            InputStreamHandle contentHandle = new InputStreamHandle();
            InputStream contentStream = documentRecord.getContent(contentHandle).get();
            DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
            if (Utilities.hasText(queryParameters.categories)) {
                documentRecord.getMetadata(metadataHandle);
            }
            String attributes = new DocumentAttributes(documentRecord.getUri(), metadataHandle).toJsonObjectNode(objectMapper, transformer).toString();
            InputStream attributesStream = new ByteArrayInputStream(attributes.getBytes(StandardCharsets.UTF_8));
            Result<InputStream, InputStream> result = Result.<InputStream, InputStream>builder()
                .output(contentStream)
                .attributes(attributesStream)
                .mediaType(makeMediaType(contentHandle.getFormat()))
                .attributesMediaType(MediaType.APPLICATION_JSON)
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

    @NotNull
    private static Transformer newTransformer() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new ModuleException("Unable to create new Transformer", ErrorType.TRANSFORMER_FACTORY_ERROR, e);
        }
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return transformer;
    }
}
