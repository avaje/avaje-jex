package org.foo.myapp.web;

import io.avaje.http.api.*;
import io.avaje.jsonb.Json;

@Path("/hi")
public interface HiApi {

  @Produces("text/plain")
  @Get
  String hi();

  @Get("/there")
  Hello hello();

  @Json
  class Hello {
    public int id2;
    public String msg;
    public String other;
  }
}
