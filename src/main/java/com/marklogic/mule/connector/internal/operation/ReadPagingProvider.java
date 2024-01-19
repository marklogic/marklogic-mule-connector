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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.client.io.JacksonParserHandle;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.mule.connector.internal.Utilities;
import com.marklogic.mule.connector.api.types.DocumentAttributes;
import com.marklogic.mule.connector.internal.error.ErrorType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

class ReadPagingProvider implements PagingProvider<DatabaseClient, Result<InputStream, InputStream>> {

    private static final Logger logger = LoggerFactory.getLogger(ReadPagingProvider.class);

    private final QueryParameters queryParameters;
    private final int pageLength;
    private final ObjectMapper objectMapper;
    private final Supplier<Transformer> transformerSupplier;

    // State that changes as the provider iterates through pages.
    private int totalDocumentsDelivered = 0;
    private Integer currentPage = -1;
    private Long serverTimestamp = null;
    private TransformerFactory transformerFactory;

    ReadPagingProvider(QueryParameters params) {
        this.queryParameters = params;
        this.pageLength = params.pageLength != null ? params.pageLength : 100;
        this.objectMapper = new ObjectMapper();
        this.transformerSupplier = this::newTransformer;
    }

    @Override
    public List<Result<InputStream, InputStream>> getPage(DatabaseClient databaseClient) {
        currentPage++;
        logger.debug("Reading page {} of results.", currentPage);

        GenericDocumentManager documentManager = databaseClient.newDocumentManager();
        if (Utilities.hasText(queryParameters.categories)) {
            documentManager.setMetadataCategories(queryParameters.buildMetadataCategories());
        }

        final QueryDefinition queryDefinition = queryParameters.makeQueryDefinition(databaseClient);
        initializeServerTimestampIfNecessary(documentManager, queryDefinition);

        int startingPosition = (currentPage * pageLength) + 1;
        documentManager.setPageLength(pageLength);
        DocumentPage documentPage = serverTimestamp != null ?
            documentManager.search(queryDefinition, startingPosition, serverTimestamp) :
            documentManager.search(queryDefinition, startingPosition);

        final List<Result<InputStream, InputStream>> results = new ArrayList<>();
        while (documentPage.hasNext()) {
            DocumentRecord document = documentPage.next();
            if (queryParameters.maxResults != null) {
                if (totalDocumentsDelivered < queryParameters.maxResults) {
                    results.add(documentToResult(document));
                    totalDocumentsDelivered++;
                } else {
                    break;
                }
            } else {
                results.add(documentToResult(document));
                totalDocumentsDelivered++;
            }
        }
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

    private void initializeServerTimestampIfNecessary(GenericDocumentManager documentManager, QueryDefinition queryDefinition) {
        if (queryParameters.consistentSnapshot && serverTimestamp == null) {
            JacksonParserHandle searchHandle = new JacksonParserHandle();
            documentManager.setPageLength(1);
            documentManager.search(queryDefinition, 1, searchHandle);
            serverTimestamp = searchHandle.getServerTimestamp();
            logger.info("Using consistent snapshot with server timestamp: {}", serverTimestamp);
        }
    }
    
    private Result<InputStream, InputStream> documentToResult(DocumentRecord documentRecord) {
        InputStreamHandle contentHandle = documentRecord.getContent(new InputStreamHandle());
        DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
        if (Utilities.hasText(queryParameters.categories)) {
            documentRecord.getMetadata(metadataHandle);
        }
        InputStream attributes = new DocumentAttributes(documentRecord.getUri(), metadataHandle).serializeToJsonStream(objectMapper, transformerSupplier);
        return Result.<InputStream, InputStream>builder()
            .output(contentHandle.get())
            .attributes(attributes)
            .mediaType(makeMediaType(contentHandle.getFormat()))
            .attributesMediaType(MediaType.APPLICATION_JSON)
            .build();
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

    /**
     * Per https://stackoverflow.com/questions/47341723/javax-transformer-fails-on-high-concurrent-environment , a
     * Transformer is reusable but depending on the implementation, it may be better to create a new one each time.
     * Testing has shown no real difference with creating a new one each time. So the TransformerFactory is reused, but
     * a new Transformer is created each time. Note that multithreading does not appear possible here as Mule runs a
     * PagingProvider via a single thread.
     *
     * @return
     */
    private Transformer newTransformer() {
        if (this.transformerFactory == null) {
            this.transformerFactory = TransformerFactory.newInstance();
        }
        final Transformer transformer;
        try {
            // Copied from https://stackoverflow.com/questions/32178558/how-to-prevent-xml-external-entity-injection-on-transformerfactory
            // to make Sonar happy about preventing external entities from being processed.
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new ModuleException("Unable to create new Transformer", ErrorType.XML_TRANSFORMER_ERROR, e);
        }
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return transformer;
    }
}
