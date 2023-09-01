package org.mule.extension.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.TextDocumentManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;

/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class BasicOperations {

  /**
   * Example of an operation that uses the configuration and a connection instance to perform some action.
   */
  @MediaType(value = ANY, strict = false)
  public String retrieveInfo(@Config BasicConfiguration configuration, @Connection BasicConnection connection){
    return "Using Configuration [" + configuration.getConfigId() + "] with Connection id [" + connection.getId() + "]";
  }

  /**
   * Example of a simple operation that receives a string parameter and returns a new string message that will be set on the payload.
   */
  @MediaType(value = ANY, strict = false)
  public String sayHi(String person) {
    return buildHelloMessage(person);
  }
  /**
   * Example of a simple operation that writes documents to MarkLogic Documents database.
   */
  @MediaType(value = ANY, strict = false)
  public void writeDocs() {
    DatabaseClientFactory.SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
    DatabaseClient databaseClient = DatabaseClientFactory.newClient("localhost",8000, securityContext);
    TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
    DocumentWriteSet batch = textDocumentManager.newWriteSet();

    batch.add("doc1.txt", new StringHandle(
            "Document - 1").withFormat(Format.TEXT));
    batch.add("doc2.txt", new StringHandle(
            "Document - 2").withFormat(Format.TEXT));
    textDocumentManager.write(batch);
  }

  /**
   * Example of a simple operation that writes a single text document to MarkLogic database.
   */
  @MediaType(value = ANY, strict = false)
  public void writeSingledoc(@Connection BasicConnection connection, String content, String uri) {
    DatabaseClientFactory.SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext(connection.getUsername(),
            connection.getPassword());
    DatabaseClient databaseClient = DatabaseClientFactory.newClient(connection.getHost(), connection.getPort(), securityContext);
    TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
    DocumentWriteSet batch = textDocumentManager.newWriteSet();

    batch.add(uri, new StringHandle(content).withFormat(Format.TEXT));
    textDocumentManager.write(batch);
  }

  /**
   * Example of a simple operation that reads documents from MarkLogic Documents database.
   */
  @MediaType(value = ANY, strict = false)
  public String readDocs() {
    DatabaseClientFactory.SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
    DatabaseClient databaseClient = DatabaseClientFactory.newClient("localhost",8000, securityContext);
    TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();StringBuffer str = new StringBuffer();
    for (DocumentRecord record : textDocumentManager.read("doc1.txt","doc2.txt")) {
      String content = record.getContentAs(String.class);
      str.append(content);
      str.append("\n");
    }
    return str.toString();
  }
  /**
   * Private Methods are not exposed as operations
   */
  private String buildHelloMessage(String person) {
    return "Hello " + person + "!!!";
  }
}
