package io.avaje.jex.htmx;

/**
 * Template render API.
 */
public interface TemplateRender {

  /**
   * Render the given template view model to the server response.
   */
  String render(Object viewModel);
}
