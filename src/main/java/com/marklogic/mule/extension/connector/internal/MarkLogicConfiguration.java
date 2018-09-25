package com.marklogic.mule.extension.connector.internal;

import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(MarkLogicOperations.class)
@ConnectionProviders(MarkLogicConnectionProvider.class)
public class MarkLogicConfiguration {

  @Parameter
  @Optional(defaultValue="testConfig-223efe")
  private String configId;

  @Parameter
  @Optional(defaultValue="4")
  private String threadCount;
  
  @Parameter
  @Optional(defaultValue="100")
  private String batchSize;
  
  @Parameter
  @Optional(defaultValue="null")
  private String serverTransform;
  
  @Parameter
  @Optional(defaultValue="null")
  private String serverTransformParams;

  @Parameter
  @Optional(defaultValue="2")
  private String secondsBeforeFlush;

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
}
