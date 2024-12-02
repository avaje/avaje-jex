package io.avaje.jex;

import java.util.Map;
import java.util.concurrent.Executor;
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

  /**
   * Set the host on which the HttpServer will bind to. Defaults to any local address.
   *
   * @param host The host.
   */
  JexConfig host(String host);

  /**
   * Sets the port number on which the HttpServer will listen for incoming requests.
   *
   * @param port The port number.
   */
  JexConfig port(int port);

  /**
   * Set the contextPath.
   *
   * @param contextPath The context path which defaults to "/".
   */
  JexConfig contextPath(String contextPath);

  /**
   * Set the socket backlog. If this value is less than or equal to zero, then a system default
   * value is used
   *
   * @param backlog the socket backlog. If this value is less than or equal to zero, then a system
   *     default value is used
   */
  JexConfig socketBacklog(int backlog);

  /**
   * Enables/Disables the default health endpoint.
   *
   * @param health whether to enable/disable.
   */
  JexConfig health(boolean health);

  /**
   * Configures whether trailing slashes in request URIs should be ignored.
   *
   * @param ignoreTrailingSlashes whether to enable/disable trailing slashes.
   */
  JexConfig ignoreTrailingSlashes(boolean ignoreTrailingSlashes);

  /**
   * Sets the JSON service used for (de)serialization.
   *
   * @param jsonService The json service instance.
   */
  JexConfig jsonService(JsonService jsonService);

  /**
   * Registers a template renderer for a specific file extension.
   *
   * @param extension The file extension.
   * @param renderer The template renderer implementation.
   */
  JexConfig renderer(String extension, TemplateRender renderer);

  /**
   * Sets the executor service used to handle incoming requests.
   *
   * @param executor The executor service.
   */
  JexConfig executor(Executor executor);

  /**
   * Enable https with the provided {@link HttpsConfigurator}
   *
   * @param https The HTTPS configuration.
   */
  JexConfig httpsConfig(HttpsConfigurator https);

  /**
   * Configures compression settings using a consumer function.
   *
   * @param consumer The consumer function to configure compression settings.
   * @return The updated configuration.
   */
  JexConfig compression(Consumer<CompressionConfig> consumer);

  /** Returns the configured port number. (Defaults to 8080 if not set) */
  int port();

  /** Returns the configured host. (Defaults to localhost if not set) */
  String host();

  /** Return the contextPath. (Defaults to "/") */
  String contextPath();

  /** Returns whether the health endpoint is enabled. */
  boolean health();

  /** Returns whether trailing slashes in request URIs are ignored. */
  boolean ignoreTrailingSlashes();

  /** Returns the configured JSON service. */
  JsonService jsonService();

  /** Return the {@link HttpsConfigurator} if https is enabled. */
  HttpsConfigurator httpsConfig();

  /** Return the schema as http or https. */
  String scheme();

  /** Returns the configured compression settings. */
  CompressionConfig compression();

  /** Returns a map of registered template renderers, keyed by file extension. */
  Map<String, TemplateRender> renderers();

  /**
   * Executor for serving requests. Defaults to a {@link
   * Executors#newVirtualThreadPerTaskExecutor()}
   */
  Executor executor();

  /**
   * Disables auto-configuring the current instance with {@link JexPlugin} loaded using the
   * ServiceLoader.
   */
  JexConfig disableSpiPlugins();

  /** Return the socket backlog. */
  int socketBacklog();

  /** Return true if SPI plugins should be loaded and registered. */
  boolean useSpiPlugins();
}
