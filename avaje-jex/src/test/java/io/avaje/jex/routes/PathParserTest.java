package io.avaje.jex.routes;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.junit.jupiter.api.Test;

class PathParserTest {

  @Test
  void matches_trailingSlash_honor() {

    var pathParser = new PathParser("/one/{id}/", false);
    assertThat(pathParser.segmentCount()).isEqualTo(3);

    assertTrue(pathParser.matches("/one/1/"));
    assertTrue(pathParser.matches("/one/2/"));
    assertFalse(pathParser.matches("/one/3//")); // accepts trailing double slash?
    assertFalse(pathParser.matches("/one/3///")); // but not triple slash?
    assertFalse(pathParser.matches("/one/1"));
    assertFalse(pathParser.matches("/one/2"));
  }

  @Test
  void matches_trailingSlash_ignore() {

    var pathParser = new PathParser("/one/{id}///", true);
    assertTrue(pathParser.matches("/one/1"));
    assertTrue(pathParser.matches("/one/2"));
    assertTrue(pathParser.matches("/one/2/"));
    assertThat(pathParser.segmentCount()).isEqualTo(2);
  }

  @Test
  void matches_litArg() {

    var pathParser = new PathParser("/one/{id}", true);
    assertTrue(pathParser.matches("/one/1"));
    assertTrue(pathParser.matches("/one/2"));
    assertThat(pathParser.segmentCount()).isEqualTo(2);
    assertThat(pathParser.raw()).isEqualTo("/one/{id}");

    Map<String, String> pathParams = pathParser.extractPathParams("/one/1");
    assertThat(pathParams.get("id")).isEqualTo("1");

    pathParams = pathParser.extractPathParams("/one/next");
    assertThat(pathParams.get("id")).isEqualTo("next");
  }

  @Test
  void matches_argArgArg() {

    final PathParser pathParser = new PathParser("/{a}/{b}/{c}", true);
    assertTrue(pathParser.matches("/1a/2b/3c"));
    assertThat(pathParser.segmentCount()).isEqualTo(3);
    assertThat(pathParser.raw()).isEqualTo("/{a}/{b}/{c}");

    Map<String, String> pathParams = pathParser.extractPathParams("/1a/2b/3c");
    assertThat(pathParams.get("a")).isEqualTo("1a");
    assertThat(pathParams).containsOnlyKeys("a", "b", "c");
    assertThat(pathParams).containsEntry("a", "1a");
    assertThat(pathParams).containsEntry("b", "2b");
    assertThat(pathParams).containsEntry("c", "3c");
  }

  @Test
  void matches_litArgArgArg() {

    final PathParser pathParser = new PathParser("/one/{a}/{b}/{c}", true);
    assertThat(pathParser.segmentCount()).isEqualTo(4);
    assertTrue(pathParser.matches("/one/1a/2b/3c"));
    assertTrue(pathParser.matches("/one/foo/bar/baz"));

    Map<String, String> pathParams = pathParser.extractPathParams("/one/1a/2b/3c");
    assertThat(pathParams.get("a")).isEqualTo("1a");
    assertThat(pathParams).containsOnlyKeys("a", "b", "c");
    assertThat(pathParams).containsEntry("a", "1a");
    assertThat(pathParams).containsEntry("b", "2b");
    assertThat(pathParams).containsEntry("c", "3c");
  }

  @Test
  void illegalPath_adjacentViolation() {
    asList("/one/*<a>/after", "*{", "}*", "*<", ">*")
        .forEach(
            path -> assertThrows(IllegalArgumentException.class, () -> new PathParser(path, true)));
  }

