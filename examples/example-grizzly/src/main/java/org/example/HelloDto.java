package org.example;

public class HelloDto {

  public long id;
  public String name;

  public static HelloDto rob() {
    HelloDto bean = new HelloDto();
    bean.id = 42;
    bean.name = "rob";
    return bean;
  }
}
