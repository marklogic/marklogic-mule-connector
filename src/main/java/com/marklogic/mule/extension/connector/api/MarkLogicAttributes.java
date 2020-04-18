package com.marklogic.mule.extension.connector.api;

import java.io.Serializable;

public class MarkLogicAttributes implements Serializable {

    private final String mimetype;
    private String myProperty;

    public MarkLogicAttributes(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getMimetype() {
        return mimetype;
    }

    @Override
    public String toString() {
        return "MarkLogicAttributes{" +
                "mimetype='" + mimetype + '\'' +
                '}';
    }

    public String getMyProperty() {
        return myProperty;
    }

    public void setMyProperty(String myProperty) {
        this.myProperty = myProperty;
    }
}
