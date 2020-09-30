package io.avaje.jex.server;

public class HelloDto {

  public long id;
  public String name;

  @Override
  public String toString() {
    return "id:" + id + " name:" + name;
  }

  public static HelloDto rob() {
    HelloDto me = new HelloDto();
    me.id = 42;
    me.name = "rob";
    return me;
  }

}
