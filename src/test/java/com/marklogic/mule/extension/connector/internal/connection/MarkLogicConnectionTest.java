package com.marklogic.mule.extension.connector.internal.connection;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.BasicAuthContext;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;
import com.marklogic.client.DatabaseClientFactory.KerberosAuthContext;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

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
    private static final String NULL_DATABASE_NAME = "null";
    private static final int PORT = 8000;
    private static final String LOCALHOST = "localhost";
    
    /**
     * Test of getId method, of class MarkLogicConnection.
     */
    @Test
    public void testGetId()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, "digest", null, null, CONNECTION_ID);
        String result = instance.getId();
        assertEquals(CONNECTION_ID, result); 
    }
    
    /**
     * Test of invalidate method, of class MarkLogicConnection.
     */
    @Test
    public void testInvalidate()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, "digest", null, null, CONNECTION_ID);;
        instance.invalidate(); 
    }
    
    /**
     * Test of invalidate method, of class MarkLogicConnection.
     */
    @Test
    public void testNegativeInvalidate()
    {
        //Currently using "application-level" because it throws an error in the constructor
        //that tests the "catch" and creates a null client that throws and error
        //when it tries to invalidate that tests the invalidate "catch"
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, "application-level", null, null, CONNECTION_ID);;
        instance.invalidate(); 
    }

    /**
     * Test of isConnected method, of class MarkLogicConnection.
     */
    @Test
    public void testIsConnected()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, "digest", null, null, CONNECTION_ID);
        boolean result = instance.isConnected(PORT);
        assertEquals(true, result);
    }
    
    /**
     * Negative Test of isConnected method, of class MarkLogicConnection.
     */
    @Test
    public void testIsNotConnected()
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, "digest", null, null, CONNECTION_ID);
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
        digestClientTest(NULL_DATABASE_NAME);
    }
    
    @Test
    public void testDefaultClient()
    {
        digestClientTest(NULL_DATABASE_NAME, "bad-authentication-type");
    }

    protected void digestClientTest(String databaseName)
    {
        digestClientTest(databaseName, "digest");
    }
    
    protected void digestClientTest(String databaseName, String authenticationType)
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, databaseName, USER_NAME, USER_PASSWORD, authenticationType, null, null, CONNECTION_ID);
        DatabaseClient result = instance.getClient();
        this.databaseClientAssert(result, !databaseName.equals(NULL_DATABASE_NAME));
        
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
        basicClientTest(NULL_DATABASE_NAME);
    }
    
    protected void basicClientTest(String databaseName)
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, databaseName, USER_NAME, USER_PASSWORD, "basic", null, null, CONNECTION_ID);
        DatabaseClient result = instance.getClient();
        this.databaseClientAssert(result, !databaseName.equals(NULL_DATABASE_NAME));
        
        DatabaseClientFactory.SecurityContext securityContext = result.getSecurityContext();
        
        assertTrue(securityContext instanceof BasicAuthContext);
        BasicAuthContext digest = (BasicAuthContext) securityContext;
        
        assertEquals(USER_NAME, digest.getUser());
        assertEquals(USER_PASSWORD, digest.getPassword());
    }
    
    //----------------- Application Level Authentication Tests ---------------//
    
    /**
     * The following two test throw an error
     * 
     * MarkLogicConnection constructor catches an exception instead of throwing
     * Exception is: java.lang.IllegalStateException: makeSecurityContext should only be called with BASIC or DIGEST Authentication
    **/
    
    @Ignore
    public void testApplicationLevelClientWithDbName()
    {
        applicationLevelClientTest("data-hub-STAGING");
    }
    
    @Ignore
    public void testApplicationLevelClientWithoutDbName()
    {
        applicationLevelClientTest(NULL_DATABASE_NAME);
    }
    
    protected void applicationLevelClientTest(String databaseName)
    {
        MarkLogicConnection instance = new MarkLogicConnection("***REMOVED***", 8020, databaseName, USER_NAME, USER_PASSWORD, "application-level", null, null, CONNECTION_ID);
        DatabaseClient result = instance.getClient();
        this.databaseClientAssert(result, !databaseName.equals(NULL_DATABASE_NAME));
        
        DatabaseClientFactory.SecurityContext securityContext = result.getSecurityContext();
        
        assertNull(securityContext);
    }
    
    //----------------- Kerveros Authentication Tests ------------------------//
    
    /**
     * The following two test throw an error
     * 
     * MarkLogicConnection constructor catches an exception instead of throwing
     * Exception is: com.marklogic.client.FailedRequestException: Unable to obtain Principal Name for authentication
    **/
    @Ignore
    public void testKerberosClientWithDbName()
    {
        kerberosClientTest(DATABASE_NAME, "null");
    }
    
    @Ignore
    public void testKerberosClientWithoutDbName()
    {
        kerberosClientTest(NULL_DATABASE_NAME, "null");
    }
    
    /**
     * The following two test throw an error
     * 
     * MarkLogicConnection constructor catches an exception instead of throwing
     * Exception is: com.marklogic.client.FailedRequestException: KrbException: Cannot locate default realm
    **/
    @Ignore
    public void testKerberosExternalClientWithDbName()
    {
        kerberosClientTest(DATABASE_NAME, "test");
    }
    
    @Ignore
    public void testKerberosExternalClientWithoutDbName()
    {
        kerberosClientTest(NULL_DATABASE_NAME, "test");
    }
    
    protected void kerberosClientTest(String databaseName, String kerberosExternalName)
    {
        MarkLogicConnection instance = new MarkLogicConnection(LOCALHOST, PORT, databaseName, USER_NAME, USER_PASSWORD, "kerberos", null, kerberosExternalName, CONNECTION_ID);
        DatabaseClient result = instance.getClient();
        this.databaseClientAssert(result, !databaseName.equals(NULL_DATABASE_NAME));
        
        DatabaseClientFactory.SecurityContext securityContext = result.getSecurityContext();
        
        assertTrue(securityContext instanceof KerberosAuthContext);   
    }
    
    //--------------------- Helper Methods -----------------------------------//
    
    protected void databaseClientAssert(DatabaseClient client)
    {
        databaseClientAssert(client, true);
    }
    
    protected void databaseClientAssert(DatabaseClient client, boolean compareDbName)
    {
        assertEquals(LOCALHOST, client.getHost());
        assertEquals(PORT, client.getPort());
        if(compareDbName)
        {
            assertEquals(DATABASE_NAME, client.getDatabase());
        }
    }
    
}
