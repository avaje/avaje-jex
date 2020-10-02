package io.avaje.jex.routes;

abstract class PathSegment {

  abstract String asRegexString(boolean extract);

  String paramName() {
    return null;
  }

  static class Parameter extends PathSegment {
    private final String name;
    private final String regex;

    Parameter(String param) {
      final String[] split = param.split(":", 2);
      this.name = split[0];
      if (split.length == 1) {
        this.regex = "[^/]+?"; // Accepting everything except slash;
      } else {
        this.regex = split[1];
      }
    }

    @Override
    public String asRegexString(boolean extract) {
      return extract ? "(" + regex + ")" : regex;
    }

    @Override
    public String paramName() {
      return name;
    }
  }

  static class Literal extends PathSegment {
    private final String content;

    Literal(String content) {
      this.content = content;
    }

    @Override
    public String asRegexString(boolean extract) {
      return content;
    }
  }

  static class Wildcard extends PathSegment {

    @Override
    public String asRegexString(boolean extract) {
      return ".*?"; // Accept everything
    }
  }

}
