package org.foo.myapp.web;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A `@Client` interface lives in src/main - not usually expected.
 */
@InjectTest
class HelloClientInterfaceInMainTest {

  @Inject
  static HelloApi client;

  @Test
  void hello() {
    String hi = client.hi();
    assertThat(hi).isEqualTo("hisay+yo");
  }

  @Test
  void hello2() {
    String hi = client.hi();
    assertThat(hi).isEqualTo("hisay+yo");
  }
}
