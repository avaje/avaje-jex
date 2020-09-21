package io.avaje.jex.core;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiRoutes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

class JexHttpServlet extends HttpServlet {

  //private static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

  private final SpiRoutes routes;

  private final ServiceManager serviceManager;

  private final HttpMethodMap methodMap = new HttpMethodMap();

  public JexHttpServlet(SpiRoutes routes, ServiceManager serviceManager) {
    this.routes = routes;
    this.serviceManager = serviceManager;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {

    final Routing.Type handlerType = method(req);
    final String uri = req.getRequestURI();
    SpiRoutes.Entry route = routes.match(handlerType, uri);
    if (route == null) {
      //todo: apply all matching filters on not found?
      handleNotFound(req, res, uri);
    } else {
      final Map<String, String> pathParams = route.pathParams(uri);
      Context ctx = new JexHttpContext(serviceManager, req, res, pathParams, route.rawPath());
      routes.before(uri, ctx);
      route.handle(ctx);
      routes.after(uri, ctx);
    }
  }

  private void handleNotFound(HttpServletRequest req, HttpServletResponse res, String requestURI) throws IOException {
    //Context ctx = new Context(req, res, Collections.emptyMap());
    res.setStatus(404);
    res.getWriter().write("not found - " + requestURI);
  }

  private Routing.Type method(HttpServletRequest hreq) {
    return methodMap.get(hreq.getMethod());
  }
}
