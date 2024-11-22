import io.avaje.jex.spi.JexExtension;

module io.avaje.jex {

  exports io.avaje.jex;
  exports io.avaje.jex.http;
  exports io.avaje.jex.spi;
  exports io.avaje.jex.core;

  requires transitive java.net.http;
  requires transitive jdk.httpserver;
  requires transitive io.avaje.applog;
  requires static com.fasterxml.jackson.core;
  requires static com.fasterxml.jackson.databind;
  requires static io.avaje.jsonb;
  requires static io.avaje.inject;
  requires static io.avaje.config;
  requires static io.avaje.spi;

  uses JexExtension;
}
