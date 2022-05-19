package org.foo.myapp.service;

import io.avaje.inject.Component;

@Component
public class HelloService {

  public String say(String say) {
    return "say+" + say;
  }
}
