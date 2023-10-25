package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;

import javax.xml.namespace.QName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    private MetadataVerifier(DocumentAttributes attributes, String expectedUri) {
        this.attributes = attributes;
        this.expectedUri = expectedUri;
    }

    public static MetadataVerifier assertMetadata(DocumentAttributes attributes, String jsonUri) {
        return new MetadataVerifier(attributes, jsonUri);
    }

    public MetadataVerifier collections(int expectedCollectionCount, String... expectedCollections) {
        this.expectedCollectionCount = expectedCollectionCount;
        this.expectedCollections = expectedCollections;
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

    public MetadataVerifier quality(int expectedQuality) {
        this.expectedQuality = expectedQuality;
        return this;
    }

    public void verify() {
        assertEquals("The expected uri should be returned in the attributes.", expectedUri, attributes.getUri());
        verifyCollections();
        verifyProperties();
        verifyPermissions();
        verifyQuality();
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
                for (int i=0; i<expectedProperties.length; i = i + 2) {
                    QName propertyKey = new QName(expectedProperties[i]);
                    assertEquals(expectedProperties[i+1], attributes.getProperties().get(propertyKey).toString());
                }
            }
        }
    }

    private void verifyPermissions() {
        if (expectedPermissionCount != null) {
            assertEquals(((long) expectedPermissionCount), attributes.getPermissions().size());
            if (expectedPermissionCount > 0) {
                for (int i=0; i<expectedPermissions.length; i = i + 2) {
                    String expectedPermission = expectedPermissions[i+1].toUpperCase();
                    boolean wasFound = false;
                    Object[] permissions = attributes.getPermissions().get(expectedPermissions[i]).toArray();
                    for (int j = 0; j < expectedPermissionCount; j++) {
                        wasFound = (permissions[j].toString().equals(expectedPermission));
                        if (wasFound) {
                            break;
                        }
                    }
                    assertTrue(wasFound);
                }
            }
        }
    }

    private void verifyCollections() {
        if (expectedCollectionCount != null) {
            assertEquals(((long) expectedCollectionCount), attributes.getCollections().size());
            if (expectedCollections != null) {
                for (String collection : expectedCollections) {
                    assertTrue(attributes.getCollections().contains(collection));
                }
            }
        }
    }
}
