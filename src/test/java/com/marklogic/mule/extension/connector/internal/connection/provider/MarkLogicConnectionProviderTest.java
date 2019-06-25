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
package com.marklogic.mule.extension.connector.internal.connection.provider;

import com.marklogic.mule.extension.connector.api.connection.AuthenticationType;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mule.runtime.api.connection.ConnectionException;
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
    private static final AuthenticationType AUTHENTICATION_LEVEL = AuthenticationType.digest;

    /**
     * Test of connect method, of class MarkLogicConnectionProvider.
     */
    @Test
    public void testConnect() throws ConnectionException
    {
        MarkLogicConnectionProvider instance = new MarkLogicConnectionProvider(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, AUTHENTICATION_LEVEL, null, null, CONNECTION_ID);
        MarkLogicConnection result = instance.connect();
        assertEquals(CONNECTION_ID, result.getId());
    }

    @Test
    public void testDisconnect() throws ConnectionException
    {
        MarkLogicConnectionProvider instance = new MarkLogicConnectionProvider(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, AUTHENTICATION_LEVEL, null, null, CONNECTION_ID);
        MarkLogicConnection connection = instance.connect();
        instance.disconnect(connection);
    }

    /**
     * Tests of validate method, of class MarkLogicConnectionProvider.
     */
    @Test
    public void testValidatePass()
    {
        MarkLogicConnectionProvider instance = new MarkLogicConnectionProvider(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, AUTHENTICATION_LEVEL, null, null, CONNECTION_ID);

        MarkLogicConnection connection = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, AUTHENTICATION_LEVEL, null, null, CONNECTION_ID);
        connection.connect();

        ConnectionValidationResult result = instance.validate(connection);

        assertTrue(result.isValid());

    }

    @Test
    public void testValidateFail()
    {
        MarkLogicConnectionProvider instance = new MarkLogicConnectionProvider();

        MarkLogicConnection connection = new MarkLogicConnection(LOCALHOST, PORT, DATABASE_NAME, USER_NAME, USER_PASSWORD, AUTHENTICATION_LEVEL, null, null, CONNECTION_ID);
        ConnectionValidationResult result = instance.validate(connection);

        String message = String.format("Connection failed %s", CONNECTION_ID);
        assertEquals(message, result.getMessage());
        assertTrue(result.getException() instanceof Exception);
    }

}
