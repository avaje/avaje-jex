package io.avaje.jex.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.jex.spi.IORuntimeException;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiContext;

import java.io.IOException;

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
      return mapper.readValue(ctx.bodyAsBytes(), clazz);
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  @Override
  public void jsonWrite(Object bean, SpiContext ctx) {
    try {
      final byte[] bytes = mapper.writeValueAsBytes(bean);
      // TODO: compression etc
      ctx.outputStream().write(bytes);
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }
}
