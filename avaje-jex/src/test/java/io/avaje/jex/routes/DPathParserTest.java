package io.avaje.jex.routes;


import org.assertj.core.api.ThrowableAssertAlternative;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DPathParserTest {

  @Test
  void matches_trailingSlash_honor() {

    var pathParser = new DPathParser("/one/{id}/", false);
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

    var pathParser = new DPathParser("/one/{id}///", true);
    assertTrue(pathParser.matches("/one/1"));
    assertTrue(pathParser.matches("/one/2"));
    assertTrue(pathParser.matches("/one/2/"));
    assertThat(pathParser.segmentCount()).isEqualTo(2);
  }

  @Test
  void matches_litArg() {

    var pathParser = new DPathParser("/one/{id}", true);
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

    final DPathParser pathParser = new DPathParser("/{a}/{b}/{c}", true);
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

    final DPathParser pathParser = new DPathParser("/one/{a}/{b}/{c}", true);
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
      .forEach(path -> assertThrows(IllegalArgumentException.class, () -> new DPathParser(path, true)));
  }

  @Test
  void matches_withSlashAccepting() {

    final DPathParser pathParser = new DPathParser("/one/<a>/after", true);
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

    final DPathParser pathParser = new DPathParser("/one/{a}/two/{b}/{c}/end", true);
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

    final DPathParser pathParser = new DPathParser("/one/two", true);
    assertTrue(pathParser.matches("/one/two"));
    assertTrue(pathParser.matches("/one/two/"));

    assertFalse(pathParser.matches("/one/2"));
    assertFalse(pathParser.matches("/one/"));
    assertFalse(pathParser.matches("/one"));
    assertFalse(pathParser.matches("/one/two/more"));
  }

  @Test
  void matches_litLit_honorTrailingSlash() {
    // ignoreTrailingSlashes=false: trailing slash must NOT match
    final DPathParser pathParser = new DPathParser("/one/two", false);
    assertTrue(pathParser.literal());

    assertTrue(pathParser.matches("/one/two"));
    assertFalse(pathParser.matches("/one/two/"));   // trailing slash rejected
    assertFalse(pathParser.matches("/one/2"));
    assertFalse(pathParser.matches("/one/two/more"));
    assertFalse(pathParser.matches("/one"));
  }

  @Test
  void matches_litLitLit_honorTrailingSlash() {
    final DPathParser pathParser = new DPathParser("/a/b/c", false);
    assertTrue(pathParser.literal());

    assertTrue(pathParser.matches("/a/b/c"));
    assertFalse(pathParser.matches("/a/b/c/"));
    assertFalse(pathParser.matches("/a/b"));
    assertFalse(pathParser.matches("/a/b/cd"));
  }

  @Test
  void matches_before_litPrefix() {
    final DPathParser pathParser = new DPathParser("/one/*", true);
    assertTrue(pathParser.matches("/one/two"));
    assertTrue(pathParser.matches("/one/two/three"));
    assertTrue(pathParser.matches("/one/two/three/four"));
  }

  @Test
  void matches_before_litPrefixAndSuffix() {
    final DPathParser pathParser = new DPathParser("/one/*/three", true);
    assertTrue(pathParser.matches("/one/two/three"));
    assertTrue(pathParser.matches("/one/foo/three"));

    assertFalse(pathParser.matches("/one/two"));
    assertFalse(pathParser.matches("/one/two/three/four"));
  }

  @Test
  void matches_before_litPrefixAndSuffixAndWild() {
    final DPathParser pathParser = new DPathParser("/one/*/three/*", true);
    assertTrue(pathParser.matches("/one/99/three/1000"));
    assertTrue(pathParser.matches("/one/99/three/1000/banana"));
    assertTrue(pathParser.matches("/one/two/three/four"));
    assertTrue(pathParser.matches("/one/42/three/"));

    assertFalse(pathParser.matches("/one/42/three"));
    assertFalse(pathParser.matches("/one/two"));
  }

  @Test
  void withRegex() {

    final DPathParser pathParser = new DPathParser("/{id:[0-9]+}", true);
    assertTrue(pathParser.matches("/1"));
    assertTrue(pathParser.matches("/99"));

    assertFalse(pathParser.matches("/a"));
    assertFalse(pathParser.matches("/foo"));
  }

  @Test
  void withRegex_andPrefix() {

    final DPathParser pathParser = new DPathParser("/one/{id:[0-9]+}", true);
    assertTrue(pathParser.matches("/one/1"));
    assertTrue(pathParser.matches("/one/99"));

    assertFalse(pathParser.matches("/one/a"));
    assertFalse(pathParser.matches("/one/foo"));
  }

  @Test
  void withRegexWithLength() {

    final DPathParser pathParser = new DPathParser("/{id:[0-9]{4}}", true);
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
    final DPathParser pathParser = new DPathParser("/path/my:action", true);
    assertThat(pathParser.segmentCount()).isEqualTo(2);
    assertThat(pathParser.literal()).isTrue();
  }

  @Test
  void withColonLiteral2() {
    final DPathParser pathParser = new DPathParser("/path/to/my:action", true);
    assertThat(pathParser.segmentCount()).isEqualTo(3);
    assertThat(pathParser.literal()).isTrue();
  }

  @Test
  void withColonLiteral3() {
    final DPathParser pathParser = new DPathParser("/path/my::action", true);
    assertThat(pathParser.segmentCount()).isEqualTo(2);
    assertThat(pathParser.literal()).isTrue();
  }

  @Test
  void withColonLiteral4() {
    final DPathParser pathParser = new DPathParser("/path/my::action:again", true);
    assertThat(pathParser.segmentCount()).isEqualTo(2);
    assertThat(pathParser.literal()).isTrue();
  }

  @Test
  void matches_splat0() {

    final DPathParser pathParser = new DPathParser("/{a}/*", true);
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

    final DPathParser pathParser = new DPathParser("/{a}/*/and/*", true);
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
    final DPathParser pathParser = new DPathParser("/{a}/<one>/and/<two>", true);
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
    final DPathParser pathParser = new DPathParser("/x{a}y{b}z", true);
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
    final DPathParser pathParser = new DPathParser("/{one}/x{two}y{three}z/{four}", true);
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
    final DPathParser pathParser = new DPathParser("/<one>/x<two>y<three>z/<four>", true);
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
      .withMessage("Path [some/a-<foo<bar>>-b] has illegal segment [a-<foo<bar>>-b] starting at position [2]");

    expectParseError("some/before/more-{foo{bar}}-b/after")
      .withMessage("Path [some/before/more-{foo{bar}}-b/after] has illegal segment [more-{foo{bar}}-b] starting at position [5]");
  }

  private ThrowableAssertAlternative<IllegalArgumentException> expectParseError(String path) {
    return assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new DPathParser(path, true));
  }
}
