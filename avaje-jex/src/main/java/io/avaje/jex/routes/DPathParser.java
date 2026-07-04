package io.avaje.jex.routes;

import io.avaje.jex.PathParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DPathParser extends PathParser {

  private final String rawPath;
  private final List<String> paramNames = new ArrayList<>();
  private final Pattern matchRegex;
  private final Pattern pathParamRegex;
  private final boolean multiSlash;
  private final boolean literal;
  private final boolean ignoreTrailingSlashes;
  private int segmentCount;

  public DPathParser(String path, boolean ignoreTrailingSlashes) {
    this.rawPath = path;
    this.ignoreTrailingSlashes = ignoreTrailingSlashes;
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

  @Override
  public boolean matches(String url) {
    if (literal) {
      int pathLen = rawPath.length();
      int urlLen = url.length();
      if (urlLen == pathLen) {
        return url.equals(rawPath);
      }
      if (ignoreTrailingSlashes && urlLen == pathLen + 1 && url.charAt(urlLen - 1) == '/') {
        return url.regionMatches(0, rawPath, 0, pathLen);
      }
      return false;
    }
    return matchRegex.matcher(url).matches();
  }


  @Override
  public Map<String, String> extractPathParams(String uri) {
    final Matcher matcher = pathParamRegex.matcher(uri);
    if (!matcher.find()) {
      return Map.of();
    }
    final int count = matcher.groupCount();
    final Map<String, String> pathMap = LinkedHashMap.newLinkedHashMap(count);
    for (int i = 1; i <= count; i++) {
      final String name = paramNames.get(i - 1);
      if (name != null) {
        // null names for wildcard placeholders
        pathMap.put(name, UrlDecode.decodeRFC3986(matcher.group(i)));
      }
    }
    return pathMap;
  }

  private PathSegment parseSegment(String segment) {
    return PathSegmentParser.parse(segment, rawPath);
  }

  /**
   * Return the raw path that was parsed (match path).
   */
  @Override
  public String raw() {
    return rawPath;
  }

  /**
   * Return the number of path segments.
   */
  @Override
  public int segmentCount() {
    return segmentCount;
  }

  /**
   * Return true if one of the segments is wildcard or slash accepting.
   */
  @Override
  public boolean multiSlash() {
    return multiSlash;
  }

  /**
   * Return true if all path segments are literal.
   */
  @Override
  public boolean literal() {
    return literal;
  }
}
