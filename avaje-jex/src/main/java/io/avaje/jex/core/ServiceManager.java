package io.avaje.jex.core;

import io.avaje.jex.Context;
import io.avaje.jex.ErrorHandling;
import io.avaje.jex.Jex;
import io.avaje.jex.TemplateRender;
import io.avaje.jex.UploadedFile;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiContext;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class ServiceManager {

  private final JsonService jsonService;

  private final ExceptionManager exceptionHandler;

  private final TemplateManager templateManager;

  private final MultipartUtil multipartUtil;

  public static ServiceManager create(Jex jex) {
    return new Builder(jex).build();
  }

  ServiceManager(JsonService jsonService, ErrorHandling errorHandling, TemplateManager templateManager, MultipartUtil multipartUtil) {
    this.jsonService = jsonService;
    this.exceptionHandler = new ExceptionManager(errorHandling);
    this.templateManager = templateManager;
    this.multipartUtil = multipartUtil;
  }

  public <T> T jsonRead(Class<T> clazz, SpiContext ctx) {
    return jsonService.jsonRead(clazz, ctx);
  }

  public void jsonWrite(Object bean, SpiContext ctx) {
    jsonService.jsonWrite(bean, ctx);
  }

  public <E> void jsonWriteStream(Stream<E> stream, SpiContext ctx) {
    try (stream) {
      jsonService.jsonWriteStream(stream.iterator(), ctx);
    }
  }

  public <E> void jsonWriteStream(Iterator<E> iterator, SpiContext ctx) {
    try {
      jsonService.jsonWriteStream(iterator, ctx);
    } finally {
      maybeClose(iterator);
    }
  }

  private <E> void maybeClose(Object iterator) {
    if (AutoCloseable.class.isAssignableFrom(iterator.getClass())) {
      try {
        ((AutoCloseable) iterator).close();
      } catch (Exception e) {
        throw new RuntimeException("Error closing iterator " + iterator, e);
      }
    }
  }

  public void handleException(Context ctx, Exception e) {
    exceptionHandler.handle(ctx, e);
  }

  public void render(Context ctx, String name, Map<String, Object> model) {
    templateManager.render(ctx, name, model);
  }

  public List<UploadedFile> uploadedFiles(HttpServletRequest req) {
    return multipartUtil.uploadedFiles(req);
  }

  public List<UploadedFile> uploadedFiles(HttpServletRequest req, String name) {
    return multipartUtil.uploadedFiles(req, name);
  }

  public Map<String, List<String>> multiPartForm(HttpServletRequest req) {
    return multipartUtil.fieldMap(req);
  }

  private static class Builder {
    private final Jex jex;

    Builder(Jex jex) {
      this.jex = jex;
    }

    ServiceManager build() {
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
