package io.avaje.jex;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import io.avaje.jex.Routing.HttpService;
import io.avaje.jex.core.TestPair;
import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class SpiRouteTest implements HttpService {

  @Override
  public void add(Routing routing) {

    routing.get("/spi", ctx -> ctx.write("hello from spi"));
  }

  @Test
  void get() {
    var pair = TestPair.create(Jex.create());
    HttpResponse<String> res = pair.request().path("spi").GET().asString();

    assertThat(res.body()).isEqualTo("hello from spi");
    pair.close();
  }
}
