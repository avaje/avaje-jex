package io.avaje.jex.core;

import com.sun.net.httpserver.HttpServer;
import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import java.lang.System.Logger.Level;

final class JdkJexServer implements Jex.Server {

  private static final System.Logger log = System.getLogger("io.avaje.jex");

  private final HttpServer server;
  private final AppLifecycle lifecycle;
  private final RoutingHandler handler;

  JdkJexServer(HttpServer server, AppLifecycle lifecycle, RoutingHandler handler) {
    this.server = server;
    this.lifecycle = lifecycle;
    this.handler = handler;
    lifecycle.registerShutdownHook(this::shutdown);
  }

  @Override
  public void onShutdown(Runnable onShutdown) {
    lifecycle.onShutdown(onShutdown, Integer.MAX_VALUE);
  }

  @Override
  public void shutdown() {
    log.log(Level.TRACE, "starting shutdown");
    lifecycle.status(AppLifecycle.Status.STOPPING);
    handler.waitForIdle(30);
    server.stop(0);
    log.log(Level.TRACE, "server http listeners stopped");
    lifecycle.status(AppLifecycle.Status.STOPPED);
  }

  @Override
  public int port() {
    return server.getAddress().getPort();
  }
}
