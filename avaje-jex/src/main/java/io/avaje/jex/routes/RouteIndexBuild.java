package io.avaje.jex.routes;

import io.avaje.jex.ExchangeHandler;

import java.util.*;

/**
 * Build the RouteIndex.
 */
final class RouteIndexBuild {

  /**
   * Partition entries by the number of path segments.
   */
  private final RouteIndexBuild.Entry[] entries = new RouteIndexBuild.Entry[6];

  /**
   * Wildcard/splat based route entries.
   */
  private final List<SpiRoutes.Entry> wildcardEntries = new ArrayList<>();

  RouteIndexBuild() {
    for (int i = 0; i < entries.length; i++) {
      entries[i] = new RouteIndexBuild.Entry();
    }
  }

  private int index(int segmentCount) {
    return Math.min(segmentCount, 5);
  }

  void add(SpiRoutes.Entry entry) {
    if (entry.multiSlash()) {
      wildcardEntries.add(entry);
    } else {
      entries[index(entry.segmentCount())].add(entry);
    }
  }

  /**
   * Build and return the RouteIndex.
   */
  RouteIndex build() {
    final List<List<SpiRoutes.Entry>> pathEntries = new ArrayList<>();
    for (Entry entry : entries) {
      pathEntries.add(entry.build());
    }
    return new RouteIndex(wildcardEntries, pathEntries);
  }

  private static class Entry {

    private final List<SpiRoutes.Entry> list = new ArrayList<>();
    private final Map<String,List<SpiRoutes.Entry>> pathMap = new LinkedHashMap<>();

    void add(SpiRoutes.Entry entry) {
      if (entry.literal()) {
        // add literal paths to the beginning
        list.addFirst(entry);
      } else {
        pathMap.computeIfAbsent(entry.matchPath(), k -> new ArrayList<>(2)).add(entry);
      }
    }

    List<SpiRoutes.Entry> build() {
      List<SpiRoutes.Entry> result = new ArrayList<>(list.size() + pathMap.size());
      result.addAll(list);
      pathMap.values().forEach(pathList -> {
        if (pathList.size() == 1) {
          result.add(pathList.getFirst());
        } else {
          ExchangeHandler[] handlers = pathList.stream()
            .map(SpiRoutes.Entry::handler)
            .toList()
            .toArray(new ExchangeHandler[0]);
          result.add(pathList.getFirst().multiHandler(handlers));
        }
      });
      return result;
    }
  }
}
