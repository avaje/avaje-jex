package io.avaje.jex.routes;

import io.avaje.jex.spi.SpiRoutes;

import java.util.ArrayList;
import java.util.List;

class RouteIndex {

  /**
   * Partition entries by the number of path segments.
   */
  private final RouteIndex.Entry[] entries = new RouteIndex.Entry[6];

  /**
   * Wildcard/splat based route entries.
   */
  private final List<SpiRoutes.Entry> wildcardEntries = new ArrayList<>();

  RouteIndex() {
    for (int i = 0; i < entries.length; i++) {
      entries[i] = new RouteIndex.Entry();
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

  SpiRoutes.Entry match(String pathInfo) {
    final SpiRoutes.Entry match = entries[index(segmentCount(pathInfo))].match(pathInfo);
    if (match != null) {
      return match;
    }
    // linear search wildcard/splat based matching
    for (SpiRoutes.Entry wildcardEntry : wildcardEntries) {
      if (wildcardEntry.matches(pathInfo)) {
        return wildcardEntry;
      }
    }
    return null;
  }

  private int segmentCount(String pathInfo) {
    if ("/".equals(pathInfo)) {
      return 0;
    }
    final int last = pathInfo.length() - 1; // ignore trailing slash
    int count = 1;
    for (int i = 1; i < last; i++) {
      if (pathInfo.charAt(i) == '/') {
        count++;
      }
    }
    return count;
  }

  long activeRequests() {
    long total = 0;
    for (RouteIndex.Entry entry : entries) {
      total += entry.activeRequests();
    }
    for (SpiRoutes.Entry entry : wildcardEntries) {
      total += entry.activeRequests();
    }
    return total;
  }

  private static class Entry {

    private final List<SpiRoutes.Entry> list = new ArrayList<>();

    void add(SpiRoutes.Entry entry) {
      list.add(entry);
    }

    SpiRoutes.Entry match(String pathInfo) {
      for (SpiRoutes.Entry entry : list) {
        if (entry.matches(pathInfo)) {
          return entry;
        }
      }
      return null;
    }

    long activeRequests() {
      long total = 0;
      for (SpiRoutes.Entry entry : list) {
        total += entry.activeRequests();
      }
      return total;
    }
  }
}
