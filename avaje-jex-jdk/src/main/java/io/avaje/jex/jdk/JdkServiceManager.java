package io.avaje.jex.jdk;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiContext;
import io.avaje.jex.spi.SpiServiceManager;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class JdkServiceManager implements SpiServiceManager {

  private final SpiServiceManager serviceManager;
  private final long outputBufferMax = 1024;
  private final int outputBufferInitial = 256;

  JdkServiceManager(SpiServiceManager serviceManager) {
    this.serviceManager = serviceManager;
  }

  OutputStream createOutputStream(JdkContext jdkContext) {
    return new BufferedOutStream(jdkContext, outputBufferMax, outputBufferInitial);
  }

  @Override
  public <T> T jsonRead(Class<T> clazz, SpiContext ctx) {
    return serviceManager.jsonRead(clazz, ctx);
  }

  @Override
  public void jsonWrite(Object bean, SpiContext ctx) {
    serviceManager.jsonWrite(bean, ctx);
  }

  @Override
  public <E> void jsonWriteStream(Stream<E> stream, SpiContext ctx) {
    serviceManager.jsonWriteStream(stream, ctx);
  }

  @Override
  public <E> void jsonWriteStream(Iterator<E> iterator, SpiContext ctx) {
    serviceManager.jsonWriteStream(iterator, ctx);
  }

  @Override
  public void maybeClose(Object iterator) {
    serviceManager.maybeClose(iterator);
  }

  @Override
  public Routing.Type lookupRoutingType(String method) {
    return serviceManager.lookupRoutingType(method);
  }

  @Override
  public void handleException(Context ctx, Exception e) {
    serviceManager.handleException(ctx, e);
  }

  @Override
  public void render(Context ctx, String name, Map<String, Object> model) {
    serviceManager.render(ctx, name, model);
  }

  @Override
  public String requestCharset(Context ctx) {
    return serviceManager.requestCharset(ctx);
  }

  @Override
  public Map<String, List<String>> formParamMap(Context ctx, String charset) {
    return serviceManager.formParamMap(ctx, charset);
  }
}
