package io.avaje.helidon.http.spi;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;

import sun.nio.ch.ThreadPool;

/** Jetty implementation of {@link com.sun.net.httpserver.HttpServer}. */
public class JettyHttpServer extends com.sun.net.httpserver.HttpServer {
  private static final System.Logger LOG =
      System.getLogger(JettyHttpServer.class.getCanonicalName());
  private final HttpServer _server;
  private final boolean _serverShared;
  private final Map<String, JettyHttpContext> _contexts = new HashMap<>();
  private final Map<String, NetworkListener> _connectors = new HashMap<>();
  private InetSocketAddress _addr;
  private ServerConfiguration _httpConfiguration;

  public JettyHttpServer(HttpServer server, boolean shared) {

    this(server, shared, server.getServerConfiguration());
  }

  public JettyHttpServer(HttpServer server, boolean shared, ServerConfiguration configuration) {
    this._server = server;
    this._serverShared = shared;
    this._httpConfiguration = configuration;
  }

  public ServerConfiguration getHttpConfiguration() {
    return _httpConfiguration;
  }

  @Override
  public void bind(InetSocketAddress addr, int backlog) throws IOException {
    this._addr = addr;
    // check if there is already a connector listening
    var connectors = _server.getListeners();
    if (connectors != null) {
      for (var connector : connectors) {
        if (connector.getPort() == addr.getPort()) {
          LOG.log(
              Level.DEBUG, "server already bound to port {}, no need to rebind", addr.getPort());
          return;
        }
      }
    }

    if (_serverShared)
      throw new IOException("grizzly server is not bound to port " + addr.getPort());

    if (LOG.isLoggable(Level.DEBUG)) {
      LOG.log(Level.DEBUG, "binding server to port " + addr.getPort());
    }

    NetworkListener listener = new NetworkListener("rizzly", addr.getHostName(), addr.getPort());
    _server.addListener(listener);
    _connectors.put(addr.getHostName() + addr.getPort(), listener);
  }

  protected HttpServer getServer() {
    return _server;
  }

  protected NetworkListener newServerConnector(InetSocketAddress addr, int backlog) {
    NetworkListener listener = new NetworkListener("rizzly", addr.getHostName(), addr.getPort());

    return listener;
  }

  @Override
  public InetSocketAddress getAddress() {
    if (_addr.getPort() == 0 && _server.isStarted())
      return new InetSocketAddress(_addr.getHostString(), _server.getListener("rizzly").getPort());
    return _addr;
  }

  @Override
  public void start() {
    if (_serverShared) return;

    try {
      _server.start();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void setExecutor(Executor executor) {
    if (executor == null)
      throw new IllegalArgumentException("missing required 'executor' argument");
  }

  @Override
  public Executor getExecutor() {
    ThreadPool threadPool = _server.getThreadPool();
    if (threadPool instanceof DelegatingThreadPool)
      return ((DelegatingThreadPool) _server.getThreadPool()).getExecutor();
    return threadPool;
  }

  @Override
  public void stop(int delay) {
    cleanUpContexts();
    cleanUpConnectors();

    if (_serverShared) return;

    _server.shutdown();
  }

  private void cleanUpContexts() {
    for (Map.Entry<String, JettyHttpContext> stringJettyHttpContextEntry : _contexts.entrySet()) {
      JettyHttpContext context = stringJettyHttpContextEntry.getValue();
      _server.removeBean(context.getJettyContextHandler());
    }
    _contexts.clear();
  }

  private void cleanUpConnectors() {
    for (var stringConnectorEntry : _connectors.entrySet()) {
      var connector = stringConnectorEntry.getValue();
      try {
        connector.shutdownNow();
      } catch (Exception ex) {
        LOG.log(Level.WARNING, "Unable to stop connector {}", connector, ex);
      }
      _server.removeListener(stringConnectorEntry.getKey());
    }
    _connectors.clear();
  }

  @Override
  public HttpContext createContext(String path, HttpHandler httpHandler) {
    checkIfContextIsFree(path);

    JettyHttpContext context = new JettyHttpContext(this, path, httpHandler);
    HttpSpiContextHandler jettyContextHandler = context.getJettyContextHandler();

    ContextHandlerCollection contexts = _server.getDescendant(ContextHandlerCollection.class);

    if (contexts == null)
      throw new RuntimeException("could not find ContextHandlerCollection, you must configure one");

    contexts.addHandler(jettyContextHandler);
    if (contexts.isStarted()) {
      try {
        jettyContextHandler.start();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    _contexts.put(path, context);
    return context;
  }

  @Override
  public HttpContext createContext(String path) {
    return createContext(path, null);
  }

  private void checkIfContextIsFree(String path) {
    Handler serverHandler = _server.getHandler();
    if (serverHandler instanceof ContextHandler) {
      ContextHandler ctx = (ContextHandler) serverHandler;
      if (ctx.getContextPath().equals(path))
        throw new RuntimeException("another context already bound to path " + path);
    }

    List<Handler> handlers = _server.getHandlers();
    for (Handler handler : handlers) {
      if (handler instanceof ContextHandler) {
        ContextHandler ctx = (ContextHandler) handler;
        if (ctx.getContextPath().equals(path))
          throw new RuntimeException("another context already bound to path " + path);
      }
    }
  }

  @Override
  public void removeContext(String path) throws IllegalArgumentException {
    JettyHttpContext context = _contexts.remove(path);
    if (context == null) return;
    HttpSpiContextHandler handler = context.getJettyContextHandler();

    ContextHandlerCollection chc = _server.getDescendant(ContextHandlerCollection.class);
    try {
      handler.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    chc.removeHandler(handler);
  }

  @Override
  public void removeContext(HttpContext context) {
    removeContext(context.getPath());
  }
}
