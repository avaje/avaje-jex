package io.avaje.jex.routes;

class PathSegmentParser {
  private static final String[] ADJACENT_VIOLATIONS = {"*{", "*<", "}*", ">*"};

  private final String seg;
  private final String rawPath;

  PathSegmentParser(String seg, String rawPath) {
    this.seg = seg;
    this.rawPath = rawPath;
  }

  PathSegment parse() {
    checkAdjacentViolations();
    if (matchOnlyStartEnd('{', '}')) {
        return new PathSegment.SlashIgnoringParameter(seg.substring(1, seg.length() - 1));
    }
    if (matchOnlyStartEnd('<', '>')) {
      return new PathSegment.SlashIgnoringParameter(seg.substring(1, seg.length() - 1));
    }

    return new PathSegment.Literal(seg);
  }

  private void checkAdjacentViolations() {
    for (String adjacentViolation : ADJACENT_VIOLATIONS) {
      if (seg.contains(adjacentViolation)) {
        throw new IllegalArgumentException("Path [" + rawPath + "] has illegal segment [" + seg + "] that contains [" + adjacentViolation + "]");
      }
    }
  }

  boolean matchOnlyStartEnd(char startChar, char endChar) {
    return startCharOnly(startChar) && endCharOnly(endChar);
  }

  boolean startCharOnly(char c) {
    // first char matches and no subsequent matching char
    return seg.charAt(0) == c && seg.indexOf(c, 1) == -1;
  }

  boolean endCharOnly(char c) {
    // last char matches and no prior matching char
    return seg.indexOf(c) == seg.length() - 1;
  }

}
