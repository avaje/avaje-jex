package io.avaje.jex.jetty;

import io.avaje.jex.JexConfig;
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

  private final JexConfig jexConfig;
  private final JettyServerConfig jettyConfig;

  JettyBuilder(Jex jex, JettyServerConfig jettyConfig) {
    this.jexConfig = jex.config();
    this.jettyConfig = jettyConfig;
  }

  Server build() {
    Server jetty = new Server(pool());
    ServerConnector connector = new ServerConnector(jetty);
    connector.setPort(jexConfig.port());
    if (jexConfig.host() != null ) {
      connector.setHost(jexConfig.host());
    }
    jetty.setConnectors(new Connector[]{connector});
    return jetty;
  }

  private ThreadPool pool() {
    if (jexConfig.virtualThreads()) {
      return virtualThreadBasePool();
    } else {
      return jettyConfig.maxThreads() == 0 ? new QueuedThreadPool() : new QueuedThreadPool(jettyConfig.maxThreads());
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
