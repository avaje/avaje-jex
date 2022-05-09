package io.avaje.jex.jetty;

import io.avaje.jex.*;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.spi.SpiServiceManager;
import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.Uptime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class JettyJexServer implements Jex.Server {

  private static final Logger log = LoggerFactory.getLogger(Jex.class);

  private final Jex jex;
  private final SpiRoutes routes;
  private final ServiceManager serviceManager;
  private final JettyServerConfig config;
  private final AppLifecycle lifecycle;
  private final long startTime;
  private final JexConfig jexConfig;
  private Server server;

  JettyJexServer(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager) {
    this.startTime = System.currentTimeMillis();
    this.jex = jex;
    this.jexConfig = jex.config();
    this.lifecycle = jex.lifecycle();
    this.routes = routes;
    this.serviceManager = new ServiceManager(serviceManager, initMultiPart());
    this.config = initConfig(jex.serverConfig());
  }

  private JettyServerConfig initConfig(ServerConfig config) {
    return config == null ? new JettyServerConfig() : (JettyServerConfig) config;
  }

  MultipartUtil initMultiPart() {
    return new MultipartUtil(initMultipartConfigElement(jexConfig.multipartConfig()));
  }

  MultipartConfigElement initMultipartConfigElement(UploadConfig uploadConfig) {
    if (uploadConfig == null) {
      final int fileThreshold = jexConfig.multipartFileThreshold();
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
      lifecycle.status(AppLifecycle.Status.STARTED);
      return this;
    } catch (Exception e) {
      throw new IllegalStateException("Error starting server", e);
    }
  }

  protected void createServer() {
    server = initServer();
    server.setHandler(initJettyHandler());
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

  protected Handler initJettyHandler() {
    var baseHandler = new JexHandler(jex, routes, serviceManager, initStaticHandler());
    if (!config.sessions()) {
      return baseHandler;
    }
    var sessionHandler = initSessionHandler();
    sessionHandler.setHandler(baseHandler);
    return sessionHandler;
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
    long startup = System.currentTimeMillis() - startTime;
    for (Connector c : server.getConnectors()) {
      String virtualThreads = config.virtualThreads() ? "with virtualThreads" : "";
      if (c instanceof ServerConnector) {
        ServerConnector sc = (ServerConnector) c;
        String host = (sc.getHost() == null) ? "0.0.0.0" : sc.getHost();
        log.info("Listening with {} {}:{} in {}ms @{}ms {}", sc.getProtocols(), host, sc.getLocalPort(), startup, Uptime.getUptime(), virtualThreads);
      } else {
        log.info("bind to {} in {}ms @{}ms {}", c, startup, Uptime.getUptime(), virtualThreads);
      }
    }
  }


}
