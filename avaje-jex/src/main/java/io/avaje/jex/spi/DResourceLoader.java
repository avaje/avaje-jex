package io.avaje.jex.spi;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

final class DResourceLoader implements ClassResourceLoader {

  private final Class<?> clazz;

  DResourceLoader(Class<?> clazz) {
    this.clazz = clazz;
  }

  @Override
  public URL loadResource(String resourcePath) {
    // cp resources always have to have a leading `/`
    var path = Objects.requireNonNull(resourcePath).transform(this::prependSlash);
    var url = clazz.getResource(path);
    if (url == null) {
      // search the module path for top level resource
      url =
          Optional.ofNullable(ClassLoader.getSystemResource(path))
              .orElseGet(() -> Thread.currentThread().getContextClassLoader().getResource(path));
    }
    return Objects.requireNonNull(url, "Unable to locate resource: " + path);
  }

  private String prependSlash(String s) {
    return s.charAt(0) == '/' ? s : "/" + s;
  }
}
