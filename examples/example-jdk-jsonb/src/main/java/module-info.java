import io.avaje.jsonb.spi.JsonbExtension;

module example.jdkTwo {

  requires io.avaje.jex;
  requires io.avaje.jsonb;
  provides JsonbExtension with org.example.jsonb.GeneratedJsonComponent;
}
