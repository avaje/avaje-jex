package io.avaje.jex.jdk;

import io.avaje.jex.spi.ProxyServiceManager;
import io.avaje.jex.spi.SpiServiceManager;

import java.io.OutputStream;

class ServiceManager extends ProxyServiceManager {

  private final long outputBufferMax = 1024;
  private final int outputBufferInitial = 256;

  ServiceManager(SpiServiceManager delegate) {
    super(delegate);
  }

  OutputStream createOutputStream(JdkContext jdkContext) {
    return new BufferedOutStream(jdkContext, outputBufferMax, outputBufferInitial);
  }

}
