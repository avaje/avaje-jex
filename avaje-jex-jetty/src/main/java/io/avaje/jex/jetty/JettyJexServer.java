package io.avaje.jex.jetty;

import io.avaje.jex.*;
import io.avaje.jex.spi.SpiServiceManager;
import io.avaje.jex.spi.SpiRoutes;
import jakarta.servlet.MultipartConfigElement;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class JettyJexServer implements Jex.Server {

  private static final Logger log = LoggerFactory.getLogger(Jex.class);

  private final Jex jex;
  private final SpiRoutes routes;
  private final ServiceManager serviceManager;
  private final JettyServerConfig config;
  private final AppLifecycle lifecycle;
  private Server server;

  JettyJexServer(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager) {
    this.jex = jex;
    this.lifecycle = jex.lifecycle();
    this.routes = routes;
    this.serviceManager = new ServiceManager(serviceManager, initMultiPart());
    this.config = initConfig(jex.serverConfig());
  }

  private JettyServerConfig initConfig(ServerConfig config) {
    return config == null ? new JettyServerConfig() : (JettyServerConfig) config;
  }

  MultipartUtil initMultiPart() {
    return new MultipartUtil(initMultipartConfigElement(jex.config.multipartConfig));
  }

  MultipartConfigElement initMultipartConfigElement(UploadConfig uploadConfig) {
    if (uploadConfig == null) {
      final int fileThreshold = jex.config.multipartFileThreshold;
      return new MultipartConfigElement(System.getProperty("java.io.tmpdir"), -1, -1, fileThreshold);
    }
    return new MultipartConfigElement(uploadConfig.location(), uploadConfig.maxFileSize(), uploadConfig.maxRequestSize(), uploadConfig.fileSizeThreshold());
  }

  @Override
  public void onShutdown(Runnable onShutdown) {
    lifecycle.onShutdown(onShutdown, Integer.MAX_VALUE);
  }

  @Override
  public void shutdown() {
    try {
      log.trace("starting shutdown");
      lifecycle.status(AppLifecycle.Status.STOPPING);
      routes.waitForIdle(30);
      server.stop();
      log.trace("server http listeners stopped");
      lifecycle.status(AppLifecycle.Status.STOPPED);
      log.info("shutdown complete");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected Jex.Server start() {
    try {
      createServer();
      server.start();
      logOnStart(server);
      lifecycle.registerShutdownHook(this::shutdown);
      return this;
    } catch (Exception e) {
      throw new IllegalStateException("Error starting server", e);
    }
  }

  protected void createServer() {
    server = initServer();
    server.setHandler(initContextHandler());
    if (server.getStopAtShutdown()) {
      // do not use Jetty ShutdownHook, use the AppLifecycle one instead
      server.setStopAtShutdown(false);
    }
    config.server(server);
    config.postConfigure();
  }

  protected Server initServer() {
    Server server = config.server();
    if (server != null) {
      return server;
    }
    return new JettyBuilder(jex, config).build();
  }

  protected ServletContextHandler initContextHandler() {
    final ServletContextHandler sc = initServletContextHandler();
    sc.setSessionHandler(initSessionHandler());
    sc.addServlet(initServletHolder(), "/*");
    config.contextHandler(sc);
    return sc;
  }

  protected ServletHolder initServletHolder() {
    final StaticHandler staticHandler = initStaticHandler();
    return new ServletHolder(new JexHttpServlet(jex, routes, serviceManager, staticHandler));
  }

  protected ServletContextHandler initServletContextHandler() {
    final ServletContextHandler ch = config.contextHandler();
    return ch != null ? ch : new ContextHandler(jex.config.contextPath, config.sessions(), config.security());
  }

  protected SessionHandler initSessionHandler() {
    SessionHandler sh = config.sessionHandler();
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
    return factory.build(server, jex, fileSources);
  }

  private void logOnStart(org.eclipse.jetty.server.Server server) {
    for (Connector c : server.getConnectors()) {
      String virtualThreads = config.virtualThreads() ? "with virtualThreads" : "";
      if (c instanceof ServerConnector) {
        ServerConnector sc = (ServerConnector) c;
        String host = (sc.getHost() == null) ? "0.0.0.0" : sc.getHost();
        log.info("Listening with {} {}:{} @{}ms {}", sc.getProtocols(), host, sc.getLocalPort(), Uptime.getUptime(), virtualThreads);
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
