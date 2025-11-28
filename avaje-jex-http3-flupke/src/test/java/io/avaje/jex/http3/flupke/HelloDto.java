package io.avaje.jex.http3.flupke;

import io.avaje.jsonb.Json;

@Json
public class HelloDto {

  public long id;
  public String name;

  @Override
  public String toString() {
    return "id:" + id + " name:" + name;
  }

  public static HelloDto rob() {
    return create(42, "rob");
  }

  public static HelloDto fi() {
    return create(45, "fi");
  }

  public static HelloDto create(long id, String name) {
    HelloDto me = new HelloDto();
    me.id = id;
    me.name = name;
    return me;
  }
}
