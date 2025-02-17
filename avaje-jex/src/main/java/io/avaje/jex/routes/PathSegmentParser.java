package io.avaje.jex.routes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

final class PathSegmentParser {

  private static final PathSegment WILDCARD = new PathSegment.Wildcard();

  private static final String[] ADJACENT_VIOLATIONS = {"*{", "*<", "}*", ">*"};

  private static final String NAME_STR = "[\\w-]+";
  private static final String NAME_OPT = "(:[^/^{]+([{]\\d+})?)?"; // Optional regex
  private static final String SLASH_STR = "<" + NAME_STR + ">";
  private static final String PARAM_STR = "[{]" + NAME_STR + NAME_OPT + "}";
  private static final String MULTI_STR = "(" + PARAM_STR + "|" + SLASH_STR + "|[*]|" + "[^{</*]+)";

  private static final Pattern MATCH_PARAM = Pattern.compile(PARAM_STR);
  private static final Pattern MATCH_MULTI = Pattern.compile(MULTI_STR);

  private final String segment;
  private final String rawPath;

  PathSegmentParser(String segment, String rawPath) {
    this.segment = segment;
    this.rawPath = rawPath;
  }

  static PathSegment parse(String seg, String rawPath) {
    if ("*".equals(seg)) {
      return WILDCARD;
    }
    return new PathSegmentParser(seg, rawPath).parse();
  }

  PathSegment parse() {
    checkAdjacentViolations();
    if (matchOnlyStartEnd('<', '>')) {
      return new PathSegment.SlashAcceptingParameter(trim(segment));
    }
    if (matchOnlyStartEnd('{', '}') || matchParamWithRegex(segment)) {
      return new PathSegment.SlashIgnoringParameter(trim(segment));
    }
    if (matchLiteral(segment)) {
      return new PathSegment.Literal(segment);
    }
    return parseMultiSegment();
  }

  private PathSegment parseMultiSegment() {
    final List<PathSegment> segments = new ArrayList<>();
    final List<String> tokens = multi(segment);

    int position = 0;
    StringBuilder appendedTokens = new StringBuilder(segment.length());
    for (String token : tokens) {
      appendedTokens.append(token);
      if (!segment.startsWith(appendedTokens.toString())) {
        throw new IllegalArgumentException(
            "Path ["
                + rawPath
                + "] has illegal segment ["
                + segment
                + "] starting at position ["
                + position
                + "]");
      }
      position += token.length();
      segments.add(tokenSegment(token));
    }
    return new PathSegment.Multi(segments);
  }

  private PathSegment tokenSegment(String token) {
    if ("*".equals(token)) {
      return WILDCARD;
    } else if (token.startsWith("<")) {
      return slashAccepting(token);
    } else if (token.startsWith("{")) {
      return slashIgnoring(token);
    } else {
      return new PathSegment.Literal(token);
    }
  }

  private PathSegment slashIgnoring(String token) {
    return new PathSegment.SlashIgnoringParameter(trim(token));
  }

  private PathSegment slashAccepting(String token) {
    return new PathSegment.SlashAcceptingParameter(trim(token));
  }

  static boolean matchParamWithRegex(String input) {
    return MATCH_PARAM.matcher(input).matches();
  }

  static List<String> multi(String input) {
    return MATCH_MULTI.matcher(input).results().map(MatchResult::group).toList();
  }

  static boolean matchLiteral(String segment) {
    return segment.indexOf('<') == -1
        && segment.indexOf('{') == -1
        && segment.indexOf('>') == -1
        && segment.indexOf('}') == -1;
  }

  private void checkAdjacentViolations() {
    for (String adjacentViolation : ADJACENT_VIOLATIONS) {
      if (segment.contains(adjacentViolation)) {
        throw new IllegalArgumentException(
            "Path ["
                + rawPath
                + "] has illegal segment ["
                + segment
                + "] that contains ["
                + adjacentViolation
                + "]");
      }
    }
  }

  private String trim(String token) {
    return token.substring(1, token.length() - 1);
  }

  boolean matchOnlyStartEnd(char startChar, char endChar) {
    return startCharOnly(startChar) && endCharOnly(endChar);
  }

  boolean startCharOnly(char c) {
    // first char matches and no subsequent matching char
    return segment.charAt(0) == c && segment.indexOf(c, 1) == -1;
  }

  boolean endCharOnly(char c) {
    // last char matches and no prior matching char
    return segment.indexOf(c) == segment.length() - 1;
  }
}
