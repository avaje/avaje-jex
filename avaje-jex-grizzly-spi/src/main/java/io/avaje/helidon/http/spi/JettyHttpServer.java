package io.avaje.helidon.http.spi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;

/** Jetty implementation of {@link com.sun.net.httpserver.HttpServer}. */
public class JettyHttpServer extends com.sun.net.httpserver.HttpsServer {
  private static final System.Logger LOG =
      System.getLogger(JettyHttpServer.class.getCanonicalName());
  private final HttpServer server;
  private final Map<String, JettyHttpContext> contexts = new HashMap<>();
  private InetSocketAddress addr;
  private ServerConfiguration httpConfiguration;
  private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private HttpsConfigurator httpsConfig;

  public JettyHttpServer(HttpServer server) {

    this(server, server.getServerConfiguration());
  }

  public JettyHttpServer(HttpServer server, ServerConfiguration configuration) {
    this.server = server;
    this.httpConfiguration = configuration;
  }

  public ServerConfiguration getHttpConfiguration() {
    return httpConfiguration;
  }

  @Override
  public void bind(InetSocketAddress addr, int backlog) throws IOException {

    this.addr = addr;
    // check if there is already a connector listening
    var connectors = server.getListeners();
    if (connectors != null) {
      for (var connector : connectors) {
        if (connector.getPort() == addr.getPort()) {
          LOG.log(
              Level.DEBUG, "server already bound to port {}, no need to rebind", addr.getPort());
          return;
        }
      }
    }

    if (LOG.isLoggable(Level.DEBUG)) {
      LOG.log(Level.DEBUG, "binding server to port " + addr.getPort());
    }
    var listener = new NetworkListener("rizzly", addr.getHostName(), addr.getPort());
    listener.getTransport().setWorkerThreadPool(executor);
    if (backlog != 0) {
      listener.getTransport().setServerConnectionBackLog(backlog);
    }
    if (httpsConfig != null) {
      listener.setSSLEngineConfig(new SSLEngineConfigurator(httpsConfig.getSSLContext()));
    }

    server.addListener(listener);
  }

  protected HttpServer getServer() {
    return server;
  }

  @Override
  public InetSocketAddress getAddress() {
    if (addr.getPort() == 0 && server.isStarted())
      return new InetSocketAddress(addr.getHostString(), server.getListener("rizzly").getPort());
    return addr;
  }

  @Override
  public void start() {

    try {
      server.start();
    } catch (IOException e) {

      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void setExecutor(Executor executor) {
    if (executor instanceof ExecutorService service) {
      this.executor = service;
    } else {
      throw new IllegalArgumentException("Grizzly only accepts ExecutorService");
    }
  }

  @Override
  public Executor getExecutor() {
    return executor;
  }

  @Override
  public void stop(int delay) {

    for (var context : contexts.values()) {
      httpConfiguration.removeHttpHandler(context.getGrizzlyHandler());
    }
    contexts.clear();

    server.shutdown();
  }

  @Override
  public HttpContext createContext(String path, HttpHandler httpHandler) {

    JettyHttpContext context = new JettyHttpContext(this, path, httpHandler);
    GrizzlyHandler jettyContextHandler = context.getGrizzlyHandler();

    httpConfiguration.addHttpHandler(
        jettyContextHandler, path.transform(this::prependSlash).transform(this::appendSlash));

    contexts.put(path, context);
    return context;
  }

  private String prependSlash(String s) {
    return s.startsWith("/") ? s : "/" + s;
  }

  private String appendSlash(String s) {
    return s.endsWith("/") ? s + "*" : s + "/*";
  }

  @Override
  public HttpContext createContext(String path) {
    return createContext(path, null);
  }

  @Override
  public void removeContext(String path) throws IllegalArgumentException {
    JettyHttpContext context = contexts.remove(path);
    if (context == null) return;
    GrizzlyHandler handler = context.getGrizzlyHandler();
    httpConfiguration.removeHttpHandler(handler);
  }

  @Override
  public void removeContext(HttpContext context) {
    removeContext(context.getPath());
  }

  @Override
  public void setHttpsConfigurator(HttpsConfigurator config) {
    httpsConfig = config;
  }

  @Override
  public HttpsConfigurator getHttpsConfigurator() {
    return httpsConfig;
  }
}
