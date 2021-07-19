package io.avaje.jex.jetty;

import io.avaje.jex.Jex;
import io.avaje.jex.StaticFileSource;

import java.util.List;

class StaticHandlerFactory {

  StaticHandler build(Jex jex, List<StaticFileSource> sourceList) {

    StaticHandler handler = new StaticHandler(jex.config.preCompressStaticFiles);
    for (StaticFileSource source : sourceList) {
      handler.addStaticFileConfig(source);
    }
    return handler;
  }
}
