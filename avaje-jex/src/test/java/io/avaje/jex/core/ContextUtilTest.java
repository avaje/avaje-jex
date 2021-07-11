package io.avaje.jex.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContextUtilTest {

  @Test
  void parseCharset_defaults() {
    assertThat(ServiceManager.parseCharset("")).isEqualTo(ServiceManager.UTF_8);
    assertThat(ServiceManager.parseCharset("junk")).isEqualTo(ServiceManager.UTF_8);
  }

  @Test
  void parseCharset_caseCheck() {
    assertThat(ServiceManager.parseCharset("app/foo; charset=ME")).isEqualTo("ME");
    assertThat(ServiceManager.parseCharset("app/foo;charset=ME")).isEqualTo("ME");
    assertThat(ServiceManager.parseCharset("app/foo;charset = ME ")).isEqualTo("ME");
    assertThat(ServiceManager.parseCharset("app/foo;charset = ME;")).isEqualTo("ME");
    assertThat(ServiceManager.parseCharset("app/foo;charset = ME;other=junk")).isEqualTo("ME");
  }
}
