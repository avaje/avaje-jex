package io.avaje.jex.core;

import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiContext;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;

import java.util.Iterator;

/**
 * Provides JsonService using avaje-jsonb.
 */
public class JsonbJsonService implements JsonService {

  private final Jsonb jsonb;

  /**
   * Create with defaults for Jsonb.
   */
  public JsonbJsonService() {
    this.jsonb = Jsonb.newBuilder().build();
  }

  /**
   * Create with a Jsonb instance that might have custom configuration.
   */
  public JsonbJsonService(Jsonb jsonb) {
    this.jsonb = jsonb;
  }

  @Override
  public <T> T jsonRead(Class<T> clazz, SpiContext ctx) {
    // TODO: Handle gzipped content
    return jsonb.type(clazz).fromJson(ctx.inputStream());
  }

  @Override
  public void jsonWrite(Object bean, SpiContext ctx) {
    // gzip compression etc ?
    try (JsonWriter writer = jsonb.writer(ctx.outputStream())) {
      jsonb.type(Object.class).toJson(bean, writer);
    }
  }

  @Override
  public <T> void jsonWriteStream(Iterator<T> iterator, SpiContext ctx) {
    try (JsonWriter writer = jsonb.writer(ctx.outputStream())) {
      writer.pretty(false);
      if (iterator.hasNext()) {
        T first = iterator.next();
        JsonType<T> type = jsonb.typeOf(first);
        type.toJson(first, writer);
        writer.writeNewLine();
        while (iterator.hasNext()) {
          type.toJson(iterator.next(), writer);
          writer.writeNewLine();
        }
      }
    }
  }
}
