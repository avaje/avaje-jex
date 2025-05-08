package io.avaje.jex.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.http.client.BasicAuthIntercept;
import io.avaje.jex.Jex;
import io.avaje.jex.core.TestPair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class BasicAuthCredentialsTest {

  static TestPair pair = init();

  static TestPair init() {

    final Jex app =
        Jex.create()
            .get("/", ctx -> {}, AppRole.USER)
            .filter(
                (ctx, chain) -> {
                  if (!ctx.routeRoles().contains(AppRole.getRole(ctx))) {

                    ctx.status(401).text("");
                  } else {
                    chain.proceed();
                  }
                });

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void testSuccess() {
    var intercept = new BasicAuthIntercept("test", "test");
    var req = pair.request();
    intercept.beforeRequest(req);
    var res = req.GET().asDiscarding();
    assertThat(res.statusCode()).isEqualTo(204);
  }

  @Test
  void testIncorrect() {
    var intercept = new BasicAuthIntercept("test1", "test1");
    var req = pair.request();
    intercept.beforeRequest(req);
    var res = req.GET().asDiscarding();
    assertThat(res.statusCode()).isEqualTo(401);
  }
}
