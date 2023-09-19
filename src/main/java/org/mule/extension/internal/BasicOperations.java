package org.mule.extension.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.TextDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import java.util.ArrayList;

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
      connection.createClient()
          .newTextDocumentManager()
          .write(uri,
              new DocumentMetadataHandle().withPermission("rest-reader", DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE),
              new StringHandle(content)
          );
  }

  /**
   * Example of a simple operation that writes text documents to MarkLogic database when given content.
   */
  @MediaType(value = ANY, strict = false)
  public void writeDocuments(@Connection BasicConnection connection, String[] contents, String uriPrefix) {
    DatabaseClient databaseClient = connection.createClient();
    TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
    DocumentWriteSet batch = textDocumentManager.newWriteSet();
    for(int i=0; i<contents.length;i++){
      batch.add(uriPrefix+Math.random()+"_i_value_is_"+i+".txt", new StringHandle(contents[i]).withFormat(Format.TEXT));
    }
    textDocumentManager.write(batch);
  }

  /**
   * Example of a simple operation that reads documents from MarkLogic Documents database.
   */
  @MediaType(value = ANY, strict = false)
  public String readDocs() {
    DatabaseClientFactory.SecurityContext securityContext = new DatabaseClientFactory.DigestAuthContext("admin", "admin");
    DatabaseClient databaseClient = DatabaseClientFactory.newClient("localhost",8000, securityContext);
    TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
    StringBuffer str = new StringBuffer();
    for (DocumentRecord record : textDocumentManager.read("doc1.txt","doc2.txt")) {
      String content = record.getContentAs(String.class);
      str.append(content);
      str.append("\n");
    }
    return str.toString();
  }
  /**
   * Example of a simple operation that reads a single document from MarkLogic database.
   */
  @MediaType(value = ANY, strict = false)
  public String readSingleDoc(@Connection BasicConnection connection, String uri) {
      return connection.createClient()
          .newTextDocumentManager()
          .readAs(uri, String.class);
  }

  /**
   * Example of a simple operation that searches document(s) from a directory in MarkLogic database and returns the content(s).
   */
  @MediaType(value = ANY, strict = false)
  public String[] searchDocs(@Connection BasicConnection connection, String directory, int pageLength) {
    DatabaseClient databaseClient = connection.createClient();
    QueryManager queryMgr = databaseClient.newQueryManager();
      TextDocumentManager textDocumentManager = databaseClient.newTextDocumentManager();
    queryMgr.setPageLength(pageLength);
    ArrayList<String> arrayList = new ArrayList<>();
    StringQueryDefinition qdef = queryMgr.newStringDefinition();
    qdef.setDirectory(directory);
      try ( DocumentPage page = textDocumentManager.search(qdef, 1) ) {
          while(page.hasNext()){
              arrayList.add(page.next().getContent(new StringHandle()).toString());
          }
      }
    return arrayList.toArray(new String[arrayList.size()]);
  }
  /**
   * Private Methods are not exposed as operations
   */
  private String buildHelloMessage(String person) {
    return "Hello " + person + "!!!";
  }
}
