/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2019 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.internal.config;

import com.marklogic.mule.extension.connector.internal.operation.MarkLogicOperations;
import com.marklogic.mule.extension.connector.internal.connection.provider.MarkLogicConnectionProvider;

import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(MarkLogicOperations.class)
@ConnectionProviders(MarkLogicConnectionProvider.class)
public class MarkLogicConfiguration {

  @DisplayName("Connection ID")
  @Parameter
  @Summary("An identifier used for the Mulesoft Connector to keep state of its connection to MarkLogic.")
  @Example("testConfig-223efe")
  private String configId;

  @Parameter
  @Summary("The thread count passed to DMSDK, representing the number of parallel processing threads.")
  @Example("4")
  private String threadCount;
  
  @Parameter
  @Summary("The batch size passed to DMSDK, representing the number of documents processed within a batch.")
  @Example("100")
  private String batchSize;
  
  @Parameter
  @Summary("The name of an already registered and deployed MarkLogic server-side Javascript, XQuery, or XSLT module.")
  @Optional(defaultValue="null")
  @Example("ml:sjsInputFlow")
  private String serverTransform;
  
  @Parameter
  @Summary("A comma-separated list of alternating transform parameter names and transform parameter values.")
  @Optional(defaultValue="null")
  @Example("entity-name,MyEntity,flow-name,loadMyEntity")
  private String serverTransformParams;

  @Parameter
  @Summary("The number of seconds before DMSDK automatically flushes the current batch if not yet filled to the specified batchSize configurable.")
  @Example("2")
  private String secondsBeforeFlush;
  
  @Parameter
  @Summary("The job name used by DMSDK to track the job.")
  @Example("myJobName")
  private String jobName;

  public String getConfigId() {
    return configId;
  }
  
  public int getThreadCount() {
    return Integer.parseInt(threadCount);
  }
  
  public int getBatchSize() {
    return Integer.parseInt(batchSize);
  }
  
  public String getServerTransform() {
    return serverTransform;
  }
  
  public String[] getServerTransformParams() {
     String[] transParams = serverTransformParams.split(",");
     return transParams;
  }

  public String getSecondsBeforeFlush() {
    return secondsBeforeFlush;
  }
  
  public String getJobName() {
    return jobName;
  }
}
