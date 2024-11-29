package io.avaje.jex.jdk;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import io.avaje.applog.AppLog;
import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import io.avaje.jex.core.SpiServiceManager;
import io.avaje.jex.routes.SpiRoutes;

public final class JdkServerStart {

  private static final System.Logger log = AppLog.getLogger("io.avaje.jex");

  public Jex.Server start(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager) {
    try {
      final var config = jex.config();
      final var port = new InetSocketAddress(config.port());
      final var https = config.httpsConfig();
      final var backlog = config.socketBacklog();

      final HttpServer server;
      final String scheme;
      if (https != null) {
        var httpsServer = HttpsServer.create(port, backlog);
        httpsServer.setHttpsConfigurator(https);
        server = httpsServer;
        scheme = "https";
      } else {
        scheme = "http";
        server = HttpServer.create(port, backlog);
      }

      final var manager = new CtxServiceManager(serviceManager, scheme, "");

      final var handler = new RoutingHandler(routes, manager, config.compression());

      server.setExecutor(config.executor());
      server.createContext("/", handler);
      server.start();

      jex.lifecycle().status(AppLifecycle.Status.STARTED);
      log.log(
          Level.INFO,
          "started com.sun.net.httpserver.HttpServer on port %s://%s".formatted(scheme, port));
      return new JdkJexServer(server, jex.lifecycle(), handler);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
