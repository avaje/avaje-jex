module example.jdkTwo {

  requires io.avaje.jex.jdk;
  requires org.slf4j;
  requires io.avaje.jsonb;

  requires io.avaje.logging.slf4j;
  provides io.avaje.jsonb.Jsonb.Component with org.example.jsonb.GeneratedJsonComponent;

}
