package io.avaje.helidon.http.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

/** Jetty implementation of {@link com.sun.net.httpserver.HttpExchange} */
public class GrizzlyHttpExchangeDelegate extends HttpExchange {
  /** Set of headers that RFC9110 says will not have a value list */
  private static final Set<String> SINGLE_VALUE_HEADERS =
      Set.of(
          "authorization",
          "content-length",
          "date",
          "expires",
          "host",
          "if-modified-since",
          "if-unmodified-since",
          "if-range",
          "last-modified",
          "location",
          "referer",
          "retry-after",
          "user-agent");

  private final HttpContext context;

  private final Request request;

  private final Headers responseHeaders = new Headers();

  private Headers requestHeaders = new Headers();

  private int statusCode = 0;

  private InputStream inputStream;

  private OutputStream outputStream;

  private HttpPrincipal httpPrincipal;

  private Response response;

  GrizzlyHttpExchangeDelegate(HttpContext httpSpiContext, Request request, Response response) {
    this.context = httpSpiContext;
    this.request = request;
    this.response = response;
    this.inputStream = request.getInputStream();
    this.outputStream = response.getOutputStream();
  }

  @Override
  public Headers getRequestHeaders() {

    if (!requestHeaders.isEmpty()) {
      return requestHeaders;
    }
    for (var name : request.getHeaderNames()) {

      if (!SINGLE_VALUE_HEADERS.contains(name.toLowerCase())) {

        for (String value : request.getHeaders(name)) {
          requestHeaders.add(name, value);
        }
      } else {
        requestHeaders.add(name, request.getHeader(name));
      }
    }
    return requestHeaders;
  }

  @Override
  public Headers getResponseHeaders() {
    return responseHeaders;
  }

  @Override
  public URI getRequestURI() {
    return URI.create(request.getRequestURI());
  }

  @Override
  public String getRequestMethod() {
    return request.getMethod().getMethodString();
  }

  @Override
  public HttpContext getHttpContext() {
    return context;
  }

  @Override
  public void close() {
    try {
      outputStream.close();
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  @Override
  public InputStream getRequestBody() {
    return inputStream;
  }

  @Override
  public OutputStream getResponseBody() {
    return outputStream;
  }

  @Override
  public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
    this.statusCode = rCode;

    for (Map.Entry<String, List<String>> stringListEntry : responseHeaders.entrySet()) {
      String name = stringListEntry.getKey();
      List<String> values = stringListEntry.getValue();
      for (String value : values) {
        response.addHeader(name, value);
      }
    }

    if (responseLength == -1) {
      response.setContentLengthLong(0);
    } else if (responseLength == 0) {
      response.setContentLengthLong(-1);
    } else {
      response.setContentLengthLong(responseLength);
    }
    
    response.setStatus(rCode);
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return InetSocketAddress.createUnresolved(request.getRemoteAddr(), request.getRemotePort());
  }

  @Override
  public int getResponseCode() {
    return statusCode;
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return new InetSocketAddress(request.getLocalAddr(), request.getLocalPort());
  }

  @Override
  public String getProtocol() {
    return request.getProtocol().getProtocolString();
  }

  @Override
  public Object getAttribute(String name) {
    return request.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    request.setAttribute(name, value);
  }

  @Override
  public void setStreams(InputStream i, OutputStream o) {
    assert inputStream != null;
    if (i != null) inputStream = i;
    if (o != null) outputStream = o;
  }

  @Override
  public HttpPrincipal getPrincipal() {
    return httpPrincipal;
  }

  public void setPrincipal(HttpPrincipal principal) {
    this.httpPrincipal = principal;
  }
}
