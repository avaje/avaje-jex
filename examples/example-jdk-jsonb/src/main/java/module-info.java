import io.avaje.jsonb.Jsonb;

module example.jdkTwo {

  requires io.avaje.jex.jdk;
  requires io.avaje.jsonb;

  provides Jsonb.GeneratedComponent with org.example.jsonb.GeneratedJsonComponent;

}
