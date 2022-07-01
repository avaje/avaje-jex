package io.avaje.jex.grizzly;

import io.avaje.jex.Jex;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.spi.SpiServiceManager;
import io.avaje.jex.spi.SpiStartServer;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger.Level;

public class GrizzlyServerStart implements SpiStartServer {

  private static final System.Logger log = System.getLogger("io.avaje.jex");

  @Override
  public Jex.Server start(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager) {

    final ServiceManager manager = new ServiceManager(serviceManager, "http", "");
    RouteHandler handler = new RouteHandler(routes, manager);

    final int port = jex.config().port();
    final HttpServer httpServer = new HttpServerBuilder()
      //.addHandler(clStaticHttpHandler, "cl")
      //.addHandler(staticHttpHandler, "static")
      .handler(handler)
      .setPort(port)
      .build();

    try {
      log.log(Level.DEBUG, "starting server on port {0,number,#}", port);
      httpServer.start();
      log.log(Level.INFO, "server started on port {0,number,#}", port);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    return new GrizzlyJexServer(httpServer, jex.lifecycle());
  }
}
