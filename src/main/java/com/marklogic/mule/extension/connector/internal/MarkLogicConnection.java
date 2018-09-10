package com.marklogic.mule.extension.connector.internal;

import java.io.IOException;
import javax.net.ssl.SSLContext;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.DatabaseClientFactory.BasicAuthContext;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;
import com.marklogic.client.DatabaseClientFactory.KerberosAuthContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MarkLogicConnection {

  private final String id;
  private final Logger logger = LoggerFactory.getLogger(MarkLogicConnection.class);
  private static DatabaseClient client;
  
  public MarkLogicConnection(String hostname, int port, String database, String username, String password, String authenticationType, String sslContext, String kerberosExternalName, String connectionId) {
    this.id = connectionId;
    /* SSL unsupported in 1.0.0 */
    /*
    SSLContext scontext;
    if (!sslContext.equals("null")) {
        scontext = SSLContext.getInstance(sslContext).init();
    }
    */
    logger.info("MarkLogic connection id = " + this.id);
    try {
        switch (authenticationType.toLowerCase()) {
            case "application-level" :
                if (database.equals("null")) {
                    this.client = DatabaseClientFactory.newClient(hostname, port);
                } else {
                    this.client = DatabaseClientFactory.newClient(hostname, port, database);
                }
                break;
            case "basic" :
                if (database.equals("null")) {
                    this.client = DatabaseClientFactory.newClient(hostname, port, new BasicAuthContext(username, password));
                } else {
                    this.client = DatabaseClientFactory.newClient(hostname, port, database, new BasicAuthContext(username, password));
                }
                break;
            case "digest" :
                if (database.equals("null")) {
                    this.client = DatabaseClientFactory.newClient(hostname, port, new DigestAuthContext(username, password));
                } else {
                    this.client = DatabaseClientFactory.newClient(hostname, port, database, new DigestAuthContext(username, password));
                }
                break;
            case "kerberos" :
                if (database.equals("null") && kerberosExternalName.equals("null")) {
                    this.client = DatabaseClientFactory.newClient(hostname, port, new KerberosAuthContext());
                } else if (database.equals("null") && !kerberosExternalName.equals("null")) {
                    this.client = DatabaseClientFactory.newClient(hostname, port, new KerberosAuthContext(kerberosExternalName));
                } else if (!database.equals("null") && kerberosExternalName.equals("null")) {
                    this.client = DatabaseClientFactory.newClient(hostname, port, database, new KerberosAuthContext());
                } else {
                    this.client = DatabaseClientFactory.newClient(hostname, port, database, new KerberosAuthContext(kerberosExternalName));
                }
                break;
            default :
                this.client = DatabaseClientFactory.newClient(hostname, port, new DigestAuthContext(username, password));
                break;
        }
        //this.client = DatabaseClientFactory.newClient(hostname, port, new DigestAuthContext(username, password));
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
