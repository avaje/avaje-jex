package io.avaje.jex.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.jex.Jex;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class JdkJexServerTest {

  @Test
  void init() {

    HelloBean bean = new HelloBean(42, "rob");

    final Jex.Server server =
        Jex.create()
            .routing(
                routing ->
                    routing
                        .get("/", ctx -> ctx.text("hello world"))
                        .get("/foo", ctx -> ctx.json(bean))
                        .post(
                            "/foo",
                            ctx -> {
                              final HelloBean in = ctx.bodyAsClass(HelloBean.class);
                              in.name = in.name + "-out";
                              ctx.json(in);
                            }))
            .port(8093)
            .start();

    final HttpClient client =
        HttpClient.builder()
            .baseUrl("http://localhost:8093")
            .bodyAdapter(new JacksonBodyAdapter())
            .build();

    final HttpResponse<String> hres = client.request().GET().asString();

    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.body()).isEqualTo("hello world");

    final HelloBean foo = client.request().path("foo").GET().bean(HelloBean.class);

    assertThat(foo.id).isEqualTo(42);

    final HelloBean foo2 =
        client
            .request()
            .path("foo")
            .header("Accepts", "application/json")
            .body(bean)
            .POST()
            .bean(HelloBean.class);

    assertThat(foo2.name).isEqualTo("rob-out");

    System.out.println("done");

    server.shutdown();
  }
}
