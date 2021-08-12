package io.avaje.jex.routes;

import java.util.List;

import static java.util.stream.Collectors.joining;

abstract class PathSegment {

  abstract String asRegexString(boolean extract);

  abstract void addParamName(List<String> paramNames);

  boolean literal() {
    return false;
  }

  boolean multiSlash() {
    return false;
  }

  static class SlashIgnoringParameter extends Parameter {
    SlashIgnoringParameter(String param) {
      super(param, "[^/]+?"); // Accepting everything except slash;);
    }
  }

  static class SlashAcceptingParameter extends Parameter {
    SlashAcceptingParameter(String param) {
      super(param, ".+?"); // Accept everything
    }

    @Override
    boolean multiSlash() {
      return true;
    }
  }

  private abstract static class Parameter extends PathSegment {
    private final String name;
    private final String regex;

    Parameter(String param, String acceptPattern) {
      final String[] split = param.split(":", 2);
      this.name = split[0];
      if (split.length == 1) {
        this.regex = acceptPattern;
      } else {
        this.regex = split[1];
      }
    }

    @Override
    public String asRegexString(boolean extract) {
      return extract ? "(" + regex + ")" : regex;
    }

    @Override
    public void addParamName(List<String> paramNames) {
      paramNames.add(name);
    }
  }

  static class Multi extends PathSegment {

    private final List<PathSegment> segments;

    Multi(List<PathSegment> segments) {
      this.segments = segments;
    }

    @Override
    boolean multiSlash() {
      for (PathSegment segment : segments) {
        if (segment.multiSlash()) {
          return true;
        }
      }
      return false;
    }

    @Override
    String asRegexString(boolean extract) {
      return segments.stream()
        .map(pathSegment -> pathSegment.asRegexString(extract))
        .collect(joining());
    }

    @Override
    void addParamName(List<String> paramNames) {
      for (PathSegment segment : segments) {
        segment.addParamName(paramNames);
      }
    }
  }

  static class Literal extends PathSegment {
    private final String content;

    Literal(String content) {
      this.content = content;
    }

    @Override
    boolean literal() {
      return true;
    }

    @Override
    public String asRegexString(boolean extract) {
      return content;
    }

    @Override
    public void addParamName(List<String> paramNames) {
      // do nothing for literal
    }
  }

  static class Wildcard extends PathSegment {

    @Override
    boolean multiSlash() {
      return true;
    }

    @Override
    public String asRegexString(boolean extract) {
      return extract ? "(.*?)" : ".*?"; // Accept everything
    }

    @Override
    public void addParamName(List<String> paramNames) {
      paramNames.add(null); // null for wildcard
    }
  }

}
