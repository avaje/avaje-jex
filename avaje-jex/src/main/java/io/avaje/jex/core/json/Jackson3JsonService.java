package io.avaje.jex.core.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.avaje.jex.spi.JsonService;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

/** Jackson 3.x JsonService */
public final class Jackson3JsonService implements JsonService {

  private final ObjectMapper mapper;
  private final Map<String, JavaType> javaTypes = new ConcurrentHashMap<>();

  /** Create with defaults for Jackson */
  public Jackson3JsonService() {
    this.mapper = new ObjectMapper();
  }

  /** Create with a Jackson instance that might have custom configuration. */
  public Jackson3JsonService(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public <T> T fromJson(Type type, InputStream is) {
    final var javaType = javaType(type);
    return mapper.readValue(is, javaType);
  }

  @Override
  public <T> T fromJson(Type type, byte[] data) {
    final var javaType = javaType(type);
    return mapper.readValue(data, javaType);
  }

  private JavaType javaType(Type type) {
    return javaTypes.computeIfAbsent(
        type.getTypeName(), k -> mapper.getTypeFactory().constructType(type));
  }

  @Override
  public void toJson(Object bean, OutputStream os) {
    try {
      try (var generator = mapper.createGenerator(os)) {
        // only flush to underlying OutputStream on success
        generator.configure(StreamWriteFeature.AUTO_CLOSE_TARGET, false);
        generator.configure(StreamWriteFeature.FLUSH_PASSED_TO_STREAM, false);
        generator.configure(StreamWriteFeature.AUTO_CLOSE_CONTENT, false);
        mapper.writeValue(generator, bean);
      }
      os.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public String toJsonString(Object bean) {
    return mapper.writeValueAsString(bean);
  }

  @Override
  public <T> void toJsonStream(Iterator<T> iterator, OutputStream os) {
    try (var generator = mapper.createGenerator(os)) {
      while (iterator.hasNext()) {
        write(iterator, generator);
      }
    }
  }

  private <T> void write(Iterator<T> iterator, final JsonGenerator generator) {
    mapper.writeValue(generator, iterator.next());
    generator.writeRaw('\n');
  }
}
