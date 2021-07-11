import io.avaje.jex.TemplateRender;
import io.avaje.jex.core.BootstapServiceManager;
import io.avaje.jex.routes.BootstrapRoutes;
import io.avaje.jex.spi.SpiRoutesProvider;
import io.avaje.jex.spi.SpiServiceManagerProvider;
import io.avaje.jex.spi.SpiStartServer;

module io.avaje.jex {

  exports io.avaje.jex;
  exports io.avaje.jex.http;
  exports io.avaje.jex.spi;
  exports io.avaje.jex.core;

  requires java.net.http;
  requires transitive org.slf4j;
  requires jetty.servlet.api;

  requires transitive com.fasterxml.jackson.databind;

  uses TemplateRender;
  uses SpiRoutesProvider;
  uses SpiServiceManagerProvider;
  uses SpiStartServer;

  provides SpiRoutesProvider with BootstrapRoutes;
  provides SpiServiceManagerProvider with BootstapServiceManager;
}
