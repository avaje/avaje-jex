package io.avaje.jex;

import java.util.Map;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.http.BadRequestException;
import io.avaje.jex.http.NotFoundException;

abstract sealed class AbstractStaticHandler implements ExchangeHandler permits StaticFileHandler, JrtResourceHandler {
  protected final Map<String, String> mimeTypes;
  protected final String filesystemRoot;
  protected final String urlPrefix;
  protected final Predicate<Context> skipFilePredicate;
  protected final Map<String, String> headers;

  protected AbstractStaticHandler(
      String urlPrefix,
      String filesystemRoot,
      Map<String, String> mimeTypes,
      Map<String, String> headers,
      Predicate<Context> skipFilePredicate) {
    this.filesystemRoot = filesystemRoot;
    this.urlPrefix = urlPrefix;
    this.skipFilePredicate = skipFilePredicate;
    this.headers = headers;
    this.mimeTypes = mimeTypes;
  }

  protected void throw404(HttpExchange jdkExchange) {
    throw new NotFoundException("File Not Found for request: " + jdkExchange.getRequestURI());
  }

  // This is one function to avoid giving away where we failed
  protected void reportPathTraversal() {
    throw new BadRequestException("Path traversal attempt detected");
  }

  protected String getExt(String path) {
    int slashIndex = path.lastIndexOf('/');
    String basename = (slashIndex < 0) ? path : path.substring(slashIndex + 1);

    int dotIndex = basename.lastIndexOf('.');
    if (dotIndex >= 0) {
      return basename.substring(dotIndex + 1);
    } else {
      return "";
    }
  }

  protected String lookupMime(String path) {
    String ext = getExt(path).toLowerCase();
    return mimeTypes.getOrDefault(ext, "application/octet-stream");
  }
}