package com.marklogic.mule.extension.connector.internal.connection;

/**
 * Created by jkrebs on 4/11/2019.
 */
public enum AuthenticationType {
    digest("digest"),
    certificate("certificate"),
    basic("basic");

    String value;

    AuthenticationType(String value) {
        this.value = value;
    }
}
