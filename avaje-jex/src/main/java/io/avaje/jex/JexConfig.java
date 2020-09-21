package io.avaje.jex;

import io.avaje.jex.spi.JsonService;

public class JexConfig {

  private int port = 7001;

  private String contextPath = "/";

  private JettyConfig jetty = new JettyConfig();

  private JsonService jsonService;

  private AccessManager accessManager;

  private boolean prefer405 = true;

  /**
   * Set the context path.
   */
  public JexConfig context(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  /**
   * Set the port.
   */
  public JexConfig port(int port) {
    this.port = port;
    return this;
  }

  /***
   * Set the AccessManager.
   */
  public JexConfig accessManager(AccessManager accessManager) {
    this.accessManager = accessManager;
    return this;
  }

  /***
   * Set the JsonService.
   */
  public JexConfig jsonService(JsonService jsonService) {
    this.jsonService = jsonService;
    return this;
  }

  /**
   * Set the jetty config.
   */
  public JexConfig jetty(JettyConfig jetty) {
    this.jetty = jetty;
    return this;
  }

  /**
   * Return the port.
   */
  public int getPort() {
    return port;
  }

  /**
   * Return the context path.
   */
  public String getContextPath() {
    return contextPath;
  }

  /**
   * Return the Jetty config.
   */
  public JettyConfig getJetty() {
    return jetty;
  }

  /**
   * Return the JsonService to use.
   */
  public JsonService getJsonService() {
    return jsonService;
  }

  /**
   * Return the AccessManager.
   */
  public AccessManager getAccessManager() {
    return accessManager;
  }

  public boolean isPrefer405() {
    return prefer405;
  }
}
