package io.avaje.jex.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContextUrlDecodeTest {

  @Test
  void parseCharset_defaults() {

    assertThat(ContextUtil.parseCharset("")).isEqualTo(ContextUtil.UTF_8);
    assertThat(ContextUtil.parseCharset("junk")).isEqualTo(ContextUtil.UTF_8);
  }

  @Test
  void parseCharset_caseCheck() {

    assertThat(ContextUtil.parseCharset("app/foo; charset=ME")).isEqualTo("ME");
    assertThat(ContextUtil.parseCharset("app/foo;charset=ME")).isEqualTo("ME");
    assertThat(ContextUtil.parseCharset("app/foo;charset = ME ")).isEqualTo("ME");
    assertThat(ContextUtil.parseCharset("app/foo;charset = ME;")).isEqualTo("ME");
    assertThat(ContextUtil.parseCharset("app/foo;charset = ME;other=junk")).isEqualTo("ME");
  }
}
