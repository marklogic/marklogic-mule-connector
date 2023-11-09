package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;
import org.junit.Assert;

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
                    QName propertyKey = new QName(expectedProperties[i]);
                    assertEquals(expectedProperties[i + 1], attributes.getProperties().get(propertyKey).toString());
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
}
