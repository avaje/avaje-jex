package org.foo.myapp.web;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;

@Client("/hello")
public interface HelloApi {

  @Produces("text/plain")
  @Get
  String hi();

  @Get("/there")
  Hello hello();

  class Hello {
    public int id2;
    public String msg;
  }
}
