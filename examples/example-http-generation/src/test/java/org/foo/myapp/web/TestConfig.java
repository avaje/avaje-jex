package org.foo.myapp.web;

import io.avaje.inject.Factory;
import io.avaje.inject.test.TestScope;

@TestScope
@Factory
public class TestConfig {

  //  @Prototype
  //  @Bean
  //  HttpClientContext.Builder justForKicks() {
  //    return HttpClientContext.builder()
  //      .bodyAdapter(new JsonbBodyAdapter());
  //    //.bodyAdapter(new JacksonBodyAdapter());
  //  }

}
