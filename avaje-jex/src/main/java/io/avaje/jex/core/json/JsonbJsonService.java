package io.avaje.jex.core.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Iterator;

import io.avaje.jex.spi.JsonService;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

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
  public <T> T fromJson(Type clazz, InputStream is) {
    return jsonb.<T>type(clazz).fromJson(is);
  }

  @Override
  public <T> T fromJson(Type clazz, byte[] data) {
    return jsonb.<T>type(clazz).fromJson(data);
  }

  @Override
  public void toJson(Object bean, OutputStream os) {
    jsonb.toJson(bean, new NoFlushJsonOutput(os));
  }

  @Override
  public String toJsonString(Object bean) {
    return jsonb.toJson(bean);
  }

  @Override
  public <T> void toJsonStream(Iterator<T> iterator, OutputStream os) {
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
