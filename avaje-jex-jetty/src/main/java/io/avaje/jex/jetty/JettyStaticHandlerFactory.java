package io.avaje.jex.jetty;

import io.avaje.jex.Jex;
import io.avaje.jex.StaticFileSource;
import io.avaje.jex.spi.StaticHandler;
import io.avaje.jex.spi.StaticHandlerFactory;

import java.util.List;

public class JettyStaticHandlerFactory implements StaticHandlerFactory {

  @Override
  public StaticHandler build(Jex jex, List<StaticFileSource> sourceList) {

    JettyStaticHandler handler = new JettyStaticHandler(jex.inner.preCompressStaticFiles);
    for (StaticFileSource source : sourceList) {
      handler.addStaticFileConfig(source);
    }
    return handler;
  }
}
