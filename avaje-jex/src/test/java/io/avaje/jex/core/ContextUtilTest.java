package io.avaje.jex.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContextUtilTest {

  @Test
  void parseCharset_defaults() {
    assertThat(SpiServiceManager.parseCharset("")).isEqualTo(SpiServiceManager.UTF_8);
    assertThat(SpiServiceManager.parseCharset("junk")).isEqualTo(SpiServiceManager.UTF_8);
  }

  @Test
  void parseCharset_caseCheck() {
    assertThat(SpiServiceManager.parseCharset("app/foo; charset=ME")).isEqualTo("ME");
    assertThat(SpiServiceManager.parseCharset("app/foo;charset=ME")).isEqualTo("ME");
    assertThat(SpiServiceManager.parseCharset("app/foo;charset = ME ")).isEqualTo("ME");
    assertThat(SpiServiceManager.parseCharset("app/foo;charset = ME;")).isEqualTo("ME");
    assertThat(SpiServiceManager.parseCharset("app/foo;charset = ME;other=junk")).isEqualTo("ME");
  }
}
