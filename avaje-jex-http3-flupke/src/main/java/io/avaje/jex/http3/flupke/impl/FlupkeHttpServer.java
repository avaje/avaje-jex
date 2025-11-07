package io.avaje.jex.http3.flupke.impl;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import io.avaje.jex.ssl.impl.SSLConfigurator;
import tech.kwik.core.server.ServerConnector;
import tech.kwik.flupke.server.Http3ApplicationProtocolFactory;
import tech.kwik.flupke.server.Http3ServerExtensionFactory;

/** Jetty implementation of {@link com.sun.net.httpserver.HttpServer}. */
class FlupkeHttpServer extends HttpsServer {

  private DatagramSocket datagram;
  private InetSocketAddress addr;
  private Executor executor;

  private FlupkeHttpContext context;

  private Consumer<ServerConnector.Builder> configuration;

  private HttpServer http1;
  private ServerConnector connector;
  private KeyStore keystore;
  private String password;

  public FlupkeHttpServer(
      Consumer<ServerConnector.Builder> configuration,
      Map<String, Http3ServerExtensionFactory> extensions,
      DatagramSocket socket,
      InetSocketAddress addr,
      int backlog)
      throws IOException {
    this.configuration = configuration;
    this.datagram = socket;
    this.addr = addr;
    http1 = HttpServer.create(addr, backlog);
  }

  @Override
  public void bind(InetSocketAddress addr, int backlog) throws IOException {
    this.addr = addr;
    if (datagram == null) {
      datagram = new DatagramSocket(addr);
    }
  }

  @Override
  public InetSocketAddress getAddress() {
    return (InetSocketAddress) datagram.getLocalSocketAddress();
  }

  @Override
  public void start() {
    try {
      var builder = ServerConnector.builder();
      bind(addr, 0);
      builder
          .withLogger(new FlupkeSystemLogger())
          .withPort(1)
          .withSocket(datagram)
          .withKeyStore(keystore, keystore.aliases().nextElement(), password.toCharArray());

      configuration.accept(builder);
      this.connector = builder.build();

      var factory = new Http3ApplicationProtocolFactory(context.flupkeHandler());

      connector.registerApplicationProtocol("h3", factory);
      connector.start();
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    http1.start();
  }

  @Override
  public void setExecutor(Executor executor) {
    this.executor = executor;
    http1.setExecutor(executor);
  }

  @Override
  public Executor getExecutor() {
    return executor;
  }

  @Override
  public void stop(int delay) {
    connector.close();
    http1.stop(delay);
  }

  @Override
  public HttpContext createContext(String path, HttpHandler httpHandler) {

    this.context = new FlupkeHttpContext(this, path, httpHandler);
    http1.createContext(path, httpHandler);
    return context;
  }

  @Override
  public HttpContext createContext(String path) {
    throw new UnsupportedOperationException("Need a handler");
  }

  @Override
  public void removeContext(String path) throws IllegalArgumentException {
    context = null;
    http1.removeContext(path);
  }

  @Override
  public void removeContext(HttpContext context) {
    this.context = null;
    http1.removeContext(context);
  }

  @Override
  public void setHttpsConfigurator(HttpsConfigurator config) {
    if (config instanceof SSLConfigurator ssl) {
      this.keystore = ssl.keyStore();
      this.password = ssl.password();
    } else {
      throw new IllegalArgumentException("Only the Jex SSL plugin supported");
    }
  }

  @Override
  public HttpsConfigurator getHttpsConfigurator() {
    throw new UnsupportedOperationException();
  }
}
