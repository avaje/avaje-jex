package org.foo.myapp.web;

import io.avaje.http.api.Client;
import io.avaje.http.client.HttpClientContext;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HiApi is a 'server' interface.
 *
 * Using Client.Import we get the client code generated in src/test.
 * Actually in target/generated-test-sources/ ...
 */
@Client.Import(types = HiApi.class)
@InjectTest
class HelloClientInterfaceViaImportTest {

  @Inject static HiApi client;

  @Inject static HttpClientContext rawClient;

  @Test
  void hello() {
    String hi = client.hi();
    assertThat(hi).isEqualTo("Yo Yo!!");

    HttpResponse<String> hres = rawClient.request().path("hi").GET().asString();
    assertThat(hres.body()).isEqualTo("Yo Yo!!");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void hello2() {
    var hello = client.hello();
    assertThat(hello.msg).isNull();
  }

  @Test
  void hello_usingRawHttpClient() {
    HttpResponse<String> hres = rawClient.request().path("hi").GET().asString();
    assertThat(hres.body()).isEqualTo("Yo Yo!!");
  }
}
