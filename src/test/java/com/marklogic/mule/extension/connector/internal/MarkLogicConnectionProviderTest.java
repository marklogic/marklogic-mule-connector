/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marklogic.mule.extension.connector.internal;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mule.runtime.api.connection.ConnectionValidationResult;

/**
 *
 * @author jshingle
 */
public class MarkLogicConnectionProviderTest
{
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String USER_PASSWORD = "test-password";
    private static final String USER_NAME = "test-user";
    private static final String DATABASE_NAME = "test";
    private static final int PORT = 8000;
    private static final String LOCALHOST = "localhost";
    private static final String AUTHENTICATION_LEVEL = "digest";
    
    public MarkLogicConnectionProviderTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of connect method, of class MarkLogicConnectionProvider.
     */
    //@Test
    public void testConnect() throws Exception
    {
        System.out.println("connect");
        MarkLogicConnectionProvider instance = new MarkLogicConnectionProvider();
        MarkLogicConnection expResult = null;
        MarkLogicConnection result = instance.connect();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testDisconnect()
    {
        MarkLogicConnection connection = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, AUTHENTICATION_LEVEL, null, null, CONNECTION_ID);
        MarkLogicConnectionProvider instance = new MarkLogicConnectionProvider();
        instance.disconnect(connection);
    }
    
    /**
     * Test of validate method, of class MarkLogicConnectionProvider.
     */
    @Test
    public void testValidate()
    {
        MarkLogicConnection connection = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, AUTHENTICATION_LEVEL, null, null, CONNECTION_ID);
        MarkLogicConnectionProvider instance = new MarkLogicConnectionProvider();
        String message = String.format("Connection failed %s", CONNECTION_ID);
        ConnectionValidationResult result = instance.validate(connection);
        
        assertEquals(message, result.getMessage());
        assertTrue(result.getException() instanceof Exception);
        
    }
    
}
