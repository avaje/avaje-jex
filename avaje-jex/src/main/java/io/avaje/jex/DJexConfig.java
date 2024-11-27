package io.avaje.jex;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

final class DJexConfig implements JexConfig {

  private int port = 8080;
  private String host;
  private String contextPath = "/";
  private boolean health = true;
  private boolean ignoreTrailingSlashes = true;
  private Executor executor;
  private JsonService jsonService;
  private final Map<String, TemplateRender> renderers = new HashMap<>();
  private SSLContext sslContext;
  private boolean useJexSpi = true;
  private final CompressionConfig compression = new CompressionConfig();

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
  public JexConfig jsonService(JsonService jsonService) {
    this.jsonService = jsonService;
    return this;
  }

  @Override
  public JexConfig renderer(String extension, TemplateRender renderer) {
    renderers.put(extension, renderer);
    return this;
  }

  @Override
  public Executor executor() {
    if (executor == null) {
      executor =
          Executors.newThreadPerTaskExecutor(
              Thread.ofVirtual().name("avaje-jex-http-", 0).factory());
    }
    return executor;
  }

  @Override
  public JexConfig executor(Executor factory) {
    this.executor = factory;
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
  public JsonService jsonService() {
    return jsonService;
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

  @Override
  public JexConfig compression(Consumer<CompressionConfig> consumer) {
    consumer.accept(compression);
    return this;
  }

  @Override
  public CompressionConfig compression() {
    return compression;
  }

  @Override
  public DJexConfig disableSpiPlugins() {
    useJexSpi = false;
    return this;
  }

  boolean useSpiPlugins() {
    return useJexSpi;
  }
}
