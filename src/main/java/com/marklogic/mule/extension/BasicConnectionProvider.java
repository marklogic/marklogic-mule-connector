/*
 * Copyright (c) 2023 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientBuilder;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.impl.SSLUtil;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;


/**
 * This class (as it's name implies) provides connection instances and the functionality to disconnect and validate those
 * connections.
 * <p>
 * All connection related parameters (values required in order to create a connection) must be
 * declared in the connection providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares that connections resolved by this provider
 * will be pooled and reused. There are other implementations like {@link CachedConnectionProvider} which lazily creates and
 * caches connections or simply {@link ConnectionProvider} if you want a new connection each time something requires one.
 */
public class BasicConnectionProvider implements PoolingConnectionProvider<DatabaseClient> {

    @DisplayName("Host")
    @Parameter
    @Optional(defaultValue = "0.0.0.0")
    private String host;

    @DisplayName("Port")
    @Parameter
    @Optional(defaultValue = "8000")
    private Integer port;

    @DisplayName("Authentication Type")
    @Parameter
    @Summary("The authentication type used to authenticate to MarkLogic. Valid values are: digest, basic.")
    @Placement(tab = Placement.DEFAULT_TAB)
    private AuthenticationType authenticationType;

    @DisplayName("Connection Type")
    @Parameter
    @Summary("The type of connection used to work with MarkLogic, either DIRECT (non-load balanced) or GATEWAY (load-balanced).")
    @Optional(defaultValue = "DIRECT")
    private ConnectionType connectionType;

    @DisplayName("Username")
    @Parameter
    @Optional
    private String username;

    @DisplayName("Password")
    @Parameter
    @Optional
    private String password;

    @DisplayName("Database")
    @Parameter
    @Summary("The MarkLogic database name.")
    @Optional
    private String database;


    @DisplayName("CloudApiKey")
    @Parameter
    @Summary("The MarkLogic cloud Api key.")
    @Optional
    private String cloudApiKey;


    @DisplayName("BasePath")
    @Parameter
    @Summary("The MarkLogic cloud Base Path.")
    @Optional
    private String cloudBasePath;

    @DisplayName("TLS Context")
    @Placement(tab = "Security")
    @Parameter
    @Optional
    private TlsContextFactory tlsContextFactory;

    @Override
    public DatabaseClient connect() {
        DatabaseClientBuilder builder = new DatabaseClientBuilder()
            .withHost(host)
            .withPort(port)
            .withBasePath(cloudBasePath)
            .withDatabase(database)
            .withAuthType(authenticationType.name())
            .withConnectionType(connectionType.getMarkLogicConnectionType())
            .withUsername(username)
            .withPassword(password)
            .withCloudApiKey(cloudApiKey);

        if (tlsContextFactory != null) {
            // TODO With Java Client 6.4, I think we may be better off handing the keystore config over to the Java
            // Client and letting it construct the SSLContext itself. That allows for constructing a TrustManager from
            // the keystore as well. Mule's TlsContextFactory doesn't have a facility for creating a TrustManager,
            // which our Java Client requires.
            // However, if the user doesn't provide a key path, we'll need to call createSslContext as we're doing
            // below. And we'll still need to honor a truststore path if one is provided.
            try {
                builder.withSSLContext(tlsContextFactory.createSslContext());
            } catch (Exception e) {
                throw new RuntimeException(String.format(
                    "Unable to create SSL context; key store path: %s; cause: %s",
                    tlsContextFactory.getKeyStoreConfiguration().getPath(), e.getMessage()
                ), e);
            }

            // TODO Make this configurable.
            builder.withSSLHostnameVerifier(DatabaseClientFactory.SSLHostnameVerifier.ANY);

            // This is what the 1.x connector is doing. I think we need to be a bit better - i.e. if the user provides
            // a truststore path, we need to use that instead. Otherwise, we can default to the JVM's default trust
            // manager. Java Client 6.4 also allows for this being constructed based on the keystore.
            builder.withTrustManager(SSLUtil.getDefaultTrustManager());
        }

        return builder.build();
    }

    @Override
    public void disconnect(DatabaseClient client) {
        client.release();
    }

    @Override
    public ConnectionValidationResult validate(DatabaseClient client) {
        DatabaseClient.ConnectionResult result = client.checkConnection();
        return result.isConnected() ?
            ConnectionValidationResult.success() :
            ConnectionValidationResult.failure(result.getErrorMessage(), new RuntimeException(result.getErrorMessage()));
    }
}
