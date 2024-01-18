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
package com.marklogic.mule.connector.api.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.mule.connector.internal.error.ErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Wraps a document's URI and metadata and knows how to serialize the data to JSON and deserialize it from JSON.
 * <p>
 * This is not intended to be part of the API package as it is not exposed in any of the public operations methods.
 * But we are not able to reuse it within our tests unless it's in this package.
 */
public class DocumentAttributes {

    private final String uri;
    private final DocumentMetadataHandle metadata;

    private static final String QUALITY = "quality";
    private static final String COLLECTIONS = "collections";
    private static final String PERMISSIONS = "permissions";
    private static final String PROPERTIES = "properties";
    private static final String METADATA_VALUES = "metadataValues";


    public DocumentAttributes(String uri, DocumentMetadataHandle metadata) {
        this.uri = uri;
        this.metadata = metadata;
    }

    /**
     * @param objectMapper
     * @param transformerSupplier a Supplier is used to lazily instantiate this in case it's never needed.
     * @return
     */
    public InputStream serializeToJsonStream(ObjectMapper objectMapper, Supplier<Transformer> transformerSupplier) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("uri", uri);
        serializeCollections(node);
        serializePermissions(node);
        serializeProperties(transformerSupplier, node);
        serializeMetadataValues(node);
        node.put(QUALITY, metadata.getQuality());
        return new ByteArrayInputStream(node.toString().getBytes(StandardCharsets.UTF_8));
    }

    public DocumentAttributes(String attributesJson) throws JsonProcessingException {
        JsonNode attributesNode = new ObjectMapper().readTree(attributesJson);
        this.uri = (attributesNode.get("uri") != null) ? attributesNode.get("uri").textValue() : null;
        this.metadata = new DocumentMetadataHandle();
        deserializeCollections(attributesNode);
        deserializePermissions(attributesNode);
        deserializeProperties(attributesNode);
        deserializeMetadataValues(attributesNode);
        if (attributesNode.has(QUALITY)) {
            metadata.setQuality(attributesNode.get(QUALITY).asInt());
        }
    }

    private void serializeCollections(ObjectNode node) {
        ArrayNode collections = node.putArray(COLLECTIONS);
        metadata.getCollections().forEach(collections::add);
    }

    private void serializePermissions(ObjectNode node) {
        ArrayNode permissionsArray = node.putArray(PERMISSIONS);
        metadata.getPermissions().forEach((role, permList) -> {
            ObjectNode roleNode = permissionsArray.addObject();
            ArrayNode permArray = roleNode.putArray(role);
            permList.forEach(perm -> permArray.add(perm.name()));
        });
    }

    private void serializeProperties(Supplier<Transformer> transformerSupplier, ObjectNode node) {
        ArrayNode propertiesArray = node.putArray(PROPERTIES);
        metadata.getProperties().forEach((property, value) -> {
            ObjectNode propertyNode = propertiesArray.addObject();
            if (value instanceof Node) {
                Writer out = new StringWriter();
                try {
                    transformerSupplier.get().transform(new DOMSource(((Node) value).getFirstChild()), new StreamResult(out));
                    propertyNode.put(property.toString(), out.toString());
                } catch (TransformerException e) {
                    throw new ModuleException(ErrorType.XML_TRANSFORMER_ERROR, e);
                }
            } else {
                propertyNode.put(property.toString(), value.toString());
            }
        });
    }

    private void serializeMetadataValues(ObjectNode node) {
        ArrayNode metadataValuesArray = node.putArray(METADATA_VALUES);
        metadata.getMetadataValues().forEach((key, value) -> {
            ObjectNode propertyNode = metadataValuesArray.addObject();
            propertyNode.put(key, value);
        });
    }

    private void deserializeCollections(JsonNode attributesNode) {
        if (attributesNode.has(COLLECTIONS)) {
            for (final JsonNode collectionNode : attributesNode.get(COLLECTIONS)) {
                this.metadata.withCollections(collectionNode.textValue());
            }
        }
    }

    private void deserializePermissions(JsonNode attributesNode) {
        if (attributesNode.has(PERMISSIONS)) {
            attributesNode.get(PERMISSIONS).forEach(permObj -> {
                String roleName = permObj.fieldNames().next();
                Set<DocumentMetadataHandle.Capability> capabilities = new HashSet<>();
                permObj.get(roleName).forEach(capability ->
                    capabilities.add(DocumentMetadataHandle.Capability.valueOf(capability.textValue()))
                );
                this.metadata.withPermission(roleName, capabilities.toArray(new DocumentMetadataHandle.Capability[]{}));
            });
        }
    }

    private void deserializeProperties(JsonNode attributesNode) {
        if (attributesNode.has(PROPERTIES)) {
            attributesNode.get(PROPERTIES).forEach(propObj -> {
                String propName = propObj.fieldNames().next();
                String propVal = propObj.get(propName).asText();
                this.metadata.withProperty(new QName(propName), propVal);
            });
        }
    }

    private void deserializeMetadataValues(JsonNode attributesNode) {
        if (attributesNode.has(METADATA_VALUES)) {
            attributesNode.get(METADATA_VALUES).forEach(metadataValueObj -> {
                String name = metadataValueObj.fieldNames().next();
                String val = metadataValueObj.get(name).textValue();
                this.metadata.withMetadataValue(name, val);
            });
        }
    }

    public String getUri() {
        return uri;
    }

    public Set<String> getCollections() {
        return metadata.getCollections();
    }

    public int getQuality() {
        return metadata.getQuality();
    }

    public Map<String, Set<DocumentMetadataHandle.Capability>> getPermissions() {
        return metadata.getPermissions();
    }

    public Map<QName, Object> getProperties() {
        return metadata.getProperties();
    }

    public Map<String, String> getMetadataValues() {
        return metadata.getMetadataValues();
    }
}
