package io.avaje.jex.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.avaje.jex.Jex;
import io.avaje.jex.Routing.HttpService;
import io.avaje.jex.security.Role;
import io.avaje.jex.spi.JexPlugin;
import io.avaje.jex.websocket.WebSocketListener.Builder;

public class WebSocketPlugin implements JexPlugin {

  private final List<HttpService> handlers = new ArrayList<>();

  public WebSocketPlugin ws(String path, Consumer<Builder> consumer, Role... roles) {
    var builder = WebSocketListener.builder();
    consumer.accept(builder);
    return ws(path, builder.build(), roles);
  }

  public WebSocketPlugin ws(String path, WebSocketListener listener, Role... roles) {
    handlers.add(r -> r.get(path, new DWebSocketHandler(listener), roles));
    return this;
  }

  @Override
  public void apply(Jex jex) {
    jex.routing().addAll(handlers);
  }

  public static WebSocketPlugin create() {
    return new WebSocketPlugin();
  }
}
