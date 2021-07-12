package io.avaje.jex.jdk;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.UploadedFile;
import io.avaje.jex.spi.HeaderKeys;
import io.avaje.jex.spi.SpiContext;
import io.avaje.jex.spi.SpiRoutes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

class JdkContext implements Context, SpiContext {

  private final ServiceManager mgr;
  private final String path;
  private final SpiRoutes.Params params;
  private final HttpExchange exchange;
  private Routing.Type mode;
  private Map<String, List<String>> formParamMap;
  private int statusCode;

  JdkContext(ServiceManager mgr, HttpExchange exchange, String path, SpiRoutes.Params params) {
    this.mgr = mgr;
    this.exchange = exchange;
    this.path = path;
    this.params = params;
  }

  @Override
  public String matchedPath() {
    return path;
  }

  @Override
  public Context attribute(String key, Object value) {
    exchange.setAttribute(key, value);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T attribute(String key) {
    return (T)exchange.getAttribute(key);
  }

  @Override
  public Map<String, Object> attributeMap() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context cookie(Cookie cookie) {
    return null;
  }

  @Override
  public String cookie(String name) {
    return null;
  }

  @Override
  public Map<String, String> cookieMap() {
    return null;
  }

  @Override
  public Context cookie(String name, String value) {
    return null;
  }

  @Override
  public Context cookie(String name, String value, int maxAge) {
    return null;
  }

  @Override
  public Context removeCookie(String name) {
    return null;
  }

  @Override
  public Context removeCookie(String name, String path) {
    return null;
  }

  @Override
  public void redirect(String location) {

  }

  @Override
  public void redirect(String location, int httpStatusCode) {

  }

  @Override
  public <T> T bodyAsClass(Class<T> beanType) {
    return mgr.jsonRead(beanType, this);
  }

  @Override
  public byte[] bodyAsBytes() {
    try {
      return exchange.getRequestBody().readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public String body() {
    return new String(bodyAsBytes(), StandardCharsets.UTF_8);
  }

  @Override
  public long contentLength() {
    final String len = header(HeaderKeys.CONTENT_LENGTH);
    return len == null ? 0 : Long.parseLong(len);
  }

  @Override
  public String contentType() {
    return header(exchange.getRequestHeaders(), HeaderKeys.CONTENT_TYPE);
  }

  @Override
  public String responseHeader(String key) {
    return header(exchange.getResponseHeaders(), key);
  }

  private String header(Headers headers, String name) {
    final List<String> values = headers.get(name);
    return values.isEmpty() ? null : values.get(0);
  }

  @Override
  public Context contentType(String contentType) {
    exchange.getResponseHeaders().set(HeaderKeys.CONTENT_TYPE, contentType);
    return this;
  }

  @Override
  public String splat(int position) {
    return params.splats.get(position);
  }

  @Override
  public List<String> splats() {
    return params.splats;
  }

  @Override
  public Map<String, String> pathParamMap() {
    return params.pathParams;
  }

  @Override
  public String pathParam(String name) {
    return params.pathParams.get(name);
  }

  @Override
  public String queryParam(String name) {
    return null;
  }

  @Override
  public List<String> queryParams(String name) {
    return null;
  }

  @Override
  public Map<String, String> queryParamMap() {
    return null;
  }

  @Override
  public String queryString() {
    return null;
  }

  @Override
  public Map<String, List<String>> formParamMap() {
    if (formParamMap == null) {
      formParamMap = initFormParamMap();
    }
    return formParamMap;
  }

  private Map<String, List<String>> initFormParamMap() {
    final String charset = mgr.requestCharset(this);
    return mgr.formParamMap(this, charset);
  }

  @Override
  public String scheme() {
    return null;
  }

  @Override
  public Context sessionAttribute(String key, Object value) {
    return null;
  }

  @Override
  public <T> T sessionAttribute(String key) {
    return null;
  }

  @Override
  public Map<String, Object> sessionAttributeMap() {
    return null;
  }

  @Override
  public String url() {
    return null;
  }

  @Override
  public String fullUrl() {
    return null;
  }

  @Override
  public String contextPath() {
    return null;
  }

  @Override
  public Context status(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  @Override
  public int status() {
    return statusCode;
  }

  @Override
  public Context json(Object bean) {
    contentType(APPLICATION_JSON);
    mgr.jsonWrite(bean, this);
    return this;
  }

  @Override
  public <E> Context jsonStream(Stream<E> stream) {
    contentType(APPLICATION_X_JSON_STREAM);
    mgr.jsonWriteStream(stream, this);
    return this;
  }

  @Override
  public <E> Context jsonStream(Iterator<E> iterator) {
    contentType(APPLICATION_X_JSON_STREAM);
    mgr.jsonWriteStream(iterator, this);
    return this;
  }

  @Override
  public Context text(String content) {
    contentType(TEXT_PLAIN_UTF8);
    return write(content);
  }

  @Override
  public Context html(String content) {
    contentType(TEXT_HTML_UTF8);
    return write(content);
  }

  @Override
  public Context write(String content) {
    try {
      writeBytes(content.getBytes(StandardCharsets.UTF_8));
      return this;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  void writeBytes(byte[] bytes) throws IOException {
    exchange.sendResponseHeaders(statusCode(), bytes.length);
    final OutputStream os = exchange.getResponseBody();
    os.write(bytes);
    os.flush();
    os.close();
  }

  int statusCode() {
    return statusCode == 0 ? 200 : statusCode;
  }

  @Override
  public Context render(String name, Map<String, Object> model) {
    mgr.render(this, name, model);
    return this;
  }

  @Override
  public Map<String, String> headerMap() {
    Map<String, String> map = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
      final List<String> value = entry.getValue();
      if (!value.isEmpty()) {
        map.put(entry.getKey(), value.get(0));
      }
    }
    return map;
  }

  @Override
  public String header(String key) {
    return header(exchange.getRequestHeaders(), key);
  }

  @Override
  public void header(String key, String value) {
    exchange.getResponseHeaders().add(key, value);
  }

  @Override
  public String host() {
    return exchange.getRemoteAddress().getHostString();
  }

  @Override
  public String ip() {
    return null;//exchange.getRemoteAddress();
  }

  @Override
  public boolean isMultipart() {
    // not really supported
    return false;
  }

  @Override
  public boolean isMultipartFormData() {
    // not really supported
    return false;
  }

  @Override
  public String method() {
    return exchange.getRequestMethod();
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public int port() {
    return 0;
  }

  @Override
  public String protocol() {
    return exchange.getProtocol();
  }

  @Override
  public UploadedFile uploadedFile(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<UploadedFile> uploadedFiles(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<UploadedFile> uploadedFiles() {
    throw new UnsupportedOperationException();
  }

  @Override
  public OutputStream outputStream() {
    return mgr.createOutputStream(this);
  }

  @Override
  public InputStream inputStream() {
    return exchange.getRequestBody();
  }

  @Override
  public void setMode(Routing.Type type) {
    this.mode = type;
  }

  HttpExchange exchange() {
    return exchange;
  }

}
