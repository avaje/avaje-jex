package org.foo.myapp.web;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * `@Client` interface in src/test for testing - ok but `@Client.Import` is probably
 * better if we want a common shared interface between server and client (e.g. loom).
 */
@InjectTest
class HelloClientInterfaceInTestTest {

  @Inject static MyTestHelloApi client;

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
