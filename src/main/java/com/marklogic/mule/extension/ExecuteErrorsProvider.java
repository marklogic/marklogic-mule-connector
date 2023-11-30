package com.marklogic.mule.extension;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

// The configuration and registration of the connector errors is
// documented at https://docs.mulesoft.com/mule-sdk/latest/errors
public class ExecuteErrorsProvider implements ErrorTypeProvider {

    @Override
    @SuppressWarnings("java:S3740") // Cannot figure out how to use a parameterized type here.
    public Set<ErrorTypeDefinition> getErrorTypes() {
        Set<ErrorTypeDefinition> errors = new HashSet<>();
        errors.add(ErrorType.CONNECTION_ERROR);
        errors.add(ErrorType.XML_TRANSFORMER_ERROR);
        return errors;
    }
}
