package io.avaje.jex.jdk;

import io.avaje.jex.spi.ProxyServiceManager;
import io.avaje.jex.spi.SpiServiceManager;

import java.io.OutputStream;

class ServiceManager extends ProxyServiceManager {

  private final String scheme;
  private final String contextPath;
  private static final long outputBufferMax = Long.getLong("jex.outputBuffer.max", 1024);
  private static final int outputBufferInitial =
      Integer.getInteger("jex.outputBuffer.initial", 256);

  ServiceManager(SpiServiceManager delegate, String scheme, String contextPath) {
    super(delegate);
    this.scheme = scheme;
    this.contextPath = contextPath;
  }

  OutputStream createOutputStream(JdkContext jdkContext) {
    return new BufferedOutStream(jdkContext, outputBufferMax, outputBufferInitial);
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
