package io.avaje.jex;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpsConfigurator;

import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.spi.JexPlugin;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

/**
 * Jex configuration interface.
 *
 * <p>Provides a fluent API for configuring Jex's various settings, including port, host, health
 * endpoint, trailing slash handling, JSON service, template renderers, executor service, HTTPS
 * configuration, compression, and plugin loading.
 */
public sealed interface JexConfig permits DJexConfig {

  /** Returns the configured compression settings. */
  CompressionConfig compression();

  /**
   * Configures compression settings using a consumer function.
   *
   * @param consumer The consumer function to configure compression settings.
   * @return The updated configuration.
   */
  JexConfig compression(Consumer<CompressionConfig> consumer);

  /** Return the contextPath. (Defaults to "/") */
  String contextPath();

  /**
   * Set the contextPath passed to the underlying HttpServer. (defaults to "/")
   *
   * @param contextPath The context path
   */
  JexConfig contextPath(String contextPath);

  /**
   * Disables auto-configuring the current instance with {@link JexPlugin} loaded using the
   * ServiceLoader.
   */
  JexConfig disableSpiPlugins();

  /**
   * Executor for serving requests. Defaults to a {@link
   * Executors#newVirtualThreadPerTaskExecutor()}
   */
  Executor executor();

  /**
   * Sets the executor service used to handle incoming requests.
   *
   * @param executor The executor service.
   */
  JexConfig executor(Executor executor);

  /** Returns whether the health endpoint is enabled. */
  boolean health();

  /**
   * Enables/Disables the default health endpoint.
   *
   * @param health whether to enable/disable.
   */
  JexConfig health(boolean health);

  /** Returns the configured host. */
  String host();

  /**
   * Set the host on which the HttpServer will bind to. Defaults to any local address.
   *
   * @param host The host.
   */
  JexConfig host(String host);

  /** Return the {@link HttpsConfigurator} if https is enabled. */
  HttpsConfigurator httpsConfig();

  /**
   * Enable https with the provided {@link HttpsConfigurator}
   *
   * @param https The HTTPS configuration.
   */
  JexConfig httpsConfig(HttpsConfigurator https);

  /** Returns whether trailing slashes in request URIs are ignored. */
  boolean ignoreTrailingSlashes();

  /**
   * Configures whether trailing slashes in request URIs should be ignored.
   *
   * @param ignoreTrailingSlashes whether to enable/disable trailing slashes.
   */
  JexConfig ignoreTrailingSlashes(boolean ignoreTrailingSlashes);

  /** The initial size of the response buffer */
  int initialStreamBufferSize();

  /**
   * Set the initial size of the response stream buffer. If exceeded, the buffer will expand until
   * it reaches the maximum configured size
   *
   * <p>Defaults to 256
   *
   * @param initialSize The initial size of the response buffer
   */
  JexConfig initialStreamBufferSize(int initialSize);

  /** Returns the configured JSON service. */
  JsonService jsonService();

  /**
   * Sets the JSON service used for (de)serialization.
   *
   * @param jsonService The json service instance.
   */
  JexConfig jsonService(JsonService jsonService);

  /** the maximum size of the response stream buffer. */
  long maxStreamBufferSize();

  /**
   * Set the maximum size of the response stream buffer. If the response data exceeds this size,
   * then it will be written to the client using chunked transfer encoding. Otherwise, the response
   * will be sent using a Content-Length header with the exact size of the response data.
   *
   * <p>Defaults to 1024
   *
   * @param maxSize The maximum size of the response
   */
  JexConfig maxStreamBufferSize(long maxSize);

  /** Returns the configured port number. (Defaults to 8080 if not set) */
  int port();

  /**
   * Sets the port number on which the HttpServer will listen for incoming requests.
   *
   * @param port The port number.
   */
  JexConfig port(int port);

  /**
   * Registers a template renderer for a specific file extension.
   *
   * @param extension The file extension.
   * @param renderer The template renderer implementation.
   */
  JexConfig renderer(String extension, TemplateRender renderer);

  /** Returns a map of registered template renderers, keyed by file extension. */
  Map<String, TemplateRender> renderers();

  /** Return the schema as http or https. */
  String scheme();

  /** Return the socket backlog. */
  int socketBacklog();

  /**
   * Set the socket backlog. If this value is less than or equal to zero, then a system default
   * value is used
   *
   * @param backlog the socket backlog. If this value is less than or equal to zero, then a system
   *     default value is used
   */
  JexConfig socketBacklog(int backlog);

  /** Return true if SPI plugins should be loaded and registered. */
  boolean useSpiPlugins();
}
