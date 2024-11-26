package io.avaje.jex.jdk;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.core.SpiServiceManager;
import io.avaje.jex.spi.SpiContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class CtxServiceManager implements SpiServiceManager {

  private final String scheme;
  private final String contextPath;

  private final SpiServiceManager delegate;

  CtxServiceManager(SpiServiceManager delegate, String scheme, String contextPath) {
    this.delegate = delegate;
    this.scheme = scheme;
    this.contextPath = contextPath;
  }

  OutputStream createOutputStream(JdkContext jdkContext) {
    return new BufferedOutStream(jdkContext);
  }

  String scheme() {
    return scheme;
  }

  public String url() {
    return scheme + "://";
  }

  public String contextPath() {
    return contextPath;
  }

  @Override
  public <T> T jsonRead(Class<T> clazz, InputStream is) {
    return delegate.jsonRead(clazz, is);
  }

  @Override
  public void jsonWrite(Object bean, OutputStream os) {
    delegate.jsonWrite(bean, os);
  }

  @Override
  public <E> void jsonWriteStream(Stream<E> stream, OutputStream os) {
    delegate.jsonWriteStream(stream, os);
  }

  @Override
  public <E> void jsonWriteStream(Iterator<E> iterator, OutputStream os) {
    delegate.jsonWriteStream(iterator, os);
  }

  @Override
  public void maybeClose(Object iterator) {
    delegate.maybeClose(iterator);
  }

  @Override
  public Routing.Type lookupRoutingType(String method) {
    return delegate.lookupRoutingType(method);
  }

  @Override
  public void handleException(SpiContext ctx, Exception e) {
    delegate.handleException(ctx, e);
  }

  @Override
  public void render(Context ctx, String name, Map<String, Object> model) {
    delegate.render(ctx, name, model);
  }

  @Override
  public String requestCharset(Context ctx) {
    return delegate.requestCharset(ctx);
  }

  @Override
  public Map<String, List<String>> formParamMap(Context ctx, String charset) {
    return delegate.formParamMap(ctx, charset);
  }

  @Override
  public Map<String, List<String>> parseParamMap(String body, String charset) {
    return delegate.parseParamMap(body, charset);
  }
}
