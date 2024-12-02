package io.avaje.jex.core;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class HealthPluginTest {

  static final int port = new Random().nextInt(1000) + 10_000;
  static Jex jex;
  static Jex.Server server;
  static HttpClient client;

  @BeforeAll
  static void setup() {
    jex = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> {
          final String one = ctx.header("one");
          Map<String, String> obj = new LinkedHashMap<>();
          obj.put("one", one);
          ctx.json(obj);
        })
      )
      .port(port);

    server =  jex.start();
    client = HttpClient.builder()
      .baseUrl("http://localhost:"+port)
      .bodyAdapter(new JacksonBodyAdapter())
      .build();
  }

  @AfterAll
  static void end() {
    server.shutdown();
  }

  @Test
  void get() {
    final HttpResponse<String> hres = client.request()
      .header("one", "hello")
      .GET().asString();
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void healthLiveness() {
    assertThat(ready("health/liveness").statusCode()).isEqualTo(200);
  }

  @Test
  void healthLiveness_various() {
    jex.lifecycle().status(AppLifecycle.Status.STOPPED);
    assertThat(ready("health/liveness").statusCode()).isEqualTo(500);
    jex.lifecycle().status(AppLifecycle.Status.STOPPING);
    assertThat(ready("health/liveness").statusCode()).isEqualTo(500);

    jex.lifecycle().status(AppLifecycle.Status.STARTING);
    assertThat(ready("health/liveness").statusCode()).isEqualTo(200);
    jex.lifecycle().status(AppLifecycle.Status.STARTED);
    assertThat(ready("health/liveness").statusCode()).isEqualTo(200);
  }

  @Test
  void healthReadiness() {
    assertThat(ready("health/readiness").statusCode()).isEqualTo(200);
  }

  @Test
  void healthReadiness_various() {
    jex.lifecycle().status(AppLifecycle.Status.STOPPING);
    assertThat(ready("health/readiness").statusCode()).isEqualTo(500);
    jex.lifecycle().status(AppLifecycle.Status.STOPPED);
    assertThat(ready("health/readiness").statusCode()).isEqualTo(500);
    jex.lifecycle().status(AppLifecycle.Status.STARTING);
    assertThat(ready("health/readiness").statusCode()).isEqualTo(500);
    jex.lifecycle().status(AppLifecycle.Status.STARTED);
    assertThat(ready("health/readiness").statusCode()).isEqualTo(200);
  }

  private HttpResponse<String> ready(String s) {
    return client.request().path(s)
      .GET().asString();
  }
}
