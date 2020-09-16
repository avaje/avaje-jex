package io.avaje.jex.core;

import io.avaje.jex.Context;
import io.avaje.jex.routes.HandlerType;
import io.avaje.jex.routes.RouteEntry;
import io.avaje.jex.routes.Routes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

class JexServlet extends HttpServlet {

  //private static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

  private final Routes routes;

  private final ServiceManager serviceManager;

  private final MethodMap methodMap = new MethodMap();

  public JexServlet(Routes routes, ServiceManager serviceManager) {
    this.routes = routes;
    this.serviceManager = serviceManager;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {

    final HandlerType handlerType = method(req);
    final String requestURI = req.getRequestURI();
    RouteEntry route = routes.match(handlerType, requestURI);
    if (route == null) {
      handleNotFound(req, res, requestURI);
    } else {
      final Map<String, String> pathParams = route.pathParams(requestURI);
      Context ctx = new RequestContext(serviceManager, req, res, pathParams, route.rawPath());
      // before filters
      route.handle(ctx);
      // after filters
    }
  }

  private void handleNotFound(HttpServletRequest req, HttpServletResponse res, String requestURI) throws IOException {
    //Context ctx = new Context(req, res, Collections.emptyMap());
    res.setStatus(404);
    res.getWriter().write("not found - " + requestURI);
  }

  private HandlerType method(HttpServletRequest hreq) {
    return methodMap.get(hreq.getMethod());
  }
}
