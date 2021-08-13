package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpServer;
import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.spi.SpiServiceManager;
import io.avaje.jex.spi.SpiStartServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public class JdkServerStart implements SpiStartServer {

  private static final Logger log = LoggerFactory.getLogger(JdkServerStart.class);

  @Override
  public Jex.Server start(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager) {
    final ServiceManager manager = new ServiceManager(serviceManager, "http", "");
    BaseHandler handler = new BaseHandler(routes, manager);
    try {
      final HttpServer server = HttpServer.create();
      server.createContext("/", handler);
      final Executor executor = jex.attribute(Executor.class);
      if (executor != null) {
        server.setExecutor(executor);
      }
      int port = jex.config.port;
      server.bind(new InetSocketAddress(port), 0);
      server.start();
      jex.lifecycle().status(AppLifecycle.Status.STARTED);
      log.info("started server on port {}", port);
      return new JdkJexServer(server, jex.lifecycle(), handler);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
