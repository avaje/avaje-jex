package io.avaje.jex.http3.flupke.core;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

import io.avaje.jex.http3.flupke.webtransport.WebTransportEntry;
import tech.kwik.core.server.ServerConnectionConfig;
import tech.kwik.core.server.ServerConnector;
import tech.kwik.flupke.server.Http3ServerExtensionFactory;

public class H3ServerProvider extends HttpServerProvider {

  private final Consumer<ServerConnector.Builder> consumer;
  private final Consumer<ServerConnectionConfig.Builder> connection;
  private final DatagramSocket datagram;
  private final List<WebTransportEntry> wts;
  private final Map<String, Http3ServerExtensionFactory> extensions;

  public H3ServerProvider(
      Consumer<ServerConnector.Builder> consumer,
      Consumer<ServerConnectionConfig.Builder> connection,
      List<WebTransportEntry> wts,
      Map<String, Http3ServerExtensionFactory> extensions,
      DatagramSocket datagram) {
    this.consumer = consumer;
    this.connection = connection;
    this.wts = wts;
    this.datagram = datagram;
    this.extensions = extensions;
  }

  @Override
  public HttpServer createHttpServer(InetSocketAddress addr, int backlog) throws IOException {
    throw new UnsupportedOperationException("Https is required for HTTP/3");
  }

  @Override
  public HttpsServer createHttpsServer(InetSocketAddress addr, int backlog) throws IOException {
    return new FlupkeHttpServer(consumer, connection, wts, extensions, datagram, addr, backlog);
  }
}
