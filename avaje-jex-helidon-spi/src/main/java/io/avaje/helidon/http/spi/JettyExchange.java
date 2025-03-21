package io.avaje.helidon.http.spi;

import com.sun.net.httpserver.HttpPrincipal;

/**
 *
 */
public interface JettyExchange
{

    HttpPrincipal getPrincipal();

    void setPrincipal(HttpPrincipal principal);
}
