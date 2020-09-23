package io.avaje.jex;

import io.avaje.jex.StaticFileSource.Location;
import io.avaje.jex.spi.JsonService;

import java.util.ArrayList;
import java.util.List;

public class JexConfig {

  private int port = 7001;

  private String contextPath = "/";

  private JettyConfig jetty = new JettyConfig();

  private JsonService jsonService;

  private AccessManager accessManager;

  private boolean prefer405 = true;
  private boolean ignoreTrailingSlashes = true;

  private final List<StaticFileSource> staticFileConfig = new ArrayList<>();

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

  public StaticFiles staticFiles() {
    return new StaticFiles();
  }

  public JexConfig addStaticFiles(StaticFileSource config) {
    this.staticFileConfig.add(config);
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

  public boolean isIgnoreTrailingSlashes() {
    return ignoreTrailingSlashes;
  }

  public List<StaticFileSource> getStaticFileConfig() {
    return staticFileConfig;
  }

  public boolean isPreCompressStaticFiles() {
    return false;
  }

  public class StaticFiles {
    public JexConfig addClasspath(String path) {
      return addStaticFiles(new StaticFileSource("/", path, Location.CLASSPATH));
    }
    public JexConfig addClasspath(String urlPrefix, String path) {
      return addStaticFiles(new StaticFileSource(urlPrefix, path, Location.CLASSPATH));
    }
    public JexConfig addExternal(String path) {
      return addStaticFiles(new StaticFileSource("/", path, Location.EXTERNAL));
    }
    public JexConfig addExternal(String urlPrefix, String path) {
      return addStaticFiles(new StaticFileSource(urlPrefix, path, Location.EXTERNAL));
    }
  }
}
