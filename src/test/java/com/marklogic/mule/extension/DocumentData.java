package com.marklogic.mule.extension;

import com.marklogic.mule.internal.api.DocumentAttributes;

public class DocumentData {

    private final String contents;

    private final DocumentAttributes attributes;
    private final String mimeType;

    DocumentData(String contents, DocumentAttributes attributes, String mimeType) {
        this.contents = contents;
        this.attributes = attributes;
        this.mimeType = mimeType;
    }

    public String getContents() {
        return contents;
    }

    public DocumentAttributes getAttributes() {
        return attributes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isText() {
        return mimeType.contains("text/plain");
    }

    public boolean isJSON() {
        return mimeType.contains("application/json");
    }

    public boolean isXML() {
        return mimeType.contains("application/xml");
    }

    public boolean isBinary() {
        return mimeType.contains("application/octet-stream");
    }
}
