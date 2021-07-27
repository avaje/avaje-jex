package io.avaje.jex.routes;

import java.util.List;

abstract class PathSegment {

  abstract String asRegexString(boolean extract);

  abstract void addParamName(List<String> paramNames);

  static class SlashIgnoringParameter extends Parameter {
    SlashIgnoringParameter(String param) {
      super(param, "[^/]+?"); // Accepting everything except slash;);
    }
  }

  static class SlashAcceptingParameter extends Parameter {
    SlashAcceptingParameter(String param) {
      super(param, ".+?"); // Accept everything
    }
  }

  private static class Parameter extends PathSegment {
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

  static class Literal extends PathSegment {
    private final String content;

    Literal(String content) {
      this.content = content;
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
    public String asRegexString(boolean extract) {
      return extract ? "(.*?)" : ".*?"; // Accept everything
    }

    @Override
    public void addParamName(List<String> paramNames) {
      paramNames.add(null); // null for splat
    }
  }

}
