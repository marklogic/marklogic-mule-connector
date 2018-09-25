package com.marklogic.mule.extension.connector.internal;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.Password;
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
  
  @DisplayName("Host name")
  @Parameter
  @Optional(defaultValue = "localhost")
  private String hostname;
  
  @DisplayName("Port")
  @Parameter
  @Optional(defaultValue = "8010")
  private int port;
  
  @DisplayName("Database")
  @Parameter
  @Optional(defaultValue = "null")
  private String database;
  
  @DisplayName("User name")
  @Parameter
  @Optional(defaultValue = "admin")
  private String username;

  @DisplayName("Password")
  @Parameter
  @Password
  @Optional(defaultValue = "admin")
  private String password;
  
  @DisplayName("Authentication Type")
  @Parameter
  @Optional(defaultValue = "digest")
  private String authenticationType;
  
  @DisplayName("SSL Context (Not Yet Supported)")
  @Parameter
  @Optional(defaultValue = "null")
  private String sslContext;
  
  @DisplayName("Kerberos External Name (where applicable)")
  @Parameter
  @Optional(defaultValue = "null")
  private String kerberosExternalName;
  
  @DisplayName("Connection ID")
  @Parameter
  @Optional(defaultValue = "testConfig-223efe")
  private String connectionId;

  @Override
  public MarkLogicConnection connect() throws ConnectionException {
    return new MarkLogicConnection(hostname, port, database, username, password, authenticationType, sslContext, kerberosExternalName, connectionId);
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
