package io.avaje.jex.http3.flupke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.ssl.SslPlugin;
import tech.kwik.flupke.Http3Client;

class Http3Test {

  @Test
  void test() throws Exception {

    var ssl =
        SslPlugin.create(
            s -> s.resourceLoader(getClass()).keystoreFromClasspath("/keystore.p12", "password"));

    var jex =
        Jex.create()
            .plugin(ssl)
            .plugin(FlupkeJexPlugin.create())
            .get(
                "/",
                ctx -> {
                  assertEquals("h3", ctx.exchange().getProtocol());

                  ctx.text("hello world");
                })
            .start();
    var body =
        (Runtime.version().feature() >= 26
                ? HttpClient.newBuilder().version(Version.valueOf("HTTP_3"))
                : Http3Client.newBuilder())
            .sslContext(ssl.sslContext())
            .build()
            .send(request(), BodyHandlers.ofString())
            .body();

    assertEquals("hello world", body);

    jex.shutdown();
  }

  HttpRequest request() {
    var builder = HttpRequest.newBuilder().uri(URI.create("https://localhost:8080")).GET();
    if (Runtime.version().feature() < 26) return builder.build();
    try {
      Class<?> httpOptionClass = Class.forName("java.net.http.HttpOption");
      Class<?> http3DiscoveryModeClass =
          Class.forName("java.net.http.HttpOption$Http3DiscoveryMode");

      var h3DiscoveryOption =
          httpOptionClass.getDeclaredField("H3_DISCOVERY").get(null); // 'null' for static field

      var h3DiscoveryModeValue =
          http3DiscoveryModeClass.getDeclaredField("HTTP_3_URI_ONLY").get(null);

      MethodType methodType =
          MethodType.methodType(HttpRequest.Builder.class, httpOptionClass, Object.class);

      MethodHandle setOptionHandle =
          MethodHandles.lookup().findVirtual(HttpRequest.Builder.class, "setOption", methodType);
      setOptionHandle.invoke(builder, h3DiscoveryOption, h3DiscoveryModeValue);

    } catch (Throwable e) {
      e.printStackTrace();
    }
    return builder.build();
  }
}
