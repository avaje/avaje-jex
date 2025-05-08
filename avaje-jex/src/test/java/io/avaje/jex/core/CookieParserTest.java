package io.avaje.jex.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

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
    String header =
        "$version=1; foo=bar; $Domain=google.com, aaa=bbb, c=cool; $Domain=google.com; $Path=\"/foo\"";
    Map<String, String> cookies = CookieParser.parse(header);
    assertThat(cookies.get("foo")).isEqualTo("bar");
    assertThat(cookies.get("aaa")).isEqualTo("bbb");
    assertThat(cookies.get("c")).isEqualTo("cool");
    assertThat(cookies).hasSize(3);
  }

  @Test
  void unquote() {
    Map<String, String> cookies =
        CookieParser.parse("foo=\"bar\"; aaa=bbb; c=\"what_the_hell\"; aaa=\"ccc\"");
    assertThat(cookies.get("foo")).isEqualTo("bar");
    assertThat(cookies.get("aaa")).isEqualTo("ccc");
    assertThat(cookies).hasSize(3);
  }

  @Test
  void tokenize() {
    String text = ",aa,,fo\"oooo\",\"bar\",co\"o'l,e\"c,df'hk,lm',";
    List<String> tokens = CookieParser.tokenize(',', text);
    assertThat(tokens).contains("aa", "fo\"oooo\"", "\"bar\"", "co\"o'l,e\"c", "df'hk", "lm'");
    tokens = CookieParser.tokenize(';', text);
    assertThat(tokens).containsExactly(text);
  }
}
