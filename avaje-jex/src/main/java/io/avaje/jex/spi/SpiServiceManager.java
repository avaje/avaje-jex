package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.UploadedFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface SpiServiceManager {

  <T> T jsonRead(Class<T> clazz, SpiContext ctx);

  void jsonWrite(Object bean, SpiContext ctx);

  <E> void jsonWriteStream(Stream<E> stream, SpiContext ctx);

  <E> void jsonWriteStream(Iterator<E> iterator, SpiContext ctx);

  void maybeClose(Object iterator);

  Routing.Type lookupRoutingType(String method);

  void handleException(Context ctx, Exception e);

  void render(Context ctx, String name, Map<String, Object> model);

  /**
   * Return the character set of the request.
   */
  String requestCharset(Context ctx);

  /**
   * Parse and return the body as form parameters.
   */
  Map<String, List<String>> formParamMap(Context ctx, String charset);

  /**
   * Return the uploaded files for the request.
   */
  default List<UploadedFile> uploadedFiles(HttpServletRequest req) {
    throw new UnsupportedOperationException();
  }

  /**
   * Return the uploaded files for the request and name.
   */
  default List<UploadedFile> uploadedFiles(HttpServletRequest req, String name) {
    throw new UnsupportedOperationException();
  }

  /**
   * Return the form parameters from a multipart form request.
   */
  default Map<String, List<String>> multiPartForm(HttpServletRequest req) {
    throw new UnsupportedOperationException();
  }

}
