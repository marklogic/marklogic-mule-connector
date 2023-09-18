package org.mule.extension.internal;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientBuilder;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;


/**
 * This class (as it's name implies) provides connection instances and the funcionality to disconnect and validate those
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

    @DisplayName("Username")
    @Parameter
    @Optional(defaultValue = "admin")
    private String username;

    @DisplayName("Password")
    @Parameter
    @Optional(defaultValue = "admin")
    private String password;

    @DisplayName("Port")
    @Parameter
    @Optional(defaultValue = "8000")
    private Integer port;

    @Override
    public DatabaseClient connect() {
        return new DatabaseClientBuilder()
            .withHost(host)
            .withPort(port)
            .withDigestAuth(username, password)
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
