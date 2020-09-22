package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonTest {

  static TestPair pair = init();

  static TestPair init() {
    Jex app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.json(HelloDto.rob()))
        .post("/", ctx -> ctx.text("bean[" + ctx.bodyAsClass(HelloDto.class) + "]")));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {

    var bean = pair.request()
      .get()
      .bean(HelloDto.class);

    assertThat(bean.id).isEqualTo(42);
    assertThat(bean.name).isEqualTo("rob");
  }

  @Test
  void post() {
    HelloDto dto = new HelloDto();
    dto.id = 42;
    dto.name = "rob";

    var res = pair.request()
      .body(dto)
      .post().asString();

    assertThat(res.body()).isEqualTo("bean[id:42 name:rob]");
    assertThat(res.statusCode()).isEqualTo(200);

    dto.id = 99;
    dto.name = "fi";

    res = pair.request()
      .body(dto)
      .post().asString();

    assertThat(res.body()).isEqualTo("bean[id:99 name:fi]");
    assertThat(res.statusCode()).isEqualTo(200);
  }

}
