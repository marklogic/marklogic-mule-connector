package org.mule.extension.internal;


/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class BasicConnection {

  private final String id;

  private String host;

  private Integer port;

  private String username;

  private String password;


  public BasicConnection(String id, String host, String username, String password, Integer port) {
    this.id = id;
    this.host = host;
    this.username = username;
    this.password = password;
    this.port = port;
  }

  public String getId() {
    return id;
  }

  public void invalidate() {
    // do something to invalidate this connection!
  }
  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
