package io.avaje.jex.jetty;

import io.avaje.jex.Jex;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Build the Jetty Server.
 */
class JettyBuilder {

  private static final Logger log = LoggerFactory.getLogger(JettyBuilder.class);

  private final Jex.Inner inner;
  private final JettyServerConfig config;

  JettyBuilder(Jex jex, JettyServerConfig config) {
    this.inner = jex.inner;
    this.config = config;
  }

  Server build() {
    Server jetty = new Server(pool());
    ServerConnector connector = new ServerConnector(jetty);
    connector.setPort(inner.port);
    if (inner.host != null ) {
      connector.setHost(inner.host);
    }
    jetty.setConnectors(new Connector[]{connector});
    return jetty;
  }

  private ThreadPool pool() {
    if (config.virtualThreads()) {
      return virtualThreadBasePool();
    } else {
      return config.maxThreads() == 0 ? new QueuedThreadPool() : new QueuedThreadPool(config.maxThreads());
    }
  }

  private ThreadPool virtualThreadBasePool() {
    try {
      final Class<?> aClass = Class.forName("io.avaje.jex.jetty.threadpool.VirtualThreadPool");
      final Constructor<?> constructor = aClass.getConstructor();
      return (ThreadPool) constructor.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to start Loom threadPool", e);
    }
  }

}
