package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;

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
}
