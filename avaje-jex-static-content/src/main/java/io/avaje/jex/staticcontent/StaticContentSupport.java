package io.avaje.jex.staticcontent;

import java.net.URLConnection;
import java.util.function.Predicate;

import io.avaje.jex.Context;
import io.avaje.jex.Routing.HttpService;

/** Builder for a static resource exchange handler. */
public sealed interface StaticContentSupport extends HttpService
    permits StaticResourceHandlerBuilder {

  /** Create and return a new static content configuration. */
  static StaticContentSupport createCP() {
    return StaticResourceHandlerBuilder.builder();
  }

  /** Create and return a new static content configuration for a File. */
  static StaticContentSupport createFile() {
    return StaticResourceHandlerBuilder.builder().file();
  }

  /**
   * Sets the HTTP path for the static resource handler.
   *
   * @param path the HTTP path prefix
   * @return the updated configuration
   */
  StaticContentSupport httpPath(String path);

  /**
   * Sets the file to serve, or the folder your files are located in. (default: "/public/")
   *
   * @param resource the root directory
   * @return the updated configuration
   */
  StaticContentSupport resource(String resource);

  /**
   * Sets the index file to be served when a directory is requests.
   *
   * @param directoryIndex the index file
   * @return the updated configuration
   */
  StaticContentSupport directoryIndex(String directoryIndex);

  /**
   * Sets a custom resource loader for loading class/module path resources. This is normally used
   * when running the application on the module path when files cannot be discovered.
   *
   * <p>Example usage: {@code config.resourceLoader(ClassResourceLoader.create(getClass())) }
   *
   * @param resourceLoader the custom resource loader
   * @return the updated configuration
   */
  StaticContentSupport resourceLoader(ClassResourceLoader resourceLoader);

  /**
   * Adds a new MIME type mapping to the configuration. (Default: uses {@link
   * URLConnection#getFileNameMap()}
   *
   * @param ext the file extension (e.g., "html", "css", "js")
   * @param mimeType the corresponding MIME type (e.g., "text/html", "text/css",
   *     "application/javascript")
   * @return the updated configuration
   */
  StaticContentSupport putMimeTypeMapping(String ext, String mimeType);

  /**
   * Adds a new response header to the configuration.
   *
   * @param key the header name
   * @param value the header value
   * @return the updated configuration
   */
  StaticContentSupport putResponseHeader(String key, String value);

  /**
   * Sets a predicate to filter files based on the request context.
   *
   * @param skipFilePredicate the predicate to use
   * @return the updated configuration
   */
  StaticContentSupport skipFilePredicate(Predicate<Context> skipFilePredicate);
}
