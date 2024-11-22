package io.avaje.jex.spi;

import io.avaje.jex.Jex;

public interface SpiServiceManagerProvider extends JexExtension {

  SpiServiceManager create(Jex jex);
}
