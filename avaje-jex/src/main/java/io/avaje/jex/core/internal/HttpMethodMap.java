package io.avaje.jex.core.internal;

import io.avaje.jex.Routing;

import java.util.HashMap;
import java.util.Map;

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
