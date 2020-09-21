package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;

import java.util.Map;

public interface SpiRoutes {

  Entry match(Routing.Type type, String pathInfo);

  interface Entry {

    boolean matches(String requestUri);

    void handle(Context ctx);

    Map<String, String> pathParams(String uri);

    String rawPath();

    int getSegmentCount();
  }
}
