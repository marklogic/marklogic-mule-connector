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
package com.marklogic.mule.extension.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.io.DocumentMetadataHandle;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class DocumentAttributes {

    private final String uri;
    private final Set<String> collections;
    private final Map<String, Set<DocumentMetadataHandle.Capability>> permissions;
    private final Map<QName, Object> properties;
    private final Map<String, String> metadataValues;
    private final int quality;

    public DocumentAttributes(String uri, DocumentMetadataHandle handle) {
        this.uri = uri;
        this.collections = handle.getCollections();
        this.permissions = handle.getPermissions();
        this.properties = handle.getProperties();
        this.metadataValues = handle.getMetadataValues();
        this.quality = handle.getQuality();
    }

    public DocumentAttributes(JsonNode attributesNode) {
        this.uri = (attributesNode.get("uri") != null) ? attributesNode.get("uri").textValue() : null;
        collections = buildCollections(attributesNode);
        permissions = buildPermissions(attributesNode);
        properties = buildProperties(attributesNode);
        metadataValues = buildMetadataValues(attributesNode);
        if (attributesNode.get("quality") != null) {
            this.quality = Integer.parseInt(attributesNode.get("quality").asText());
        } else {
            this.quality = 0;
        }
    }

    private Set<String> buildCollections(JsonNode attributesNode) {
        final Set<String> collectionsMap = new HashSet<>();
        if (attributesNode.get("collections") != null) {
            for (final JsonNode collectionNode : attributesNode.get("collections")) {
                collectionsMap.add(collectionNode.textValue());
            }
        }
        return collectionsMap;
    }

    private Map<String, Set<DocumentMetadataHandle.Capability>> buildPermissions(JsonNode attributesNode) {
        final Map<String, Set<DocumentMetadataHandle.Capability>> permissionsMap = new HashMap<>();
        if (attributesNode.get("permissions") != null) {
            attributesNode.get("permissions").forEach(permObj -> {
                String roleName = permObj.fieldNames().next();
                Set<DocumentMetadataHandle.Capability> permList = new HashSet<>();
                permObj.get(roleName).forEach(capability -> {
                    String capString = capability.textValue();
                    permList.add(DocumentMetadataHandle.Capability.valueOf(capString));
                });
                permissionsMap.put(roleName, permList);
            });
        }
        return permissionsMap;
    }

    private Map<QName, Object> buildProperties(JsonNode attributesNode) {
        final Map<QName, Object> propertiesMap = new HashMap<>();
        if (attributesNode.get("properties") != null) {
            attributesNode.get("properties").forEach(propObj -> {
                String propName = propObj.fieldNames().next();
                String propVal = propObj.get(propName).asText();
                propertiesMap.put(new QName(propName), propVal);
            });
        }
        return propertiesMap;
    }

    private Map<String, String> buildMetadataValues(JsonNode attributesNode) {
        final Map<String, String> metadataValuesMap = new HashMap<>();
        if (attributesNode.get("metadataValues") != null) {
            attributesNode.get("metadataValues").forEach(metadataValueObj -> {
                String name = metadataValueObj.fieldNames().next();
                String val = metadataValueObj.get(name).textValue();
                metadataValuesMap.put(name, val);
            });
        }
        return metadataValuesMap;
    }

    public ObjectNode toJsonObjectNode(ObjectMapper objectMapper, Transformer transformer) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("uri", uri);
        addCollectionsToJsonObjectNode(node);
        addPermissionsToJsonObjectNode(node);
        addPropertiesToJsonObjectNode(transformer, node);
        addMetadataValuesToJsonObjectNode(node);
        node.put("quality", quality);
        return node;
    }

    private void addCollectionsToJsonObjectNode(ObjectNode node) {
        ArrayNode colls = node.putArray("collections");
        if (collections != null) {
            collections.forEach(c -> colls.add(c));
        }
    }

    private void addPermissionsToJsonObjectNode(ObjectNode node) {
        ArrayNode permissionsArray = node.putArray("permissions");
        if (permissions != null) {
            permissions.forEach((role, permList) -> {
                ObjectNode roleNode = permissionsArray.addObject();
                ArrayNode permArray = roleNode.putArray(role);
                permList.forEach(perm -> permArray.add(perm.name()));
            });
        }
    }

    private void addPropertiesToJsonObjectNode(Transformer transformer, ObjectNode node) {
        ArrayNode propertiesArray = node.putArray("properties");
        if (properties != null) {
            properties.forEach((property, value) -> {
                ObjectNode propertyNode = propertiesArray.addObject();
                if (value instanceof Node) {
                    Writer out = new StringWriter();
                    try {
                        transformer.transform(new DOMSource(((Node) value).getFirstChild()), new StreamResult(out));
                        propertyNode.put(property.toString(), out.toString());
                    } catch (TransformerException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    propertyNode.put(property.toString(), value.toString());
                }
            });
        }
    }

    private void addMetadataValuesToJsonObjectNode(ObjectNode node) {
        ArrayNode metadataValuesArray = node.putArray("metadataValues");
        if (metadataValues != null) {
            metadataValues.forEach((key, value) -> {
                ObjectNode propertyNode = metadataValuesArray.addObject();
                propertyNode.put(key, value);
            });
        }
    }

    public static DocumentAttributes buildDocumentAttributes(String attributesJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(attributesJson);
        return new DocumentAttributes(node);
    }

    @Override
    public String toString() {
        // This is solely for testing/debugging.
        return new StringBuilder()
            .append("[").append(uri)
            .append(";").append(collections)
            .append(";").append(permissions)
            .append(";").append(quality)
            .append(";").append(metadataValues)
            .append(";").append(properties)
            .append("]")
            .toString();
    }

    public String getUri() {
        return uri;
    }

    public Set<String> getCollections() {
        return collections;
    }

    public int getQuality() {
        return quality;
    }

    public Map<String, Set<DocumentMetadataHandle.Capability>> getPermissions() {
        return permissions;
    }

    public Map<QName, Object> getProperties() {
        return properties;
    }

    public Map<String, String> getMetadataValues() {
        return metadataValues;
    }
}
