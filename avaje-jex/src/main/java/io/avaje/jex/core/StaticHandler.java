package io.avaje.jex.core;

import io.avaje.jex.StaticFileSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface StaticHandler {

  void addStaticFileConfig(StaticFileSource config);

  boolean handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}
