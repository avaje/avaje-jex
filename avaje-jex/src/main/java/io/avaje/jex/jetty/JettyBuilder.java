package io.avaje.jex.jetty;

import io.avaje.jex.Jex;
import io.avaje.jex.jetty.threadpool.VirtualThreadPool;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build the Jetty Server.
 */
class JettyBuilder {

  private static final Logger log = LoggerFactory.getLogger(JettyBuilder.class);

  private final Jex.Inner inner;
  private final Jex.Jetty config;

  JettyBuilder(Jex jex) {
    this.inner = jex.inner;
    this.config = jex.jetty;
  }

  Server build() {
    Server jetty = new Server(pool());
    ServerConnector connector = new ServerConnector(jetty);
    connector.setPort(inner.port);
    jetty.setConnectors(new Connector[]{connector});
    return jetty;
  }

  private ThreadPool pool() {
    if (config.virtualThreads) {
      return new VirtualThreadPool();
    } else {
      return config.maxThreads == 0 ? new QueuedThreadPool() : new QueuedThreadPool(config.maxThreads);
    }
  }

}
