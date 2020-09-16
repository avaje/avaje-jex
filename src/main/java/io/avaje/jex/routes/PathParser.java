package io.avaje.jex.routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PathParser {

  private static final PathSegment.Wildcard wildcard = new PathSegment.Wildcard();

  //private final List<PathSegment> segments = new ArrayList<>();
  private final String rawPath;
  private final List<String> paramNames = new ArrayList<>();
  private final Pattern matchRegex;
  private final Pattern pathParamRegex;

  PathParser(String path) {
    this.rawPath = path;
    StringJoiner full = new StringJoiner("/");
    for (String rawSeg : path.split("/")) {
      if (!rawSeg.isEmpty()) {
        final PathSegment pathSegment = parseSegment(rawSeg);
        //segments.add(pathSegment);
        full.add(pathSegment.asRegexString());
        final String paramName = pathSegment.paramName();
        if (paramName != null) {
          paramNames.add(paramName);
        }
      }
    }

    final String rawRegex = "^/" + full.toString() + "/?$";
    this.matchRegex = Pattern.compile(rawRegex);
    this.pathParamRegex = Pattern.compile(rawRegex.replace("[^/]+?", "([^/]+?)"));
  }

  public boolean matches(String url) {
    return matchRegex.matcher(url).matches();
  }

  public Map<String, String> extractPathParams(String uri) {
    Map<String, String> pathMap = new LinkedHashMap<>();
    final List<String> values = values(uri);
    for (int i = 0; i < values.size(); i++) {
      pathMap.put(paramNames.get(i), Util.urlDecode(values.get(i)));
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


  private PathSegment parseSegment(String seg) {
    if (seg.startsWith("{")) {
      return new PathSegment.Parameter(seg.substring(1, seg.length() - 1));
    }
    if (seg.equals("*")) {
      return wildcard;
    }
    return new PathSegment.Literal(seg);
  }

  public String raw() {
    return rawPath;
  }
}
