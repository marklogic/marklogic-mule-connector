/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2024 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.mule.connector.api.error.provider;

import com.marklogic.mule.connector.api.error.ErrorType;
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
