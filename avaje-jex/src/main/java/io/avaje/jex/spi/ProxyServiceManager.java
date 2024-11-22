package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Provides a delegating proxy to a SpiServiceManager.
 * <p>
 * Can be used by specific implementations like Jetty and JDK Http Server to add core functionality
 * to provide to the specific context implementation.
 */
public abstract class ProxyServiceManager implements SpiServiceManager {

  protected final SpiServiceManager delegate;

  protected ProxyServiceManager(SpiServiceManager delegate) {
    this.delegate = delegate;
  }

  @Override
  public <T> T jsonRead(Class<T> clazz, SpiContext ctx) {
    return delegate.jsonRead(clazz, ctx);
  }

  @Override
  public void jsonWrite(Object bean, SpiContext ctx) {
    delegate.jsonWrite(bean, ctx);
  }

  @Override
  public <E> void jsonWriteStream(Stream<E> stream, SpiContext ctx) {
    delegate.jsonWriteStream(stream, ctx);
  }

  @Override
  public <E> void jsonWriteStream(Iterator<E> iterator, SpiContext ctx) {
    delegate.jsonWriteStream(iterator, ctx);
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
