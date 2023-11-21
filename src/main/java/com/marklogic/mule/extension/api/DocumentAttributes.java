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

import com.marklogic.client.io.DocumentMetadataHandle;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Set;

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
