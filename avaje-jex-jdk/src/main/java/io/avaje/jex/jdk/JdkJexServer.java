package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpServer;
import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JdkJexServer implements Jex.Server {

  private static final Logger log = LoggerFactory.getLogger(JdkJexServer.class);

  private final HttpServer server;
  private final AppLifecycle lifecycle;
  private final BaseHandler handler;

  JdkJexServer(HttpServer server, AppLifecycle lifecycle, BaseHandler handler) {
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
    log.trace("starting shutdown");
    lifecycle.status(AppLifecycle.Status.STOPPING);
    handler.waitForIdle(30);
    server.stop(0);
    log.trace("server http listeners stopped");
    lifecycle.status(AppLifecycle.Status.STOPPED);
  }
}
