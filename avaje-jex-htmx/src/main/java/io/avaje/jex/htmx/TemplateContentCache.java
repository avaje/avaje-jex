package io.avaje.jex.htmx;

import io.avaje.jex.http.Context;

/** Defines caching of template content. */
public interface TemplateContentCache {

  /** Return the key given the request. */
  String key(Context req);

  /** Return the key given the request with form parameters. */
  String key(Context req, Object formParams);

  /** Return the content given the key. */
  String content(String key);

  /** Put the content into the cache. */
  void contentPut(String key, String content);
}
