package io.avaje.jex.jdk;

import io.avaje.jex.spi.ProxyServiceManager;
import io.avaje.jex.spi.SpiServiceManager;

import java.io.OutputStream;

final class ServiceManager extends ProxyServiceManager {

  private final String scheme;
  private final String contextPath;

  ServiceManager(SpiServiceManager delegate, String scheme, String contextPath) {
    super(delegate);
    this.scheme = scheme;
    this.contextPath = contextPath;
  }

  OutputStream createOutputStream(JdkContext jdkContext) {
    return new BufferedOutStream(jdkContext);
  }

  String scheme() {
    return scheme;
  }

  public String url() {
    return scheme + "://";
  }

  public String contextPath() {
    return contextPath;
  }
}
