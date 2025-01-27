package org.foo.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import org.foo.myapp.service.HelloService;

@Controller
@Path("/hello")
class HelloController {

  final HelloService service;

  HelloController(HelloService service) {
    this.service = service;
  }

  @Produces("text/plain")
  @Get
  String hello() {
    return "hi" + service.say("yo");
  }
}
