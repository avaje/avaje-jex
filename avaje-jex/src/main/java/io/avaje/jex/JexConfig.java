package io.avaje.jex;

import io.avaje.jex.spi.JsonService;

import java.util.Map;

/**
 * Jex configuration.
 */
public interface JexConfig {

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
   * Set the AccessManager to use.
   */
  JexConfig accessManager(AccessManager accessManager);

  /**
   * Set the upload configuration.
   */
  JexConfig multipartConfig(UploadConfig multipartConfig);

  /**
   * Set the multipartFileThreshold.
   */
  JexConfig multipartFileThreshold(int multipartFileThreshold);

  /**
   * Register a template renderer explicitly.
   *
   * @param extension The extension the renderer applies to.
   * @param renderer  The template render to use for the given extension.
   */
  JexConfig renderer(String extension, TemplateRender renderer);

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

  /**
   * Return the access manager.
   */
  AccessManager accessManager();

  /**
   * Return the multipartConfig.
   */
  UploadConfig multipartConfig();

  /**
   * Return the multipartFileThreshold.
   */
  int multipartFileThreshold();

  /**
   * Return the template renderers registered by extension.
   */
  Map<String, TemplateRender> renderers();

}
