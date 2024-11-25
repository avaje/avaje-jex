package io.avaje.jex.spi;

import java.net.URL;

/**
 * Plugin API for loading resources from the classpath or module path.
 *
 * <p>When not specified Avaje Jex provides a default implementation that looks to find resources
 * using the class loader associated with the StaticResourceLoader.
 *
 * <p>Note there is a fallback to use {@link ClassLoader#getSystemResourceAsStream(String)} if the
 * StaticResourceLoader returns null.
 */
public non-sealed interface StaticResourceLoader extends JexExtension {

  /** Return the URI for the given resource or null if it can not be found. */
  URL getResourceURI(String resourcePath);
}
