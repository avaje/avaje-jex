package io.avaje.jex.core;

import java.util.HashMap;
import java.util.Map;

import io.avaje.jex.Routing;

final class HttpMethodMap {

  private final Map<String, Routing.Type> map = new HashMap<>();

  HttpMethodMap() {
    for (Routing.Type value : Routing.Type.values()) {
      map.put(value.name(), value);
    }
  }

  Routing.Type get(String method) {
    return map.get(method);
  }
}
