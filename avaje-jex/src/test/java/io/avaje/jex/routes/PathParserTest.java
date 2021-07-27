package io.avaje.jex.routes;


import io.avaje.jex.spi.SpiRoutes;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PathParserTest {

  @Test
  void matches_trailingSlash_honor() {

    var pathParser = new PathParser("/one/{id}/", false);
    assertThat(pathParser.getSegmentCount()).isEqualTo(3);

    assertTrue(pathParser.matches("/one/1/"));
    assertTrue(pathParser.matches("/one/2/"));
    assertTrue(pathParser.matches("/one/3//")); // accepts trailing double slash?
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
    assertThat(pathParser.getSegmentCount()).isEqualTo(2);
  }

  @Test
  void matches_litArg() {

    var pathParser = new PathParser("/one/{id}", true);
    assertTrue(pathParser.matches("/one/1"));
    assertTrue(pathParser.matches("/one/2"));
    assertThat(pathParser.getSegmentCount()).isEqualTo(2);
    assertThat(pathParser.raw()).isEqualTo("/one/{id}");

    Map<String, String> pathParams = pathParser.extractPathParams("/one/1").pathParams;
    assertThat(pathParams.get("id")).isEqualTo("1");

    pathParams = pathParser.extractPathParams("/one/next").pathParams;
    assertThat(pathParams.get("id")).isEqualTo("next");
  }

  @Test
  void matches_argArgArg() {

    final PathParser pathParser = new PathParser("/{a}/{b}/{c}", true);
    assertTrue(pathParser.matches("/1a/2b/3c"));
    assertThat(pathParser.getSegmentCount()).isEqualTo(3);
    assertThat(pathParser.raw()).isEqualTo("/{a}/{b}/{c}");

    Map<String, String> pathParams = pathParser.extractPathParams("/1a/2b/3c").pathParams;
    assertThat(pathParams.get("a")).isEqualTo("1a");
    assertThat(pathParams).containsOnlyKeys("a", "b", "c");
    assertThat(pathParams).containsEntry("a", "1a");
    assertThat(pathParams).containsEntry("b", "2b");
    assertThat(pathParams).containsEntry("c", "3c");
  }

  @Test
  void matches_litArgArgArg() {

    final PathParser pathParser = new PathParser("/one/{a}/{b}/{c}", true);
    assertThat(pathParser.getSegmentCount()).isEqualTo(4);
    assertTrue(pathParser.matches("/one/1a/2b/3c"));
    assertTrue(pathParser.matches("/one/foo/bar/baz"));

    Map<String, String> pathParams = pathParser.extractPathParams("/one/1a/2b/3c").pathParams;
    assertThat(pathParams.get("a")).isEqualTo("1a");
    assertThat(pathParams).containsOnlyKeys("a", "b", "c");
    assertThat(pathParams).containsEntry("a", "1a");
    assertThat(pathParams).containsEntry("b", "2b");
    assertThat(pathParams).containsEntry("c", "3c");
  }

  @Test
  void illegalPath_adjacentViolation() {
    asList("/one/*<a>/after", "*{", "}*", "*<", ">*")
      .forEach(path -> assertThrows(IllegalArgumentException.class, () -> new PathParser(path, true)));
  }

  @Test
  void matches_withSlashAccepting() {

    final PathParser pathParser = new PathParser("/one/<a>/after", true);
    assertThat(pathParser.getSegmentCount()).isEqualTo(3);
    assertTrue(pathParser.matches("/one/bazz/after"));
    assertTrue(pathParser.matches("/one/foo/bar/after"));

    Map<String, String> pathParams = pathParser.extractPathParams("/one/foo/bar/after").pathParams;
    assertThat(pathParams.get("a")).isEqualTo("foo/bar");
    assertThat(pathParams).containsOnlyKeys("a");

    pathParams = pathParser.extractPathParams("/one/bazz/after").pathParams;
    assertThat(pathParams.get("a")).isEqualTo("bazz");
    assertThat(pathParams).containsOnlyKeys("a");
  }

  @Test
  void matches_litArgLitArgArgLit() {

    final PathParser pathParser = new PathParser("/one/{a}/two/{b}/{c}/end", true);
    assertThat(pathParser.getSegmentCount()).isEqualTo(6);
    assertTrue(pathParser.matches("/one/1a/two/2b/3c/end"));
    assertFalse(pathParser.matches("/on/1a/two/2b/3c/end"));
    assertFalse(pathParser.matches("/one/1a/tw/2b/3c/end"));
    assertFalse(pathParser.matches("/one/1a/two/2b/3c/en"));
    assertFalse(pathParser.matches("/one/1a/two/2b/3c/end/extra"));
    assertFalse(pathParser.matches("extra/one/1a/two/2b/3c/end"));

    Map<String, String> pathParams = pathParser.extractPathParams("/one/1a/two/2b/3c/end").pathParams;
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
  void matches_splat0() {

    final PathParser pathParser = new PathParser("/{a}/*", true);
    assertTrue(pathParser.matches("/1a/2b/3c"));
    assertThat(pathParser.getSegmentCount()).isEqualTo(2);
    assertThat(pathParser.raw()).isEqualTo("/{a}/*");

    final SpiRoutes.Params params = pathParser.extractPathParams("/1a/2b/3c");
    assertThat(params.pathParams.get("a")).isEqualTo("1a");
    assertThat(params.splats).containsOnly("2b/3c");

    assertThat(params.pathParams).containsOnlyKeys("a");
    assertThat(params.pathParams).containsEntry("a", "1a");
  }

  @Test
  void matches_splat0LiteralSplat() {

    final PathParser pathParser = new PathParser("/{a}/*/and/*", true);
    assertThat(pathParser.raw()).isEqualTo("/{a}/*/and/*");
    assertThat(pathParser.getSegmentCount()).isEqualTo(4);

    assertTrue(pathParser.matches("/1/2/and/3"));
    assertFalse(pathParser.matches("/1/2/3/4"));
    assertTrue(pathParser.matches("/1/a/b/c/d/and/f/g/h/i"));

    SpiRoutes.Params params = pathParser.extractPathParams("/1a/2b/and/3c");
    assertThat(params.pathParams.get("a")).isEqualTo("1a");
    assertThat(params.pathParams).containsOnlyKeys("a");

    assertThat(params.splats).containsOnly("2b", "3c");

    params = pathParser.extractPathParams("/1/a/b/c/d/and/f/g/h/i");
    assertThat(params.pathParams.get("a")).isEqualTo("1");
    assertThat(params.pathParams).containsOnlyKeys("a");

    assertThat(params.splats).containsOnly("a/b/c/d", "f/g/h/i");
  }

  @Test
  void multiSegment_noSlashes() {
    final PathParser pathParser = new PathParser("/x{a}y{b}z", true);
    assertThat(pathParser.raw()).isEqualTo("/x{a}y{b}z");
    assertThat(pathParser.getSegmentCount()).isEqualTo(1);

    assertTrue(pathParser.matches("/xAyBz"));
    assertTrue(pathParser.matches("/xHELLOyTHEREz"));
    assertTrue(pathParser.matches("/xAAAAyBBBBz"));

    assertFalse(pathParser.matches("/AAAAyBBBBz"));
    assertFalse(pathParser.matches("/xAAAABBBBz"));
    assertFalse(pathParser.matches("/xAAAAyBBBB"));

    final SpiRoutes.Params params = pathParser.extractPathParams("/xHELLOyTHEREz");
    assertThat(params.pathParams.get("a")).isEqualTo("HELLO");
    assertThat(params.pathParams.get("b")).isEqualTo("THERE");
    assertThat(params.pathParams).containsOnlyKeys("a", "b");
  }

  @Test
  void multiSegment_mixed() {
    final PathParser pathParser = new PathParser("/{one}/x{two}y{three}z/{four}", true);
    assertThat(pathParser.getSegmentCount()).isEqualTo(3);

    assertTrue(pathParser.matches("/0/x1y2z/3"));

    final SpiRoutes.Params params = pathParser.extractPathParams("/0/x1y2z/3");
    assertThat(params.pathParams.get("one")).isEqualTo("0");
    assertThat(params.pathParams.get("two")).isEqualTo("1");
    assertThat(params.pathParams.get("three")).isEqualTo("2");
    assertThat(params.pathParams.get("four")).isEqualTo("3");
    assertThat(params.pathParams).containsOnlyKeys("one", "two", "three", "four");
  }

  @Test
  void matchMulti_when_illegalSegments_expect_IllegalArgumentException() {

    expectParseError("some/a-<foo<bar>>-b")
      .withMessage("Path [some/a-<foo<bar>>-b] has illegal segment [a-<foo<bar>>-b] starting at position [2]");

    expectParseError("some/before/more-{foo{bar}}-b/after")
      .withMessage("Path [some/before/more-{foo{bar}}-b/after] has illegal segment [more-{foo{bar}}-b] starting at position [5]");
  }

  private ThrowableAssertAlternative<IllegalArgumentException> expectParseError(String path) {
    return assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new PathParser(path, true));
  }
}
