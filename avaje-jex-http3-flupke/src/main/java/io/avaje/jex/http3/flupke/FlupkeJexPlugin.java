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
import tech.kwik.core.server.ServerConnectionConfig;
import tech.kwik.core.server.ServerConnector;
import tech.kwik.flupke.server.Http3ServerExtensionFactory;

/**
 * A plugin that configures Jex to use the Flupke library for HTTP/3 and WebTransport functionality.
 *
 * <p>This plugin allows customization of the underlying Flupke server components and registers
 * WebTransport handlers.
 */
public final class FlupkeJexPlugin implements JexPlugin {

  private DatagramSocket socket;
  private Map<String, Http3ServerExtensionFactory> extensions = Map.of();
  private List<WebTransportEntry> wts = new ArrayList<>();
  private Consumer<ServerConnector.Builder> consumer = b -> {};
  private Consumer<ServerConnectionConfig.Builder> connection = b -> {};

  private FlupkeJexPlugin() {}

  /**
   * Creates a new instance of the {@code FlupkeJexPlugin}.
   *
   * @return The new plugin instance.
   */
  public static FlupkeJexPlugin create() {
    return new FlupkeJexPlugin();
  }

  /**
   * Provides a custom {@code DatagramSocket} for the HTTP/3 server to use.
   *
   * <p>This is typically used for specific network configurations or testing.
   *
   * @param socket The custom DatagramSocket to use.
   * @return This plugin instance for chaining.
   */
  public FlupkeJexPlugin customSocket(DatagramSocket socket) {
    this.socket = socket;
    return this;
  }

  /**
   * Sets a map of named {@code Http3ServerExtensionFactory} instances for advanced server
   * customization.
   *
   * @param extensions A map where the key is the extension name and the value is the factory.
   * @return This plugin instance for chaining.
   */
  public FlupkeJexPlugin extensions(Map<String, Http3ServerExtensionFactory> extensions) {
    this.extensions = extensions;
    return this;
  }

  /**
   * Provides a {@code Consumer} to configure the underlying Flupke {@code ServerConnector.Builder}.
   *
   * <p>This allows for low-level configuration of connection settings.
   *
   * @param consumer The consumer to apply configurations to the connector builder.
   * @return This plugin instance for chaining.
   * @see ServerConnector.Builder
   */
  public FlupkeJexPlugin connectorConfig(Consumer<ServerConnector.Builder> consumer) {
    this.consumer = consumer;
    return this;
  }

  /**
   * Provides a {@code Consumer} to configure the underlying Flupke {@code
   * ServerConnectionConfig.Builder}.
   *
   * @param consumer The consumer to apply configurations to the connection config builder.
   * @return This plugin instance for chaining.
   * @see ServerConnectionConfig.Builder
   */
  public FlupkeJexPlugin connectionConfig(Consumer<ServerConnectionConfig.Builder> consumer) {
    this.connection = consumer;
    return this;
  }

  /**
   * Registers a new WebTransport handler for the specified path.
   *
   * <p>This is the simpler method if you have a pre-built {@code WebTransportHandler} instance.
   *
   * @param path The URL path (e.g., "/my-webtransport-endpoint").
   * @param handler The fully configured WebTransportHandler.
   * @return This plugin instance for chaining.
   */
  public FlupkeJexPlugin webTransport(String path, WebTransportHandler handler) {
    this.wts.add(new WebTransportEntry(path, handler));
    return this;
  }

  /**
   * Registers a new WebTransport handler for the specified path using a builder pattern.
   *
   * <p>This method accepts a {@code Consumer} that receives a {@code WebTransportHandler.Builder}
   * for fluent configuration of the event listeners.
   *
   * @param path The URL path (e.g., "/my-webtransport-endpoint").
   * @param consumer A consumer to configure the {@code WebTransportHandler.Builder}.
   * @return This plugin instance for chaining.
   * @see WebTransportHandler.Builder
   */
  public FlupkeJexPlugin webTransport(String path, Consumer<WebTransportHandler.Builder> consumer) {
    var b = WebTransportHandler.builder();
    consumer.accept(b);
    this.wts.add(new WebTransportEntry(path, b.build()));
    return this;
  }

  /**
   * Applies this plugin to the Jex instance.
   *
   * <p>This method registers the {@link H3ServerProvider} with Jex, injecting all accumulated
   * configurations for HTTP/3 and WebTransport.
   *
   * @param jex The Jex instance being configured.
   */
  @Override
  public void apply(Jex jex) {
    jex.config()
        .serverProvider(new H3ServerProvider(consumer, connection, wts, extensions, socket));
  }
}
