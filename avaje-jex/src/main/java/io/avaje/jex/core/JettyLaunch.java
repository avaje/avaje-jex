package io.avaje.jex.core;

import io.avaje.jex.Jex;
import io.avaje.jex.StaticFileSource;
import io.avaje.jex.routes.RoutesBuilder;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.staticfiles.JettyStaticHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class JettyLaunch implements Jex.Server {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(JettyLaunch.class);

  private final Jex jex;
  private final SpiRoutes routes;
  private final Logger defaultLogger;
  private Server server;

  public JettyLaunch(Jex jex) {
    this.jex = jex;
    this.defaultLogger = Log.getLog();
    this.routes = new RoutesBuilder(jex.routing(), jex).build();
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
    try {
      disableJettyLog();
      server = createServer();
      server.start();
      logOnStart(server);
      enableJettyLog();
      return this;
    } catch (Exception e) {
      throw new IllegalStateException("Error starting server", e);
    }
  }

  protected Server createServer() {
    Server server = initServer();
    server.setHandler(initContextHandler());
    server.setStopAtShutdown(true);
    return server;
  }

  private Server initServer() {
    Server server = jex.jetty.server;
    return server != null ? server : new Server(jex.inner.port);
  }

  protected ServletContextHandler initContextHandler() {
    final ServletContextHandler sc = initServletContextHandler();
    sc.setSessionHandler(initSessionHandler());
    sc.addServlet(initServletHolder(), "/*");
    return sc;
  }

  protected ServletHolder initServletHolder() {
    final ServiceManager manager = serviceManager();
    final StaticHandler staticHandler = initStaticHandler();
    return new ServletHolder(new JexHttpServlet(jex, routes, manager, staticHandler));
  }

  protected ServletContextHandler initServletContextHandler() {
    final ServletContextHandler ch = jex.jetty.contextHandler;
    return ch != null ? ch : new ContextHandler(jex.inner.contextPath, jex.jetty.sessions, jex.jetty.security);
  }

  protected SessionHandler initSessionHandler() {
    SessionHandler sh = jex.jetty.sessionHandler;
    return sh == null ? defaultSessionHandler() : sh;
  }

  protected SessionHandler defaultSessionHandler(){
    SessionHandler sh = new SessionHandler();
    sh.setHttpOnly(true);
    return sh;
  }

  protected StaticHandler initStaticHandler() {
    final List<StaticFileSource> staticFileSources = jex.staticFiles().getSources();
    if (staticFileSources == null || staticFileSources.isEmpty()) {
      return null;
    }
    final JettyStaticHandler handler = new JettyStaticHandler(jex.inner.preCompressStaticFiles);
    for (StaticFileSource fileConfig : staticFileSources) {
      handler.addStaticFileConfig(fileConfig);
    }
    return handler;
  }

  protected ServiceManager serviceManager() {
    return new ServiceManager(initJsonService(), jex.errorHandling());
  }

  protected JsonService initJsonService() {
    final JsonService jsonService = jex.inner.jsonService;
    if (jsonService != null) {
      return jsonService;
    }
    return detectJackson() ? defaultJacksonService() : null;
  }

  protected JsonService defaultJacksonService() {
    return new JacksonJsonService();
  }

  protected boolean detectJackson() {
    try {
      Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
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

  private static class ContextHandler extends ServletContextHandler {

    ContextHandler(String contextPath, boolean sessions, boolean security) {
      super(null, contextPath, sessions, security);
    }

    @Override
    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      request.setAttribute("jetty-target", target);
      request.setAttribute("jetty-request", baseRequest);
      nextHandle(target, baseRequest, request, response);
      //super.doHandle(target, baseRequest,request, response);
    }
  }
}
