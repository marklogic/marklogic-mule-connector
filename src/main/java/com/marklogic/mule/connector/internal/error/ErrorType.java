package com.marklogic.mule.connector.internal.error;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

public enum ErrorType implements ErrorTypeDefinition<ErrorType> {

    CONNECTION_ERROR,

    XML_TRANSFORMER_ERROR;

}