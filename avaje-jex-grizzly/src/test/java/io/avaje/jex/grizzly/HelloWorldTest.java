package io.avaje.jex.grizzly;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWorldTest {

  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello"))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("hello");
  }

  @Test
  void getAgain() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("hello");
  }

}
