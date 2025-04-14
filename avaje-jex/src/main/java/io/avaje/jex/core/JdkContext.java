package io.avaje.jex.core;

import static io.avaje.jex.core.Constants.APPLICATION_JSON;
import static io.avaje.jex.core.Constants.APPLICATION_X_JSON_STREAM;
import static io.avaje.jex.core.Constants.TEXT_HTML_UTF8;
import static io.avaje.jex.core.Constants.TEXT_PLAIN_UTF8;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.net.ssl.SSLSession;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpStatus;
import io.avaje.jex.http.RedirectException;
import io.avaje.jex.security.BasicAuthCredentials;
import io.avaje.jex.security.Role;
import io.avaje.jex.spi.JsonService;

final class JdkContext implements Context {

  private static final String SET_COOKIE = "Set-Cookie";
  private static final String COOKIE = "Cookie";
  private final ServiceManager mgr;
  private final String matchedPath;
  private final Map<String, String> pathParams;
  private final Map<String, Object> attributes = new HashMap<>();
  private final Set<Role> roles;
  private final HttpExchange exchange;
  private Mode mode;
  private Map<String, List<String>> formParams;
  private Map<String, List<String>> queryParams;
  private Map<String, String> cookieMap;
  private int statusCode;
  private byte[] bodyBytes;

  private Charset characterEncoding;

  JdkContext(
      ServiceManager mgr,
      HttpExchange exchange,
      String path,
      Map<String, String> pathParams,
      Set<Role> roles) {
    this.mgr = mgr;
    this.roles = roles;
    this.exchange = exchange;
    this.matchedPath = path;
    this.pathParams = pathParams;
  }

  /** Create when no route matched. */
  JdkContext(ServiceManager mgr, HttpExchange exchange, String path, Set<Role> roles) {
    this.mgr = mgr;
    this.roles = roles;
    this.exchange = exchange;
    this.matchedPath = path;
    this.pathParams = null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T attribute(String key) {
    return (T) attributes.get(key);
  }

  @Override
  public Context attribute(String key, Object value) {
    attributes.put(key, value);
    return this;
  }

  @Override
  public BasicAuthCredentials basicAuthCredentials() {
    return getBasicAuthCredentials(header("Authorization"));
  }

  private static BasicAuthCredentials getBasicAuthCredentials(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
      return null;
    }

    String base64Credentials = authorizationHeader.substring("Basic ".length());
    byte[] decodedCredentials = Base64.getDecoder().decode(base64Credentials);
    String credentialsString = new String(decodedCredentials);

    String[] credentials = credentialsString.split(":", 2);
    if (credentials.length != 2) {
      throw new IllegalStateException("Invalid Basic Auth header");
    }

    return new BasicAuthCredentials(credentials[0], credentials[1]);
  }

  @Override
  public String body() {
    return new String(bodyAsBytes(), characterEncoding());
  }

