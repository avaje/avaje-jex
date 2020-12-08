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
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

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

  public void handleException(Context ctx, Exception e) {
    exceptionHandler.handle(ctx, e);
  }

  public void render(Context ctx, String name, Map<String, Object> model) {
    templateManager.render(ctx, name, model);
  }

  public List<UploadedFile> uploadedFiles(HttpServletRequest req, String name) {
    return multipartUtil.uploadedFiles(req, name);
  }

  public Map<String, List<String>> multiPartForm(HttpServletRequest req) {
    return multipartUtil.fieldMap(req);
  }

  private static class Builder {
    private final Jex jex;
    private ErrorHandling errorHandling;
    private JsonService jsonService;
    private TemplateManager templateManager;
    private MultipartUtil multipartUtil;

    Builder(Jex jex) {
      this.jex = jex;
    }

    ServiceManager build() {
      this.errorHandling = jex.errorHandling();
      this.templateManager = initTemplateMgr();
      this.jsonService = initJsonService();
      this.multipartUtil = initMultiPart();

      return new ServiceManager(jsonService, errorHandling, templateManager, multipartUtil);
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
