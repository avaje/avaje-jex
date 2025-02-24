package io.avaje.jex;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.spi.HttpServerProvider;

import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

/**
 * Jex configuration interface.
 *
 * <p>Provides a fluent API for configuring Jex's various settings, including port, host, health
 * endpoint, trailing slash handling, JSON service, template renderers, executor service, HTTPS
 * configuration, compression, and plugin loading.
 */
public final class JexConfig {

  private int port = 8080;
  private String contextPath = "/";
  private String host;
  private int socketBacklog = 0;
  private boolean health = true;
  private boolean ignoreTrailingSlashes = true;
  private Executor executor;
  private JsonService jsonService;
  private final Map<String, TemplateRender> renderers = new HashMap<>();
  private HttpsConfigurator httpsConfig;
  private final CompressionConfig compression = new CompressionConfig();
  private int bufferInitial = 256;
  private long bufferMax = 4096L;
  private HttpServerProvider serverProvider;

  /** Returns the configured compression settings. */
  public CompressionConfig compression() {
    return compression;
  }

  /**
   * Configures compression settings using a consumer function.
   *
   * @param consumer The consumer function to configure compression settings.
   * @return The updated configuration.
   */
  public JexConfig compression(Consumer<CompressionConfig> consumer) {
    consumer.accept(compression);
    return this;
  }

  /** Return the contextPath. (Defaults to "/") */
  public String contextPath() {
    return contextPath;
  }

  /**
   * Set the contextPath passed to the underlying HttpServer. (defaults to "/")
   *
   * @param contextPath The context path
   */
  public JexConfig contextPath(String contextPath) {
    if (!this.contextPath.equals(contextPath)) {
      this.contextPath =
          contextPath
              .transform(s -> s.startsWith("/") ? s : "/" + s)
              .transform(s -> s.endsWith("/") ? s.substring(0, s.lastIndexOf("/")) : s);
    }
    return this;
  }

  /**
   * Executor for serving requests. Defaults to a {@link
   * Executors#newVirtualThreadPerTaskExecutor()}
   */
  public Executor executor() {
    if (executor == null) {
      executor =
          Executors.newThreadPerTaskExecutor(
              Thread.ofVirtual().name("avaje-jex-http-", 0).factory());
    }
    return executor;
  }

  /**
   * Sets the executor service used to handle incoming requests.
   *
   * @param executor The executor service.
   */
  public JexConfig executor(Executor executor) {
    this.executor = executor;
    return this;
  }

  /** Returns whether the health endpoint is enabled. */
  public boolean health() {
    return health;
  }

  /**
   * Enables/Disables the default health endpoint.
   *
   * @param health whether to enable/disable.
   */
  public JexConfig health(boolean health) {
    this.health = health;
    return this;
  }

  /** Returns the configured host. */
  public String host() {
    return host;
  }

  /**
   * Set the host on which the HttpServer will bind to. Defaults to any local address.
   *
   * @param host The host.
   */
  public JexConfig host(String host) {
    this.host = host;
    return this;
  }

  /** Return the {@link HttpsConfigurator} if https is enabled. */
  public HttpsConfigurator httpsConfig() {
    return httpsConfig;
  }

  /**
   * Enable https with the provided {@link HttpsConfigurator}
   *
   * @param https The HTTPS configuration.
   */
  public JexConfig httpsConfig(HttpsConfigurator https) {
    this.httpsConfig = https;
    return this;
  }

  /** Returns whether trailing slashes in request URIs are ignored. */
  public boolean ignoreTrailingSlashes() {
    return ignoreTrailingSlashes;
  }

  /**
   * Configures whether trailing slashes in request URIs should be ignored.
   *
   * @param ignoreTrailingSlashes whether to enable/disable trailing slashes.
   */
  public JexConfig ignoreTrailingSlashes(boolean ignoreTrailingSlashes) {
    this.ignoreTrailingSlashes = ignoreTrailingSlashes;
    return this;
  }

  /** The initial size of the response buffer */
  public int initialStreamBufferSize() {
    return bufferInitial;
  }

  /**
   * Set the initial size of the response stream buffer. If exceeded, the buffer will expand until
   * it reaches the maximum configured size
   *
   * <p>Defaults to 256
   *
   * @param initialSize The initial size of the response buffer
   */
  public JexConfig initialStreamBufferSize(int initialSize) {
    bufferInitial = initialSize;
    return this;
  }

  /** Returns the configured JSON service. */
  public JsonService jsonService() {
    return jsonService;
  }

  /**
   * Sets the JSON service used for (de)serialization.
   *
   * @param jsonService The json service instance.
   */
  public JexConfig jsonService(JsonService jsonService) {
    this.jsonService = jsonService;
    return this;
  }

  /** the maximum size of the response stream buffer. */
  public long maxStreamBufferSize() {
    return bufferMax;
  }

  /**
   * Set the maximum size of the response stream buffer. If the response data exceeds this size, it
   * will be written to the client using chunked transfer encoding. Otherwise, the response will be
   * sent using a Content-Length header with the exact size of the response data.
   *
   * <p>Defaults to 4096
   *
   * @param maxSize The maximum size of the response
   */
  public JexConfig maxStreamBufferSize(long maxSize) {
    bufferMax = maxSize;
    return this;
  }

  /** Returns the configured port number. (Defaults to 8080 if not set) */
  public int port() {
    return port;
  }

  /**
   * Sets the port number on which the HttpServer will listen for incoming requests. *
   *
   * <p>The default value is 8080. If The port is set to 0, the server will randomly choose an
   * available port.
   *
   * @param port The port number.
   */
  public JexConfig port(int port) {
    this.port = port;
    return this;
  }

  /**
   * Registers a template renderer for a specific file extension.
   *
   * @param extension The file extension.
   * @param renderer The template renderer implementation.
   */
  public JexConfig renderer(String extension, TemplateRender renderer) {
    renderers.put(extension, renderer);
    return this;
  }

  /** Returns a map of registered template renderers, keyed by file extension. */
  public Map<String, TemplateRender> renderers() {
    return renderers;
  }

  /** Return the schema as http or https. */
  public String scheme() {
    return httpsConfig == null ? "http" : "https";
  }

  /**
   * Provide the provider used to create the {@link HttpServer} instance. If not set, {@link
   * HttpServerProvider#provider()} will be used to create the server
   */
  public HttpServerProvider serverProvider() {
    return this.serverProvider != null ? serverProvider : HttpServerProvider.provider();
  }

  /**
   * Configure Provider used to created {@link HttpServer} instances. If not set, {@link
   * HttpServerProvider#provider()} will be used to create the server.
   *
   * @param serverProvider provider used to create the server
   */
  public JexConfig serverProvider(HttpServerProvider serverProvider) {
    this.serverProvider = serverProvider;
    return this;
  }

  /** Return the socket backlog. */
  public int socketBacklog() {
    return socketBacklog;
  }

  /**
   * Set the socket backlog. If this value is less than or equal to zero, then a system default
   * value is used
   *
   * @param backlog the socket backlog. If this value is less than or equal to zero, then a system
   *     default value is used
   */
  public JexConfig socketBacklog(int backlog) {
    this.socketBacklog = backlog;
    return this;
  }
}