  @Override
  public byte[] bodyAsBytes() {
    try {
      if (bodyBytes == null) {
        bodyBytes = exchange.getRequestBody().readAllBytes();
      }
      return bodyBytes;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public InputStream bodyAsInputStream() {
    return exchange.getRequestBody();
  }

  @Override
  public <T> T bodyAsType(Type beanType) {
    return mgr.fromJson(beanType, bodyAsBytes());
  }

  @Override
  public <T> T bodyStreamAsType(Type beanType) {
    return bodyBytes == null ? mgr.fromJson(beanType, bodyAsInputStream()) : bodyAsType(beanType);
  }

  private Charset characterEncoding() {
    if (characterEncoding == null) {
      characterEncoding = mgr.requestCharset(this);
    }
    return characterEncoding;
  }

  @Override
  public long contentLength() {
    final String len = header(Constants.CONTENT_LENGTH);
    return len == null ? 0 : Long.parseLong(len);
  }

  @Override
  public String contentType() {
    return header(exchange.getRequestHeaders(), Constants.CONTENT_TYPE);
  }

  @Override
  public Context contentType(String contentType) {
    exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, contentType);
    return this;
  }

  @Override
  public String contextPath() {
    return exchange.getHttpContext().getPath();
  }

  @Override
  public Context cookie(Cookie cookie) {
    header(SET_COOKIE, cookie.toString());
    return this;
  }

  @Override
  public String cookie(String name) {
    return cookieMap().get(name);
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
  public Map<String, String> cookieMap() {
    if (cookieMap == null) {
      cookieMap = parseCookies();
    }
    return cookieMap;
  }

  @Override
  public HttpExchange exchange() {
    return exchange;
  }

  @Override
  public Map<String, List<String>> formParamMap() {
    if (formParams == null) {
      formParams = initFormParamMap();
    }
    return formParams;
  }

  private String header(Headers headers, String name) {
    return headers.getFirst(name);
  }

  @Override
  public String header(String key) {
    return header(exchange.getRequestHeaders(), key);
  }

  @Override
  public List<String> headerValues(String key) {
    return exchange.getRequestHeaders().get(key);
  }

  @Override
  public Context header(String key, List<String> value) {
    exchange.getResponseHeaders().put(key, value);
    return this;
  }

  @Override
  public Context header(String key, String value) {
    exchange.getResponseHeaders().add(key, value);
    return this;
  }

  @Override
  public Map<String, String> headerMap() {
    Map<String, String> map = new LinkedHashMap<>();
    for (var entry : exchange.getRequestHeaders().entrySet()) {
      final List<String> value = entry.getValue();
      if (!value.isEmpty()) {
        map.put(entry.getKey(), value.getFirst());
      }
    }
    return map;
  }

  @Override
  public Context headerMap(Map<String, List<String>> map) {
    exchange.getResponseHeaders().putAll(map);
    return this;
  }

  @Override
  public Headers requestHeaders() {
    return exchange.getRequestHeaders();
  }

  @Override
  public Headers responseHeaders() {
    return exchange.getResponseHeaders();
  }

  @Override
  public List<String> responseHeaderValues(String key) {
    return exchange.getResponseHeaders().get(key);
  }

  @Override
  public String host() {
    return header(Constants.HOST);
  }

  @Override
  public void html(String content) {
    contentType(TEXT_HTML_UTF8);
    write(content);
  }

  private Map<String, List<String>> initFormParamMap() {
    return mgr.formParamMap(this, characterEncoding());
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
  public void json(Object bean) {
    contentType(APPLICATION_JSON);
    mgr.toJson(bean, outputStream());
  }

  @Override
  public <E> void jsonStream(Iterator<E> iterator) {
    contentType(APPLICATION_X_JSON_STREAM);
    mgr.toJsonStream(iterator, outputStream());
  }

  @Override
  public <E> void jsonStream(Stream<E> stream) {
    contentType(APPLICATION_X_JSON_STREAM);
    mgr.toJsonStream(stream, outputStream());
  }

  @Override
  public String matchedPath() {
    return matchedPath;
  }

  @Override
  public String method() {
    return exchange.getRequestMethod();
  }

  @Override
  public OutputStream outputStream() {
    return mgr.createOutputStream(this);
  }

  private Map<String, String> parseCookies() {
    final String cookieHeader = header(exchange.getRequestHeaders(), COOKIE);
    if (cookieHeader == null || cookieHeader.isEmpty()) {
      return emptyMap();
    }
    return CookieParser.parse(cookieHeader);
  }

  @Override
  public String path() {
    return exchange.getRequestURI().getPath();
  }

  @Override
  public String pathParam(String name) {
    return pathParams.get(name);
  }

  @Override
  public Map<String, String> pathParamMap() {
    return pathParams;
  }

  public void performRedirect() {
    try {
      exchange.sendResponseHeaders(statusCode(), -1);
      exchange.getResponseBody().close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
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
  public String queryParam(String name) {
    final List<String> vals = queryParams(name);
    return vals.isEmpty() ? null : vals.getFirst();
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
        single.put(entry.getKey(), value.getFirst());
      }
    }
    return single;
  }

  private Map<String, List<String>> queryParams() {
    if (queryParams == null) {
      queryParams = mgr.parseParamMap(queryString(), StandardCharsets.UTF_8);
    }
    return queryParams;
  }

  @Override
  public List<String> queryParams(String name) {
    final List<String> vals = queryParams().get(name);
    return vals == null ? emptyList() : vals;
  }

  @Override
  public String queryString() {
    return exchange.getRequestURI().getQuery();
  }

  @Override
  public void redirect(String location) {
    redirect(location, HttpStatus.FOUND_302.status());
  }

  @Override
  public void redirect(String location, int statusCode) {
    header(Constants.LOCATION, location);
    status(statusCode);
    if (mode != Mode.EXCHANGE) {
      throw new RedirectException("Redirect");
    } else {
      performRedirect();
    }
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
  public Context render(String name, Map<String, Object> model) {
    mgr.render(this, name, model);
    return this;
  }

  @Override
  public String responseHeader(String key) {
    return header(exchange.getResponseHeaders(), key);
  }

  @Override
  public boolean responseSent() {
    return exchange.getResponseCode() > 1;
  }

  @Override
  public Set<Role> routeRoles() {
    return roles;
  }

  @Override
  public String scheme() {
    return mgr.scheme();
  }

  void setMode(Mode type) {
    this.mode = type;
  }

  @Override
  public SSLSession sslSession() {
    return exchange instanceof HttpsExchange ex ? ex.getSSLSession() : null;
  }

  @Override
  public int status() {
    return statusCode;
  }

  @Override
  public Context status(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  int statusCode() {
    return statusCode == 0 ? 200 : statusCode;
  }

  @Override
  public void text(String content) {
    contentType(TEXT_PLAIN_UTF8);
    write(content);
  }

  @Override
  public URI uri() {
    return exchange.getRequestURI();
  }

  @Override
  public void write(byte[] bufferBytes, int length) {
    try (var os = exchange.getResponseBody()) {
      exchange.sendResponseHeaders(statusCode(), length == 0 ? -1 : length);
      os.write(bufferBytes, 0, length);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void write(InputStream is) {
    try (var os = outputStream()) {
      is.transferTo(os);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void write(String content) {
    write(content.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public JsonService jsonService() {
    return mgr.jsonService();
  }
}
