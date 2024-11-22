package io.avaje.jex.jdk;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import io.avaje.applog.AppLog;
import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import io.avaje.jex.routes.SpiRoutes;
import io.avaje.jex.spi.SpiServiceManager;

public class JdkServerStart {

  private static final System.Logger log = AppLog.getLogger("io.avaje.jex");

  public Jex.Server start(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager) {
    final ServiceManager manager = new ServiceManager(serviceManager, "http", "");
    BaseHandler handler = new BaseHandler(routes, manager);
    try {

      int port = jex.config().port();
      final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

      server.createContext("/", handler);
      server.setExecutor(Executors.newThreadPerTaskExecutor(jex.config().threadFactory()));

      server.start();
      jex.lifecycle().status(AppLifecycle.Status.STARTED);
      String jexVersion = Jex.class.getPackage().getImplementationVersion();
      log.log(Level.INFO, "started server on port {0,number,#} version {1}", port, jexVersion);
      return new JdkJexServer(server, jex.lifecycle(), handler);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
