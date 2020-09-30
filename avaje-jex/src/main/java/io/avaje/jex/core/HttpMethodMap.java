package io.avaje.jex.core;

import io.avaje.jex.Routing;

import java.util.HashMap;
import java.util.Map;

public final class HttpMethodMap {

  private final Map<String, Routing.Type> map = new HashMap<>();

  public HttpMethodMap() {
    for (Routing.Type value : Routing.Type.values()) {
      map.put(value.name(), value);
    }
  }

  public Routing.Type get(String method) {
    return map.get(method);
  }
}
