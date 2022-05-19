package org.foo.myapp.web;

import io.avaje.http.api.*;

@Path("/hi")
public interface HiApi {

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
