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
package com.marklogic.mule.connector.api.types;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Set;

public class DocumentAttributes {

    private final String uri;
    private final Set<String> collections;
    private final Map<String, Set<String>> permissions;
    private final Map<QName, String> properties;
    private final Map<String, String> metadataValues;
    private final int quality;

    public DocumentAttributes(
        String uri, Set<String> collections, Map<String, Set<String>> permissions,
        Map<QName, String> properties, Map<String, String> metadataValues, int quality)
    {
        this.uri = uri;
        this.collections = collections;
        this.permissions = permissions;
        this.properties = properties;
        this.metadataValues = metadataValues;
        this.quality = quality;
    }

    @Override
    public String toString() {
        // This is solely for testing/debugging.
        return "[" + uri +
                ";" + collections +
                ";" + permissions +
                ";" + quality +
                ";" + metadataValues +
                ";" + properties +
                "]";
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

    public Map<String, Set<String>> getPermissions() {
        return permissions;
    }

    public Map<QName, String> getProperties() {
        return properties;
    }

    public Map<String, String> getMetadataValues() {
        return metadataValues;
    }
}
