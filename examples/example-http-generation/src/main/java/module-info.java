open module example.http.generation {

  requires io.avaje.config;
  requires io.avaje.jsonb;
  requires io.avaje.http.api;
  requires io.avaje.jex;
  requires io.avaje.inject;

  requires io.avaje.http.client;

  provides io.avaje.inject.spi.InjectExtension with org.foo.myapp.MyappModule;
  provides io.avaje.jsonb.spi.JsonbExtension with org.foo.myapp.web.jsonb.GeneratedJsonComponent;
  provides io.avaje.http.client.HttpClient.GeneratedComponent with org.foo.myapp.web.httpclient.GeneratedHttpComponent;

}
