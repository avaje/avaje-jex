package io.avaje.jex;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;

import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

/** Jex configuration. */
public sealed interface JexConfig permits DJexConfig {

  /** Set the port to use. Defaults to 7001. */
  JexConfig port(int port);

  /** Set the host to bind to. */
  JexConfig host(String host);

  /** Set the contextPath. */
  JexConfig contextPath(String contextPath);

  /** Set to true to include the health endpoint. Defaults to true. */
  JexConfig health(boolean health);

  /** Set to true to ignore trailing slashes. Defaults to true. */
  JexConfig ignoreTrailingSlashes(boolean ignoreTrailingSlashes);

  /** Set the JsonService to use. */
  JexConfig jsonService(JsonService jsonService);

  /**
   * Register a template renderer explicitly.
   *
   * @param extension The extension the renderer applies to.
   * @param renderer The template render to use for the given extension.
   */
  JexConfig renderer(String extension, TemplateRender renderer);

  /** Set executor for serving requests. */
  JexConfig executor(Executor executor);

  /**
   * Executor for serving requests. Defaults to a {@link
   * Executors#newVirtualThreadPerTaskExecutor()}
   */
  Executor executor();

  /** Return the port to use. */
  int port();

  /** Return the host to bind to. */
  String host();

  /** Return the contextPath to use. */
  String contextPath();

  /** Return true to include the health endpoint. */
  boolean health();

  /** Return true to ignore trailing slashes. */
  boolean ignoreTrailingSlashes();

  /** Return the JsonService. */
  JsonService jsonService();

  /** Return the {@link HttpsConfigurator} if https is enabled. */
  HttpsConfigurator httpsConfig();

  /** Enable https with the provided {@link HttpsConfigurator} */
  JexConfig httpsConfig(HttpsConfigurator https);

  /** Return the template renderers registered by extension. */
  Map<String, TemplateRender> renderers();

  /** configure compression via consumer */
  JexConfig compression(Consumer<CompressionConfig> consumer);

  /** get compression configuration */
  CompressionConfig compression();

  /** whether to disable JexPlugins loaded from ServiceLoader */
  JexConfig disableSpiPlugins();
}
