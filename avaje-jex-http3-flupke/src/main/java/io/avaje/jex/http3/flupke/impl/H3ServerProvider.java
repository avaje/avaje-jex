package io.avaje.jex.http3.flupke.impl;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

import tech.kwik.core.server.ServerConnector.Builder;
import tech.kwik.flupke.server.Http3ServerExtensionFactory;

public class H3ServerProvider extends HttpServerProvider {

  private final Consumer<Builder> consumer;
  private final DatagramSocket datagram;
  private final Map<String, Http3ServerExtensionFactory> extensions;

  public H3ServerProvider(
      Consumer<Builder> consumer,
      Map<String, Http3ServerExtensionFactory> extensions,
      DatagramSocket datagram) {
    this.consumer = consumer;
    this.datagram = datagram;
    this.extensions = extensions;
  }

  @Override
  public HttpServer createHttpServer(InetSocketAddress addr, int backlog) throws IOException {
    throw new UnsupportedOperationException("Https is required for HTTP/3");
  }

  @Override
  public HttpsServer createHttpsServer(InetSocketAddress addr, int backlog) throws IOException {
    return new FlupkeHttpServer(consumer, extensions, datagram, addr, backlog);
  }
}
