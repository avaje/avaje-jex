import io.avaje.jex.TemplateRender;
import io.avaje.jex.core.BootstapServiceManager;
import io.avaje.jex.routes.BootstrapRoutes;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiRoutesProvider;
import io.avaje.jex.spi.SpiServiceManagerProvider;
import io.avaje.jex.spi.SpiStartServer;

module io.avaje.jex {

  exports io.avaje.jex;
  exports io.avaje.jex.http;
  exports io.avaje.jex.spi;
  exports io.avaje.jex.core;

  requires transitive java.net.http;
  requires transitive io.avaje.applog;
  requires static com.fasterxml.jackson.core;
  requires static com.fasterxml.jackson.databind;
  requires static io.avaje.jsonb;
  requires static io.avaje.inject;
  requires static io.avaje.config;

  uses TemplateRender;
  uses SpiRoutesProvider;
  uses SpiServiceManagerProvider;
  uses SpiStartServer;
  uses JsonService;

  provides SpiRoutesProvider with BootstrapRoutes;
  provides SpiServiceManagerProvider with BootstapServiceManager;
}
