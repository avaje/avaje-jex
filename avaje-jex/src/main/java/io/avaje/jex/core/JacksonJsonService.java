package io.avaje.jex.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;

public class JacksonJsonService implements JsonService {

  private final ObjectMapper mapper;

  public JacksonJsonService() {
    this.mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public JacksonJsonService(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public <T> T jsonRead(Class<T> clazz, SpiContext ctx) {
    try {
      // TODO: Handle gzipped content
      // read direct
      return mapper.readValue(ctx.inputStream(), clazz);
      //return mapper.readValue(ctx.bodyAsBytes(), clazz);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void jsonWrite(Object bean, SpiContext ctx) {
    try {
      // gzip compression etc ?
      OutputStream os = ctx.outputStream();
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
  public <T> void jsonWriteStream(Iterator<T> iterator, SpiContext ctx) {
    final JsonGenerator generator;
    try {
      generator = mapper.createGenerator(ctx.outputStream());
      generator.setPrettyPrinter(null);
      try {
        while (iterator.hasNext()) {
          try {
            mapper.writeValue(generator, iterator.next());
            generator.writeRaw('\n');
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        }
      } finally {
        generator.flush();
        generator.close();
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
