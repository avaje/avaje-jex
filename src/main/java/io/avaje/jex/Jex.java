package io.avaje.jex;

import io.avaje.jex.routes.EndpointGroup;
import io.avaje.jex.routes.WebApi;

import java.util.Set;

public class Jex {

  private Jex() {
    // hide
  }

  public static Jex create() {
    return new Jex();
  }

  public Jex routes(EndpointGroup endpointGroup) {
    endpointGroup.addEndpoints();
    return this;
  }

  public Jex get(String path, Handler handler, Set<Role> permittedRoles) {
    WebApi.get(path, handler, permittedRoles);
    return this;
  }

  public Jex get(String path, Handler handler) {
    WebApi.get(path, handler);
    return this;
  }

  public Jex get(Handler handler) {
    WebApi.get(handler);
    return this;
  }

  public Jex get(Handler handler, Set<Role> permittedRoles) {
    WebApi.get(handler, permittedRoles);
    return this;
  }

  public Jex post(String path, Handler handler, Set<Role> permittedRoles) {
    WebApi.post(path, handler, permittedRoles);
    return this;
  }

  public Jex post(String path, Handler handler) {
    WebApi.post(path, handler);
    return this;
  }

  public Jex post(Handler handler) {
    WebApi.post(handler);
    return this;
  }

  public Jex post(Handler handler, Set<Role> permittedRoles) {
    WebApi.post(handler, permittedRoles);
    return this;
  }

}
