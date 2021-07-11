package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.avaje.jex.Jex;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.spi.SpiServiceManager;
import io.avaje.jex.spi.SpiStartServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class JdkServerStart implements SpiStartServer {

  @Override
  public Jex.Server start(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager) {

    final ServiceManager manager = new ServiceManager(serviceManager);
    HttpHandler handler = new BaseHandler(routes, manager);
    try {
      final HttpServer server = HttpServer.create();
      server.createContext("/", handler);

      int port = jex.inner.port;
      server.bind(new InetSocketAddress(port), 0);
      server.start();

      return new JdkJexServer(server);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
