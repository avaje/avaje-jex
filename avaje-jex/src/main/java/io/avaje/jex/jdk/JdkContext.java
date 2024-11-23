package io.avaje.jex.jdk;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.UploadedFile;
import io.avaje.jex.http.ErrorCode;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.spi.HeaderKeys;
import io.avaje.jex.spi.SpiContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

class JdkContext implements Context, SpiContext {

  private static final String UTF8 = "UTF8";
  private static final int SC_MOVED_TEMPORARILY = 302;
  private static final String SET_COOKIE = "Set-Cookie";
  private static final String COOKIE = "Cookie";
  private final ServiceManager mgr;
  private final String path;
  private final Map<String, String> pathParams;
  private final HttpExchange exchange;
  private Routing.Type mode;
  private Map<String, List<String>> formParams;
  private Map<String, List<String>> queryParams;
  private Map<String, String> cookieMap;
  private int statusCode;
  private String characterEncoding;

  JdkContext(ServiceManager mgr, HttpExchange exchange, String path, Map<String, String> pathParams) {
    this.mgr = mgr;
    this.exchange = exchange;
    this.path = path;
    this.pathParams = pathParams;
  }

  /**
   * Create when no route matched.
   */
  JdkContext(ServiceManager mgr, HttpExchange exchange, String path) {
    this.mgr = mgr;
    this.exchange = exchange;
    this.path = path;
    this.pathParams = null;
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
    return (T) exchange.getAttribute(key);
  }

  private Map<String, String> parseCookies() {
    final String cookieHeader = header(exchange.getRequestHeaders(), COOKIE);
    if (cookieHeader == null || cookieHeader.isEmpty()) {
      return emptyMap();
    }
    return CookieParser.parse(cookieHeader);
  }

  @Override
  public Map<String, String> cookieMap() {
    if (cookieMap == null) {
      cookieMap = parseCookies();
    }
    return cookieMap;
  }

  @Override
  public String cookie(String name) {
    return cookieMap().get(name);
  }

  @Override
  public Context cookie(Cookie cookie) {
    header(SET_COOKIE, cookie.toString());
    return this;
  }

  @Override
  public Context cookie(String name, String value) {
    header(SET_COOKIE, Cookie.of(name, value).toString());
    return this;
  }

  @Override
  public Context cookie(String name, String value, int maxAge) {
    header(SET_COOKIE, Cookie.of(name, value).maxAge(Duration.ofSeconds(maxAge)).toString());
    return this;
  }

  @Override
  public Context removeCookie(String name) {
    header(SET_COOKIE, Cookie.expired(name).path("/").toString());
    return this;
  }

  @Override
  public Context removeCookie(String name, String path) {
    header(SET_COOKIE, Cookie.expired(name).path(path).toString());
    return this;
  }

  @Override
  public void redirect(String location) {
    redirect(location, SC_MOVED_TEMPORARILY);
  }

  @Override
  public void redirect(String location, int statusCode) {
    header(HeaderKeys.LOCATION, location);
    status(statusCode);
    if (mode == Routing.Type.FILTER) {
      throw new HttpResponseException(ErrorCode.REDIRECT);
    } else {
      performRedirect();
    }
  }

  @Override
  public void performRedirect() {
    try {
      exchange.sendResponseHeaders(statusCode(), 0);
      exchange.getResponseBody().close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
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

  private String characterEncoding() {
    if (characterEncoding == null) {
      characterEncoding = mgr.requestCharset(this);
    }
    return characterEncoding;
  }

  @Override
  public String body() {
    return new String(bodyAsBytes(), Charset.forName(characterEncoding()));
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
    return (values == null || values.isEmpty()) ? null : values.get(0);
  }

  @Override
  public Context contentType(String contentType) {
    exchange.getResponseHeaders().set(HeaderKeys.CONTENT_TYPE, contentType);
    return this;
  }

  @Override
  public Map<String, String> pathParamMap() {
    return pathParams;
  }

  @Override
  public String pathParam(String name) {
    return pathParams.get(name);
  }

  @Override
  public String queryParam(String name) {
    final List<String> vals = queryParams(name);
    return vals == null || vals.isEmpty() ? null : vals.get(0);
  }

  private Map<String, List<String>> queryParams() {
    if (queryParams == null) {
      queryParams = mgr.parseParamMap(queryString(), UTF8);
    }
    return queryParams;
  }

  @Override
  public List<String> queryParams(String name) {
    final List<String> vals = queryParams().get(name);
    return vals == null ? emptyList() : vals;
  }

  @Override
  public Map<String, String> queryParamMap() {
    final Map<String, List<String>> map = queryParams();
    if (map.isEmpty()) {
      return emptyMap();
    }
    final Map<String, String> single = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
      final List<String> value = entry.getValue();
      if (value != null && !value.isEmpty()) {
        single.put(entry.getKey(), value.get(0));
      }
    }
    return single;
  }

  @Override
  public String queryString() {
    return exchange.getRequestURI().getQuery();
  }

  @Override
  public Map<String, List<String>> formParamMap() {
    if (formParams == null) {
      formParams = initFormParamMap();
    }
    return formParams;
  }

  private Map<String, List<String>> initFormParamMap() {
    return mgr.formParamMap(this, characterEncoding());
  }

  @Override
  public String scheme() {
    return mgr.scheme();
  }

  @Override
  public String url() {
    return scheme() + "://" + host() + path;
  }

  @Override
  public String contextPath() {
    return mgr.contextPath();
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
  public Context header(String key, String value) {
    exchange.getResponseHeaders().add(key, value);
    return this;
  }

  @Override
  public String host() {
    return header(HeaderKeys.HOST);
  }

  @Override
  public String ip() {
    final InetSocketAddress remote = exchange.getRemoteAddress();
    if (remote == null) {
      return "";
    }
    InetAddress address = remote.getAddress();
    return address == null ? remote.getHostString() : address.getHostAddress();
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
    return exchange.getLocalAddress().getPort();
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

  @Override
  public HttpExchange jdkExchange() {
    return exchange;
  }
}
