package io.avaje.jex.core;

import io.avaje.jex.Jex;
import io.avaje.jex.spi.SpiServiceManager;
import io.avaje.jex.spi.SpiServiceManagerProvider;

public class BootstapServiceManager implements SpiServiceManagerProvider {
  @Override
  public SpiServiceManager create(Jex jex) {
    return CoreServiceManager.create(jex);
  }
}
