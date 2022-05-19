package org.foo.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.PostConstruct;
import io.avaje.jex.Jex;

@Factory
public class JexConfiguration {

  @Bean
  Jex buildJex() {
    return Jex.create()
      .port(8002);
  }

  @PostConstruct
  void post() {
    // System.out.println("PostConstruct");
  }
}
