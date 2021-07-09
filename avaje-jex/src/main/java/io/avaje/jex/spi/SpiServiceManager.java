package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.UploadedFile;
import io.avaje.jex.spi.SpiContext;
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

  List<UploadedFile> uploadedFiles(HttpServletRequest req);

  List<UploadedFile> uploadedFiles(HttpServletRequest req, String name);

  Map<String, List<String>> multiPartForm(HttpServletRequest req);
}
