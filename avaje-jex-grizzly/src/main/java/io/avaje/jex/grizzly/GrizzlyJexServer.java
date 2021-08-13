package io.avaje.jex.grizzly;

import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class GrizzlyJexServer implements Jex.Server {

  private static final Logger log = LoggerFactory.getLogger(GrizzlyJexServer.class);

  private final HttpServer server;
  private final AppLifecycle lifecycle;
  private final ReentrantLock lock = new ReentrantLock();
  private final int maxWaitSeconds = 30;
  private boolean shutdown;

  GrizzlyJexServer(HttpServer server, AppLifecycle lifecycle) {
    this.server = server;
    this.lifecycle = lifecycle;
    lifecycle.registerShutdownHook(this::shutdown);
    lifecycle.status(AppLifecycle.Status.STARTED);
  }

  @Override
  public void onShutdown(Runnable onShutdown) {
    lifecycle.onShutdown(onShutdown, Integer.MAX_VALUE);
  }

  @Override
  public void shutdown() {
    lock.lock();
    try {
      if (shutdown) {
        log.trace("shutdown in progress");
      } else {
        shutdown = true;
        lifecycle.status(AppLifecycle.Status.STOPPING);
        log.debug("initiate shutdown with maxWaitSeconds {}", maxWaitSeconds);
        try {
          server.shutdown(maxWaitSeconds, TimeUnit.SECONDS).get();
        } catch (InterruptedException |ExecutionException e) {
          log.error("Error during server shutdown", e);
        }
        log.trace("server http listeners stopped");
        lifecycle.status(AppLifecycle.Status.STOPPED);
      }
    } finally {
      lock.unlock();
    }
  }
}
