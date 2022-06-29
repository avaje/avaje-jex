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
    Hello hello = new Hello();
    hello.id2 = 42;
    hello.msg = "hello";
    hello.other = "other";
    return hello;
  }
}
