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
package com.marklogic.mule.extension.connector.internal.connection;

import java.io.IOException;
import javax.net.ssl.SSLContext;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.DatabaseClientFactory.BasicAuthContext;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;
import com.marklogic.client.DatabaseClientFactory.KerberosAuthContext;
import com.marklogic.mule.extension.connector.internal.exception.MarkLogicConnectorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MarkLogicConnection {

  private static final Logger logger = LoggerFactory.getLogger(MarkLogicConnection.class);
  
  private DatabaseClient client;
    private final String hostname;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String authenticationType;
    private final String sslContext;
    private final String kerberosExternalName;
    private final String connectionId;
  
  
  public MarkLogicConnection(String hostname, int port, String database, String username, String password, String authenticationType, String sslContext, String kerberosExternalName, String connectionId) {
    this.hostname = hostname;
    this.port = port;
    this.database = database;
    this.username = username;
    this.password = password;
    this.authenticationType = (authenticationType == null) ? "": authenticationType;
    this.sslContext = sslContext;
    this.kerberosExternalName = kerberosExternalName;
    this.connectionId = connectionId;
  }

  public void connect() throws MarkLogicConnectorException
  {
    /* SSL unsupported in 1.0.0 */
    /*
    SSLContext scontext;
    if (!sslContext.equals("null")) {
        scontext = SSLContext.getInstance(sslContext).init();
    }
    */
    if(this.isDefined(sslContext))
    {
        throw new MarkLogicConnectorException("SSL is not currently supported.");
    }
      
    logger.info("MarkLogic connection id = " + this.getId());
    
    DatabaseClientFactory.SecurityContext security;
        
    try {
        switch (authenticationType.toLowerCase().trim()) {
            case "application-level" :
                throw new MarkLogicConnectorException("Application-Level security is not allowed.");
            case "basic" :
                security = new BasicAuthContext(this.username, this.password);
                break;
            case "digest" :
                security = new DigestAuthContext(this.username, this.password);
                break;
            case "kerberos" :
                throw new MarkLogicConnectorException("Kerberos security is not currently supported.");
//                if(this.isDefined(this.kerberosExternalName))
//                {
//                    security = new KerberosAuthContext(this.kerberosExternalName);
//                }
//                else
//                {
//                    security = new KerberosAuthContext();
//                }
//                break;
            default : 
                throw new MarkLogicConnectorException("Authentication Type must be set.");
        }
        
        if(this.isDefined(this.database))
        {
            this.createClient(this.hostname, this.port, this.database, security);
        }
        else
        {
            this.createClient(this.hostname, this.port, security);
        }
        
    } catch (Exception e) {
        logger.error("MarkLogic connection failed. " + e);
        throw new MarkLogicConnectorException("MarkLogic connection failed", e);
    }
  }
  
  public String getId() {
    return this.connectionId;
  }

  public void invalidate() {
    client.release();
  }
  
  public boolean isConnected(int port) {
    
    if (this.client != null && this.client.getPort() == port) {
        return true;
    } else {
        logger.warn("Could not determine MarkLogicConnection port");
        return false;
    }
  }
  
  public DatabaseClient getClient() {
    return this.client;
  }

    private boolean isDefined(String str)
    {
        return str != null && !str.isEmpty() && !"null".equals(str);
    }

    private void createClient(String hostname, int port, String database, DatabaseClientFactory.SecurityContext security) throws Exception
    {
        this.client = DatabaseClientFactory.newClient(hostname, port, database, security);
    }

    private void createClient(String hostname, int port, DatabaseClientFactory.SecurityContext security) throws Exception
    {
        this.client = DatabaseClientFactory.newClient(hostname, port, security);
    }
}
