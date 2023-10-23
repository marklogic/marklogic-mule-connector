package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientBuilder;
import com.marklogic.mule.extension.connection.AuthenticationType;
import com.marklogic.mule.extension.connection.ConnectionType;
import org.mule.runtime.api.connection.*;
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
    @Placement(tab="Security")
    @Parameter
    @Optional
    private TlsContextFactory tlsContextFactory;

    @Override
    public DatabaseClient connect() {
        return new DatabaseClientBuilder()
            .withHost(host)
            .withPort(port)
            .withBasePath(cloudBasePath)
            .withDatabase(database)
            .withAuthType(authenticationType.name())
            .withConnectionType(connectionType.getMarkLogicConnectionType())
            .withUsername(username)
            .withPassword(password)
            .withCloudApiKey(cloudApiKey)
            .build();
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
