module io.avaje.jex.http2.jetty {
  requires transitive io.avaje.jex.ssl;
  requires transitive org.eclipse.jetty.server;
  requires transitive org.eclipse.jetty.http.spi;
  requires transitive org.eclipse.jetty.http2.common;
  requires transitive org.eclipse.jetty.http2.server;
  requires java.base;
}
