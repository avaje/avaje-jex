import io.avaje.jex.jetty.JettyStartServer;
import io.avaje.jex.spi.SpiStartServer;

module io.avaje.jex.jetty {

  exports io.avaje.jex.jetty;

  requires transitive io.avaje.jex;
  //requires io.avaje.jex.jettyx;
  requires java.net.http;
  requires transitive jetty.servlet.api;
  requires transitive org.slf4j;
  requires transitive org.eclipse.jetty.http;
  requires transitive org.eclipse.jetty.server;
  requires transitive org.eclipse.jetty.io;
  requires transitive org.eclipse.jetty.util;


  provides SpiStartServer with JettyStartServer;
}
