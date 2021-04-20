package io.avaje.jex.jetty;

import io.avaje.jex.Jex;
import io.avaje.jex.StaticFileSource;
import io.avaje.jex.core.ServiceManager;
import io.avaje.jex.routes.RoutesBuilder;
import io.avaje.jex.spi.SpiRoutes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.Uptime;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class JettyLaunch implements Jex.Server {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(JettyLaunch.class);

  private final Jex jex;
  private final SpiRoutes routes;
  private Server server;

  JettyLaunch(Jex jex) {
    this.jex = jex;
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

  protected Jex.Server start() {
    try {
      server = createServer();
      server.start();
      logOnStart(server);
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

  protected Server initServer() {
    Server server = jex.jetty.server;
    if (server != null) {
      return server;
    }
    return new JettyBuilder(jex).build();
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

  protected SessionHandler defaultSessionHandler() {
    SessionHandler sh = new SessionHandler();
    sh.setHttpOnly(true);
    return sh;
  }

  protected StaticHandler initStaticHandler() {
    final List<StaticFileSource> fileSources = jex.staticFiles().getSources();
    if (fileSources == null || fileSources.isEmpty()) {
      return null;
    }
    StaticHandlerFactory factory = new StaticHandlerFactory();
    return factory.build(jex, fileSources);
  }

  protected ServiceManager serviceManager() {
    return ServiceManager.create(jex);
  }

  private void logOnStart(org.eclipse.jetty.server.Server server) {
    for (Connector c : server.getConnectors()) {
      String virtualThreads = jex.jetty.virtualThreads ? "with virtualThreads" : "";
      if (c instanceof ServerConnector) {
        ServerConnector sc = (ServerConnector) c;
        String host = (sc.getHost() == null) ? "localhost" : sc.getHost();
        log.info("Listening with {} host:{} port:{} @{}ms {}", sc.getProtocols(), host, sc.getLocalPort(), Uptime.getUptime(), virtualThreads);
      } else {
        log.info("bind to {} @{}ms {}", c, Uptime.getUptime(), virtualThreads);
      }
    }
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
