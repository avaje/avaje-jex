package io.avaje.jex.core;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelTest {

  @Test
  void toMap_pair() {
    final Map<String, Object> map = Model.toMap("a", 42);
    assertThat(map).hasSize(1);
    assertThat(map.get("a")).isEqualTo(42);
  }

  @Test
  void toMap_others() {

    final Map<String, Object> map = Model.toMap("a", 42, "b", "bval", "c", 99);
    assertThat(map).hasSize(3);
    assertThat(map.get("a")).isEqualTo(42);
    assertThat(map.get("b")).isEqualTo("bval");
    assertThat(map.get("c")).isEqualTo(99);
  }

  @Test
  void toMap_oddArgs_expect_IllegalArgumentException() {
    assertThatThrownBy(() -> Model.toMap("a", 42, "b"))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
