package io.avaje.jex.core.json;

import io.avaje.jex.spi.JsonService;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/** Provides JsonService using avaje-jsonb. */
public final class JsonbJsonService implements JsonService {

  private final Jsonb jsonb;

  /** Create with defaults for Jsonb. */
  public JsonbJsonService() {
    this.jsonb = Jsonb.builder().build();
  }

  /** Create with a Jsonb instance that might have custom configuration. */
  public JsonbJsonService(Jsonb jsonb) {
    this.jsonb = jsonb;
  }

  @Override
  public <T> T jsonRead(Class<T> clazz, InputStream is) {
    return jsonb.type(clazz).fromJson(is);
  }

  @Override
  public void jsonWrite(Object bean, OutputStream os) {
    jsonb.toJson(bean, os);
  }

  @Override
  public <T> void jsonWriteStream(Iterator<T> iterator, OutputStream os) {
    try (JsonWriter writer = jsonb.writer(os)) {
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
