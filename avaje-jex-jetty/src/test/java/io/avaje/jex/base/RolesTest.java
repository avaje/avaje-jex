package io.avaje.jex.base;

import io.avaje.jex.Jex;
import io.avaje.jex.Role;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RolesTest {

  enum AppRoles implements Role {
    ADMIN,
    USER,
  }

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .accessManager((handler, ctx, permittedRoles) -> {
        final String role = ctx.queryParam("role");
        if (role == null || !permittedRoles.contains(AppRoles.valueOf(role))) {
          ctx.status(401).text("Unauthorized");
        } else {
          ctx.attribute("authBy", role);
          handler.handle(ctx);
        }
      })
      .routing(routing -> routing
        .get(ctx -> ctx.text("get"))
        .get("/multi", ctx -> ctx.text("multi-" + ctx.attribute("authBy"))).withRoles(AppRoles.ADMIN, AppRoles.USER)
        .get("/user", ctx -> ctx.text("user")).withRoles(AppRoles.USER)
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void noRoles() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("get");
  }

  @Test
  void singleRole_withRole() {
    HttpResponse<String> res = pair.request()
      .path("user").queryParam("role", "USER")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("user");
  }

  @Test
  void singleRole_withoutRole() {
    HttpResponse<String> res = pair.request()
      .path("user")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(401);
    assertThat(res.body()).isEqualTo("Unauthorized");
  }

  @Test
  void multiRole_withRole() {
    HttpResponse<String> res = pair.request()
      .path("multi").queryParam("role", "USER")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("multi-USER");
  }

  @Test
  void multiRole_withRole2() {
    HttpResponse<String> res = pair.request()
      .path("multi").queryParam("role", "ADMIN")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("multi-ADMIN");
  }

  @Test
  void multiRole_withoutRole() {
    HttpResponse<String> res = pair.request()
      .path("multi")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(401);
    assertThat(res.body()).isEqualTo("Unauthorized");
  }

}
