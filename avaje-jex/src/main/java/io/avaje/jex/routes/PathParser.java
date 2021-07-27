package io.avaje.jex.routes;

import io.avaje.jex.spi.SpiRoutes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PathParser {

  private final String rawPath;
  private final List<String> paramNames = new ArrayList<>();
  private final Pattern matchRegex;
  private final Pattern pathParamRegex;
  private final boolean multiSlash;
  private int segmentCount;

  PathParser(String path, boolean ignoreTrailingSlashes) {
    this.rawPath = path;
    final RegBuilder regBuilder = new RegBuilder();
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
  }

  public boolean matches(String url) {
    return matchRegex.matcher(url).matches();
  }

  public SpiRoutes.Params extractPathParams(String uri) {
    Map<String, String> pathMap = new LinkedHashMap<>();
    final List<String> values = values(uri);
    for (int i = 0; i < values.size(); i++) {
      final String name = paramNames.get(i);
      if (name != null) {
        // null names for wildcard placeholders
        pathMap.put(name, UrlDecode.decode(values.get(i)));
      }
    }
    return new SpiRoutes.Params(pathMap);
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
  public String raw() {
    return rawPath;
  }

  /**
   * Return the number of path segments.
   */
  public int getSegmentCount() {
    return segmentCount;
  }

  /**
   * Return true if one of the segments is wildcard or slash accepting.
   */
  public boolean multiSlash() {
    return multiSlash;
  }
}
