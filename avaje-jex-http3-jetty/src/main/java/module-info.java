module io.avaje.jex.http3.jetty {
  requires transitive io.avaje.jex.ssl;
  requires transitive org.eclipse.jetty.server;
  requires transitive org.eclipse.jetty.http3.common;
  requires transitive org.eclipse.jetty.http3.server;
  requires transitive org.eclipse.jetty.http.spi;
  requires java.base;
}
