package io.avaje.jex.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class ContextUtilTest {

  @Test
  void parseCharset_defaults() {
    assertThat(ServiceManager.parseCharset("")).isEqualTo(StandardCharsets.UTF_8);
    assertThat(ServiceManager.parseCharset("junk")).isEqualTo(StandardCharsets.UTF_8);
  }

  @Test
  void parseCharset_caseCheck() {
    assertThat(ServiceManager.parseCharset("app/foo; charset=Us-AsCiI")).isEqualTo(StandardCharsets.US_ASCII);
    assertThat(ServiceManager.parseCharset("app/foo;charset=Us-AsCiI")).isEqualTo(StandardCharsets.US_ASCII);
    assertThat(ServiceManager.parseCharset("app/foo;charset = Us-AsCiI ")).isEqualTo(StandardCharsets.US_ASCII);
    assertThat(ServiceManager.parseCharset("app/foo;charset = Us-AsCiI;")).isEqualTo(StandardCharsets.US_ASCII);
    assertThat(ServiceManager.parseCharset("app/foo;charset = Us-AsCiI;other=junk")).isEqualTo(StandardCharsets.US_ASCII);
  }
}
