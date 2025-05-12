package io.avaje.jex.spi;

import io.avaje.jex.http.Context;
import java.util.Map;

/**
 * Template rendering typically of html.
 *
 * @see JexExtension SPI registration details.
 */
public non-sealed interface TemplateRender extends JexExtension {

  /**
   * Return the extensions this template renders for by default.
   *
   * <p>When the template render is not explicitly registered, it can be automatically registered
   * via ServiceLoader with the provided extensions by default.
   */
  String[] defaultExtensions();

  /**
   * Render the template and model typically as html to the given context.
   *
   * @param context The context to render the template to
   * @param name The template name
   * @param model The model of key value pairs used when rendering the template
   */
  void render(Context context, String name, Map<String, Object> model);
}
