package io.avaje.jex;

import java.io.InputStream;
import java.net.URL;

/**
 * Loading resources from the classpath or module path.
 *
 * <p>When not specified Avaje Jex provides a default implementation that looks to find resources
 * using the class loader associated with the ClassResourceLoader.
 *
 * <p>As a fallback, {@link ClassLoader#getSystemResourceAsStream(String)} is used if the loader returns null.
 */
public interface ClassResourceLoader {

  static ClassResourceLoader fromClass(Class<?> clazz) {

    return new DefaultResourceLoader(clazz);
  }

  /** Return the URL for the given resource or return null if it cannot be found. */
  URL getResource(String resourcePath);

  InputStream getResourceAsStream(String resourcePath);
}
