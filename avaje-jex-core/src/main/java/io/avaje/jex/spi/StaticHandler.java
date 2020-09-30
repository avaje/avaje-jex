package io.avaje.jex.spi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface StaticHandler {

  boolean handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}
