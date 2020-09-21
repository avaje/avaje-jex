package io.avaje.jex;

import io.avaje.jex.spi.JsonService;

public class JexConfig {

  private int port = 7001;

  private String contextPath = "/";

  private JettyConfig jetty = new JettyConfig();

  private JsonService jsonService;

  public JexConfig context(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  public JexConfig port(int port) {
    this.port = port;
    return this;
  }

  public int getPort() {
    return port;
  }

  public String getContextPath() {
    return contextPath;
  }

  public JettyConfig getJetty() {
    return jetty;
  }

  public JsonService getJsonService() {
    return jsonService;
  }

}
