package io.avaje.jex.routes;

abstract class PathSegment {

  abstract String asRegexString();

  public String paramName() {
    return null;
  }

  static class Parameter extends PathSegment {
    private final String name;

    public Parameter(String name) {
      this.name = name;
    }

    @Override
    public String asRegexString() {
      return "[^/]+?"; // Accepting everything except slash;
    }

    @Override
    public String paramName() {
      return name;
    }
  }

  static class Literal extends PathSegment {
    private final String content;

    public Literal(String content) {
      this.content = content;
    }

    @Override
    public String asRegexString() {
      return content;
    }
  }

  static class Wildcard extends PathSegment {

    @Override
    public String asRegexString() {
      return ".*?"; // Accept everything
    }
  }

}
