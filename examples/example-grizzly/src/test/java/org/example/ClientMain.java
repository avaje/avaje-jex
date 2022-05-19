package org.example;

import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

public class ClientMain {

  public static void main(String[] args) {

    final HttpClientContext ctx = HttpClientContext.builder()
      .baseUrl("http://localhost:7003")
      .bodyAdapter(new JacksonBodyAdapter())
      .version(HttpClient.Version.HTTP_1_1)
      .build();

    final HttpResponse<String> res = ctx.request()
      .path("foo/99")
      .GET()
      .asPlainString();
    final HttpHeaders headers = res.headers();
    System.out.println("got " + res.body());

    HelloDto bean = ctx.request()
      .GET()
      .bean(HelloDto.class);

    System.out.println("bean " + bean);
  }
}
