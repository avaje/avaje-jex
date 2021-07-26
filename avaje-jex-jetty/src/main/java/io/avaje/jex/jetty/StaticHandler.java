package io.avaje.jex.jetty;

import io.avaje.jex.StaticFileSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.EmptyResource;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class StaticHandler {

  private static final Logger log = LoggerFactory.getLogger(StaticHandler.class);

  private final List<ResourceHandler> handlers = new ArrayList<>();
  private final Server server;
  private final boolean preCompress;

  StaticHandler(boolean preCompress, Server server) {
    this.preCompress = preCompress;
    this.server = server;
  }

  void addStaticFileConfig(StaticFileSource config) {
    ResourceHandler handler;
    if ("/webjars".equals(config.getPath())) {
      handler = new WebjarHandler();
    } else {
      PrefixableHandler h = new PrefixableHandler(config.getUrlPathPrefix());
      h.setResourceBase(getResourcePath(config));
      h.setDirAllowed(false);
      h.setEtags(true);
      handler = h;
    }
    log.info("Static file handler added {}", config);

    try {
      handler.setServer(server);
      handler.start();
    } catch (Exception e) {
      throw new RuntimeException("Error starting Jetty static resource handler", e);
    }
    handlers.add(handler);
  }

  private String getResourcePath(StaticFileSource config) {
    if (config.getLocation() == StaticFileSource.Location.CLASSPATH) {
      var resource = Resource.newClassPathResource(config.getPath());
      if (resource == null) {
        throw new RuntimeException(noSuchDir(config) + " Depending on your setup, empty folders might not get copied to classpath.");
      }
      return resource.toString();
    }
    final File path = new File(config.getPath());
    if (!path.exists()) {
      throw new RuntimeException(noSuchDir(config) + " path: " + path.getAbsolutePath());
    }
    return config.getPath();
  }

  private String noSuchDir(StaticFileSource config) {
    return "Static resource directory with path: '" + config.getPath() + "' does not exist.";
  }

  boolean handle(HttpServletRequest req, HttpServletResponse res) {
    final String target = (String) req.getAttribute("jetty-target");
    final Request baseRequest = (Request) req.getAttribute("jetty-request");
    for (ResourceHandler handler : handlers) {
      try {
        var resource = handler.getResource(target);
        if (isFile(resource) || isDirectoryWithWelcomeFile(resource, handler, target)) {
//            val maxAge = if (target.startsWith("/immutable/") || handler is WebjarHandler) 31622400 else 0
//            httpResponse.setHeader(HeaderKeys.CACHE_CONTROL, "max-age=$maxAge");

          // Remove the default content type because Jetty will not set the correct one
          // if the HTTP response already has a content type set
//            if (precompressStaticFiles && PrecompressingResourceHandler.handle(resource, httpRequest, httpResponse)) {
//              return true
//            }
          res.setContentType(null);
          handler.handle(target, baseRequest, req, res);
          req.setAttribute("handled-as-static-file", true);
//            (httpResponse as JavalinResponseWrapper).outputStream.finalize()
          return true;
        }
      } catch (Exception e) { // it's fine
//          if (!Util.isClientAbortException(e)) {
//            Javalin.log?.error("Exception occurred while handling static resource", e)
//          }
        log.error("Exception occurred while handling static resource", e);
      }
    }
    return false;
  }

  private boolean isFile(Resource resource) {
    return resource != null && resource.exists() && !resource.isDirectory();
  }

  private boolean isDirectoryWithWelcomeFile(Resource resource, ResourceHandler handler, String target) {
    //String path = target.removeSuffix("/")+"/index.html";
    if (target.endsWith("/")) {
      target = target.substring(0, target.length() - 1);
    }
    String path = target + "/index.html";
    if (resource == null || !resource.isDirectory()) {
      return false;
    }
    try {
      final Resource indexHtml = handler.getResource(path);
      return indexHtml != null && indexHtml.exists();
    } catch (IOException e) {
      log.warn("Error checking for welcome file", e);
      return false;
    }
  }

  private static class WebjarHandler extends ResourceHandler {
    @Override
    public Resource getResource(String path) throws IOException {
      final Resource resource = Resource.newClassPathResource("META-INF/resources" + path);
      return (resource != null) ? resource : super.getResource(path);
    }
  }

  private static class PrefixableHandler extends ResourceHandler {

    private final String urlPathPrefix;

    PrefixableHandler(String urlPathPrefix) {
      this.urlPathPrefix = urlPathPrefix;
    }

    @Override
    public Resource getResource(String path) throws IOException {
      if (urlPathPrefix.equals("/")) {
        return super.getResource(path); // same as regular ResourceHandler
      }
      String targetPath = target(path);
      if ("".equals(targetPath)) {
        return super.getResource("/"); // directory without trailing '/'
      }
      if (!path.startsWith(urlPathPrefix)) {
        return EmptyResource.INSTANCE;
      }
      if (!targetPath.startsWith("/")) {
        return EmptyResource.INSTANCE;
      } else {
        return super.getResource(targetPath);
      }
    }

    private String target(String path) {
      if (path.startsWith(urlPathPrefix)) {
        return path.substring(urlPathPrefix.length());
      } else {
        return path;
      }
    }
  }
}
