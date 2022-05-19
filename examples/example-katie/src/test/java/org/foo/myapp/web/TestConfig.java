package org.foo.myapp.web;

import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Prototype;
import io.avaje.inject.test.TestScope;

@TestScope
@Factory
public class TestConfig {

  @Prototype
  @Bean
  HttpClientContext.Builder justForKicks() {
    return HttpClientContext.builder()
      .bodyAdapter(new JacksonBodyAdapter());
  }

}
