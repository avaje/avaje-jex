package org.foo.myapp.web;

import io.avaje.http.api.Controller;

@Controller
public class HiController implements HiApi {

  @Override
  public String hi() {
    return "Yo Yo!!";
  }

  @Override
  public Hello hello() {
    return new Hello(42, "hello", "other");
  }
}
