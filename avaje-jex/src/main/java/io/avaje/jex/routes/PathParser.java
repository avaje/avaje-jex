package io.avaje.jex.routes;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PathParser {

  private final String rawPath;
  private final List<String> paramNames = new ArrayList<>();
  private final Pattern matchRegex;
  private final Pattern pathParamRegex;
  private final boolean multiSlash;
  private final boolean literal;
  private int segmentCount;

  PathParser(String path, boolean ignoreTrailingSlashes) {
    this.rawPath = path;
    final RegBuilder regBuilder = new RegBuilder(ignoreTrailingSlashes);
    for (String rawSeg : path.split("/")) {
      if (!rawSeg.isEmpty()) {
        segmentCount++;
        regBuilder.add(parseSegment(rawSeg), paramNames);
      }
    }
    if (!ignoreTrailingSlashes && path.endsWith("/")) {
      segmentCount++;
      regBuilder.trailingSlash();
    }
    this.matchRegex = regBuilder.matchRegex();
    this.pathParamRegex = regBuilder.extractRegex();
    this.multiSlash = regBuilder.multiSlash();
    this.literal = segmentCount > 1 && regBuilder.literal();
  }

  boolean matches(String url) {
    return matchRegex.matcher(url).matches();
  }

  Map<String, String> extractPathParams(String uri) {
    Map<String, String> pathMap = new LinkedHashMap<>();
    final List<String> values = values(uri);
    for (int i = 0; i < values.size(); i++) {
      final String name = paramNames.get(i);
      if (name != null) {
        // null names for wildcard placeholders
        pathMap.put(name, UrlDecode.decode(values.get(i)));
      }
    }
    return pathMap;
  }

  private List<String> values(String uri) {
    final Matcher matcher = pathParamRegex.matcher(uri);
    if (!matcher.find()) {
      return Collections.emptyList();
    }
    final int i = matcher.groupCount();
    final List<String> values = new ArrayList<>(i);
    for (int j = 1; j <= i; j++) {
      values.add(matcher.group(j));
    }
    return values;
  }

  private PathSegment parseSegment(String segment) {
    return PathSegmentParser.parse(segment, rawPath);
  }

  /**
   * Return the raw path that was parsed (match path).
   */
  String raw() {
    return rawPath;
  }

  /**
   * Return the number of path segments.
   */
  int segmentCount() {
    return segmentCount;
  }

  /**
   * Return true if one of the segments is wildcard or slash accepting.
   */
  boolean multiSlash() {
    return multiSlash;
  }

  /**
   * Return true if all path segments are literal.
   */
  boolean literal() {
    return literal;
  }
}
