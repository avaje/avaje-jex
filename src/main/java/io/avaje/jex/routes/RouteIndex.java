package io.avaje.jex.routes;

import java.util.ArrayList;
import java.util.List;

class RouteIndex {

  private final List<RouteEntry> list = new ArrayList<>();

  void add(RouteEntry entry) {
    list.add(entry);
  }

  RouteEntry match(String pathInfo) {
    for (RouteEntry entry : list) {
      if (entry.matches(pathInfo)) {
        return entry;
      }
    }
    return null;
  }
}
