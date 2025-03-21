package io.avaje.helidon.http.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

public class JettyHttpExchange extends HttpExchange implements JettyExchange {
  private final GrizzlyHttpExchangeDelegate delegate;

  public JettyHttpExchange(HttpContext context, Request req, Response resp) {

    delegate = new GrizzlyHttpExchangeDelegate(context, req, resp);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public Headers getRequestHeaders() {
    return delegate.getRequestHeaders();
  }

  @Override
  public Headers getResponseHeaders() {
    return delegate.getResponseHeaders();
  }

  @Override
  public URI getRequestURI() {
    return delegate.getRequestURI();
  }

  @Override
  public String getRequestMethod() {
    return delegate.getRequestMethod();
  }

  @Override
  public HttpContext getHttpContext() {
    return delegate.getHttpContext();
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }

  @Override
  public InputStream getRequestBody() {
    return delegate.getRequestBody();
  }

  @Override
  public OutputStream getResponseBody() {
    return delegate.getResponseBody();
  }

  @Override
  public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
    delegate.sendResponseHeaders(rCode, responseLength);
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return delegate.getRemoteAddress();
  }

  @Override
  public int getResponseCode() {
    return delegate.getResponseCode();
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return delegate.getLocalAddress();
  }

  @Override
  public String getProtocol() {
    return delegate.getProtocol();
  }

  @Override
  public Object getAttribute(String name) {
    return delegate.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    delegate.setAttribute(name, value);
  }

  @Override
  public void setStreams(InputStream i, OutputStream o) {
    delegate.setStreams(i, o);
  }

  @Override
  public HttpPrincipal getPrincipal() {
    return delegate.getPrincipal();
  }

  @Override
  public void setPrincipal(HttpPrincipal principal) {
    delegate.setPrincipal(principal);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
