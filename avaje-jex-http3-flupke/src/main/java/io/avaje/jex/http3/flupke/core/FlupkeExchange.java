package io.avaje.jex.http3.flupke.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import tech.kwik.flupke.server.HttpServerRequest;
import tech.kwik.flupke.server.HttpServerResponse;

class FlupkeExchange extends HttpExchange {

  private final HttpServerRequest request;
  private final HttpServerResponse response;
  private final Map<String, Object> attributes = new HashMap<>();
  private final Headers responseHeaders = new Headers();

  private Headers requestHeaders;
  private final HttpContext ctx;
  private int statusCode = 0;
  private InputStream is;
  private final PlaceholderOutputStream os = new PlaceholderOutputStream();

  public FlupkeExchange(HttpServerRequest request, HttpServerResponse response, HttpContext ctx) {
    this.request = request;
    this.response = response;
    this.ctx = ctx;
    this.is = request.body();
  }

  @Override
  public Headers getRequestHeaders() {
    if (requestHeaders == null) {
      requestHeaders = new Headers(request.headers().map());
      requestHeaders.computeIfAbsent("Host", k -> List.of(request.authority()));
    }
    return requestHeaders;
  }

  @Override
  public Headers getResponseHeaders() {
    return responseHeaders;
  }

  @Override
  public URI getRequestURI() {
    return URI.create(request.path());
  }

  @Override
  public String getRequestMethod() {
    return request.method();
  }

  @Override
  public HttpContext getHttpContext() {
    return ctx;
  }

  @Override
  public void close() {
    try (var __ = is;
        var ___ = response.getOutputStream(); ) {
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public InputStream getRequestBody() {
    return is;
  }

  @Override
  public OutputStream getResponseBody() {
    return os;
  }

  @Override
  public void sendResponseHeaders(int status, long responseLength) throws IOException {
    statusCode = status;
    if (responseLength > 0) {
      responseHeaders.add("Content-length", Long.toString(responseLength));
    }
    response.setHeaders(HttpHeaders.of(responseHeaders, (a, b) -> true));
    response.setStatus(status);
    os.wrapped = response.getOutputStream();
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return request.remoteAddress();
  }

  @Override
  public int getResponseCode() {
    return statusCode;
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return (InetSocketAddress) ctx.getAttributes().get("local_inet_address");
  }

  @Override
  public String getProtocol() {
    return "h3";
  }

  @Override
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  @Override
  public void setStreams(InputStream i, OutputStream o) {
    is = i;
    os.wrapped = o;
  }

  @Override
  public HttpPrincipal getPrincipal() {
    throw new UnsupportedOperationException();
  }

  /**
   * An OutputStream which wraps another wtStream which is supplied either at creation time, or
   * sometime later. If a caller/user tries to write to this wtStream before the wrapped wtStream
   * has been provided, then an IOException will be thrown.
   */
  class PlaceholderOutputStream extends OutputStream {

    OutputStream wrapped;

    void setWrappedStream(OutputStream os) {
      wrapped = os;
    }

    boolean isWrapped() {
      return wrapped != null;
    }

    private void checkWrap() throws IOException {
      if (wrapped == null) {
        throw new IOException("response headers not sent yet");
      }
    }

    @Override
    public void write(int b) throws IOException {
      checkWrap();
      wrapped.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      checkWrap();
      wrapped.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      checkWrap();
      wrapped.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      checkWrap();
      wrapped.flush();
    }

    @Override
    public void close() throws IOException {
      checkWrap();
      wrapped.close();
    }
  }
}
