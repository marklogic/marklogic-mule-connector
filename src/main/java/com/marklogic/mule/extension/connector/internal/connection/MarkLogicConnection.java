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

import com.marklogic.mule.extension.connector.api.connection.AuthenticationType;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.ext.DatabaseClientConfig;
import com.marklogic.client.ext.DefaultConfiguredDatabaseClientFactory;
import com.marklogic.client.ext.SecurityContextType;
import com.marklogic.mule.extension.connector.internal.exception.MarkLogicConnectorException;

import com.marklogic.mule.extension.connector.internal.operation.MarkLogicConnectionInvalidationListener;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;

public final class MarkLogicConnection
{

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicConnection.class);

    //private static final SecurityContextType DEFAULT_AUTHENTICATION_TYPE = SecurityContextType.BASIC;
    private DatabaseClient client;
    private final String hostname;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final AuthenticationType authenticationType;
    private final boolean useSSL;
    private final TlsContextFactory sslContext;
    private final String kerberosExternalName;
    private final String connectionId;
    private Set<MarkLogicConnectionInvalidationListener> markLogicClientInvalidationListeners = new HashSet<>();

    public MarkLogicConnection(String hostname, int port, String database, String username, String password, AuthenticationType authenticationType, TlsContextFactory sslContext, String kerberosExternalName, String connectionId)
    {

        this.useSSL = (sslContext != null);
        this.sslContext = sslContext;
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.authenticationType = authenticationType;
        this.kerberosExternalName = kerberosExternalName;
        this.connectionId = connectionId;
    }

    public void connect() throws MarkLogicConnectorException
    {

        logger.info("MarkLogic connection id = " + this.getId());
        try
        {
            this.createClient();
        }
        catch (Exception e)
        {
            String message = "Error creating MarkLogic connection";
            logger.error(message, e);
            throw new MarkLogicConnectorException(message, e);
        }

    }

    public String getId()
    {
        return this.connectionId;
    }

    public void invalidate()
    {
        client.release();
        markLogicClientInvalidationListeners.forEach((listener) ->
        {
            listener.markLogicConnectionInvalidated();
        });
        logger.debug("MarkLogic connection invalidated.");
  }
    public boolean isConnected(int port)
    {

        if (this.client != null && this.client.getPort() == port)
        {
            return true;
        }
        else
        {
            logger.warn("Could not determine MarkLogicConnection port");
            return false;
        }
    }

    public DatabaseClient getClient()
    {
        return this.client;
    }

    private boolean isDefined(String str)
    {
        return str != null && !str.trim().isEmpty() && !"null".equalsIgnoreCase(str.trim());
    }

    private void createClient() throws Exception
    {

        DatabaseClientConfig config = new DatabaseClientConfig();

        config.setHost(hostname);
        config.setPort(port);

        switch (authenticationType)
        {
            case basic:
                config.setSecurityContextType(SecurityContextType.BASIC);
                break;
            case digest:
                config.setSecurityContextType(SecurityContextType.DIGEST);
                break;
            case certificate:
                config.setSecurityContextType(SecurityContextType.CERTIFICATE);
        }

        config.setUsername(username);
        config.setPassword(password);

        if (isDefined(database))
        {
            config.setDatabase(database);
        }

        if (useSSL)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("Creating connection using SSL connection with SSL Context: '%s'.", sslContext));
            }
            SSLContext context = sslContext.createSslContext();
            config.setSslContext(context);
            
            // N.b: Figure out what this means:
            // config.setConnectionType(DatabaseClient.ConnectionType.GATEWAY or DatabaseClient.ConnectionType.DIRECT);
            if (AuthenticationType.certificate == authenticationType && sslContext.isTrustStoreConfigured())
            {
                String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm);
                final KeyStore trustStore = getTrustStore(sslContext.getTrustStoreConfiguration().getType());
                try (final InputStream is = new FileInputStream(sslContext.getTrustStoreConfiguration().getPath()))
                {
                    trustStore.load(is, sslContext.getTrustStoreConfiguration().getPassword().toCharArray());
                }
                trustManagerFactory.init(trustStore);
                X509TrustManager tm = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
                if (logger.isDebugEnabled())
                {
                    Enumeration<String> enumera = trustStore.aliases();
                    while (enumera.hasMoreElements())
                    {
                        logger.debug("Got cert with alias: " + enumera.nextElement());
                    }
                }
                config.setTrustManager(tm);
            }
        }
        else
        {
            logger.debug("Creating connection without using SSL.");
        }

        config.setSslHostnameVerifier(DatabaseClientFactory.SSLHostnameVerifier.ANY);

        
        client = new DefaultConfiguredDatabaseClientFactory().newDatabaseClient(config);
    }

    private KeyStore getTrustStore(String trustStoreType) throws KeyStoreException
    {
        // N.b: Make Key Store Provider Configurable in the UI
        String keyStoreProvider = "SUN";
        if ("PKCS12".equals(trustStoreType))
        {
            logger.warn(trustStoreType + " truststores are deprecated. JKS is preferred.");
            keyStoreProvider = "BC";
        }

        if (keyStoreProvider != null && keyStoreProvider.equals(""))
        {
            try
            {
                return KeyStore.getInstance(trustStoreType, keyStoreProvider);
            }
            catch (KeyStoreException | NoSuchProviderException e)
            {
                logger.error("Unable to load " + keyStoreProvider + " " + trustStoreType
                        + " keystore.  This may cause issues getting trusted CA certificates as well as Certificate Chains for use in TLS.", e);
            }
        }
        return KeyStore.getInstance(trustStoreType);

    }

    public void addMarkLogicClientInvalidationListener(MarkLogicConnectionInvalidationListener listener) 
    {
        markLogicClientInvalidationListeners.add(listener);
    }
    
    public void removeMarkLogicClientInvalidationListener(MarkLogicConnectionInvalidationListener listener) 
    {
        markLogicClientInvalidationListeners.remove(listener);
    }
}
