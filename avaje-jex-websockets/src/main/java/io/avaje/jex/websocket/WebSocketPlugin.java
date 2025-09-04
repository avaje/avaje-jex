package io.avaje.jex.websocket;

import java.util.ArrayList;
import java.util.List;

import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jex.security.Role;
import io.avaje.jex.spi.JexPlugin;

public class WebSocketPlugin implements JexPlugin {

  private final List<Routing.HttpService> handlers = new ArrayList<>();

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
