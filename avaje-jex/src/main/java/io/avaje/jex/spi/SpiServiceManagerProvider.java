package io.avaje.jex.spi;

import io.avaje.jex.Jex;

public interface SpiServiceManagerProvider {

  SpiServiceManager create(Jex jex);
}
