package io.avaje.jex;

import java.util.Map;

public interface Context {

  String matchedPath();

  <T> T bodyAsClass(Class<T> clazz);

  byte[] bodyAsBytes();

  String body();

  Map<String, String> pathParams();

  String pathParam(String name);

  String queryParam(String name);

  Context text(String content);

  Context json(Object bean);

  Context contentType(String contentType);
}
