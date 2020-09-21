package io.avaje.jex.routes;

import io.avaje.jex.spi.SpiRoutes;

import java.util.ArrayList;
import java.util.List;

class RouteIndex {

  /**
   * Partition entries by the number of path segments.
   */
  private final Entry[] entries = new Entry[6];

  RouteIndex() {
    for (int i = 0; i < entries.length; i++) {
      entries[i] = new Entry();
    }
  }

  private int index(int segmentCount) {
    return Math.min(segmentCount, 5);
  }

  void add(SpiRoutes.Entry entry) {
    entries[index(entry.getSegmentCount())].add(entry);
  }

  SpiRoutes.Entry match(String pathInfo) {
    return entries[index(segmentCount(pathInfo))].match(pathInfo);
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
  }
}
