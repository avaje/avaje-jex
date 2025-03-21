package io.avaje.helidon.http.spi;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class GrizzlyHttpServerProvider extends HttpServerProvider {

  private org.glassfish.grizzly.http.server.HttpServer server;

  public GrizzlyHttpServerProvider(HttpServer server) {

    this.server = server;
  }

  public GrizzlyHttpServerProvider() {

    this.server = new HttpServer();
  }

  @Override
  public com.sun.net.httpserver.HttpServer createHttpServer(InetSocketAddress addr, int backlog)
      throws IOException {

    return createServer(addr, backlog);
  }

  @Override
  public HttpsServer createHttpsServer(InetSocketAddress addr, int backlog) throws IOException {
    return createServer(addr, backlog);
  }

  private com.sun.net.httpserver.HttpsServer createServer(InetSocketAddress addr, int backlog)
      throws IOException {
    if (server == null) {
      server = new HttpServer();
    }

    JettyHttpServer jettyHttpServer = new JettyHttpServer(server);
    if (addr != null) jettyHttpServer.bind(addr, backlog);
    return jettyHttpServer;
  }
}
