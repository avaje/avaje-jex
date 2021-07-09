package io.avaje.jex.core;

import io.avaje.jex.*;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiContext;

import io.avaje.jex.spi.SpiServiceManager;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

class ServiceManager implements SpiServiceManager {

  private final HttpMethodMap methodMap = new HttpMethodMap();

  private final JsonService jsonService;

  private final ExceptionManager exceptionHandler;

  private final TemplateManager templateManager;

  private final MultipartUtil multipartUtil;

  static SpiServiceManager create(Jex jex) {
    return new Builder(jex).build();
  }

  ServiceManager(JsonService jsonService, ErrorHandling errorHandling, TemplateManager templateManager, MultipartUtil multipartUtil) {
    this.jsonService = jsonService;
    this.exceptionHandler = new ExceptionManager(errorHandling);
    this.templateManager = templateManager;
    this.multipartUtil = multipartUtil;
  }

  @Override
  public <T> T jsonRead(Class<T> clazz, SpiContext ctx) {
    return jsonService.jsonRead(clazz, ctx);
  }

  @Override
  public void jsonWrite(Object bean, SpiContext ctx) {
    jsonService.jsonWrite(bean, ctx);
  }

  @Override
  public <E> void jsonWriteStream(Stream<E> stream, SpiContext ctx) {
    try (stream) {
      jsonService.jsonWriteStream(stream.iterator(), ctx);
    }
  }

  @Override
  public <E> void jsonWriteStream(Iterator<E> iterator, SpiContext ctx) {
    try {
      jsonService.jsonWriteStream(iterator, ctx);
    } finally {
      maybeClose(iterator);
    }
  }

  @Override
  public void maybeClose(Object iterator) {
    if (AutoCloseable.class.isAssignableFrom(iterator.getClass())) {
      try {
        ((AutoCloseable) iterator).close();
      } catch (Exception e) {
        throw new RuntimeException("Error closing iterator " + iterator, e);
      }
    }
  }

  @Override
  public Routing.Type lookupRoutingType(String method) {
    return methodMap.get(method);
  }

  @Override
  public void handleException(Context ctx, Exception e) {
    exceptionHandler.handle(ctx, e);
  }

  @Override
  public void render(Context ctx, String name, Map<String, Object> model) {
    templateManager.render(ctx, name, model);
  }

  @Override
  public List<UploadedFile> uploadedFiles(HttpServletRequest req) {
    return multipartUtil.uploadedFiles(req);
  }

  @Override
  public List<UploadedFile> uploadedFiles(HttpServletRequest req, String name) {
    return multipartUtil.uploadedFiles(req, name);
  }

  @Override
  public Map<String, List<String>> multiPartForm(HttpServletRequest req) {
    return multipartUtil.fieldMap(req);
  }

  private static class Builder {
    private final Jex jex;

    Builder(Jex jex) {
      this.jex = jex;
    }

    SpiServiceManager build() {
      return new ServiceManager(initJsonService(), jex.errorHandling(), initTemplateMgr(), initMultiPart());
    }

    JsonService initJsonService() {
      final JsonService jsonService = jex.inner.jsonService;
      if (jsonService != null) {
        return jsonService;
      }
      return detectJackson() ? defaultJacksonService() : null;
    }

    JsonService defaultJacksonService() {
      return new JacksonJsonService();
    }

    boolean detectJackson() {
      try {
        Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
        return true;
      } catch (ClassNotFoundException e) {
        return false;
      }
    }

    TemplateManager initTemplateMgr() {
      TemplateManager mgr = new TemplateManager();
      mgr.register(jex.inner.renderers);
      for (TemplateRender render : ServiceLoader.load(TemplateRender.class)) {
        mgr.registerDefault(render);
      }
      return mgr;
    }

    MultipartUtil initMultiPart() {
      MultipartConfigElement config = jex.inner.multipartConfig;
      if (config == null) {
        final int fileThreshold = jex.inner.multipartFileThreshold;
        config = new MultipartConfigElement(System.getProperty("java.io.tmpdir"), -1, -1, fileThreshold);
      }
      return new MultipartUtil(config);
    }
  }
}
