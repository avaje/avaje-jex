package io.avaje.jex.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CookieParserTest {

  @Test
  void emptyAndNull() {
    assertThat(CookieParser.parse(null)).isEmpty();
    assertThat(CookieParser.parse("")).isEmpty();
  }

  @Test
  void basicMultiValue() {
    Map<String, String> cookies = CookieParser.parse("foo=bar; aaa=bbb; c=what_the_hell; aaa=ccc");
    assertThat(cookies.get("foo")).isEqualTo("bar");
    assertThat(cookies.get("aaa")).isEqualTo("ccc");
    assertThat(cookies.get("c")).isEqualTo("what_the_hell");
    assertThat(cookies).hasSize(3);
  }

  @Test
  void rfc2965() {
    String header = "$version=1; foo=bar; $Domain=google.com, aaa=bbb, c=cool; $Domain=google.com; $Path=\"/foo\"";
    Map<String, String> cookies = CookieParser.parse(header);
    assertThat(cookies.get("foo")).isEqualTo("bar");
    assertThat(cookies.get("aaa")).isEqualTo("bbb");
    assertThat(cookies.get("c")).isEqualTo("cool");
    assertThat(cookies).hasSize(3);
  }

  @Test
  void unquote() {
    Map<String, String> cookies = CookieParser.parse("foo=\"bar\"; aaa=bbb; c=\"what_the_hell\"; aaa=\"ccc\"");
    assertThat(cookies.get("foo")).isEqualTo("bar");
    assertThat(cookies.get("aaa")).isEqualTo("ccc");
    assertThat(cookies).hasSize(3);
  }

  @Test
  void tokenize() {
    String text = ",aa,,fo\"oooo\",\"bar\",co\"o'l,e\"c,df'hk,lm',";
    List<String> tokens = CookieParser.tokenize(',', text);
    assertThat(tokens).contains("aa", "fo\"oooo\"", "\"bar\"", "co\"o'l,e\"c", "df'hk", "lm'");
    tokens = CookieParser.tokenize(';',  text);
    assertThat(tokens).containsExactly(text);
  }

  @Test
  void quotedEmptyValue_skipped() {
    // foo="" has an empty value after unwrapping — should be excluded from the map
    Map<String, String> cookies = CookieParser.parse("foo=\"\"; bar=baz");
    assertThat(cookies).doesNotContainKey("foo");
    assertThat(cookies.get("bar")).isEqualTo("baz");
  }

  @Test
  void commaAndSemicolonSeparated() {
    // cookies may be comma- or semicolon-separated
    Map<String, String> cookies = CookieParser.parse("a=1, b=2; c=3");
    assertThat(cookies.get("a")).isEqualTo("1");
    assertThat(cookies.get("b")).isEqualTo("2");
    assertThat(cookies.get("c")).isEqualTo("3");
  }
}
