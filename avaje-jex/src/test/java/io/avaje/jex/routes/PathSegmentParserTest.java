package io.avaje.jex.routes;

import org.junit.jupiter.api.Test;

import static io.avaje.jex.routes.PathSegmentParser.matchParamWithRegex;
import static io.avaje.jex.routes.PathSegmentParser.multi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathSegmentParserTest {

  @Test
  void matchParamBasic() {
    assertTrue(matchParamWithRegex("{id}"));
    assertTrue(matchParamWithRegex("{fooBar}"));
  }

  @Test
  void matchParamWithRegexOption() {
    assertTrue(matchParamWithRegex("{id:[0-9]}"));
    assertTrue(matchParamWithRegex("{id:[0-9]+}"));
    assertTrue(matchParamWithRegex("{id:[0-9]{4}}"));
    assertTrue(matchParamWithRegex("{fooBar:[blah]{108}}"));
  }

  @Test
  void matchParamWithRegex_notMatched() {
    assertFalse(matchParamWithRegex("{id:[0-9]{a}}"));
    assertFalse(matchParamWithRegex("{id:[0-9]{}}"));
    assertFalse(matchParamWithRegex("{id:{4}}"));
    assertFalse(matchParamWithRegex("{:bar{4}}"));
    assertFalse(matchParamWithRegex("id:[0-9]{4}}"));
    assertFalse(matchParamWithRegex("{fooBar:[blah]{}}"));
  }

  @Test
  void matchMulti() {
    assertThat(multi("before{id:[0-9]+}<foo>after")).containsExactly("before", "{id:[0-9]+}", "<foo>", "after");

    assertThat(multi("a-<foo>-b")).containsExactly("a-", "<foo>", "-b");
    assertThat(multi("a-{foo}-b")).containsExactly("a-", "{foo}", "-b");
    assertThat(multi("a-<foo>-*")).containsExactly("a-", "<foo>", "-", "*");
    assertThat(multi("a-{foo}-*")).containsExactly("a-", "{foo}", "-", "*");
    assertThat(multi("a-<foo>.<bar>")).containsExactly("a-", "<foo>", ".", "<bar>");
  }

  @Test
  void matchMultiError() {
    assertThat(multi("a-<foo<bar>>-b")).containsExactly("a-", "foo", "<bar>", ">-b");
    assertThat(multi("a-{foo{bar}}-b")).containsExactly("a-", "foo", "{bar}", "}-b");
  }
}
