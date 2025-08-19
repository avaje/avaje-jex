package io.avaje.jex.staticcontent;

import java.net.URLConnection;
import java.util.function.Predicate;

import io.avaje.jex.http.Context;
import io.avaje.jex.security.Role;
import io.avaje.jex.spi.ClassResourceLoader;
import io.avaje.jex.spi.JexPlugin;

/**
 * Static content resource handler.
 * <pre>{@code
 *
 *  var staticContent = StaticContent.createFile("src/test/resources/public")
 *     .directoryIndex("index.html")
 *     .preCompress()
 *     .build()
 *
 *  Jex.create()
 *    .plugin(staticContent)
 *    .port(8080)
 *    .start();
 *
 * }</pre>
 */
public sealed interface StaticContent extends JexPlugin
  permits StaticResourceHandlerBuilder {

  /**
   * Create and return a new static content class path configuration.
   *
   * @param resourceRoot The file to serve, or the directory the files are located in.
   */
  static Builder ofClassPath(String resourceRoot) {
    return StaticResourceHandlerBuilder.builder(resourceRoot);
  }

  /**
   * Create and return a new static content class path configuration with the
   * `/public` directory as the root.
   */
  static Builder ofClassPath() {
    return StaticResourceHandlerBuilder.builder("/public/");
  }

  /**
   * Create and return a new static content configuration for a File.
   *
   * @param resourceRoot The path of the file to serve, or the directory the files are located in.
   */
  static Builder ofFile(String resourceRoot) {
    return StaticResourceHandlerBuilder.builder(resourceRoot).file();
  }

  /**
   * Builder for StaticContent.
   */
  sealed interface Builder
    permits StaticResourceHandlerBuilder {

    /**
     * Sets the HTTP route for the static resource handler.
     *
     * @param path the HTTP path prefix
     * @param roles the security roles for the route
     * @return the updated configuration
     */
    Builder route(String path, Role... roles);

    /**
     * Sets the index file to be served when a directory is requested.
     *
     * @param directoryIndex the index file
     * @return the updated configuration
     */
    Builder directoryIndex(String directoryIndex);

    /**
     * Sent resources will be pre-compressed and cached in memory when this is enabled
     *
     * @return the updated configuration
     */
    Builder preCompress();

    /**
     * Sets a custom resource loader for loading class/module path resources. This is normally used
     * when running the application on the module path when files cannot be discovered.
     *
     * <p>Example usage: {@code service.resourceLoader(ClassResourceLoader.create(getClass())) }
     *
     * @param resourceLoader the custom resource loader
     * @return the updated configuration
     */
    Builder resourceLoader(ClassResourceLoader resourceLoader);

    /**
     * Sets a custom resource loader for loading class/module path resources using the given class.
     * This is normally used when running the application on the module path when files cannot be
     * discovered.
     *
     * @param clazz the class used to custom load resources
     * @return the updated configuration
     */
    default Builder resourceLoader(Class<?> clazz) {
      return resourceLoader(ClassResourceLoader.fromClass(clazz));
    }

    /**
     * Adds a new MIME type mapping to the configuration. (Default: uses {@link
     * URLConnection#getFileNameMap()}
     *
     * @param ext the file extension (e.g., "html", "css", "js")
     * @param mimeType the corresponding MIME type (e.g., "text/html", "text/css",
     *     "application/javascript")
     * @return the updated configuration
     */
    Builder putMimeTypeMapping(String ext, String mimeType);

    /**
     * Adds a new response header to the configuration.
     *
     * @param key   the header name
     * @param value the header value
     * @return the updated configuration
     */
    Builder putResponseHeader(String key, String value);

    /**
     * Sets a predicate to filter files based on the request context.
     *
     * @param skipFilePredicate the predicate to use
     * @return the updated configuration
     */
    Builder skipFilePredicate(Predicate<Context> skipFilePredicate);

    /**
     * Build and return the StaticContent.
     */
    StaticContent build();
  }
}
