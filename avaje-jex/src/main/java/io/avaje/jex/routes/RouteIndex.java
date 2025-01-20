package io.avaje.jex.routes;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

final class RouteIndex {

  /**
   * Partition entries by the number of path segments.
   */
  private final IndexEntry[] entries;

  /**
   * Wildcard/splat based route entries.
   */
  private final SpiRoutes.Entry[] wildcardEntries;

  RouteIndex(List<SpiRoutes.Entry> wildcards, List<List<SpiRoutes.Entry>> pathEntries) {
    this.wildcardEntries = wildcards.toArray(new SpiRoutes.Entry[0]);
    this.entries = pathEntries.stream()
      .map(RouteIndex::toEntry)
      .toList()
      .toArray(new IndexEntry[0]);
  }

  @Override
  public String toString() {

    return "RouteIndex{"
        + Stream.concat(
                Arrays.stream(entries)
                    .filter(i -> i.pathEntries.length > 0)
                    .map(i -> i.pathEntries)
                    .flatMap(Arrays::stream)
                    .map(Object::toString),
                Arrays.stream(wildcardEntries).map(Object::toString))
            .sorted().collect(joining(", "))
        + '}';
  }

  private static IndexEntry toEntry(List<SpiRoutes.Entry> routeEntries) {
    return new IndexEntry(routeEntries.toArray(new SpiRoutes.Entry[0]));
  }

  private int index(int segmentCount) {
    return Math.min(segmentCount, 5);
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
    for (IndexEntry entry : entries) {
      total += entry.activeRequests();
    }
    for (SpiRoutes.Entry entry : wildcardEntries) {
      total += entry.activeRequests();
    }
    return total;
  }

  private static final class IndexEntry {

    private final SpiRoutes.Entry[] pathEntries;

    IndexEntry(SpiRoutes.Entry[] pathEntries) {
      this.pathEntries = pathEntries;
    }

    @Override
    public String toString() {
      return Arrays.toString(pathEntries);
    }

    SpiRoutes.Entry match(String pathInfo) {
      for (SpiRoutes.Entry entry : pathEntries) {
        if (entry.matches(pathInfo)) {
          return entry;
        }
      }
      return null;
    }

    long activeRequests() {
      long total = 0;
      for (SpiRoutes.Entry entry : pathEntries) {
        total += entry.activeRequests();
      }
      return total;
    }
  }
}
