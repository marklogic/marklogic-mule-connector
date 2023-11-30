package com.marklogic.mule.extension;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

public enum ErrorType implements ErrorTypeDefinition<ErrorType> {

    CONNECTION_ERROR,

    XML_TRANSFORMER_ERROR;

}
