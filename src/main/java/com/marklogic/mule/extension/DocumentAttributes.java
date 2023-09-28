package com.marklogic.mule.extension;

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
