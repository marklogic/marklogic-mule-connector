/**
 * MarkLogic Connector
 *
 * Copyright © 2018 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
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
  private static final Logger logger = LoggerFactory.getLogger(MarkLogicConnection.class);
  // Purported "code smell" from Sonar for updating static field from dynamic method
  // private static DatabaseClient client; 
  private DatabaseClient client;
  
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
    } catch (Exception e) {
        // logger.error("MarkLogic connection failed. " + e.getMessage());
        // Try returning the entire exception to pass Sonor code quality
        logger.error("MarkLogic connection failed. " + e);
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
        // logger.warn("MarkLogic disconnect failed. " + e.getMessage());
        // Try returning the entire exception to pass Sonor code quality
        logger.warn("MarkLogic disconnect failed. " + e);
    }
  }
  
  public boolean isConnected(int port) {
    Integer connectedPort = client.getPort();
    Integer configuredPort = Integer.valueOf(port); // Cleaned up for Sonar; was: new Integer(port);
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
