package io.avaje.jex.grizzly.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import javax.net.ssl.SSLSession;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpsExchange;

final class GrizzlyHttpsExchange extends HttpsExchange implements GrizzlyExchange {
  private final GrizzlyHttpExchangeDelegate delegate;

  public GrizzlyHttpsExchange(HttpContext jaxWsContext, Request req, Response resp) {
    delegate = new GrizzlyHttpExchangeDelegate(jaxWsContext, req, resp);
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

  @Override
  public SSLSession getSSLSession() {
    return null;
  }
}
