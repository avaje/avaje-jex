package io.avaje.jex.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.avaje.jex.Jex;
import io.avaje.jex.Routing.HttpService;
import io.avaje.jex.security.Role;
import io.avaje.jex.spi.JexPlugin;

/**
 * A plugin for the Jex web framework to simplify the registration of WebSocket handlers.
 *
 * <p>This class provides a fluent API for mapping specific URL paths to {@link WebSocketListener}
 * implementations and integrates them into the Jex application's routing.
 *
 * <p><strong>**Note on Server Compatibility:**</strong> WebSocket support may be limited or unavailable with older versions of the default
 * JDK server provider or certain third-party providers like Jetty, requiring a newer version or an
 * alternative server implementation to function correctly.
 */
public class WebSocketPlugin implements JexPlugin {

  private final List<HttpService> handlers = new ArrayList<>();

  /**
   * Registers a WebSocket listener for a given path using a fluent builder approach.
   *
   * @param path The URL path to which the WebSocket endpoint will be mapped (e.g., "/ws/chat").
   * @param consumer A {@code Consumer} that configures the {@link WebSocketListener.Builder} to
   *     create the listener.
   * @param roles Optional roles required to access this WebSocket endpoint.
   * @return This {@code WebSocketPlugin} instance for method chaining.
   */
  public WebSocketPlugin ws(
      String path, Consumer<WebSocketListener.Builder> consumer, Role... roles) {
    var builder = WebSocketListener.builder();
    consumer.accept(builder);
    return ws(path, builder.build(), roles);
  }

  /**
   * Registers a pre-built {@link WebSocketListener} for a given path.
   *
   * @param path The URL path to which the WebSocket endpoint will be mapped (e.g., "/ws/chat").
   * @param listener The {@link WebSocketListener} instance that handles WebSocket events.
   * @param roles Optional roles required to access this WebSocket endpoint.
   * @return This {@code WebSocketPlugin} instance for method chaining.
   */
  public WebSocketPlugin ws(String path, WebSocketListener listener, Role... roles) {
    handlers.add(r -> r.get(path, new DWebSocketHandler(listener), roles));
    return this;
  }

  /**
   * Applies the plugin to the Jex application.
   *
   * <p>This method adds all registered WebSocket handlers to the Jex router and checks for server
   * provider compatibility before application startup.
   *
   * @param jex The {@link Jex} instance to which the plugin is being applied.
   * @throws UnsupportedOperationException if the current server provider is detected as
   *     incompatible with WebSocket functionality.
   */
  @Override
  public void apply(Jex jex) {
    jex.routing().addAll(handlers);

    var provider = jex.config().serverProvider().getClass().getPackageName();
    if (provider.indexOf("sun.") != -1) {
      throw new UnsupportedOperationException(
          "WebSocket not yet supported for the JDK's built-in httpserver: https://bugs.openjdk.org/browse/JDK-8368695. Use a different server provider such as robaho"
              .formatted(jex.config().serverProvider().getClass()));
    }
    if (provider.indexOf("jetty.") != -1) {
      throw new UnsupportedOperationException(
          "WebSocket not supported for %s, use a different server provider"
              .formatted(jex.config().serverProvider().getClass()));
    }
  }

  /**
   * Creates a new instance of the {@code WebSocketPlugin}.
   *
   * @return A new {@code WebSocketPlugin}.
   */
  public static WebSocketPlugin create() {
    return new WebSocketPlugin();
  }
}
