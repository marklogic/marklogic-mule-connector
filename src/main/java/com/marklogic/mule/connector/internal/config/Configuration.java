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
package com.marklogic.mule.connector.internal.config;

import com.marklogic.mule.connector.internal.operation.Operations;
import com.marklogic.mule.connector.internal.connection.provider.ConnectionProvider;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

@org.mule.runtime.extension.api.annotation.Operations(Operations.class)
@ConnectionProviders(ConnectionProvider.class)
@DisplayName("Connection Configuration")
@Summary("Defines how connections are made to MarkLogic.")
public class Configuration {

}
