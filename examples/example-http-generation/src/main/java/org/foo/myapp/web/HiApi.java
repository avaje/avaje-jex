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
  record Hello(int id2, String msg, String other) {}
}
