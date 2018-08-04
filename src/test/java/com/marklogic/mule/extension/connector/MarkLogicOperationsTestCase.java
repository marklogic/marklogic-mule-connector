package com.marklogic.mule.extension.connector;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;

public class MarkLogicOperationsTestCase extends MuleArtifactFunctionalTestCase {

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  /*@Test
  public void executeSayHiOperation() throws Exception {
    String payloadValue = ((String) flowRunner("sayHiFlow").run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue, is("Hello Mariano Gonzalez!!!"));
  }*/

//  @Test
//  public void executeRetrieveInfoOperation() throws Exception {
//    String payloadValue = ((String) flowRunner("retrieveInfoFlow")
//                                      .run()
//                                      .getMessage()
//                                      .getPayload()
//                                      .getValue());
//    assertThat(payloadValue, is("Using Configuration [configId] with Connection id [testConfig-223efe]"));
//  }
//
//  @Test
//  public void executeImportDocsOperation() throws Exception {
//    String payloadValue = ((String) flowRunner("importDocsFlow")
//                                      .run()
//                                      .getMessage()
//                                      .getPayload()
//                                      .getValue());
//    assertThat(payloadValue, is("Success"));
//  }

  @Test
  public void executeExportByCollection() throws Exception
  {
    String payloadValue = ((String) flowRunner("exportFileByCollectionFlow")
                                    .run()
                                    .getMessage()
                                    .getPayload()
                                    .getValue());

    assertThat(payloadValue, is("Success"));
  }

  
  @Test
  public void executeExportByCollectionWithOptions() throws Exception
  {
    String payloadValue = ((String) flowRunner("exportFileByCollectionWithOptionsFlow")
                                    .run()
                                    .getMessage()
                                    .getPayload()
                                    .getValue());

    assertThat(payloadValue, is("Success"));
  }
  
  @Test
  public void executeExportByUris() throws Exception
  {
    String payloadValue = ((String) flowRunner("exportFileByUrisFlow")
                                    .run()
                                    .getMessage()
                                    .getPayload()
                                    .getValue());

    assertThat(payloadValue, is("Success"));
  }
  
  @Test
  public void executeExportByUrisWithOptions() throws Exception
  {
    String payloadValue = ((String) flowRunner("exportFileByUrisWithOptionsFlow")
                                    .run()
                                    .getMessage()
                                    .getPayload()
                                    .getValue());

    assertThat(payloadValue, is("Success"));
  }
  
  
  
//  @Test
//  public void executeExportByPattern() throws Exception
//  {
//    String payloadValue = ((String) flowRunner("exportFileByPatternFlow")
//            .run()
//            .getMessage()
//            .getPayload()
//            .getValue());
//
//    assertThat(payloadValue, is("Success"));
//  }
  
  @Test
  public void executeFetchByUris() throws Exception
  {
    List<String> payloadValue = ((List<String>) flowRunner("fetchFileByUrisFlow")
                                    .run()
                                    .getMessage()
                                    .getPayload()
                                    .getValue());

    assertThat(payloadValue, containsInAnyOrder("[0, null, false]"));
  }
  
  @Test
  public void executeFetchByCollection() throws Exception
  {
    List<String> payloadValue = ((List<String>) flowRunner("fetchFileByCollectionFlow")
                                    .run()
                                    .getMessage()
                                    .getPayload()
                                    .getValue());

    assertThat(payloadValue, containsInAnyOrder("[0, null, false]"));
  }
  
}
