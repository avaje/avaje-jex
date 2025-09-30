package io.avaje.jex.core;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.sun.net.httpserver.HttpServer;

import io.avaje.applog.AppLog;
import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import io.avaje.jex.JexConfig;
import io.avaje.jex.routes.RoutesBuilder;

public final class BootstrapServer {

  private BootstrapServer() {}

  private static final System.Logger log = AppLog.getLogger("io.avaje.jex");

  public static Jex.Server start(Jex jex) {
    long startTime = System.currentTimeMillis();
    final var config = jex.config();
    if (config.health()) {
      jex.plugin(new HealthPlugin());
    }

    CoreServiceLoader.plugins().forEach(p -> p.apply(jex));

    var routing = jex.routing();
    routing.addAll(CoreServiceLoader.spiRoutes());
    final var routes = new RoutesBuilder(routing, config).build();

    try {
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
      var serviceManager = ServiceManager.create(jex);
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
          "Avaje Jex started {0} in {1}ms on {2}://{3}:{4,number,#}",
          serverClass,
          System.currentTimeMillis() - startTime,
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