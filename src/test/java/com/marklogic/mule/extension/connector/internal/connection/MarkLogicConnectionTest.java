/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2023 MarkLogic Corporation.
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

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.BasicAuthContext;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;
import com.marklogic.mule.extension.connector.api.connection.AuthenticationType;
import com.marklogic.mule.extension.connector.api.connection.MarkLogicConnectionType;
import com.marklogic.mule.extension.connector.internal.connection.provider.MarkLogicConnectionProvider;
import com.marklogic.mule.extension.connector.internal.operation.MarkLogicConnectionInvalidationListener;
import org.junit.Test;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextKeyStoreConfiguration;
import org.mule.runtime.api.tls.TlsContextTrustStoreConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MarkLogicConnectionTest {

    private static final String CONNECTION_ID = "test-connection-id";
    private static final String USER_PASSWORD = "test-password";
    private static final String USER_NAME = "test-user";
    private static final String DATABASE_NAME = "test";
    private static final String EMPTY_DATABASE_NAME = "";
    private static final int PORT = 8000;
    private static final int SSL_PORT = 8021;
    private static final String LOCALHOST = "localhost";

    /**
     * Test of getId method, of class MarkLogicConnection.
     */
    @Test
    public void testGetId()
    {
        MarkLogicConnection instance = new MarkLogicConnection(new MarkLogicConnectionProvider()
            .withHostname(LOCALHOST)
            .withPort(PORT)
            .withDatabase(DATABASE_NAME)
            .withUsername(USER_NAME)
            .withPassword(USER_PASSWORD)
            .withAuthenticationType(AuthenticationType.digest)
            .withMarklogicConnectionType(MarkLogicConnectionType.DIRECT)
            .withConnectionId(CONNECTION_ID));
        String result = instance.getId();
        assertEquals(CONNECTION_ID, result);
    }
    
    @Test
    public void testIsConnectedNull() {
        MarkLogicConnection instance = new MarkLogicConnection(new MarkLogicConnectionProvider()
            .withHostname(LOCALHOST)
            .withPort(PORT)
            .withDatabase(DATABASE_NAME)
            .withUsername(USER_NAME)
            .withPassword(USER_PASSWORD)
            .withAuthenticationType(AuthenticationType.digest)
            .withMarklogicConnectionType(MarkLogicConnectionType.DIRECT)
            .withConnectionId(CONNECTION_ID));

        assertFalse(instance.isConnected(PORT));
    }

    /**
     * Test of invalidate method, of class MarkLogicConnection.
     */
    @Test
    public void testInvalidate() throws ConnectionException, InitialisationException
    {
        MarkLogicConnectionInvalidationListener listener = mock(MarkLogicConnectionInvalidationListener.class);

        MarkLogicConnection instance = new MarkLogicConnection(new MarkLogicConnectionProvider()
            .withHostname(LOCALHOST)
            .withPort(PORT)
            .withUsername(USER_NAME)
            .withPassword(USER_PASSWORD)
            .withAuthenticationType(AuthenticationType.digest)
            .withMarklogicConnectionType(MarkLogicConnectionType.DIRECT)
            .withConnectionId(CONNECTION_ID));

        instance.addMarkLogicClientInvalidationListener(listener);
        instance.connect();
        instance.invalidate();
        instance.removeMarkLogicClientInvalidationListener(listener);
        
        verify(listener).markLogicConnectionInvalidated();
        
    }

    /**
     * Test of isConnected method, of class MarkLogicConnection.
     */
    @Test
    public void testIsConnected() throws ConnectionException, InitialisationException
    {
        MarkLogicConnection instance = new MarkLogicConnection(new MarkLogicConnectionProvider()
            .withHostname(LOCALHOST)
            .withPort(PORT)
            .withDatabase(EMPTY_DATABASE_NAME)
            .withUsername(USER_NAME)
            .withPassword(USER_PASSWORD)
            .withAuthenticationType(AuthenticationType.digest)
            .withMarklogicConnectionType(MarkLogicConnectionType.DIRECT)
            .withConnectionId(CONNECTION_ID));

        instance.connect();
        boolean result = instance.isConnected(PORT);
        assertEquals(true, result);
    }

    /**
     * Negative Test of isConnected method, of class MarkLogicConnection.
     */
    @Test
    public void testIsNotConnected() throws ConnectionException, InitialisationException
    {
        MarkLogicConnection instance = new MarkLogicConnection(new MarkLogicConnectionProvider()
            .withHostname(LOCALHOST)
            .withPort(PORT)
            .withUsername(USER_NAME)
            .withPassword(USER_PASSWORD)
            .withAuthenticationType(AuthenticationType.digest)
            .withMarklogicConnectionType(MarkLogicConnectionType.DIRECT)
            .withConnectionId(CONNECTION_ID));

        instance.connect();
        boolean result = instance.isConnected(8001);
        assertEquals(false, result);
    }

    //----------------- Digest & Default Authentication Tests ----------------//
    @Test
    public void testDigestClientWithDbName() throws ConnectionException, InitialisationException
    {
        digestClientTest(DATABASE_NAME);
    }

    //Should have used paramatized test 
    @Test
    public void testDigestClientWithoutDbName() throws ConnectionException, InitialisationException
    {
        digestClientTest(EMPTY_DATABASE_NAME);
    }

    protected void digestClientTest(String databaseName) throws ConnectionException, InitialisationException
    {
        MarkLogicConnection instance = new MarkLogicConnection(new MarkLogicConnectionProvider()
            .withHostname(LOCALHOST)
            .withPort(PORT)
            .withDatabase(databaseName)
            .withUsername(USER_NAME)
            .withPassword(USER_PASSWORD)
            .withAuthenticationType(AuthenticationType.digest)
            .withMarklogicConnectionType(MarkLogicConnectionType.DIRECT)
            .withConnectionId(CONNECTION_ID));

        instance.connect();
        DatabaseClient result = instance.getClient();
        this.databaseClientAssert(result, !databaseName.equals(EMPTY_DATABASE_NAME));

        DatabaseClientFactory.SecurityContext securityContext = result.getSecurityContext();

        assertTrue(securityContext instanceof DigestAuthContext);
        DigestAuthContext digest = (DigestAuthContext) securityContext;

        assertEquals(USER_NAME, digest.getUser());
        assertEquals(USER_PASSWORD, digest.getPassword());
    }

    //----------------- Basic Authentication Tests ---------------------------//
    @Test
    public void testBasicClientWithDbName() throws ConnectionException, InitialisationException
    {
        basicClientTest(DATABASE_NAME);
    }

    @Test
    public void testBasicClientWithoutDbName() throws ConnectionException, InitialisationException
    {
        basicClientTest(EMPTY_DATABASE_NAME);
    }

    protected void basicClientTest(String databaseName) throws ConnectionException, InitialisationException
    {
        MarkLogicConnection instance = new MarkLogicConnection(new MarkLogicConnectionProvider()
            .withHostname(LOCALHOST)
            .withPort(PORT)
            .withDatabase(databaseName)
            .withUsername(USER_NAME)
            .withPassword(USER_PASSWORD)
            .withAuthenticationType(AuthenticationType.basic)
            .withMarklogicConnectionType(MarkLogicConnectionType.DIRECT)
            .withConnectionId(CONNECTION_ID));

        instance.connect();
        DatabaseClient result = instance.getClient();
        this.databaseClientAssert(result, !databaseName.equals(EMPTY_DATABASE_NAME));

        DatabaseClientFactory.SecurityContext securityContext = result.getSecurityContext();

        assertTrue(securityContext instanceof BasicAuthContext);
        BasicAuthContext digest = (BasicAuthContext) securityContext;

        assertEquals(USER_NAME, digest.getUser());
        assertEquals(USER_PASSWORD, digest.getPassword());
    }

    //--------------------- SSL Context Tests --------------------------------//
    @Test
    public void sslContextTest() throws ConnectionException, InitialisationException
    {
        TlsContextFactory tlsContextFactory = new TlsContextFactory()
        {
            @Override
            public SSLContext createSslContext()
            {
                return null;
            }

            @Override
            public SSLSocketFactory createSocketFactory()
            {
                return null;
            }

            @Override
            public SSLServerSocketFactory createServerSocketFactory()
            {
                return null;
            }

            @Override
            public String[] getEnabledCipherSuites()
            {
                return new String[0];
            }

            @Override
            public String[] getEnabledProtocols()
            {
                return new String[0];
            }

            @Override
            public boolean isKeyStoreConfigured()
            {
                return false;
            }

            @Override
            public boolean isTrustStoreConfigured()
            {
                return false;
            }

            @Override
            public TlsContextKeyStoreConfiguration getKeyStoreConfiguration()
            {
                return null;
            }

            @Override
            public TlsContextTrustStoreConfiguration getTrustStoreConfiguration()
            {
                return null;
            }
        };

        MarkLogicConnection instance = new MarkLogicConnection(new MarkLogicConnectionProvider()
            .withHostname(LOCALHOST)
            .withPort(SSL_PORT)
            .withDatabase(EMPTY_DATABASE_NAME)
            .withUsername(USER_NAME)
            .withPassword(USER_PASSWORD)
            .withAuthenticationType(AuthenticationType.basic)
            .withMarklogicConnectionType(MarkLogicConnectionType.DIRECT)
            .withTlsContextFactory(tlsContextFactory)
            .withConnectionId(CONNECTION_ID));

        instance.connect();
        assertTrue(true);
    }

    //----------------- Default Level Authentication Tests -------------------//

    /*
    These tests are currently invalid as Default is not an option at this time
    @Test(expected = MarkLogicConnectorException.class)
    public void defaultAuthenticationClientTest()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, NULL_DATABASE_NAME, USER_NAME, USER_PASSWORD, null, null, null, CONNECTION_ID);
        instance.connect();
    }

    @Test(expected = MarkLogicConnectorException.class)
    public void nullAuthenticationClientTest()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, EMPTY_DATABASE_NAME, USER_NAME, USER_PASSWORD, null, null, null, CONNECTION_ID);
        instance.connect();
    }
     */
    //----------------- Application Level Authentication Tests ---------------//
/*
    This test is currently invalid as application-level is not an option at this time
    @Test(expected = MarkLogicConnectorException.class)
    public void applicationLevelAuthenticationClientTest()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, NULL_DATABASE_NAME, USER_NAME, USER_PASSWORD, "application-level", null, null, CONNECTION_ID);
        instance.connect();
    }
     */
    //--------------------- Helper Methods -----------------------------------//

    protected void databaseClientAssert(DatabaseClient client, boolean compareDbName)
    {
        assertEquals(LOCALHOST, client.getHost());
        assertEquals(PORT, client.getPort());
        if (compareDbName)
        {
            assertEquals(DATABASE_NAME, client.getDatabase());
        }
    }
}
