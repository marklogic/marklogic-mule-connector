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
  private String configId;

  @Parameter
  private String threadCount;
  
  @Parameter
  private String batchSize;
  
  @Parameter
  private String outputCollections;
  
  @Parameter
  private String outputPermissions;
  
  @Parameter
  private String outputQuality;

  @Parameter
  private String format;
  
  @Parameter
  private String outputUriPrefix;
  
  @Parameter
  private String outputUriSuffix;
  
  /*@Parameter
  @Optional(defaultValue="test-filename")
  private String outputUriId;*/

  @Parameter
  @Optional
  private String generateOutputUriBasename;

  @Parameter
  @Optional(defaultValue="StringHandle")
  private String handleType;
  
  @Parameter
  @Optional
  private String serverTransform;
  
  @Parameter
  @Optional
  private String serverTransformParams;
  
  public String getConfigId() {
    return configId;
  }
  
  public int getThreadCount() {
    return Integer.parseInt(threadCount);
  }
  
  public int getBatchSize() {
    return Integer.parseInt(batchSize);
  }
  
  public String[] getOutputCollections() {
     String[] collectionValues = outputCollections.split(",");
     return collectionValues;
  }
  
  public String[] getOutputPermissions() {
     String[] permissions = outputPermissions.split(",");
     return permissions;
  }
  
  public int getOutputQuality() {
    return Integer.parseInt(outputQuality);
  }
  
  public String getOutputPrefix() {
    return outputUriPrefix;
  }
  
  public String getOutputSuffix() {
    return outputUriSuffix;
  } 
  
  /*public String getOutputUriId() {
    return outputUriId;
  }*/
  
  public Boolean getGenerateOutputUriBasename() {
    return Boolean.valueOf(generateOutputUriBasename);
  };

  public String getServerTransform() {
    return serverTransform;
  }
  
  public String[] getServerTransformParams() {
     String[] transParams = serverTransformParams.split(",");
     return transParams;
  }
}
