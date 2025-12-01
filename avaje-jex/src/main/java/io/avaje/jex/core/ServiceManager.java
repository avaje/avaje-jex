package io.avaje.jex.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger.Level;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.avaje.applog.AppLog;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jex.compression.CompressedOutputStream;
import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.core.json.JacksonJsonService;
import io.avaje.jex.core.json.JsonbJsonService;
import io.avaje.jex.http.Context;
import io.avaje.jex.routes.UrlDecode;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

/** Core service methods available to Context implementations. */
final class ServiceManager {

  private static final System.Logger log = AppLog.getLogger("io.avaje.jex");

  private final CompressionConfig compressionConfig;
  private final JsonService jsonService;
  private final ExceptionManager exceptionHandler;
  private final TemplateManager templateManager;
  private final String scheme;
  private final int bufferInitial;
  private final long bufferMax;
  private final int rangeChunks;
  private final long maxRequestSize;

  static ServiceManager create(Jex jex) {
    return new Builder(jex).build();
  }

  ServiceManager(
      CompressionConfig compressionConfig,
      JsonService jsonService,
      ExceptionManager manager,
      TemplateManager templateManager,
      String scheme,
      long bufferMax,
      int bufferInitial,
      int rangeChunks,
      long maxRequestSize) {
    this.compressionConfig = compressionConfig;
    this.jsonService = jsonService;
    this.exceptionHandler = manager;
    this.templateManager = templateManager;
    this.scheme = scheme;
    this.bufferInitial = bufferInitial;
    this.bufferMax = bufferMax;
    this.rangeChunks = rangeChunks;
    this.maxRequestSize = maxRequestSize;
  }

  OutputStream createOutputStream(JdkContext jdkContext) {
    var out = new BufferedOutStream(jdkContext, bufferInitial, bufferMax);
    if (compressionConfig.compressionEnabled()) {
      return new CompressedOutputStream(compressionConfig, jdkContext, out);
    }
    return out;
  }

  JsonService jsonService() {
    return jsonService;
  }

  <T> T fromJson(Type type, InputStream is) {
    return jsonService.fromJson(type, is);
  }

  <T> T fromJson(Type type, byte[] data) {
    return jsonService.fromJson(type, data);
  }

  void toJson(Object bean, OutputStream os) {
    jsonService.toJson(bean, os);
  }

  <E> void toJsonStream(Stream<E> stream, OutputStream os) {
    try (stream) {
      jsonService.toJsonStream(stream.iterator(), os);
    }
  }

  <E> void toJsonStream(Iterator<E> iterator, OutputStream os) {
    try {
      jsonService.toJsonStream(iterator, os);
    } finally {
      maybeClose(iterator);
    }
  }

  void writeRange(Context ctx, InputStream is, long totalBytes) {
    RangeWriter.write(ctx, is, totalBytes, rangeChunks);
  }

  void maybeClose(Object iterator) {
    if (iterator instanceof AutoCloseable closeable) {
      try {
        closeable.close();
      } catch (Exception e) {
        throw new RuntimeException("Error closing iterator " + iterator, e);
      }
    }
  }

  Routing.Type lookupRoutingType(String method) {
    try {
      return Routing.Type.valueOf(method);
    } catch (Exception e) {
      return null;
    }
  }

  void handleException(JdkContext ctx, Exception t) {
    exceptionHandler.handle(ctx, t);
  }

  void render(Context ctx, String name, Map<String, Object> model) {
    templateManager.render(ctx, name, model);
  }

  Charset requestCharset(Context ctx) {
    return parseCharset(ctx.header(Constants.CONTENT_TYPE));
  }

  static Charset parseCharset(String header) {
    if (header != null) {
      for (String val : header.split(";")) {
        val = val.trim();
        if (val.regionMatches(true, 0, "charset", 0, "charset".length())) {
          return Charset.forName(val.split("=")[1].trim());
        }
      }
    }
    return StandardCharsets.UTF_8;
  }

  Map<String, List<String>> formParamMap(Context ctx, Charset charset) {
    return parseParamMap(ctx.body(), charset);
  }

  Map<String, List<String>> parseParamMap(String body, Charset charset) {
    if (body == null || body.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, List<String>> map = new LinkedHashMap<>();
    for (String pair : body.split("&")) {
      final String[] split1 = pair.split("=", 2);
      String key = UrlDecode.decode(split1[0], charset);
      String val = split1.length > 1 ? UrlDecode.decode(split1[1], charset) : "";
      map.computeIfAbsent(key, s -> new ArrayList<>()).add(val);
    }
    return map;
  }

  String scheme() {
    return scheme;
  }

  long maxRequestSize() {
    return maxRequestSize;
  }

  private static final class Builder {

    private final Jex jex;

    Builder(Jex jex) {
      this.jex = jex;
    }

    ServiceManager build() {
      return new ServiceManager(
          jex.config().compression(),
          initJsonService(),
          new ExceptionManager(jex.routing().errorHandlers()),
          initTemplateMgr(),
          jex.config().scheme(),
          jex.config().maxStreamBufferSize(),
          jex.config().initialStreamBufferSize(),
          jex.config().rangeChunkSize(),
          jex.config().maxRequestSize());
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
      ModuleLayer bootLayer = ModuleLayer.boot();
      return bootLayer
        .findModule("io.avaje.jex")
        .map(m -> {
          if (bootLayer.findModule("io.avaje.jsonb").isPresent()) {
            return new JsonbJsonService();
          }
          if (bootLayer.findModule("com.fasterxml.jackson.databind").isPresent()) {
            return new JacksonJsonService();
          }
          return null;
        })
        .orElseGet(() -> {
          try {
            return new JsonbJsonService();
          } catch (NoClassDefFoundError e) {
            // I guess it don't exist
          }
          try {
            return new JacksonJsonService();
          } catch (NoClassDefFoundError e) {
            return null;
          }
        });
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
