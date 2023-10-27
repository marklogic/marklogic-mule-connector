package org.mule.extension;

import com.marklogic.mule.extension.DocumentAttributes;

public class DocumentData {

    private final String contents;

    private final DocumentAttributes attributes;

    DocumentData(String contents, DocumentAttributes attributes) {
        this.contents = contents;
        this.attributes = attributes;
    }

    public String getContents() {
        return contents;
    }

    public DocumentAttributes getAttributes() {
        return attributes;
    }
}