  @Test
  void matches_withSlashAccepting() {

    final PathParser pathParser = new PathParser("/one/<a>/after", true);
    assertThat(pathParser.segmentCount()).isEqualTo(3);
    assertTrue(pathParser.matches("/one/bazz/after"));
    assertTrue(pathParser.matches("/one/foo/bar/after"));

    Map<String, String> pathParams = pathParser.extractPathParams("/one/foo/bar/after");
    assertThat(pathParams.get("a")).isEqualTo("foo/bar");
    assertThat(pathParams).containsOnlyKeys("a");

    pathParams = pathParser.extractPathParams("/one/bazz/after");
    assertThat(pathParams.get("a")).isEqualTo("bazz");
    assertThat(pathParams).containsOnlyKeys("a");
  }

  @Test
  void matches_litArgLitArgArgLit() {

    final PathParser pathParser = new PathParser("/one/{a}/two/{b}/{c}/end", true);
    assertThat(pathParser.segmentCount()).isEqualTo(6);
    assertTrue(pathParser.matches("/one/1a/two/2b/3c/end"));
    assertFalse(pathParser.matches("/on/1a/two/2b/3c/end"));
    assertFalse(pathParser.matches("/one/1a/tw/2b/3c/end"));
    assertFalse(pathParser.matches("/one/1a/two/2b/3c/en"));
    assertFalse(pathParser.matches("/one/1a/two/2b/3c/end/extra"));
    assertFalse(pathParser.matches("extra/one/1a/two/2b/3c/end"));

    Map<String, String> pathParams = pathParser.extractPathParams("/one/1a/two/2b/3c/end");
    assertThat(pathParams).containsOnlyKeys("a", "b", "c");
    assertThat(pathParams).containsEntry("a", "1a");
    assertThat(pathParams).containsEntry("b", "2b");
    assertThat(pathParams).containsEntry("c", "3c");
  }

  @Test
  void matches_litLit() {

    final PathParser pathParser = new PathParser("/one/two", true);
    assertTrue(pathParser.matches("/one/two"));
    assertTrue(pathParser.matches("/one/two/"));

    assertFalse(pathParser.matches("/one/2"));
    assertFalse(pathParser.matches("/one/"));
    assertFalse(pathParser.matches("/one"));
    assertFalse(pathParser.matches("/one/two/more"));
  }

  @Test
  void matches_before_litPrefix() {
    final PathParser pathParser = new PathParser("/one/*", true);
    assertTrue(pathParser.matches("/one/two"));
    assertTrue(pathParser.matches("/one/two/three"));
    assertTrue(pathParser.matches("/one/two/three/four"));
  }

  @Test
  void matches_before_litPrefixAndSuffix() {
    final PathParser pathParser = new PathParser("/one/*/three", true);
    assertTrue(pathParser.matches("/one/two/three"));
    assertTrue(pathParser.matches("/one/foo/three"));

    assertFalse(pathParser.matches("/one/two"));
    assertFalse(pathParser.matches("/one/two/three/four"));
  }

  @Test
  void matches_before_litPrefixAndSuffixAndWild() {
    final PathParser pathParser = new PathParser("/one/*/three/*", true);
    assertTrue(pathParser.matches("/one/99/three/1000"));
    assertTrue(pathParser.matches("/one/99/three/1000/banana"));
    assertTrue(pathParser.matches("/one/two/three/four"));
    assertTrue(pathParser.matches("/one/42/three/"));

    assertFalse(pathParser.matches("/one/42/three"));
    assertFalse(pathParser.matches("/one/two"));
  }

  @Test
  void withRegex() {

    final PathParser pathParser = new PathParser("/{id:[0-9]+}", true);
    assertTrue(pathParser.matches("/1"));
    assertTrue(pathParser.matches("/99"));

    assertFalse(pathParser.matches("/a"));
    assertFalse(pathParser.matches("/foo"));
  }

  @Test
  void withRegex_andPrefix() {

    final PathParser pathParser = new PathParser("/one/{id:[0-9]+}", true);
    assertTrue(pathParser.matches("/one/1"));
    assertTrue(pathParser.matches("/one/99"));

    assertFalse(pathParser.matches("/one/a"));
    assertFalse(pathParser.matches("/one/foo"));
  }

