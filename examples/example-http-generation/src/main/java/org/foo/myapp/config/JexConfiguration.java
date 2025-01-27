package org.foo.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.PostConstruct;
import io.avaje.jex.spi.JexPlugin;

@Factory
public class JexConfiguration {

  @Bean
  JexPlugin buildJex() {
    return j -> j.port(8002);
  }

  @PostConstruct
  void post() {
    // System.out.println("PostConstruct");
  }
}
