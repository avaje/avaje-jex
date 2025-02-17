package io.avaje.jex.core;

import io.avaje.jsonb.Json;

@Json
public class HelloBean {

  public int id;
  public String name;

  public HelloBean(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public HelloBean() {}
}
