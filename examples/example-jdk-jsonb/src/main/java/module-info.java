module example.jdkTwo {

  requires io.avaje.jex.jdk;
  requires org.slf4j;
  requires io.avaje.jsonb;

  //requires static io.avaje.jsonb.generator;

  provides io.avaje.jsonb.Jsonb.Component with org.example.jsonb.GeneratedJsonComponent;
  //exports org.example to com.fasterxml.jackson.databind;
}
