package io.avaje.jex.core;

import java.net.URL;
import java.util.Objects;

import io.avaje.jex.spi.StaticResourceLoader;

/** Default StaticResourceLoader. */
final class DefaultResourceLoader implements StaticResourceLoader {

  @Override
  public URL getResourceURI(String resourcePath) {
    var url = getClass().getResource(resourcePath);
    if (url == null) {
      // search the module path for top level resource
      url = ClassLoader.getSystemResource(resourcePath);
    }
    return Objects.requireNonNull(url, "Unable to locate resource: " + resourcePath);
  }
}