  @Test
  void withRegexWithLength() {

    final PathParser pathParser = new PathParser("/{id:[0-9]{4}}", true);
    assertTrue(pathParser.matches("/1234"));
    assertTrue(pathParser.matches("/9987"));

    assertFalse(pathParser.matches("/1"));
    assertFalse(pathParser.matches("/12"));
    assertFalse(pathParser.matches("/123"));
    assertFalse(pathParser.matches("/12345"));
    assertFalse(pathParser.matches("/a"));
    assertFalse(pathParser.matches("/foo"));
  }

  @Test
  void withColonLiteral() {
    final PathParser pathParser = new PathParser("/path/my:action", true);
    assertThat(pathParser.segmentCount()).isEqualTo(2);
    assertThat(pathParser.literal()).isTrue();
  }

  @Test
  void withColonLiteral2() {
    final PathParser pathParser = new PathParser("/path/to/my:action", true);
    assertThat(pathParser.segmentCount()).isEqualTo(3);
    assertThat(pathParser.literal()).isTrue();
  }

  @Test
  void withColonLiteral3() {
    final PathParser pathParser = new PathParser("/path/my::action", true);
    assertThat(pathParser.segmentCount()).isEqualTo(2);
    assertThat(pathParser.literal()).isTrue();
  }

  @Test
  void withColonLiteral4() {
    final PathParser pathParser = new PathParser("/path/my::action:again", true);
    assertThat(pathParser.segmentCount()).isEqualTo(2);
    assertThat(pathParser.literal()).isTrue();
  }

  @Test
  void matches_splat0() {

    final PathParser pathParser = new PathParser("/{a}/*", true);
    assertTrue(pathParser.matches("/1a/2b/3c"));
    assertThat(pathParser.segmentCount()).isEqualTo(2);
    assertThat(pathParser.raw()).isEqualTo("/{a}/*");

    final Map<String, String> params = pathParser.extractPathParams("/1a/2b/3c");
    assertThat(params.get("a")).isEqualTo("1a");
    assertThat(params).containsOnlyKeys("a");
    assertThat(params).containsEntry("a", "1a");
  }

  @Test
  void matches_splat0LiteralSplat() {

    final PathParser pathParser = new PathParser("/{a}/*/and/*", true);
    assertThat(pathParser.raw()).isEqualTo("/{a}/*/and/*");
    assertThat(pathParser.segmentCount()).isEqualTo(4);

    assertTrue(pathParser.matches("/1/2/and/3"));
    assertFalse(pathParser.matches("/1/2/3/4"));
    assertTrue(pathParser.matches("/1/a/b/c/d/and/f/g/h/i"));

    Map<String, String> params = pathParser.extractPathParams("/1a/2b/and/3c");
    assertThat(params.get("a")).isEqualTo("1a");
    assertThat(params).containsOnlyKeys("a");

    params = pathParser.extractPathParams("/1/a/b/c/d/and/f/g/h/i");
    assertThat(params.get("a")).isEqualTo("1");
    assertThat(params).containsOnlyKeys("a");
  }

  @Test
  void matches_slashConsumers() {
    final PathParser pathParser = new PathParser("/{a}/<one>/and/<two>", true);
    assertThat(pathParser.raw()).isEqualTo("/{a}/<one>/and/<two>");
    assertThat(pathParser.segmentCount()).isEqualTo(4);

    assertTrue(pathParser.matches("/1/2/and/3"));
    assertFalse(pathParser.matches("/1/2/3/4"));
    assertTrue(pathParser.matches("/1/a/b/c/d/and/f/g/h/i"));

    Map<String, String> params = pathParser.extractPathParams("/1a/2/b/and/3c/more/here");
    assertThat(params.get("a")).isEqualTo("1a");
    assertThat(params.get("one")).isEqualTo("2/b");
    assertThat(params.get("two")).isEqualTo("3c/more/here");
    assertThat(params).containsOnlyKeys("a", "one", "two");

    params = pathParser.extractPathParams("/1/a/b/c/d/and/f/g/h/i");
    assertThat(params.get("a")).isEqualTo("1");
    assertThat(params.get("one")).isEqualTo("a/b/c/d");
    assertThat(params.get("two")).isEqualTo("f/g/h/i");
  }

