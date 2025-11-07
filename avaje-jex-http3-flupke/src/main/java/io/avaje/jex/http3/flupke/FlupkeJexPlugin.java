package io.avaje.jex.http3.flupke;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.avaje.jex.Jex;
import io.avaje.jex.http3.flupke.impl.H3ServerProvider;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEntry;
import io.avaje.jex.http3.flupke.webtransport.WebTransportHandler;
import io.avaje.jex.spi.JexPlugin;
import tech.kwik.core.server.ServerConnector.Builder;
import tech.kwik.flupke.server.Http3ServerExtensionFactory;

public final class FlupkeJexPlugin implements JexPlugin {

  private DatagramSocket socket;
  private Map<String, Http3ServerExtensionFactory> extensions = Map.of();
  private List<WebTransportEntry> wts = new ArrayList<>();
  private Consumer<Builder> consumer = b -> {};

  private FlupkeJexPlugin() {}

  public static FlupkeJexPlugin create() {
    return new FlupkeJexPlugin();
  }

  public FlupkeJexPlugin customSocket(DatagramSocket socket) {
    this.socket = socket;
    return this;
  }

  public FlupkeJexPlugin extensions(Map<String, Http3ServerExtensionFactory> extensions) {
    this.extensions = extensions;
    return this;
  }

  public FlupkeJexPlugin config(Consumer<Builder> consumer) {
    this.consumer = consumer;
    return this;
  }

  public FlupkeJexPlugin webTransport(String path, WebTransportHandler handler) {
    this.wts.add(new WebTransportEntry(path, handler));
    return this;
  }

  public FlupkeJexPlugin webTransport(String path, Consumer<WebTransportHandler.Builder> consumer) {
    var b = WebTransportHandler.builder();
    consumer.accept(b);
    this.wts.add(new WebTransportEntry(path, b.build()));
    return this;
  }

  @Override
  public void apply(Jex jex) {
    jex.config().serverProvider(new H3ServerProvider(consumer, wts,extensions, socket));
  }
}
