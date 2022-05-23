module example.jdkTwo {

  requires io.avaje.jex.jdk;
  requires io.avaje.jsonb;

  provides io.avaje.jsonb.Jsonb.Component with org.example.jsonb.GeneratedJsonComponent;

}
