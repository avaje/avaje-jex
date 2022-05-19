package org.foo.myapp.web;

import io.avaje.http.client.HttpClientContext;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Using a raw HttpClientContext - not bad.
 */
@InjectTest
class HelloControllerTest {

  @Inject HttpClientContext client;

  @Test
  void hello() {
    HttpResponse<String> hello = client.request().path("hello")
      .GET()
      .asString();

    assertThat(hello.statusCode()).isEqualTo(200);
  }

}
