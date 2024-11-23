package io.avaje.jex.core;

import io.avaje.jex.Context;
import io.avaje.jex.spi.TemplateRender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Render templates typically as html.
 */
public final class TemplateManager {

  private final Map<String, TemplateRender> map = new HashMap<>();

  private final Set<Class<?>> renderTypes = new HashSet<>();

  /**
   * Register all the extension renderer pairs.
   */
  public void register(Map<String, TemplateRender> source) {
    map.putAll(source);
    map.values().stream().forEach(templateRender -> renderTypes.add(templateRender.getClass()));
  }

  /**
   * Auto register via ServiceLoader if it has not already been explicitly registered.
   */
  public void registerDefault(TemplateRender render) {
    if (!renderTypes.contains(render.getClass())) {
      for (String extension : render.defaultExtensions()) {
        map.computeIfAbsent(extension, k->render);
      }
    }
  }

  /**
   * Register an extension and renderer.
   */
  public void register(String extn, TemplateRender renderer) {
    map.put(extn, renderer);
  }

  /**
   * Render the template and model typically as html to the context.
   *
   * @param ctx   The context to render the template to
   * @param name  The name of the template
   * @param model The model key value pairs to render use with the template
   */
  public void render(Context ctx, String name, Map<String, Object> model) {
    final String extn = extension(name);
    if (extn == null) {
      throw new IllegalArgumentException("No extension, not handled yet - " + name);
    }
    final TemplateRender renderer = map.get(extn);
    if (renderer == null) {
      throw new IllegalArgumentException("No renderer registered for extension - " + extn);
    }
    renderer.render(ctx, name, model);
  }

  private String extension(String name) {
    final int pos = name.indexOf('.');
    return pos == -1 ? null : name.substring(pos + 1);
  }

}
