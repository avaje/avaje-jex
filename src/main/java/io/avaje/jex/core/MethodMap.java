package io.avaje.jex.core;

import io.avaje.jex.routes.HandlerType;

import java.util.HashMap;
import java.util.Map;

final class MethodMap {

  private final Map<String, HandlerType> map = new HashMap<>();

  MethodMap() {
    for (HandlerType value : HandlerType.values()) {
      map.put(value.name(), value);
    }
  }

  HandlerType get(String method) {
    return map.get(method);
  }
}
