package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpServer;
import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.spi.SpiServiceManager;
import io.avaje.jex.spi.SpiStartServer;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public class JdkServerStart implements SpiStartServer {

  private static final System.Logger log = System.getLogger("io.avaje.jex");

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
      int port = jex.config().port();
      server.bind(new InetSocketAddress(port), 0);
      server.start();
      jex.lifecycle().status(AppLifecycle.Status.STARTED);
      String jexVersion = Jex.class.getPackage().getImplementationVersion();
      log.log(Level.INFO, "started server on port {0,number,#} version {1}", port, jexVersion);
      return new JdkJexServer(server, jex.lifecycle(), handler);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
