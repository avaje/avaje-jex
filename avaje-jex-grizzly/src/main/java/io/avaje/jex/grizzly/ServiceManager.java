package io.avaje.jex.grizzly;

import io.avaje.jex.spi.ProxyServiceManager;
import io.avaje.jex.spi.SpiServiceManager;

import java.io.OutputStream;

class ServiceManager extends ProxyServiceManager {

  private final String scheme;
  private final String contextPath;

  ServiceManager(SpiServiceManager delegate, String scheme, String contextPath) {
    super(delegate);
    this.scheme = scheme;
    this.contextPath = contextPath;
  }

  String contextPath() {
    return contextPath;
  }
}
