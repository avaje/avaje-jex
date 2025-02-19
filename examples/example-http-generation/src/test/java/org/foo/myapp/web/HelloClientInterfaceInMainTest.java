package org.foo.myapp.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.http.api.Client;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

/**
 * A `@Client` interface lives in src/main - not usually expected.
 */
@Client.Import(HelloApi.class)
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
