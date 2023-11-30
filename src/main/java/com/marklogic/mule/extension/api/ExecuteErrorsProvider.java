package com.marklogic.mule.extension.api;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

// The configuration and registration of the connector errors is
// documented at https://docs.mulesoft.com/mule-sdk/latest/errors
public class ExecuteErrorsProvider implements ErrorTypeProvider {
    @Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        HashSet<ErrorTypeDefinition> errors = new HashSet<>();
        errors.add(ErrorType.CONNECTION_ERROR);
        errors.add(ErrorType.TRANSFORMER_FACTORY_ERROR);
        return errors;
    }
}
