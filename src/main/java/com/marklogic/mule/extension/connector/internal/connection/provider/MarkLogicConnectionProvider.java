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

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class (as it's name implies) provides connection instances and the
 * functionality to disconnect and validate those connections.
 * <p>
 * All connection related parameters (values required in order to create a
 * connection) must be declared in the connection providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares
 * that connections resolved by this provider will be pooled and reused. There
 * are other implementations like {@link CachedConnectionProvider} which lazily
 * creates and caches connections or simply {@link ConnectionProvider} if you
 * want a new connection each time something requires one.
 */
public class MarkLogicConnectionProvider implements PoolingConnectionProvider<MarkLogicConnection>
{

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicConnectionProvider.class);

    @DisplayName("Host name")
    @Parameter
    @Summary("The hostname against which operations should run.")
    @Example("localhost")
    private String hostname;

    @DisplayName("Port")
    @Parameter
    @Summary("The app server port against which operations should run.")
    @Example("8010")
    private int port;

    @DisplayName("Database")
    @Parameter
    @Summary("The MarkLogic database name (i.e., xdmp:database-name()), against which operations should run. If not supplied or left as null, the database will be determined automatically by the app server port being called.")
    @Optional(defaultValue = "null")
    @Example("data-hub-STAGING")
    private String database;

    @DisplayName("User name")
    @Parameter
    @Summary("The named user.")
    @Example("admin")
    private String username;

    @DisplayName("Password")
    @Parameter
    @Summary("The named user's password.")
    @Password
    @Example("admin")
    private String password;

    @DisplayName("Authentication Type")
    @Parameter
    @Summary("The authentication type used to authenticate to MarkLogic. Valid values are: digest, basic.")
    private AuthenticationType authenticationType;

    @DisplayName("TLS Context")
    @Parameter
    @Optional
    private TlsContextFactory tlsContextFactory;

    @DisplayName("Kerberos External Name (Not Yet Supported)")
    @Parameter
    @Summary("If \"kerberos\" is used for the authenticationType parameter, a Kerberos external name value can be supplied if needed.")
    @Optional(defaultValue = "null")
    private String kerberosExternalName;

    @DisplayName("Connection ID")
    @Parameter
    @Summary("An identifier used for the Mulesoft Connector to keep state of its connection to MarkLogic. Also set on the Connector configuration parameters.")
    @Example("testConfig-223efe")
    private String connectionId;

    //Mainly Used for testing
    MarkLogicConnectionProvider(String hostname, int port, String database, String username, String password, AuthenticationType authenticationType, TlsContextFactory tlsContextFactory, String kerberosExternalName, String connectionId)
    {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.authenticationType = authenticationType;
        this.tlsContextFactory = tlsContextFactory;
        this.kerberosExternalName = kerberosExternalName;
        this.connectionId = connectionId;
    }

    public MarkLogicConnectionProvider()
    {
    }

    @Override
    public MarkLogicConnection connect() throws ConnectionException
    {

        MarkLogicConnection conn = new MarkLogicConnection(hostname, port, database, username, password, authenticationType, tlsContextFactory, kerberosExternalName, connectionId);
        conn.connect();
        return conn;
    }

    @Override
    public void disconnect(MarkLogicConnection connection)
    {
        connection.invalidate();
    }

    @Override
    public ConnectionValidationResult validate(MarkLogicConnection connection)
    {
        ConnectionValidationResult result;
        if (connection.isConnected(port))
        {
            result = ConnectionValidationResult.success();
        }
        else
        {
            result = ConnectionValidationResult.failure("Connection failed " + connection.getId(), new Exception());
        }
        return result;
    }
}
