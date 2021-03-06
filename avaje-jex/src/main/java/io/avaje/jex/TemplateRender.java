package io.avaje.jex;

import java.util.Map;

/**
 * Template rendering typically of html.
 */
public interface TemplateRender {

  /**
   * Return the extensions this template renders for by default.
   * <p>
   * When the template render is not explicitly registered it can be
   * automatically registered via ServiceLoader and these are the extensions
   * it will register for by default.
   */
  String[] defaultExtensions();

  /**
   * Render the template and model typically as html to the given context.
   *
   * @param context The context to render the template to
   * @param name    The template name
   * @param model   The model of key value pairs used when rendering the template
   */
  void render(Context context, String name, Map<String, Object> model);
}
