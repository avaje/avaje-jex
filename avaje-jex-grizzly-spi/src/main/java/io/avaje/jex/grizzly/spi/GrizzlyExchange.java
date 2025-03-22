package io.avaje.jex.grizzly.spi;

import com.sun.net.httpserver.HttpPrincipal;

sealed interface GrizzlyExchange permits GrizzlyHttpExchange, GrizzlyHttpsExchange {

  HttpPrincipal getPrincipal();

  void setPrincipal(HttpPrincipal principal);
}
