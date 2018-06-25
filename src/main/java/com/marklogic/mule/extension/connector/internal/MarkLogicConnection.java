package com.marklogic.mule.extension.connector.internal;

import java.io.IOException;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class MarkLogicConnection {

  private final String id;
  private final Logger logger = LoggerFactory.getLogger(MarkLogicConnection.class);
  private static DatabaseClient client;
  
  public MarkLogicConnection(String hostname, String username, String password, int port, String connectionId) {
    this.id = connectionId;
    logger.info("MarkLogic connection id = " + this.id);
    try {
        this.client = DatabaseClientFactory.newClient(hostname, port, new DigestAuthContext(username, password));
    } catch (Exception e) {
        logger.error("MarkLogic connection failed. " + e.getMessage());
    }
  }

  public String getId() {
    return this.id;
  }

  public void invalidate() {
    // do something to invalidate/disconnect this connection!
    try {
        client.release();
    } catch (Exception e) {
        logger.warn("MarkLogic disconnect failed. " + e.getMessage());
    }
  }
  
  public boolean isConnected(int port) {
    Integer connectedPort = client.getPort();
    Integer configuredPort = new Integer(port);
    if (connectedPort.equals(configuredPort)) {
        return true;
    } else {
        logger.warn("Could not determine MarkLogicConnection port");
        return false;
    }
  }
  
  public DatabaseClient getClient() {
    return this.client;
  }

}
