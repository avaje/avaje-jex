package io.avaje.jex.cors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;

class CorsPluginTest {

  static Jex.Server server;
  static HttpClient client;
  static String baseUrl;

  @BeforeAll
  static void setUp() {
    client = HttpClient.newHttpClient();

    CorsPlugin plugin =
        CorsPlugin.builder()
          .createRule()
            .path("/api/*")
            .allowHost("https://example.com")
            .allowCredentials(false)
            .maxAge(3600)
            .buildRule()
          .createRule()
            .path("/public/*")
            .anyHost()
            .buildRule()
          .createRule()
            .path("/reflect/*")
            .reflectClientOrigin(true)
            .allowCredentials(true)
            .buildRule()
          .build();
    server =
        Jex.create()
            .plugin(plugin)
            .get("/api/hello", ctx -> ctx.text("hello"))
            .get("/public/data", ctx -> ctx.text("public"))
            .get("/reflect/me", ctx -> ctx.text("reflected"))
            .get("/other", ctx -> ctx.text("other"))
            .port(0)
            .start();

    baseUrl = "http://localhost:" + server.port();
  }

  @AfterAll
  static void tearDown() {
    if (server != null) server.shutdown();
  }

  @Test
  void allowedOrigin_setsAccessControlAllowOriginHeader() throws Exception {
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/hello"))
            .header("Origin", "https://example.com")
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
        .hasValue("https://example.com");
    assertThat(response.headers().firstValue("Vary")).hasValue("Origin");
  }

  @Test
  void disallowedOrigin_returns400() throws Exception {
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/hello"))
            .header("Origin", "https://evil.com")
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(400);
  }

  @Test
  void noOriginHeader_noCorsHeaders() throws Exception {
    var request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/hello")).GET().build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
  }

  @Test
  void anyHost_setsWildcardAllowOrigin() throws Exception {
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/public/data"))
            .header("Origin", "https://anyone.io")
            .timeout(Duration.ofSeconds(567))
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).hasValue("*");
  }

  @Test
  void reflectClientOrigin_returnsClientOriginAndCredentials() throws Exception {
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/reflect/me"))
            .header("Origin", "https://dynamic-client.com")
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
        .hasValue("https://dynamic-client.com");
    assertThat(response.headers().firstValue("Access-Control-Allow-Credentials")).hasValue("true");
  }

  @Test
  void preflightRequest_returns204WithPreflightHeaders() throws Exception {
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/hello"))
            .header("Origin", "https://example.com")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "Content-Type")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(204);
    assertThat(response.headers().firstValue("Access-Control-Allow-Methods")).hasValue("POST");
    assertThat(response.headers().firstValue("Access-Control-Allow-Headers"))
        .hasValue("Content-Type");
    assertThat(response.headers().firstValue("Access-Control-Max-Age")).hasValue("3600");
  }

  @Test
  void preflightRequest_disallowedOrigin_returns400() throws Exception {
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/hello"))
            .header("Origin", "https://attacker.com")
            .header("Access-Control-Request-Method", "DELETE")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(400);
  }

  @Test
  void rule_doesNotApplyToNonMatchingPath() throws Exception {
    // /other doesn't match /api/*, /public/*, or /reflect/*
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/other"))
            .header("Origin", "https://example.com")
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
  }

  @Test
  void exposeHeaders_areIncludedInResponse() throws Exception {
    Jex.Server extraServer =
        Jex.create()
            .plugin(
              CorsPlugin.builder()
                  .createRule()
                    .allowHost("https://example.com")
                    .exposeHeader("X-Custom-Header")
                    .exposeHeader("X-Another-Header")
                    .buildRule()
                  .build())
            .get("/data", ctx -> ctx.text("ok"))
            .port(0)
            .start();

    try {
      var request =
          HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:" + extraServer.port() + "/data"))
              .header("Origin", "https://example.com")
              .GET()
              .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.headers().firstValue("Access-Control-Expose-Headers"))
          .hasValue("X-Custom-Header,X-Another-Header");
    } finally {
      extraServer.shutdown();
    }
  }

  @Test
  void anyHost_withCredentials_throwsOnStartup() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Jex.create()
                .plugin(
                  CorsPlugin.builder()
                    .createRule()
                      .anyHost()
                      .allowCredentials(true)
                      .buildRule()
                    .build())
                .port(0)
                .start());
  }

  @Test
  void allowedOriginsAndReflectClientOrigin_together_throwsOnStartup() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Jex.create()
                .plugin(
                  CorsPlugin.builder()
                    .createRule()
                      .allowHost("https://example.com")
                      .reflectClientOrigin(true)
                      .buildRule()
                    .build())
                .port(0)
                .start());
  }

  @Test
  void noOriginsAndNoReflect_throwsOnStartup() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Jex.create()
                .plugin(
                  CorsPlugin.builder()
                      .createRule()
                        .path("/api/*")
                        .buildRule()
                      .build())
                .port(0)
                .start());
  }

  @Test
  void noRulesProvided_throwsIllegalArgument() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            CorsPlugin.builder().build());
  }

  // ─── null origin value ───────────────────────────────────────────────────

  @Test
  void nullStringOrigin_isRejectedAndNoCorsHeaderSet() throws Exception {
    // Browsers send Origin: null for sandboxed iframes / file:// origins
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/hello"))
            .header("Origin", "null")
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
  }

  // ─── allowHost validation ─────────────────────────────────────────────────

  @Test
  void allowHost_withNullString_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () ->
      CorsPlugin.builder().createRule().allowHost("null"));
  }

  @Test
  void allowHost_withTooManyWildcards_throwsIllegalArgument() {
    assertThrows(
        IllegalArgumentException.class,
        () -> CorsPlugin.builder().createRule().allowHost("*.*.example.com"));
  }

  @Test
  void allowHost_withWildcardNotAtStart_throwsIllegalArgument() {
    assertThrows(
        IllegalArgumentException.class, () -> CorsPlugin.builder().createRule().allowHost("example.*.com"));
  }

  // ─── maxAge omitted when negative ────────────────────────────────────────

  @Test
  void preflight_maxAgeNegative_maxAgeHeaderOmitted() throws Exception {
    Jex.Server extraServer =
        Jex.create()
            .plugin(
              CorsPlugin.builder()
                .createRule()
                .allowHost("https://example.com")
                .maxAge(-1)
                .buildRule()
              .build()) // explicit -1: omit header
            .get("/hello", ctx -> ctx.text("hi"))
            .port(0)
            .start();

    try {
      var request =
          HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:" + extraServer.port() + "/hello"))
              .header("Origin", "https://example.com")
              .header("Access-Control-Request-Method", "GET")
              .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
              .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofString());

      assertThat(response.headers().firstValue("Access-Control-Max-Age")).isEmpty();
    } finally {
      extraServer.shutdown();
    }
  }
}
