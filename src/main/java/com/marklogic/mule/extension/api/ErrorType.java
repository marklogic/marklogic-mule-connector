package com.marklogic.mule.extension.api;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

public enum ErrorType implements ErrorTypeDefinition<ErrorType> {

    // TODO Test that this works.
    CONNECTION_ERROR,

    TRANSFORMER_FACTORY_ERROR;

}
