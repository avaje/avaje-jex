package io.avaje.jex.routes;


import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathParserTest {

  @Test
  void matches_litArg() {

    var pathParser = new PathParser("/one/{id}");
    assertTrue(pathParser.matches("/one/1"));
    assertTrue(pathParser.matches("/one/2"));
    assertThat(pathParser.getSegmentCount()).isEqualTo(2);
    assertThat(pathParser.raw()).isEqualTo("/one/{id}");

    Map<String, String> pathParams = pathParser.extractPathParams("/one/1");
    assertThat(pathParams.get("id")).isEqualTo("1");

    pathParams = pathParser.extractPathParams("/one/next");
    assertThat(pathParams.get("id")).isEqualTo("next");
  }

  @Test
  void matches_argArgArg() {

    final PathParser pathParser = new PathParser("/{a}/{b}/{c}");
    assertTrue(pathParser.matches("/1a/2b/3c"));
    assertThat(pathParser.getSegmentCount()).isEqualTo(3);
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

    final PathParser pathParser = new PathParser("/one/{a}/{b}/{c}");
    assertThat(pathParser.getSegmentCount()).isEqualTo(4);
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
  void matches_litArgLitArgArgLit() {

    final PathParser pathParser = new PathParser("/one/{a}/two/{b}/{c}/end");
    assertThat(pathParser.getSegmentCount()).isEqualTo(6);
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

    final PathParser pathParser = new PathParser("/one/two");
    assertTrue(pathParser.matches("/one/two"));
    assertTrue(pathParser.matches("/one/two/"));

    assertFalse(pathParser.matches("/one/2"));
    assertFalse(pathParser.matches("/one/"));
    assertFalse(pathParser.matches("/one"));
    assertFalse(pathParser.matches("/one/two/more"));
  }

  @Test
  void matches_before_litPrefix() {
    final PathParser pathParser = new PathParser("/one/*");
    assertTrue(pathParser.matches("/one/two"));
    assertTrue(pathParser.matches("/one/two/three"));
    assertTrue(pathParser.matches("/one/two/three/four"));
  }

  @Test
  void matches_before_litPrefixAndSuffix() {
    final PathParser pathParser = new PathParser("/one/*/three");
    assertTrue(pathParser.matches("/one/two/three"));
    assertTrue(pathParser.matches("/one/foo/three"));

    assertFalse(pathParser.matches("/one/two"));
    assertFalse(pathParser.matches("/one/two/three/four"));
  }

  @Test
  void matches_before_litPrefixAndSuffixAndWild() {
    final PathParser pathParser = new PathParser("/one/*/three/*");
    assertTrue(pathParser.matches("/one/99/three/1000"));
    assertTrue(pathParser.matches("/one/99/three/1000/banana"));
    assertTrue(pathParser.matches("/one/two/three/four"));
    assertTrue(pathParser.matches("/one/42/three/"));

    assertFalse(pathParser.matches("/one/42/three"));
    assertFalse(pathParser.matches("/one/two"));
  }

}
