package io.avaje.jex.core.internal;

import org.junit.jupiter.api.Test;

import io.avaje.jex.core.internal.CoreServiceManager;

import static org.assertj.core.api.Assertions.assertThat;

class ContextUtilTest {

  @Test
  void parseCharset_defaults() {
    assertThat(CoreServiceManager.parseCharset("")).isEqualTo(CoreServiceManager.UTF_8);
    assertThat(CoreServiceManager.parseCharset("junk")).isEqualTo(CoreServiceManager.UTF_8);
  }

  @Test
  void parseCharset_caseCheck() {
    assertThat(CoreServiceManager.parseCharset("app/foo; charset=ME")).isEqualTo("ME");
    assertThat(CoreServiceManager.parseCharset("app/foo;charset=ME")).isEqualTo("ME");
    assertThat(CoreServiceManager.parseCharset("app/foo;charset = ME ")).isEqualTo("ME");
    assertThat(CoreServiceManager.parseCharset("app/foo;charset = ME;")).isEqualTo("ME");
    assertThat(CoreServiceManager.parseCharset("app/foo;charset = ME;other=junk")).isEqualTo("ME");
  }
}
