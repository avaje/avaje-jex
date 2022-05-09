package io.avaje.jex;

import io.avaje.jex.spi.JsonService;

import java.util.HashMap;
import java.util.Map;

class DJexConfig implements JexConfig {

  private int port = 7001;
  private String host;
  private String contextPath = "/";
  private boolean health = true;
  private boolean ignoreTrailingSlashes = true;

  private boolean preCompressStaticFiles;
  private JsonService jsonService;
  private AccessManager accessManager;
  private UploadConfig multipartConfig;
  private int multipartFileThreshold = 8 * 1024;
  private final Map<String, TemplateRender> renderers = new HashMap<>();

  @Override
  public JexConfig port(int port) {
    this.port = port;
    return this;
  }

  @Override
  public JexConfig host(String host) {
    this.host = host;
    return this;
  }

  @Override
  public JexConfig contextPath(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  @Override
  public JexConfig health(boolean health) {
    this.health = health;
    return this;
  }

  @Override
  public JexConfig ignoreTrailingSlashes(boolean ignoreTrailingSlashes) {
    this.ignoreTrailingSlashes = ignoreTrailingSlashes;
    return this;
  }

  @Override
  public JexConfig preCompressStaticFiles(boolean preCompressStaticFiles) {
    this.preCompressStaticFiles = preCompressStaticFiles;
    return this;
  }

  @Override
  public JexConfig jsonService(JsonService jsonService) {
    this.jsonService = jsonService;
    return this;
  }

  @Override
  public JexConfig accessManager(AccessManager accessManager) {
    this.accessManager = accessManager;
    return this;
  }

  @Override
  public JexConfig multipartConfig(UploadConfig multipartConfig) {
    this.multipartConfig = multipartConfig;
    return this;
  }

  @Override
  public JexConfig multipartFileThreshold(int multipartFileThreshold) {
    this.multipartFileThreshold = multipartFileThreshold;
    return this;
  }

  @Override
  public JexConfig renderer(String extension, TemplateRender renderer) {
    renderers.put(extension, renderer);
    return this;
  }

  @Override
  public int port() {
    return port;
  }

  @Override
  public String host() {
    return host;
  }

  @Override
  public String contextPath() {
    return contextPath;
  }

  @Override
  public boolean health() {
    return health;
  }

  @Override
  public boolean ignoreTrailingSlashes() {
    return ignoreTrailingSlashes;
  }

  @Override
  public boolean preCompressStaticFiles() {
    return preCompressStaticFiles;
  }

  @Override
  public JsonService jsonService() {
    return jsonService;
  }

  @Override
  public AccessManager accessManager() {
    return accessManager;
  }

  @Override
  public UploadConfig multipartConfig() {
    return multipartConfig;
  }

  @Override
  public int multipartFileThreshold() {
    return multipartFileThreshold;
  }

  @Override
  public Map<String, TemplateRender> renderers() {
    return renderers;
  }

}
