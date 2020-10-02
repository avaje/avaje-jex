package io.avaje.jex.routes;

import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * Helper for PathParser to build regex for the path.
 */
class RegBuilder {
  private final StringJoiner full = new StringJoiner("/");
  private final StringJoiner extract = new StringJoiner("/");
  private boolean trailingSlash;

  String add(PathSegment pathSegment) {
    full.add(pathSegment.asRegexString(false));
    extract.add(pathSegment.asRegexString(true));
    return pathSegment.paramName();
  }

  void trailingSlash() {
    trailingSlash = true;
  }

  /**
   * Return the regex for path matching.
   */
  Pattern matchRegex() {
    return Pattern.compile(wrap(full.toString()));
  }

  /**
   * Return the regex used to extract path parameters.
   */
  Pattern extractRegex() {
    return Pattern.compile(wrap(extract.toString()));
  }

  private String wrap(String parts) {
    if (trailingSlash) {
      parts += "\\/";
    }
    return "^/" + parts + "/?$";
  }

}
