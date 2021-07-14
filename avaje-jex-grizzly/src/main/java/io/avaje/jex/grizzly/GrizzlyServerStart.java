package io.avaje.jex.grizzly;

import io.avaje.jex.Jex;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.spi.SpiServiceManager;
import io.avaje.jex.spi.SpiStartServer;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

public class GrizzlyServerStart implements SpiStartServer {

  private static final Logger log = LoggerFactory.getLogger(GrizzlyJexServer.class);

  @Override
  public Jex.Server start(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager) {

    final ServiceManager manager = new ServiceManager(serviceManager, "http", "");
    RouteHandler handler = new RouteHandler(routes, manager);

    final int port = jex.inner.port;
    final HttpServer httpServer = new HttpServerBuilder()
      //.addHandler(clStaticHttpHandler, "cl")
      //.addHandler(staticHttpHandler, "static")
      .handler(handler)
      .setPort(port)
      .build();

    try {
      log.debug("starting server on port {}", port);
      httpServer.start();
      log.info("server started on port {}", port);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    return new GrizzlyJexServer(httpServer, jex.lifecycle());
  }
}
