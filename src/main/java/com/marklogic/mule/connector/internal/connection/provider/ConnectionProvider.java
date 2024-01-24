/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2024 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.mule.connector.internal.connection.provider;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientBuilder;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.impl.SSLUtil;
import com.marklogic.mule.connector.api.types.AuthenticationType;
import com.marklogic.mule.connector.api.types.ConnectionType;
import com.marklogic.mule.connector.api.types.HostnameVerifier;
import com.marklogic.mule.connector.internal.error.ErrorType;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextTrustStoreConfiguration;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.exception.ModuleException;

import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import static org.mule.runtime.extension.api.annotation.param.display.Placement.DEFAULT_TAB;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.SECURITY_TAB;

/**
 * Implements {@code CachedConnectionProvider} per
 * https://docs.mulesoft.com/mule-sdk/latest/define-configurations-and-connection-providers#defining-a-connection-management-strategy ,
 * as a DatabaseClient is a threadsafe object.
 */
public class ConnectionProvider implements CachedConnectionProvider<DatabaseClient>, Initialisable {

    @Parameter
    @Summary("The hostname of the MarkLogic server to connect to.")
    @Example("localhost")
    @Placement(tab = DEFAULT_TAB)
    private String host = "localhost";

    @Parameter
    @Summary("The port of the MarkLogic REST API app server to connect to.")
    @Example("8000")
    @Placement(tab = DEFAULT_TAB)
    private Integer port;

    @Parameter
    @DisplayName("Authentication Type")
    @Summary("The authentication required by the MarkLogic REST API app server.")
    // This is actually required, but oddly, setting it to Optional with a default value achieves the desired result
    // in Anypoint of making it required and having a default value.
    @Optional(defaultValue = "DIGEST")
    private AuthenticationType authenticationType;

    @Parameter
    @DisplayName("Connection Type")
    @Summary("Set to GATEWAY when connecting to MarkLogic through a load balancer; otherwise select DEFAULT.")
    @Optional(defaultValue = "DIRECT")
    private ConnectionType connectionType;

    @Parameter
    @Summary("MarkLogic username to use when 'Authentication Type' is set to BASIC or DIGEST.")
    @Optional
    private String username;

    @Parameter
    @Summary("Password for the MarkLogic user when 'Authentication Type' is set to BASIC or DIGEST.")
    @Optional
    @Password
    private String password;

    @Parameter
    @DisplayName("MarkLogic Cloud API Key")
    @Summary("API key to use when 'Authentication Type' is set to MARKLOGIC_CLOUD.")
    @Optional
    @Password
    private String cloudApiKey;

    @Parameter
    @DisplayName("SAML Token")
    @Summary("SAML access token to use when 'Authentication Type' is set to SAML.")
    @Optional
    @Password
    private String samlToken;

    @Parameter
    @DisplayName("Kerberos Principal")
    @Summary("Kerberos principal to use when 'Authentication Type' is set to KERBEROS.")
    @Optional
    private String kerberosPrincipal;

    @Parameter
    @DisplayName("Base Path")
    @Summary("Base path for each request to MarkLogic; typically used when connecting through a reverse proxy; required by MarkLogic Cloud.")
    @Optional
    private String basePath;

    @Parameter
    @DisplayName("Database")
    @Summary("Identifies the MarkLogic content database to query; only required when the database associated with " +
        "the app server identified by the 'Port' value is not the one you wish to query.")
    @Optional
    @Placement(tab = SECURITY_TAB)
    private String database;

    @Parameter
    @DisplayName("TLS Context")
    @Summary("Controls how SSL/TLS connections are made with MarkLogic.")
    @Optional
    @Placement(tab = SECURITY_TAB)
    private TlsContextFactory tlsContextFactory;

    @Parameter
    @DisplayName("Hostname Verifier")
    @Placement(tab = SECURITY_TAB)
    @Summary("Specifies how a hostname is verified during SSL authentication. COMMON allows any level of subdomain " +
        "for SSL certificates with wildcard domains. STRICT only allows one subdomain level for SSL certificates with " +
        "wildcard domains. ANY disables hostname verification and is not recommended for production usage.")
    @Optional(defaultValue = "COMMON")
    private HostnameVerifier hostnameVerifier;

    @Override
    public DatabaseClient connect() {
        DatabaseClientBuilder builder = new DatabaseClientBuilder()
            .withHost(host)
            .withPort(port)
            .withBasePath(basePath)
            .withDatabase(database)
            .withAuthType(authenticationType.name())
            .withConnectionType(DatabaseClient.ConnectionType.valueOf(connectionType.name()))
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

    // See https://docs.mulesoft.com/mule-sdk/latest/define-configurations-and-connection-providers#handling-ssl-connections .
    @Override
    public void initialise() throws InitialisationException {
        if (this.tlsContextFactory != null && tlsContextFactory instanceof Initialisable) {
            ((Initialisable) tlsContextFactory).initialise();
        }
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
            // Have not found a way to cause this error to happen yet, but createSslContext() can throw a
            // checked exception so it needs to be handled. Attempts at providing invalid inputs result in
            // the "initialize()" call failing instead.
            String message = String.format("Unable to create SSL context; cause: %s", e.getMessage());
            throw new ModuleException(message, ErrorType.CONNECTION_ERROR, e);
        }

        builder.withTrustManager(tlsContextFactory.getTrustStoreConfiguration().isInsecure() ?
            INSECURE_TRUST_MANAGER :
            newTrustManager(tlsContextFactory.getTrustStoreConfiguration())
        );

        if (hostnameVerifier != null) {
            switch (hostnameVerifier) {
                case COMMON:
                    builder.withSSLHostnameVerifier(DatabaseClientFactory.SSLHostnameVerifier.COMMON);
                    break;
                case STRICT:
                    builder.withSSLHostnameVerifier(DatabaseClientFactory.SSLHostnameVerifier.STRICT);
                    break;
                case ANY:
                default:
                    builder.withSSLHostnameVerifier(DatabaseClientFactory.SSLHostnameVerifier.ANY);
                    break;
            }
        }
    }

    /**
     * This is depending on what are technically internal methods in the Java Client.
     * Ideally the Java Client can make this public, as it is handling some tedious but well-known Java code
     * for constructing a trust manager based on a truststore.
     *
     * @param config contains the configuration necessary to build the X509 Trust Manager
     * @return an X509TrustManager object
     */
    private X509TrustManager newTrustManager(TlsContextTrustStoreConfiguration config) {
        KeyStore trustStore = SSLUtil.getKeyStore(config.getPath(), config.getPassword().toCharArray(), config.getType());
        return (X509TrustManager) SSLUtil.getTrustManagers(config.getAlgorithm(), trustStore)[0];
    }

    private static final X509TrustManager INSECURE_TRUST_MANAGER = new X509TrustManager() {
        @Override
        @SuppressWarnings("java:S4830")
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // Intentionally empty to support Anypoint's "Insecure" checkbox for a trust store.
        }

        @Override
        @SuppressWarnings("java:S4830")
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // Intentionally empty to support Anypoint's "Insecure" checkbox for a trust store.
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };
}
