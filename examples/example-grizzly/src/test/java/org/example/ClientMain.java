package org.example;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

public class ClientMain {

  public static void main(String[] args) {

    final HttpClient ctx = HttpClient.builder()
      .baseUrl("http://localhost:7003")
      .bodyAdapter(new JacksonBodyAdapter())
      .version(java.net.http.HttpClient.Version.HTTP_1_1)
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
