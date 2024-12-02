package io.avaje.jex.core;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import io.avaje.jex.JexConfig;
import io.avaje.jex.routes.RoutesBuilder;
import io.avaje.jex.routes.SpiRoutes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static java.lang.System.Logger.Level.INFO;

public final class BootstrapServer {

  private static final System.Logger log = System.getLogger("io.avaje.jex");

  public static Jex.Server start(Jex jex) {
    final var config = jex.config();
    if (config.health()) {
      jex.plugin(new HealthPlugin());
    }

    if (config.useSpiPlugins()) {
      CoreServiceLoader.plugins().forEach(p -> p.apply(jex));
    }

    final SpiRoutes routes =
      new RoutesBuilder(jex.routing(), config.ignoreTrailingSlashes())
        .build();

    return start(jex, routes);
  }

  static Jex.Server start(Jex jex, SpiRoutes routes) {
    SpiServiceManager serviceManager = CoreServiceManager.create(jex);
    try {
      final var config = jex.config();
      final var socketAddress = createSocketAddress(config);
      final var https = config.httpsConfig();

      final HttpServer server;
      if (https != null) {
        var httpsServer = HttpsServer.create(socketAddress, config.socketBacklog());
        httpsServer.setHttpsConfigurator(https);
        server = httpsServer;
      } else {
        server = HttpServer.create(socketAddress, config.socketBacklog());
      }

      final var scheme = config.scheme();
      final var contextPath = config.contextPath();
      final var manager = new CtxServiceManager(serviceManager, scheme, contextPath);
      final var handler = new RoutingHandler(routes, manager, config.compression());

      server.setExecutor(config.executor());
      server.createContext(contextPath, handler);
      server.start();

      jex.lifecycle().status(AppLifecycle.Status.STARTED);
      log.log(
        INFO,
        "started com.sun.net.httpserver.HttpServer on port {0}://{1}",
        scheme, socketAddress);
      return new JdkJexServer(server, jex.lifecycle(), handler);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static InetSocketAddress createSocketAddress(JexConfig config) throws UnknownHostException {
    final var inetAddress = config.host() == null ? null : InetAddress.getByName(config.host());
    return new InetSocketAddress(inetAddress, config.port());
  }
}
