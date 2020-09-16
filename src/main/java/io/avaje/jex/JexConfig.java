package io.avaje.jex;

import io.avaje.jex.jetty.JettyLaunch;
import io.avaje.jex.spi.JsonService;

public class JexConfig {

  private int port = 7001;

  private String contextPath = "/";

  private Jetty jetty = new Jetty();

  private JsonService jsonService;

  public JexConfig context(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  public JexConfig port(int port) {
    this.port = port;
    return this;
  }

  public JexServer start() {
    return new JettyLaunch(this).start();
  }

  public int getPort() {
    return port;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Jetty getJetty() {
    return jetty;
  }

  public JsonService getJsonService() {
    return jsonService;
  }

  public static class Jetty {
    boolean sessions = true;
    boolean security = true;

    public boolean isSessions() {
      return sessions;
    }

    public boolean isSecurity() {
      return security;
    }
  }
}
