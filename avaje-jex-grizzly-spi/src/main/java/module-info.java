import com.sun.net.httpserver.spi.HttpServerProvider;

module io.avaje.jex.grizzly {
  exports io.avaje.jex.grizzly.spi;

  requires transitive io.avaje.jex;
  requires transitive jdk.httpserver;
  requires transitive org.glassfish.grizzly.http.server;
  requires transitive org.glassfish.grizzly.http;
  requires transitive org.glassfish.grizzly;
  requires static io.avaje.spi;
  requires static java.net.http;

  provides HttpServerProvider with
      io.avaje.jex.grizzly.spi.GrizzlyHttpServerProvider;
}
