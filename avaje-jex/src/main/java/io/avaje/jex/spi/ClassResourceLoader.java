package io.avaje.jex.spi;

import java.net.URL;

/**
 * Resource loader that works on the classpath or module path.
 *
 * <p>When not specified Avaje Jex provides a default implementation that looks to find resources
 * using the class loader associated with the ClassResourceLoader.
 *
 * <p>As a fallback, {@link ClassLoader#getSystemResourceAsStream(String)} is used if the loader
 * returns null.
 */
public interface ClassResourceLoader {

  /**
   * Create a {@code ClassResourceLoader} instance based on a given Class.
   *
   * @param clazz The class to use for resource loading.
   * @return A new {@code ClassResourceLoader} instance.
   */
  static ClassResourceLoader fromClass(Class<?> clazz) {
    return new DResourceLoader(clazz);
  }

  /**
   * Loads the specified resource and returns its URL.
   *
   * @param resourcePath The path to the resource.
   * @return The URL of the resource.
   */
  URL loadResource(String resourcePath);
}
