package io.avaje.jex.jetty;

import io.avaje.jex.Jex;
import io.avaje.jex.StaticFileSource;
import org.eclipse.jetty.server.Server;

import java.util.List;

class StaticHandlerFactory {

  StaticHandler build(Server server, Jex jex, List<StaticFileSource> sourceList) {
    StaticHandler handler = new StaticHandler(jex.config.preCompressStaticFiles, server);
    for (StaticFileSource source : sourceList) {
      handler.addStaticFileConfig(source);
    }
    return handler;
  }
}
