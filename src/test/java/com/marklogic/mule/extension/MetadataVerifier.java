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
package com.marklogic.mule.extension;

import com.marklogic.mule.connector.api.types.DocumentAttributes;
import org.junit.Assert;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;

public class MetadataVerifier {

    DocumentAttributes attributes;
    String expectedUri;
    Integer expectedCollectionCount = null;
    String[] expectedCollections = null;
    Integer expectedPermissionCount = null;
    String[] expectedPermissions = null;
    Integer expectedQuality = null;
    Integer expectedPropertyCount = null;
    String[] expectedProperties = null;
    Integer expectedMetadataValueCount = null;
    String[] expectedMetadataValues = null;

    private MetadataVerifier(DocumentAttributes attributes, String expectedUri) {
        this.attributes = attributes;
        this.expectedUri = expectedUri;
    }

    public static MetadataVerifier assertMetadata(DocumentAttributes attributes, String jsonUri) {
        return new MetadataVerifier(attributes, jsonUri);
    }

    public MetadataVerifier includesCollections(String... expectedCollections) {
        this.expectedCollections = expectedCollections;
        return this;
    }

    public MetadataVerifier collections(int expectedCollectionCount, String... expectedCollections) {
        this.expectedCollectionCount = expectedCollectionCount;
        this.expectedCollections = expectedCollections;
        return this;
    }

    public MetadataVerifier includesPermissions(String... expectedPermissions) {
        this.expectedPermissions = expectedPermissions;
        return this;
    }

    public MetadataVerifier permissions(int expectedPermissionCount, String... expectedPermissions) {
        this.expectedPermissionCount = expectedPermissionCount;
        this.expectedPermissions = expectedPermissions;
        return this;
    }

    public MetadataVerifier properties(int expectedPropertyCount, String... expectedProperties) {
        this.expectedPropertyCount = expectedPropertyCount;
        this.expectedProperties = expectedProperties;
        return this;
    }

    public MetadataVerifier metadataValues(int expectedMetadataValueCount, String... expectedMetadataValues) {
        this.expectedMetadataValueCount = expectedMetadataValueCount;
        this.expectedMetadataValues = expectedMetadataValues;
        return this;
    }

    public MetadataVerifier quality(int expectedQuality) {
        this.expectedQuality = expectedQuality;
        return this;
    }

    public void verify() {
        if (expectedUri != null) {
            assertEquals("The expected uri should be returned in the attributes.", expectedUri, attributes.getUri());
        }
        verifyCollections();
        verifyProperties();
        verifyPermissions();
        verifyQuality();
        verifyMetadataValues();
    }

    private void verifyQuality() {
        if (expectedQuality != null) {
            assertEquals(((long) expectedQuality), attributes.getQuality());
        }
    }

    private void verifyProperties() {
        if (expectedPropertyCount != null) {
            assertEquals(((long) expectedPropertyCount), attributes.getProperties().size());
            if (expectedPropertyCount > 0) {
                for (int i = 0; i < expectedProperties.length; i = i + 2) {
                    boolean notFound = true;
                    for (QName key : attributes.getProperties().keySet()) {
                        if (key.toString().equals(expectedProperties[i])) {
                            notFound = false;
                            assertEquals(expectedProperties[i + 1], attributes.getProperties().get(key));
                            break;
                        }
                    }
                    if (notFound) {
                        fail("A matching property key was not found.");
                    }
                }
            }
        }
    }

    private void verifyPermissions() {
        if (expectedPermissionCount != null) {
            assertEquals(((long) expectedPermissionCount), attributes.getPermissions().size());
        }
        if (expectedPermissions != null) {
            for (int i = 0; i < expectedPermissions.length; i = i + 2) {
                String expectedPermission = expectedPermissions[i + 1].toUpperCase();
                boolean wasFound = false;
                if (attributes.getPermissions().get(expectedPermissions[i]) != null) {
                    Object[] permissions = attributes.getPermissions().get(expectedPermissions[i]).toArray();
                    for (Object permission : permissions) {
                        wasFound = (permission.toString().equals(expectedPermission));
                        if (wasFound) {
                            break;
                        }
                    }
                    assertTrue(wasFound);
                } else {
                    Assert.fail("Expected permission was not found");
                }
            }
        }
    }

    private void verifyCollections() {
        if (expectedCollectionCount != null) {
            assertEquals(((long) expectedCollectionCount), attributes.getCollections().size());
        }
        if (expectedCollections != null) {
            for (String collection : expectedCollections) {
                assertTrue(attributes.getCollections().contains(collection));
            }
        }
    }

    private void verifyMetadataValues() {
        if (expectedMetadataValueCount != null) {
            assertEquals(((int) expectedMetadataValueCount), attributes.getMetadataValues().size());
        }
        if (expectedMetadataValues != null) {
            for (int i = 0; i < expectedMetadataValues.length; i = i + 2) {
                if (!(attributes.getMetadataValues().containsKey(expectedMetadataValues[i]) &&
                    attributes.getMetadataValues().get(expectedMetadataValues[i]).equals(expectedMetadataValues[i + 1]))) {
                    fail("Key::Value not found: " + expectedMetadataValues[i] + "::" + expectedMetadataValues[i + 1]);
                }
            }
        }
    }
}
