package io.avaje.jex;

import java.lang.module.ModuleDescriptor.Builder;
import java.net.URL;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for a static resource exchange handler.
 *
 * @param root the root where your files are located (default: "/public")
 * @param directoryIndex The "file" which will be returned if a root is requested.
 * @param location Location.CLASSPATH (jar) or Location.FILE (file system) (default: CLASSPATH)
 * @param headers headers that will be set for response
 * @param skipFilePredicate predicate to skip certain files in the root based on the request
 *     (default: CLASSPATH)
 * @param mimeTypes configuration for file extension based Mime Types
 */
public sealed interface StaticContentConfig permits StaticFileHandlerBuilder {

  static StaticContentConfig create() {
    return StaticFileHandlerBuilder.builder();
  }

  /** Return a new ExchangeHandler that will serve the resource builder */
  ExchangeHandler createHandler();

  /** The HttpServer route where the handler will be activate. */
  StaticContentConfig httpPath(String urlPrefix);

  /** Set a new value for {@code root }. */
  String httpPath();

  /** The file or the folder your files are located (default: "/public/") */
  StaticContentConfig resource(String root);

  /** Set a new value for {@code directoryIndex }. */
  StaticContentConfig directoryIndex(String directoryIndex);

  /** Set a new value for {@code classPathResourceFunction }. */
  StaticContentConfig classPathResourceFunction(Function<String, URL> classPathResourceFunction);

  /** Add new key/value pair to the {@code mimeTypes } map. */
  StaticContentConfig putMimeTypeMapping(String key, String value);

  /** Add new key/value pair to the {@code headers } map. */
  StaticContentConfig putResponseHeader(String key, String value);

  /** Set a new value for {@code skipFilePredicate }. */
  StaticContentConfig skipFilePredicate(Predicate<Context> skipFilePredicate);

  /** Set a new value for {@code location }. Defaults to CLASSPATH */
  StaticContentConfig location(ResourceLocation location);
}
