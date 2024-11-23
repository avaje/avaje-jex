package io.avaje.jex.render.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.avaje.jex.Context;
import io.avaje.jex.spi.TemplateRender;
import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class FreeMarkerRender implements TemplateRender {

  private final Configuration configuration;

  public FreeMarkerRender(Configuration configuration) {
    this.configuration = configuration;
  }

  public FreeMarkerRender() {
    this.configuration = defaultConfiguration();
  }

  private Configuration defaultConfiguration() {
    Configuration configuration = new Configuration(new Version(2, 3, 26));
    configuration.setClassForTemplateLoading(FreeMarkerRender.class, "/");
    return configuration;
  }

  @Override
  public String[] defaultExtensions() {
    return new String[]{"ftl"};
  }

  @Override
  public void render(Context context, String filePath, Map<String, Object> model) {
    var stringWriter = new StringWriter();
    try {
      final Template template = configuration.getTemplate(filePath);
      template.process(model, stringWriter);
      context.html(stringWriter.toString());

    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (TemplateException e) {
      throw new IllegalStateException(e);
    }
  }
}
