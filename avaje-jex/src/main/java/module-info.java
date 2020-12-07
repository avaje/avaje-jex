module io.avaje.jex {

  exports io.avaje.jex;
  exports io.avaje.jex.http;
  exports io.avaje.jex.spi;

  requires java.net.http;
  requires transitive jetty.servlet.api;
  requires transitive org.slf4j;
  requires transitive org.eclipse.jetty.http;
  requires transitive org.eclipse.jetty.servlet;
  requires transitive org.eclipse.jetty.server;
  requires transitive org.eclipse.jetty.io;
  requires transitive org.eclipse.jetty.util;

  requires transitive com.fasterxml.jackson.databind;

  uses io.avaje.jex.spi.SpiStartServer;
  uses io.avaje.jex.TemplateRender;
}
