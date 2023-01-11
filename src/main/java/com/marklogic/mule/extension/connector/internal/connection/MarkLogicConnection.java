/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2021 MarkLogic Corporation.
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
import com.marklogic.mule.extension.connector.api.connection.MarkLogicConnectionType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.ext.DatabaseClientConfig;
import com.marklogic.client.ext.DefaultConfiguredDatabaseClientFactory;
import com.marklogic.client.ext.SecurityContextType;
import com.marklogic.mule.extension.connector.internal.config.MarkLogicConfiguration;
import com.marklogic.mule.extension.connector.internal.connection.provider.MarkLogicConnectionProvider;
import com.marklogic.mule.extension.connector.internal.error.exception.MarkLogicConnectorException;

import com.marklogic.mule.extension.connector.internal.operation.InsertionBatcherContext;
import com.marklogic.mule.extension.connector.internal.operation.MarkLogicConnectionInvalidationListener;
import com.marklogic.mule.extension.connector.internal.operation.MarkLogicInsertionBatcher;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;

public final class MarkLogicConnection
{

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicConnection.class);

    private DatabaseClient client;
    private final String hostname;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final AuthenticationType authenticationType;
    private final MarkLogicConnectionType marklogicConnectionType;
    private final boolean useSSL;
    private final TlsContextFactory sslContext;
    private final String kerberosExternalName;
    private final String connectionId;
    private Set<MarkLogicConnectionInvalidationListener> markLogicClientInvalidationListeners = new HashSet<>();

    private final HashMap<Integer, MarkLogicInsertionBatcher> insertionBatchers;
    private final ReentrantLock insertionBatchersLock;

    public MarkLogicConnection(MarkLogicConnectionProvider provider) {
        this.insertionBatchers = new HashMap<>();
        this.insertionBatchersLock = new ReentrantLock(true);

        this.useSSL = provider.getTlsContextFactory() != null;
        if (provider.getTlsContextFactory() instanceof Initialisable) {
            try {
                ((Initialisable) provider.getTlsContextFactory()).initialise();
            } catch (InitialisationException e) {
                String message = "Error initializing SSL Context.";
                throw new MarkLogicConnectorException(message, e);
            }
        }
        this.sslContext = provider.getTlsContextFactory();

        this.hostname = provider.getHostname();
        this.port = provider.getPort();
        this.database = provider.getDatabase();
        this.username = provider.getUsername();
        this.password = provider.getPassword();
        this.authenticationType = provider.getAuthenticationType();
        this.marklogicConnectionType = provider.getMarklogicConnectionType();
        this.kerberosExternalName = provider.getKerberosExternalName();
        this.connectionId = provider.getConnectionId();
    }

    public void connect() throws MarkLogicConnectorException
    {
        logger.debug("Kerberos external name: {}", this.kerberosExternalName);
        logger.info("MarkLogic connection id = {}", this.getId());
        try
        {
            this.createClient();
        }
        catch (Exception e)
        {
            String message = "Error creating MarkLogic connection";
            throw new MarkLogicConnectorException(message, e);
        }
    }

    public DatabaseClient getClient()
    {
        return this.client;
    }
        
    public String getId()
    {
        return this.connectionId;
    }

    public void invalidate()
    {
        markLogicClientInvalidationListeners.forEach(MarkLogicConnectionInvalidationListener::markLogicConnectionInvalidated);
        releaseInsertionBatchers();
        client.release();
        logger.info("MarkLogic connection invalidated.");
    }
    
    public boolean isConnected(int port)
    {

        if (this.client != null && this.client.getPort() == port)
        {
            return true;
        }
        else
        {
            logger.warn("Could not determine MarkLogicConnection port or client is null");
            return false;
        }
    }

    public void addMarkLogicClientInvalidationListener(MarkLogicConnectionInvalidationListener listener) 
    {
        markLogicClientInvalidationListeners.add(listener);
    }
    
    public void removeMarkLogicClientInvalidationListener(MarkLogicConnectionInvalidationListener listener) 
    {
        markLogicClientInvalidationListeners.remove(listener);
    }
    
    private boolean isDefined(String str)
    {
        return str != null && !str.trim().isEmpty() && !"null".equalsIgnoreCase(str.trim());
    }
    
    private void createClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException
    {

        DatabaseClientConfig config = new DatabaseClientConfig();

        config.setHost(hostname);
        config.setPort(port);
        
        if (isDefined(database))
        {
            config.setDatabase(database);
        }
        
        setConfigAuthType(config);
        setConfigMLConnectionType(config);
        
        config.setUsername(username);
        config.setPassword(password);

        if (useSSL)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("Creating connection using SSL connection with SSL Context: '%s'.", sslContext));
            }
            config.setSslContext(sslContext.createSslContext());
        }
        else
        {
            logger.debug("Creating connection without using SSL.");
        }

        config.setSslHostnameVerifier(DatabaseClientFactory.SSLHostnameVerifier.ANY);

        
        client = new DefaultConfiguredDatabaseClientFactory().newDatabaseClient(config);
    }

    private void setConfigAuthType(DatabaseClientConfig config) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException
    {
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
                setTrustManager(config);
                break;
            default:
                config.setSecurityContextType(SecurityContextType.DIGEST);
                break;
        }
    }
    
    private void setConfigMLConnectionType(DatabaseClientConfig config)
    {
        config.setConnectionType(MarkLogicConnectionType.GATEWAY.equals(marklogicConnectionType) ?
            DatabaseClient.ConnectionType.GATEWAY :
            DatabaseClient.ConnectionType.DIRECT);
    }
        
    private void setTrustManager(DatabaseClientConfig config) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException
    {
        if (sslContext.isTrustStoreConfigured())
        {
            String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm);
            final KeyStore trustStore = getTrustStore(sslContext.getTrustStoreConfiguration().getType());

            try (final InputStream is = new FileInputStream(sslContext.getTrustStoreConfiguration().getPath())) {
                trustStore.load(is, sslContext.getTrustStoreConfiguration().getPassword().toCharArray());
            }

            trustManagerFactory.init(trustStore);
            X509TrustManager tm = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];

            if (logger.isDebugEnabled()) {
                Enumeration<String> enumera = trustStore.aliases();
                while (enumera.hasMoreElements()) {
                    logger.debug("Found cert with alias: {}", enumera.nextElement());
                }
            }
            config.setTrustManager(tm);
        }
    }
    
    private KeyStore getTrustStore(String trustStoreType) throws KeyStoreException
    {
        // N.b: Make Key Store Provider Configurable in the UI
        String keyStoreProvider = "SUN";
        if ("PKCS12".equals(trustStoreType))
        {
            logger.warn("{} truststores are deprecated. JKS is preferred.", trustStoreType);
            keyStoreProvider = "BC";
        }

        try
        {
            return KeyStore.getInstance(trustStoreType, keyStoreProvider);
        }
        catch (KeyStoreException | NoSuchProviderException e)
        {
            logger.error("Unable to load " + keyStoreProvider + " " + trustStoreType
                    + " keystore.  This may cause issues getting trusted CA certificates as well as Certificate Chains for use in TLS.", e);
        }
        return KeyStore.getInstance(trustStoreType);
    }
	
	public MarkLogicInsertionBatcher getInsertionBatcher(MarkLogicConfiguration config, String outputCollections, String outputPermissions,
                                                         int outputQuality, String temporalCollection,
                                                         String serverTransform, String serverTransformParams) {
        if (client == null)
            throw new MarkLogicConnectorException("Cannot initialize insertion batcher; client is not yet connected.");

        InsertionBatcherContext context = new InsertionBatcherContext();
        context.setConfiguration(config);
        context.setConnection(this);
        context.setOutputCollections(outputCollections);
        context.setOutputPermissions(outputPermissions);
        context.setOutputQuality(outputQuality);
        context.setJobName(config.getJobName());
        context.setTemporalCollection(temporalCollection);
        context.setServerTransform(serverTransform);
        context.setServerTransformParams(serverTransformParams);
        final int signature = context.computeSignature();

        insertionBatchersLock.lock();
        try {
            MarkLogicInsertionBatcher insertionBatcher = insertionBatchers.getOrDefault(signature, null);
            if (insertionBatcher == null) {
                insertionBatcher = new MarkLogicInsertionBatcher(context);
                insertionBatchers.put(insertionBatcher.getSignature(), insertionBatcher);
                if (insertionBatcher.getSignature() != signature) {
                    logger.warn("Computed batcher signature {} different than generated by instance {}", signature, insertionBatcher.getSignature());
                }
            }
            return insertionBatcher;
        }
        finally {
            insertionBatchersLock.unlock();
        }
    }

    private void releaseInsertionBatchers() {
        insertionBatchersLock.lock();
        try {
            for (MarkLogicInsertionBatcher insertionBatcher : insertionBatchers.values()) {
                insertionBatcher.release();
            }
        }
        finally {
            insertionBatchersLock.unlock();
        }
    }
}
