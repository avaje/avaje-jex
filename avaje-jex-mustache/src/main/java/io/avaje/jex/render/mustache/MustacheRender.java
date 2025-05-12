package io.avaje.jex.render.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import io.avaje.jex.http.Context;
import io.avaje.jex.spi.TemplateRender;
import io.avaje.spi.ServiceProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Map;

@ServiceProvider
public class MustacheRender implements TemplateRender {

  private final MustacheFactory mustacheFactory;

  public MustacheRender(MustacheFactory mustacheFactory) {
    this.mustacheFactory = mustacheFactory;
  }

  public MustacheRender() {
    this.mustacheFactory = new DefaultMustacheFactory("./");
  }

  @Override
  public String[] defaultExtensions() {
    return new String[] {"mustache"};
  }

  @Override
  public void render(Context ctx, String name, Map<String, Object> model) {
    try {
      var writer = new StringWriter();
      mustacheFactory.compile(name).execute(writer, model).close();
      ctx.html(writer.toString());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