  @Test
  void multiSegment_noSlashes() {
    final PathParser pathParser = new PathParser("/x{a}y{b}z", true);
    assertThat(pathParser.raw()).isEqualTo("/x{a}y{b}z");
    assertThat(pathParser.segmentCount()).isEqualTo(1);

    assertTrue(pathParser.matches("/xAyBz"));
    assertTrue(pathParser.matches("/xHELLOyTHEREz"));
    assertTrue(pathParser.matches("/xAAAAyBBBBz"));

    assertFalse(pathParser.matches("/AAAAyBBBBz"));
    assertFalse(pathParser.matches("/xAAAABBBBz"));
    assertFalse(pathParser.matches("/xAAAAyBBBB"));

    final Map<String, String> params = pathParser.extractPathParams("/xHELLOyTHEREz");
    assertThat(params.get("a")).isEqualTo("HELLO");
    assertThat(params.get("b")).isEqualTo("THERE");
    assertThat(params).containsOnlyKeys("a", "b");
  }

  @Test
  void multiSegment_mixed() {
    final PathParser pathParser = new PathParser("/{one}/x{two}y{three}z/{four}", true);
    assertThat(pathParser.segmentCount()).isEqualTo(3);
    assertFalse(pathParser.multiSlash());

    assertTrue(pathParser.matches("/0/x1y2z/3"));

    final Map<String, String> params = pathParser.extractPathParams("/0/x1y2z/3");
    assertThat(params.get("one")).isEqualTo("0");
    assertThat(params.get("two")).isEqualTo("1");
    assertThat(params.get("three")).isEqualTo("2");
    assertThat(params.get("four")).isEqualTo("3");
    assertThat(params).containsOnlyKeys("one", "two", "three", "four");
  }

  @Test
  void multiSegment_mixed_slashConsuming() {
    final PathParser pathParser = new PathParser("/<one>/x<two>y<three>z/<four>", true);
    assertThat(pathParser.segmentCount()).isEqualTo(3);
    assertTrue(pathParser.multiSlash());

    assertTrue(pathParser.matches("/0/x1y2z/3"));
    assertTrue(pathParser.matches("/0/SLASH0/x1/SLASH1y2/SLASH2z/3/SLASH/SLASH"));

    Map<String, String> params = pathParser.extractPathParams("/0/x1y2z/3");
    assertThat(params.get("one")).isEqualTo("0");
    assertThat(params.get("two")).isEqualTo("1");
    assertThat(params.get("three")).isEqualTo("2");
    assertThat(params.get("four")).isEqualTo("3");
    assertThat(params).containsOnlyKeys("one", "two", "three", "four");

    params = pathParser.extractPathParams("/0/SLASH0/x1/SLASH1y2/SLASH2z/3/SLASH/SLASH");
    assertThat(params.get("one")).isEqualTo("0/SLASH0");
    assertThat(params.get("two")).isEqualTo("1/SLASH1");
    assertThat(params.get("three")).isEqualTo("2/SLASH2");
    assertThat(params.get("four")).isEqualTo("3/SLASH/SLASH");
  }

  @Test
  void matchMulti_when_illegalSegments_expect_IllegalArgumentException() {

    expectParseError("some/a-<foo<bar>>-b")
        .withMessage(
            "Path [some/a-<foo<bar>>-b] has illegal segment [a-<foo<bar>>-b] starting at position [2]");

    expectParseError("some/before/more-{foo{bar}}-b/after")
        .withMessage(
            "Path [some/before/more-{foo{bar}}-b/after] has illegal segment [more-{foo{bar}}-b] starting at position [5]");
  }

  private ThrowableAssertAlternative<IllegalArgumentException> expectParseError(String path) {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new PathParser(path, true));
  }
}
