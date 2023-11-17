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
import com.marklogic.mule.extension.api.AuthenticationType;
import com.marklogic.mule.extension.api.ConnectionType;
import com.marklogic.mule.extension.api.HostnameVerifier;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextTrustStoreConfiguration;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * This class (as it's name implies) provides connection instances and the functionality to disconnect and validate those
 * connections.
 * <p>
 * All connection related parameters (values required in order to create a connection) must be
 * declared in the connection providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares that connections resolved by this provider
 * will be pooled and reused. There are other implementations like {@link CachedConnectionProvider} which lazily creates and
 * caches connections or simply {@link org.mule.runtime.api.connection.ConnectionProvider} if you want a new connection each time something requires one.
 */
public class ConnectionProvider implements PoolingConnectionProvider<DatabaseClient> {

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
    @Summary("The authentication type used to authenticate to MarkLogic.")
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

    @DisplayName("MarkLogic Cloud API Key")
    @Parameter
    @Summary("The API key for authenticating with a MarkLogic Cloud instance.")
    @Optional
    private String cloudApiKey;

    @DisplayName("SAML Token")
    @Parameter
    @Summary("SAML access token for when the MarkLogic app server requires 'saml' authentication.")
    @Optional
    private String samlToken;

    @DisplayName("Kerberos Principal")
    @Parameter
    @Summary("Kerberos principal for when the MarkLogic app server requires 'kerberos' authentication.")
    @Optional
    private String kerberosPrincipal;

    @DisplayName("Base Path")
    @Parameter
    @Summary("TODO")
    @Optional
    private String basePath;

    @DisplayName("Database")
    @Parameter
    @Summary("Identifies the MarkLogic content database to query; only required when the database associated with " +
        "the app server identified by the 'Port' value is not the one you wish to query.")
    @Optional
    private String database;

    @DisplayName("TLS Context")
    @Placement(tab = "SSL/TLS")
    @Summary("Controls how SSL/TLS connections are made with MarkLogic.")
    @Parameter
    @Optional
    private TlsContextFactory tlsContextFactory;

    @DisplayName("Hostname Verifier")
    @Placement(tab = "Security")
    @Summary("TODO")
    @Parameter
    @Optional
    private HostnameVerifier hostnameVerifier;

    @Override
    public DatabaseClient connect() {
        DatabaseClientBuilder builder = new DatabaseClientBuilder()
            .withHost(host)
            .withPort(port)
            .withBasePath(basePath)
            .withDatabase(database)
            .withAuthType(authenticationType.name())
            .withConnectionType(connectionType.getMarkLogicConnectionType())
            .withUsername(username)
            .withPassword(password)
            .withCloudApiKey(cloudApiKey)
            .withSAMLToken(samlToken)
            .withKerberosPrincipal(kerberosPrincipal)
            .withSSLHostnameVerifier(DatabaseClientFactory.SSLHostnameVerifier.ANY);

        if (tlsContextFactory != null) {
            configureSSL(builder);
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

    private void configureSSL(DatabaseClientBuilder builder) {
        try {
            builder.withSSLContext(tlsContextFactory.createSslContext());
        } catch (Exception e) {
            throw new RuntimeException(String.format(
                "Unable to create SSL context; cause: %s",
                tlsContextFactory.getKeyStoreConfiguration().getPath(), e.getMessage()
            ), e);
        }

        builder.withTrustManager(tlsContextFactory.getTrustStoreConfiguration().isInsecure() ?
            INSECURE_TRUST_MANAGER :
            newTrustManager(tlsContextFactory.getTrustStoreConfiguration())
        );

        if (hostnameVerifier != null) {
            builder.withSSLHostnameVerifier(hostnameVerifier.getSslHostnameVerifier());
        }
    }

    /**
     * This is depending on what are technically internal methods in the Java Client.
     * TODO Ideally the Java Client can make this public, as it is handling some tedious but well-known Java code
     * for constructing a trust manager based on a truststore.
     *
     * @param config
     * @return
     */
    private X509TrustManager newTrustManager(TlsContextTrustStoreConfiguration config) {
        KeyStore trustStore = SSLUtil.getKeyStore(config.getPath(), config.getPassword().toCharArray(), config.getType());
        return (X509TrustManager) SSLUtil.getTrustManagers(config.getAlgorithm(), trustStore)[0];
    }

    private final static X509TrustManager INSECURE_TRUST_MANAGER = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };
}
