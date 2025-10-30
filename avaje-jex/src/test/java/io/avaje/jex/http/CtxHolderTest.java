package io.avaje.jex.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.core.TestPair;

class CtxHolderTest {
  static TestPair init() {
    final Jex app = Jex.create().get("/", __ -> Context.currentRequest().text("dummy"));
    return TestPair.create(app);
  }

  @Test
  void get() {
    try (var pair = init()) {
      assertEquals(200, pair.request().GET().asDiscarding().statusCode());
    }
  }

  @Test
  void notSet() {
    assertThrows(NoSuchElementException.class, Context::currentRequest);
  }
}
