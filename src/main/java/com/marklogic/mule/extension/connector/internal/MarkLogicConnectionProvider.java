package com.marklogic.mule.extension.connector.internal;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class MarkLogicConnectionProvider implements PoolingConnectionProvider<MarkLogicConnection> {

  private final Logger logger = LoggerFactory.getLogger(MarkLogicConnectionProvider.class);

 /* Parameters that are always required to be configured. */
  @DisplayName("Host name")
  @Parameter
  private String hostname;
  
  @DisplayName("User name")
  @Parameter
  private String username;

  @DisplayName("Password")
  @Parameter
  private String password;

  @DisplayName("Port")
  @Parameter
  private int port;
  
  @DisplayName("Connection ID")
  @Parameter
  @Optional(defaultValue = "testConfig-223efe")
  private String connectionId;

 /* A parameter that is not required to be configured by the user. */
  /*@DisplayName("Friendly Name")
  @Parameter
  @Optional(defaultValue = "100")
  private int optionalParameter;*/

  @Override
  public MarkLogicConnection connect() throws ConnectionException {
    return new MarkLogicConnection(hostname, username, password, port, connectionId);
  }

  @Override
  public void disconnect(MarkLogicConnection connection) {
    try {
      connection.invalidate();
    } catch (Exception e) {
      logger.error("Error while disconnecting [" + connection.getId() + "]: " + e.getMessage(), e);
    }
  }

  @Override
  public ConnectionValidationResult validate(MarkLogicConnection connection) {
    ConnectionValidationResult result;
    if (connection.isConnected(port)) {
        result = ConnectionValidationResult.success();
    } else {
        result = ConnectionValidationResult.failure("Connection failed " + connection.getId(), new Exception());
    }
    return result;
  }
}
