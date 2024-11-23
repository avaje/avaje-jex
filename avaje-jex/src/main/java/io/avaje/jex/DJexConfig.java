package io.avaje.jex;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLContext;

import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

final class DJexConfig implements JexConfig {

  private int port = 8080;
  private String host;
  private String contextPath = "/";
  private boolean health = true;
  private boolean ignoreTrailingSlashes = true;
  private ThreadFactory factory;

  private boolean preCompressStaticFiles;
  private JsonService jsonService;
  private UploadConfig multipartConfig;
  private int multipartFileThreshold = 8 * 1024;
  private final Map<String, TemplateRender> renderers = new HashMap<>();
  private SSLContext sslContext;

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
  public ThreadFactory threadFactory() {

    if (factory == null) {
      factory =
          Thread.ofVirtual().name("avaje-jex-http-", 0).factory();
    }

    return factory;
  }

  @Override
  public JexConfig threadFactory(ThreadFactory factory) {
    this.factory = factory;
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

  @Override
  public SSLContext sslContext() {

    return this.sslContext;
  }

  @Override
  public JexConfig sslContext(SSLContext ssl) {
    this.sslContext = ssl;
    return this;
  }
}
