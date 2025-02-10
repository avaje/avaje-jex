package io.avaje.jex.routes;

import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * Helper for PathParser to build regex for the path.
 */
final class RegBuilder {
  private final StringJoiner full = new StringJoiner("/");
  private final StringJoiner extract = new StringJoiner("/");
  private final boolean ignoreTrailingSlashes;
  private boolean trailingSlash;
  private boolean multiSlash;
  private boolean literal = true;

  public RegBuilder(boolean ignoreTrailingSlashes) {
    this.ignoreTrailingSlashes = ignoreTrailingSlashes;
  }

  void add(PathSegment pathSegment, List<String> paramNames) {
    full.add(pathSegment.asRegexString(false));
    extract.add(pathSegment.asRegexString(true));
    pathSegment.addParamName(paramNames);
    if (!multiSlash) {
      multiSlash = pathSegment.multiSlash();
    }
    if (literal && !pathSegment.literal()) {
      literal = false;
    }
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

    if (!ignoreTrailingSlashes) {
      return "^/" + parts;
    }

    return "^/" + parts + "/?$";
  }

  /**
   * Return true if any of the segments consume unknown number of slashes.
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
