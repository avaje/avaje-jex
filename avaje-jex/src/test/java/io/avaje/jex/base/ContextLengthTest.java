package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ContextLengthTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .post("/", ctx -> {
          ctx.text("contentLength:" + ctx.contentLength()+" type:"+ctx.contentType());
        })
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void when_noReqContentType() {
    HttpResponse<String> res = pair.request().body("MyBodyContent")
      .post().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("contentLength:13 type:null");
  }

  @Test
  void requestContentLengthAndType_notReqContentType() {
    HttpResponse<String> res = pair.request()
      .formParam("a","my-a-val")
      .formParam("b", "my-b-val")
      .post().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("contentLength:21 type:application/x-www-form-urlencoded");
  }

}
