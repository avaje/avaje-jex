package io.avaje.jex;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLContext;

import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

/**
 * Jex configuration.
 */
public sealed interface JexConfig permits DJexConfig {

  /**
   * Set the port to use. Defaults to 7001.
   */
  JexConfig port(int port);

  /**
   * Set the host to bind to.
   */
  JexConfig host(String host);

  /**
   * Set the contextPath.
   */
  JexConfig contextPath(String contextPath);

  /**
   * Set to true to include the health endpoint. Defaults to true.
   */
  JexConfig health(boolean health);

  /**
   * Set to true to ignore trailing slashes. Defaults to true.
   */
  JexConfig ignoreTrailingSlashes(boolean ignoreTrailingSlashes);

  /**
   * Set to true to pre compress static files. Defaults to false.
   */
  JexConfig preCompressStaticFiles(boolean preCompressStaticFiles);

  /**
   * Set the JsonService to use.
   */
  JexConfig jsonService(JsonService jsonService);

  /**
   * Register a template renderer explicitly.
   *
   * @param extension The extension the renderer applies to.
   * @param renderer  The template render to use for the given extension.
   */
  JexConfig renderer(String extension, TemplateRender renderer);

  /**
   * ThreadFactory for serving requests. Defaults to a {@link Thread#ofVirtual()} factory
   */
  JexConfig threadFactory(ThreadFactory executor);

  /**
   * Executor for serving requests. Defaults to {@link Executors#newVirtualThreadPerTaskExecutor()}
   */
  ThreadFactory threadFactory();

  /**
   * Return the port to use.
   */
  int port();

  /**
   * Return the host to bind to.
   */
  String host();

  /**
   * Return the contextPath to use.
   */
  String contextPath();

  /**
   * Return true to include the health endpoint.
   */
  boolean health();

  /**
   * Return true to ignore trailing slashes.
   */
  boolean ignoreTrailingSlashes();

  /**
   * Return true if static files should be pre compressed.
   */
  boolean preCompressStaticFiles();

  /**
   * Return the JsonService.
   */
  JsonService jsonService();

  /** Return the ssl context if https is enabled. */
  SSLContext sslContext();

  /** Enable https with the provided SSLContext. */
  JexConfig sslContext(SSLContext ssl);

  /**
   * Return the template renderers registered by extension.
   */
  Map<String, TemplateRender> renderers();

}
