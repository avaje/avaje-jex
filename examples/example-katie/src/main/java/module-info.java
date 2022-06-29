import io.avaje.jsonb.Jsonb;

open module example.katie {

  requires io.avaje.kate;
//  requires io.avaje.jsonb;
//  requires io.avaje.http.api;
//  requires io.avaje.jex;
//  requires io.avaje.jex.jetty;
//  requires io.avaje.inject;

  requires io.avaje.http.client;

  provides io.avaje.inject.spi.Module with org.foo.myapp.MyappModule;
  provides Jsonb.GeneratedComponent with org.foo.myapp.web.jsonb.GeneratedJsonComponent;
}
