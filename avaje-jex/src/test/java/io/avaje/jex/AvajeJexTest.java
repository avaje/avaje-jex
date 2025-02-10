package io.avaje.jex;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;

public class AvajeJexTest {

  @Test
  void canStart() {
    assertThatNoException().isThrownBy(() -> AvajeJex.start().shutdown());
  }
}
