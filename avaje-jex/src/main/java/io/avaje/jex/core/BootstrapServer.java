package io.avaje.jex.core;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.sun.net.httpserver.HttpServer;

import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import io.avaje.jex.JexConfig;
import io.avaje.jex.routes.RoutesBuilder;
import io.avaje.jex.routes.SpiRoutes;

public final class BootstrapServer {

  private BootstrapServer() {}

  private static final System.Logger log = System.getLogger("io.avaje.jex");

  public static Jex.Server start(Jex jex) {
    final var config = jex.config();
    if (config.health()) {
      jex.plugin(new HealthPlugin());
    }

    CoreServiceLoader.plugins().forEach(p -> p.apply(jex));

    final SpiRoutes routes = new RoutesBuilder(jex.routing(), config).build();

    return start(jex, routes);
  }

  static Jex.Server start(Jex jex, SpiRoutes routes) {
    try {
      final var config = jex.config();
      final var socketAddress = createSocketAddress(config);
      final var https = config.httpsConfig();
      final var provider = config.serverProvider();
      final HttpServer server;
      if (https != null) {
        var httpsServer = provider.createHttpsServer(socketAddress, config.socketBacklog());
        httpsServer.setHttpsConfigurator(https);
        server = httpsServer;
      } else {
        server = provider.createHttpServer(socketAddress, config.socketBacklog());
      }

      final var scheme = config.scheme();
      final var contextPath = config.contextPath();
      ServiceManager serviceManager = ServiceManager.create(jex);
      final var handler = new RoutingHandler(routes, serviceManager);

      final var serverClass = server.getClass();

      // jetty's server does not support setExecutor with virtual threads (VT)
      // as it has it's own impl that will auto-use VTs
      if (!serverClass.getName().contains("jetty")) {
        server.setExecutor(config.executor());
      }

      server.createContext(contextPath, handler);
      server.start();
      var actualAddress = server.getAddress();
      jex.lifecycle().status(AppLifecycle.Status.STARTED);
      log.log(
          INFO,
          "Avaje Jex {0} started {1} on {2}://{3}:{4,number,#}",
          BootstrapServer.class.getPackage().getImplementationVersion(),
          serverClass,
          scheme,
          actualAddress.getHostName(),
          actualAddress.getPort());
      log.log(DEBUG, routes);
      return new JdkJexServer(server, jex.lifecycle(), handler);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static InetSocketAddress createSocketAddress(JexConfig config)
      throws UnknownHostException {
    final var inetAddress = config.host() == null ? null : InetAddress.getByName(config.host());
    return new InetSocketAddress(inetAddress, config.port());
  }
}
