package io.avaje.jex.spi;

import io.avaje.jex.Jex;

public interface SpiServer {

  Jex.Server start(Jex jex);

}
