package io.avaje.jex.staticcontent;

import static io.avaje.jex.core.Constants.CONTENT_LENGTH;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.compression.CompressedOutputStream;
import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.core.Constants;
import io.avaje.jex.http.BadRequestException;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExchangeHandler;
import io.avaje.jex.http.NotFoundException;

abstract sealed class AbstractStaticHandler implements ExchangeHandler
    permits StaticFileHandler, StaticClassResourceHandler {

  protected final Map<String, String> mimeTypes;
  protected final CompressionConfig compressionConfig;
  protected final String filesystemRoot;
  protected final String urlPrefix;
  protected final Predicate<Context> skipFilePredicate;
  protected final Map<String, String> headers;
  protected final BiFunction<Context, String, ContentDisposition> contentDisposition;
  protected final boolean precompress;
  protected final Map<String, CachedResource> compressedFiles = new ConcurrentHashMap<>();
  private static final FileNameMap MIME_MAP = URLConnection.getFileNameMap();

  protected AbstractStaticHandler(
      String urlPrefix,
      String filesystemRoot,
      Map<String, String> mimeTypes,
      Map<String, String> headers,
      Predicate<Context> skipFilePredicate,
      BiFunction<Context, String, ContentDisposition> contentDisposition,
      boolean precompress,
      CompressionConfig compressionConfig) {
    this.compressionConfig = compressionConfig;
    this.filesystemRoot = filesystemRoot;
    this.urlPrefix = urlPrefix;
    this.skipFilePredicate = skipFilePredicate;
    this.headers = headers;
    this.contentDisposition = contentDisposition;
    this.mimeTypes = mimeTypes;
    this.precompress = precompress;
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
    }
    return "";
  }

  protected String lookupMime(String path) {
    var lower = path.toLowerCase();
    return Objects.requireNonNullElseGet(
        MIME_MAP.getContentTypeFor(path),
        () -> {
          String ext = getExt(lower);
          return mimeTypes.getOrDefault(ext, "application/octet-stream");
        });
  }

  /** Set the Content-Disposition header when one has been configured. */
  protected void applyContentDisposition(Context ctx, String path) {
    if (contentDisposition == null) {
      return;
    }
    var disposition = contentDisposition.apply(ctx, fileName(path));
    if (disposition != null) {
      ctx.header(Constants.CONTENT_DISPOSITION, List.of(disposition.headerValue()));
    }
  }

  private static String fileName(String path) {
    int separator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
    return separator < 0 ? path : path.substring(separator + 1);
  }

  protected boolean isCached(final String path) {
    return precompress && compressedFiles.containsKey(path);
  }

  protected void addCachedEntry(Context ctx, String urlPath, InputStream fis) throws IOException {
    var baos = new ByteArrayOutputStream();
    CompressedOutputStream compressed = new CompressedOutputStream(compressionConfig, ctx, baos);
    fis.transferTo(compressed);
    compressed.close();
    var bytes = baos.toByteArray();
    var responseHeaders = Map.copyOf(ctx.exchange().getResponseHeaders());
    if ("HEAD".equals(ctx.method())) {
      ctx.header(Constants.CONTENT_LENGTH, String.valueOf(bytes.length));
      ctx.writeEmpty(200);
      return;
    }
    ctx.write(bytes);
    var encoding = ctx.responseHeader(Constants.CONTENT_ENCODING);
    compressedFiles.put(
        urlPath, new CachedResource(responseHeaders, bytes, encoding != null, encoding));
  }

  protected boolean writeCached(Context ctx, String path) throws IOException {
    var cached = compressedFiles.get(path);
    var bytes = cached.bytes();

    boolean isHead = "HEAD".equals(ctx.method());
    if (cached.isCompressed()) {
      if (ctx.header(Constants.ACCEPT_ENCODING) == null) {
        return false;
      }
      var compressor =
          compressionConfig.findMatchingCompressor(List.of(ctx.header(Constants.ACCEPT_ENCODING)));

      if (compressor.isEmpty() || !compressor.get().encoding().equals(cached.encoding())) {
        return false;
      }
      ctx.headerMap(cached.headers());
      applyContentDisposition(ctx, path);
      ctx.header(Constants.CONTENT_LENGTH, String.valueOf(bytes.length));
      if (isHead) {
        ctx.writeEmpty(200);
      } else {
        ctx.write(bytes);
      }
      return true;
    }

    ctx.header(Constants.CONTENT_TYPE, cached.headers().get(Constants.CONTENT_TYPE));
    applyContentDisposition(ctx, path);
    if (isHead) {
      writeHeadResponse(ctx, new ByteArrayInputStream(bytes));
      return true;
    }
    ctx.write(new ByteArrayInputStream(bytes));
    return true;
  }

  void writeHeadResponse(Context ctx, InputStream fis) throws IOException {
    var os = new CountingOutputStream();
    CompressedOutputStream compressed = new CompressedOutputStream(compressionConfig, ctx, os);
    fis.transferTo(compressed);
    compressed.close();
    ctx.header(CONTENT_LENGTH, String.valueOf(os.count()));
    ctx.writeEmpty(200);
  }
}
