package io.avaje.jex.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.System.Logger.Level;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.avaje.jex.Context;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jex.core.json.JacksonJsonService;
import io.avaje.jex.core.json.JsonbJsonService;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

/** Core service methods available to Context implementations. */
final class SpiServiceManager {

  private static final System.Logger log = System.getLogger("io.avaje.jex");
  static final String UTF_8 = "UTF-8";

  private final HttpMethodMap methodMap = new HttpMethodMap();
  private final JsonService jsonService;
  private final ExceptionManager exceptionHandler;
  private final TemplateManager templateManager;
  private final String scheme;
  private final String contextPath;

  static SpiServiceManager create(Jex jex) {
    return new Builder(jex).build();
  }

  SpiServiceManager(
      JsonService jsonService,
      ExceptionManager manager,
      TemplateManager templateManager,
      String scheme,
      String contextPath) {
    this.jsonService = jsonService;
    this.exceptionHandler = manager;
    this.templateManager = templateManager;
    this.scheme = scheme;
    this.contextPath = contextPath;
  }

  OutputStream createOutputStream(JdkContext jdkContext) {
    return new BufferedOutStream(jdkContext);
  }

  public <T> T fromJson(Type type, InputStream is) {
    return jsonService.fromJson(type, is);
  }

  public void toJson(Object bean, OutputStream os) {
    jsonService.toJson(bean, os);
  }

  public <E> void toJsonStream(Stream<E> stream, OutputStream os) {
    try (stream) {
      jsonService.toJsonStream(stream.iterator(), os);
    }
  }

  public <E> void toJsonStream(Iterator<E> iterator, OutputStream os) {
    try {
      jsonService.toJsonStream(iterator, os);
    } finally {
      maybeClose(iterator);
    }
  }

  public void maybeClose(Object iterator) {
    if (iterator instanceof AutoCloseable closeable) {
      try {
        closeable.close();
      } catch (Exception e) {
        throw new RuntimeException("Error closing iterator " + iterator, e);
      }
    }
  }

  public Routing.Type lookupRoutingType(String method) {
    return methodMap.get(method);
  }

  public void handleException(JdkContext ctx, Exception e) {
    exceptionHandler.handle(ctx, e);
  }

  public void render(Context ctx, String name, Map<String, Object> model) {
    templateManager.render(ctx, name, model);
  }

  public String requestCharset(Context ctx) {
    return parseCharset(ctx.header(Constants.CONTENT_TYPE));
  }

  static String parseCharset(String header) {
    if (header != null) {
      for (String val : header.split(";")) {
        val = val.trim();
        if (val.regionMatches(true, 0, "charset", 0, "charset".length())) {
          return val.split("=")[1].trim();
        }
      }
    }
    return UTF_8;
  }

  public Map<String, List<String>> formParamMap(Context ctx, String charset) {
    return parseParamMap(ctx.body(), charset);
  }

  public Map<String, List<String>> parseParamMap(String body, String charset) {
    if (body == null || body.isEmpty()) {
      return Collections.emptyMap();
    }
    try {
      Map<String, List<String>> map = new LinkedHashMap<>();
      for (String pair : body.split("&")) {
        final String[] split1 = pair.split("=", 2);
        String key = URLDecoder.decode(split1[0], charset);
        String val = split1.length > 1 ? URLDecoder.decode(split1[1], charset) : "";
        map.computeIfAbsent(key, s -> new ArrayList<>()).add(val);
      }
      return map;
    } catch (UnsupportedEncodingException e) {
      throw new UncheckedIOException(e);
    }
  }

  String scheme() {
    return scheme;
  }

  String contextPath() {
    return contextPath;
  }

  private static final class Builder {

    private final Jex jex;

    Builder(Jex jex) {
      this.jex = jex;
    }

    SpiServiceManager build() {
      return new SpiServiceManager(
          initJsonService(),
          new ExceptionManager(jex.routing().errorHandlers()),
          initTemplateMgr(),
          jex.config().scheme(),
          jex.config().contextPath());
    }

    JsonService initJsonService() {
      final JsonService jsonService = jex.config().jsonService();
      if (jsonService != null) {
        return jsonService;
      }

      var json = CoreServiceLoader.jsonService().orElseGet(this::defaultJsonService);

      if (json == null) log.log(Level.WARNING, "No Json library configured");

      return json;
    }

    /** Create a reasonable default JsonService if Jackson or avaje-jsonb are present. */
    JsonService defaultJsonService() {
      if (detectJackson()) {
        try {
          return new JacksonJsonService();
        } catch (IllegalAccessError errorNotInModulePath) {
          // not in module path
          log.log(
              Level.DEBUG,
              "Not using Jackson due to module path {0}",
              errorNotInModulePath.getMessage());
        }
      }
      return detectJsonb() ? new JsonbJsonService() : null;
    }

    boolean detectJackson() {
      return detectTypeExists("com.fasterxml.jackson.databind.ObjectMapper");
    }

    boolean detectJsonb() {
      return detectTypeExists("io.avaje.jsonb.Jsonb");
    }

    private boolean detectTypeExists(String className) {
      try {
        Class.forName(className);
        return true;
      } catch (ClassNotFoundException e) {
        return false;
      }
    }

    TemplateManager initTemplateMgr() {
      TemplateManager mgr = new TemplateManager();
      mgr.register(jex.config().renderers());
      for (TemplateRender render : CoreServiceLoader.getRenders()) {
        mgr.registerDefault(render);
      }
      return mgr;
    }
  }
}
