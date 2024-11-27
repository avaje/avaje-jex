package io.avaje.jex;

import java.net.URLConnection;
import java.util.function.Predicate;

/** Builder for a static resource exchange handler. */
public sealed interface StaticContentConfig permits StaticResourceHandlerBuilder {

  static StaticContentConfig create() {
    return StaticResourceHandlerBuilder.builder();
  }

  /** Return a new ExchangeHandler that will serve the resources */
  ExchangeHandler createHandler();

  /**
   * Sets the HTTP path for the static resource handler.
   *
   * @param path the HTTP path prefix
   * @return the updated configuration
   */
  StaticContentConfig httpPath(String path);

  /**
   * Gets the current HTTP path.
   *
   * @return the current HTTP path
   */
  String httpPath();

  /**
   * Sets the file to serve, or the folder your files are located in. (default: "/public/")
   *
   * @param root the root directory
   * @return the updated configuration
   */
  StaticContentConfig resource(String resource);

  /**
   * Sets the index file to be served when a directory is requests.
   *
   * @param directoryIndex the index file
   * @return the updated configuration
   */
  StaticContentConfig directoryIndex(String directoryIndex);

  /**
   * Sets a custom resource loader for loading class/module path resources. This is normally used
   * when running the application on the module path when files cannot be discovered.
   *
   * <p>Example usage: {@code config.resourceLoader(ClassResourceLoader.create(getClass())) }
   *
   * @param resourceLoader the custom resource loader
   * @return the updated configuration
   */
  StaticContentConfig resourceLoader(ClassResourceLoader resourceLoader);

  /**
   * Adds a new MIME type mapping to the configuration. (Default: uses {@link
   * URLConnection#getFileNameMap()}
   *
   * @param ext the file extension (e.g., "html", "css", "js")
   * @param mimeType the corresponding MIME type (e.g., "text/html", "text/css",
   *     "application/javascript")
   * @return the updated configuration
   */
  StaticContentConfig putMimeTypeMapping(String ext, String mimeType);

  /**
   * Adds a new response header to the configuration.
   *
   * @param key the header name
   * @param value the header value
   * @return the updated configuration
   */
  StaticContentConfig putResponseHeader(String key, String value);

  /**
   * Sets a predicate to filter files based on the request context.
   *
   * @param skipFilePredicate the predicate to use
   * @return the updated configuration
   */
  StaticContentConfig skipFilePredicate(Predicate<Context> skipFilePredicate);

  /**
   * Sets the resource location (CLASSPATH or FILE).
   *
   * @param location the resource location
   * @return the updated configuration
   */
  StaticContentConfig location(ResourceLocation location);

  /** the resource location */
  public enum ResourceLocation {
    CLASS_PATH,
    FILE
  }
}
