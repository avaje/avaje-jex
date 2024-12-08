package io.avaje.jex.core.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.avaje.jex.spi.JsonService;

/** Jackson JsonService */
public final class JacksonJsonService implements JsonService {

  private final ObjectMapper mapper;
  private final Map<String, JavaType> javaTypes = new ConcurrentHashMap<>();

  /** Create with defaults for Jackson */
  public JacksonJsonService() {
    this.mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  /** Create with a Jackson instance that might have custom configuration. */
  public JacksonJsonService(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public <T> T fromJson(Class<T> type, InputStream is) {
    try {
      return mapper.readValue(is, type);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public <T> T fromJson(Type type, InputStream is) {
    try {
      final var javaType =
        javaTypes.computeIfAbsent(
          type.getTypeName(), k -> mapper.getTypeFactory().constructType(type));

      return mapper.readValue(is, javaType);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void toJson(Object bean, OutputStream os) {
    try {
      try (JsonGenerator generator = mapper.createGenerator(os)) {
        // only flush to underlying OutputStream on success
        generator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        generator.disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
        generator.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        mapper.writeValue(generator, bean);
        generator.flush();
      }
      os.flush();
      os.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public <T> void toJsonStream(Iterator<T> iterator, OutputStream os) {
    final JsonGenerator generator;
    try {
      generator = mapper.createGenerator(os);
      generator.setPrettyPrinter(null);
      try {
        while (iterator.hasNext()) {
          write(iterator, generator);
        }
      } finally {
        generator.flush();
        generator.close();
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private <T> void write(Iterator<T> iterator, final JsonGenerator generator) {
    try {
      mapper.writeValue(generator, iterator.next());
      generator.writeRaw('\n');
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
