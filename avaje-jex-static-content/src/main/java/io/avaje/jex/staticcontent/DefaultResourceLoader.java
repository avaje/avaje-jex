package io.avaje.jex.staticcontent;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

final class DefaultResourceLoader implements ClassResourceLoader {

  private final Class<?> clazz;

  DefaultResourceLoader() {
    this.clazz = DefaultResourceLoader.class;
  }

  DefaultResourceLoader(Class<?> clazz) {
    this.clazz = clazz;
  }

  @Override
  public URL loadResource(String resourcePath) {
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
}
