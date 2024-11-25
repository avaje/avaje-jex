package io.avaje.jex;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

final class DefaultResourceLoader implements ClassResourceLoader {

  private final Class<?> clazz;

  DefaultResourceLoader(Class<?> clazz) {

    this.clazz = clazz;
  }

  @Override
  public URL getResource(String resourcePath) {

    var url = clazz.getResource(resourcePath);
    if (url == null) {
      // search the module path for top level resource
      url =
          Optional.ofNullable(ClassLoader.getSystemResource(resourcePath))
              .orElseGet(
                  () -> Thread.currentThread().getContextClassLoader().getResource(resourcePath));
    }
    return Objects.requireNonNull(url, "Unable to locate resource: " + resourcePath);
  }

  @Override
  public InputStream getResourceAsStream(String resourcePath) {

    var url = clazz.getResourceAsStream(resourcePath);
    if (url == null) {
      // search the module path for top level resource
      url =
          Optional.ofNullable(ClassLoader.getSystemResourceAsStream(resourcePath))
              .orElseGet(
                  () ->
                      Thread.currentThread()
                          .getContextClassLoader()
                          .getResourceAsStream(resourcePath));
    }
    return Objects.requireNonNull(url, "Unable to locate resource: " + resourcePath);
  }
}
