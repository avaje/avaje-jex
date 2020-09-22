package io.avaje.jex.core;

import io.avaje.jex.Jex;
import io.avaje.jex.JexConfig;
import io.avaje.jex.JettyConfig;
import io.avaje.jex.routes.RoutesBuilder;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.spi.JsonService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.slf4j.LoggerFactory;

public class JettyLaunch implements Jex.Server {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(JettyLaunch.class);

  private final Jex jex;
  private final JexConfig config;
  private final JettyConfig jetty;
  private final Logger defaultLogger;

  private org.eclipse.jetty.server.Server server;

  public JettyLaunch(Jex jex) {
    this.jex = jex;
    this.config = jex.config();
    this.jetty = config.getJetty();
    this.defaultLogger = Log.getLog();
  }

  @Override
  public void shutdown() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Jex.Server start() {
    final SpiRoutes routes = new RoutesBuilder(jex.routing(), jex.config()).build();
    try {
      disableJettyLog();
      server = createServer(routes);
      server.start();
      logOnStart(server);
      enableJettyLog();
      return this;
    } catch (Exception e) {
      throw new IllegalStateException("Error starting server", e);
    }
  }

  private ServiceManager serviceManager() {
    return new ServiceManager(initJsonService(), jex.errorHandling());
  }

  private JsonService initJsonService() {
    final JsonService jsonService = config.getJsonService();
    if (jsonService != null) {
      return jsonService;
    }
    return detectJackson() ? defaultJacksonService() : null;
  }

  private JsonService defaultJacksonService() {
    return new JacksonJsonService();
  }

  private boolean detectJackson() {
    try {
      Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private org.eclipse.jetty.server.Server createServer(SpiRoutes routes) {
    org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(config.getPort());
    server.setHandler(createContextHandler(routes));
    server.setStopAtShutdown(true);
    return server;
  }

  private ServletContextHandler createContextHandler(SpiRoutes routes) {
    ServletContextHandler sc = new ServletContextHandler(null, config.getContextPath(), jetty.isSessions(), jetty.isSecurity());
    //SessionHandler sh = new SessionHandler();
    //sc.setSessionHandler();
    sc.addServlet(new ServletHolder(new JexHttpServlet(config, routes, serviceManager())), "/*");
    return sc;
  }

  private void logOnStart(org.eclipse.jetty.server.Server server) {
    for (Connector c : server.getConnectors()) {
      if (c instanceof ServerConnector) {
        ServerConnector sc = (ServerConnector) c;
        String host = (sc.getHost() == null) ? "localhost" : sc.getHost();
        log.info("Listening with {} host:{} port:{}", sc.getProtocols(), host, sc.getLocalPort());
      } else {
        log.info("bind to {}", c);
      }
    }
  }

  private void enableJettyLog() {
    Log.setLog(defaultLogger);
  }

  private void disableJettyLog() {
    Log.setLog(new JettyNoopLogger());
  }
}
