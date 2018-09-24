package com.marklogic.mule.extension.connector;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;

import com.marklogic.mule.extension.connector.internal.MarkLogicInsertionBatcher;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;

public class MarkLogicOperationsTestCase extends MuleArtifactFunctionalTestCase {

  private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Test
  public void executeRetrieveInfoOperation() throws Exception {
    String payloadValue = ((String) flowRunner("retrieveInfoFlow")
                                      .run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue, is("Using Configuration [configId] with Connection id [testConfig-223efe]"));
  }

  @Test
  public void executeImportDocsOperation() throws Exception {
    String payloadValue = ((String) flowRunner("importDocsFlow")
                                      .run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue, payloadValue.matches(UUID_REGEX));
  }
  
  @Test
  public void executeGetJobReportOperation() throws Exception {
    String payloadValue = ((String) flowRunner("getJobReportFlow")
                                      .run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue, startsWith("{\"importResults\":[{\"jobID\":\""));
  }  
}