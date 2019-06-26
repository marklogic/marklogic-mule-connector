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
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.BasicAuthContext;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;
import com.marklogic.mule.extension.connector.internal.operation.MarkLogicConnectionInvalidationListener;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextKeyStoreConfiguration;
import org.mule.runtime.api.tls.TlsContextTrustStoreConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import static org.mockito.Mockito.*;

/**
 *
 * @author jshingler
 */
public class MarkLogicConnectionTest
{

    private static final String CONNECTION_ID = "test-connection-id";
    private static final String USER_PASSWORD = "test-password";
    private static final String USER_NAME = "test-user";
    private static final String DATABASE_NAME = "test";
    private static final String NULL_DATABASE_NAME = null;
    private static final String EMPTY_DATABASE_NAME = "";
    private static final String NULL_STR_DATABASE_NAME = "null";
    private static final int PORT = 8000;
    private static final String LOCALHOST = "localhost";

    /**
     * Test of getId method, of class MarkLogicConnection.
     */
    @Test
    public void testGetId()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, AuthenticationType.digest, null, null, CONNECTION_ID);
        String result = instance.getId();
        assertEquals(CONNECTION_ID, result);
    }
    
    @Test
    public void testIsConnectedNull()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, AuthenticationType.digest, null, null, CONNECTION_ID);
        assertFalse(instance.isConnected(PORT));
    }

    /**
     * Test of invalidate method, of class MarkLogicConnection.
     */
    @Test
    public void testInvalidate()
    {
        MarkLogicConnectionInvalidationListener listener = mock(MarkLogicConnectionInvalidationListener.class);
        
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, NULL_STR_DATABASE_NAME, USER_NAME, USER_PASSWORD, AuthenticationType.digest, null, null, CONNECTION_ID);;
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
    public void testIsConnected()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, EMPTY_DATABASE_NAME, USER_NAME, USER_PASSWORD, AuthenticationType.digest, null, null, CONNECTION_ID);
        instance.connect();
        boolean result = instance.isConnected(PORT);
        assertEquals(true, result);
    }

    /**
     * Negative Test of isConnected method, of class MarkLogicConnection.
     */
    @Test
    public void testIsNotConnected()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, NULL_DATABASE_NAME, USER_NAME, USER_PASSWORD, AuthenticationType.digest, null, null, CONNECTION_ID);
        instance.connect();
        boolean result = instance.isConnected(8001);
        assertEquals(false, result);
    }

    //----------------- Digest & Default Authentication Tests ----------------//
    @Test
    public void testDigestClientWithDbName()
    {
        digestClientTest(DATABASE_NAME);
    }

    //Should have used paramatized test 
    @Test
    public void testDigestClientWithoutDbName()
    {
        digestClientTest(EMPTY_DATABASE_NAME);
    }

    protected void digestClientTest(String databaseName)
    {
        digestClientTest(databaseName, AuthenticationType.digest);
    }

    protected void digestClientTest(String databaseName, AuthenticationType authenticationType)
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, databaseName, USER_NAME, USER_PASSWORD, authenticationType, null, null, CONNECTION_ID);
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
    public void testBasicClientWithDbName()
    {
        basicClientTest(DATABASE_NAME);
    }

    @Test
    public void testBasicClientWithoutDbName()
    {
        basicClientTest(EMPTY_DATABASE_NAME);
    }

    protected void basicClientTest(String databaseName)
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, databaseName, USER_NAME, USER_PASSWORD, AuthenticationType.basic, null, null, CONNECTION_ID);
        instance.connect();
        DatabaseClient result = instance.getClient();
        this.databaseClientAssert(result, !databaseName.equals(EMPTY_DATABASE_NAME));

        DatabaseClientFactory.SecurityContext securityContext = result.getSecurityContext();

        assertTrue(securityContext instanceof BasicAuthContext);
        BasicAuthContext digest = (BasicAuthContext) securityContext;

        assertEquals(USER_NAME, digest.getUser());
        assertEquals(USER_PASSWORD, digest.getPassword());
    }

    //----------------- Kerveros Authentication Tests ------------------------//
    /**
     * The following two test throw an error
     * <p>
     * Underlying Exception is: com.marklogic.client.FailedRequestException:
     * Unable to obtain Principal Name for authentication
    *
     */
    /*
These tests are currently invalid as KERBEROS is not an option at this time
    @Test(expected = MarkLogicConnectorException.class)
    public void testKerberosClientWithDbName()
    {
        kerberosClientTest(DATABASE_NAME, "null");
    }
    
    @Test(expected = MarkLogicConnectorException.class)
    public void testKerberosClientWithoutDbName()
    {
        kerberosClientTest(NULL_DATABASE_NAME, "null");
    }
    
    /**
     * The following two test throw an error
     * 
     * Underlying Exception is: com.marklogic.client.FailedRequestException: KrbException: Cannot locate default realm
    **/
 /*
These tests are currently invalid as KERBEROS is not an option at this time

    @Test(expected = MarkLogicConnectorException.class)
    public void testKerberosExternalClientWithDbName()
    {
        kerberosClientTest(DATABASE_NAME, "test");
    }
    
    @Test(expected = MarkLogicConnectorException.class)
    public void testKerberosExternalClientWithoutDbName()
    {
        kerberosClientTest(NULL_DATABASE_NAME, "test");
    }
    protected void kerberosClientTest(String databaseName, String kerberosExternalName)
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, databaseName, USER_NAME, USER_PASSWORD, AuthenticationType.KERBEROS, null, kerberosExternalName, CONNECTION_ID);
        instance.connect();  
    }
     */
    //--------------------- SSL Context Tests --------------------------------//
    @Test
    public void sslContextTest()
    {
        TlsContextFactory tlsContextFactory = new TlsContextFactory()
        {
            @Override
            public SSLContext createSslContext() throws KeyManagementException, NoSuchAlgorithmException
            {
                return null;
            }

            @Override
            public SSLSocketFactory createSocketFactory() throws KeyManagementException, NoSuchAlgorithmException
            {
                return null;
            }

            @Override
            public SSLServerSocketFactory createServerSocketFactory() throws KeyManagementException, NoSuchAlgorithmException
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
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, EMPTY_DATABASE_NAME, USER_NAME, USER_PASSWORD, AuthenticationType.certificate, tlsContextFactory, null, CONNECTION_ID);
        instance.connect();
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
    protected void databaseClientAssert(DatabaseClient client)
    {
        databaseClientAssert(client, true);
    }

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
