package io.avaje.jex.jetty;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.UploadedFile;
import io.avaje.jex.http.RedirectResponse;
import io.avaje.jex.spi.HeaderKeys;
import io.avaje.jex.spi.SpiContext;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.FormFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Fields;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

class JexHttpContext implements SpiContext {

  private final ServiceManager mgr;
  protected final Request req;
  private final Response res;
  private final Map<String, String> pathParams;
  private final String matchedPath;
  private String characterEncoding;
  private Routing.Type mode;
  private Map<String, List<String>> formParamMap;
  private Fields queryParams;

  JexHttpContext(ServiceManager mgr, Request req, Response res, String matchedPath) {
    this.mgr = mgr;
    this.req = req;
    this.res = res;
    this.matchedPath = matchedPath;
    this.pathParams = emptyMap();
  }

  JexHttpContext(ServiceManager mgr, Request req, Response res, String matchedPath, Map<String, String> pathParams) {
    this.mgr = mgr;
    this.req = req;
    this.res = res;
    this.matchedPath = matchedPath;
    this.pathParams = pathParams;
  }

  @Override
  public void setMode(Routing.Type mode) {
    this.mode = mode;
  }

  private String characterEncoding() {
    if (characterEncoding == null) {
      characterEncoding = mgr.requestCharset(this);
    }
    return characterEncoding;
  }

  public Request req() {
    return req;
  }

  public Response res() {
    return res;
  }

  @Override
  public boolean isCommitted() {
    return res.isCommitted();
  }

  @Override
  public void reset() {
    res.reset();
  }

  @Override
  public Context attribute(String key, Object value) {
    req.setAttribute(key, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T attribute(String key) {
    return (T) req.getAttribute(key);
  }

  @Override
  public String cookie(String name) {
    List<HttpCookie> cookies = Request.getCookies(req);
    if (cookies != null) {
      for (HttpCookie cookie : cookies) {
        if (cookie.getName().equals(name)) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  @Override
  public Map<String, String> cookieMap() {
    List<HttpCookie> cookies = Request.getCookies(req);
    if (cookies == null) {
      return emptyMap();
    }
    final Map<String, String> map = new LinkedHashMap<>();
    for (HttpCookie cookie : cookies) {
      map.put(cookie.getName(), cookie.getValue());
    }
    return map;
  }

  @Override
  public Context cookie(Cookie cookie) {
    var newCookie = HttpCookie.build(cookie.name(), cookie.value());
    var path = cookie.path() == null ? "/" : cookie.path();
    newCookie.path(path);
    final String domain = cookie.domain();
    if (domain != null) {
      newCookie.domain(domain);
    }
    final Duration duration = cookie.maxAge();
    if (duration != null) {
      newCookie.maxAge((int) duration.toSeconds());
    }
    newCookie.httpOnly(cookie.httpOnly());
    newCookie.secure(cookie.secure());
    Response.addCookie(res, newCookie.build());
    return this;
  }

  @Override
  public Context cookie(String name, String value, int maxAge) {
    var newCookie = HttpCookie.build(name, value)
      .path("/")
      .maxAge(maxAge)
      .build();
    Response.addCookie(res, newCookie);
    return this;
  }

  @Override
  public Context cookie(String name, String value) {
    return cookie(name, value, -1);
  }

  @Override
  public Context removeCookie(String name) {
    return removeCookie(name, null);
  }

  @Override
  public Context removeCookie(String name, String path) {
    if (path == null) {
      path = "/";
    }
    var newCookie = HttpCookie.build(name, "")
      .path(path)
      .maxAge(0);
    Response.addCookie(res, newCookie.build());
    return this;
  }

  @Override
  public void redirect(String location) {
    redirect(location, 302);
  }

  @Override
  public void redirect(String location, int statusCode) {
    //Response.sendRedirect();
    res.getHeaders().add(HeaderKeys.LOCATION, location);
    status(statusCode);
    if (mode == Routing.Type.BEFORE) {
      throw new RedirectResponse(statusCode);
    }
  }

  @Override
  public void performRedirect() {
    // do nothing
  }

  @Override
  public String matchedPath() {
    return matchedPath;
  }

  @Override
  public InputStream inputStream() {
    return Request.asInputStream(req);
  }

  @Override
  public <T> T bodyAsClass(Class<T> clazz) {
    return mgr.jsonRead(clazz, this);
  }

  @Override
  public byte[] bodyAsBytes() {
    try {
      return Content.Source.asByteBuffer(req).array();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public String body() {
    try {
      return Content.Source.asString(req, Charset.forName(characterEncoding()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public long contentLength() {
    return req.getLength();
  }

  @Override
  public Map<String, String> pathParamMap() {
    return pathParams;
  }

  @Override
  public String pathParam(String name) {
    return pathParams.get(name);
  }

  private Fields _queryParams() {
    if (queryParams == null) {
      queryParams = Request.extractQueryParameters(req);
    }
    return queryParams;
  }

  @Override
  public String queryParam(String name) {
    Fields.Field field = _queryParams().get(name);
    return field == null ? null : field.getValue();
  }

  @Override
  public List<String> queryParams(String name) {
    Fields.Field field = _queryParams().get(name);
    return field == null ? emptyList() : field.getValues();
  }

  @Override
  public Map<String, String> queryParamMap() {
    final Map<String, String> map = new LinkedHashMap<>();
    for (Fields.Field field : _queryParams()) {
      map.put(field.getName(), field.getValue());
    }
    return map;
  }

  @Override
  public String queryString() {
    return req.getHttpURI().getQuery();
  }

  @Override
  public Map<String, List<String>> formParamMap() {
    if (formParamMap == null) {
      formParamMap = initFormParamMap();
    }
    return formParamMap;
  }

  private Map<String, List<String>> initFormParamMap() {
    if (isMultipartFormData()) {
      throw new IllegalStateException("not supported yet");
    }
    Map<String, List<String>> map = new LinkedHashMap<>();
    try {
      for (Fields.Field formField : FormFields.from(req).get()) {
        map.put(formField.getName(), formField.getValues());
      }
      return map;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String scheme() {
    return req.getHttpURI().getScheme();
  }

  @Override
  public Context sessionAttribute(String key, Object value) {
    req.getSession(true).setAttribute(key, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T sessionAttribute(String key) {
    var session = req.getSession(false);
    return session == null ? null : (T) session.getAttribute(key);
  }

  @Override
  public Map<String, Object> sessionAttributeMap() {
    final Map<String, Object> map = new LinkedHashMap<>();
    final var session = req.getSession(false);
    if (session == null) {
      return emptyMap();
    }
    Set<String> nameSet = session.getAttributeNameSet();
    for (String name : nameSet) {
      map.put(name, session.getAttribute(name));
    }
    return map;
  }

  @Override
  public String url() {
    return req.getHttpURI().getPath();
  }

  @Override
  public String fullUrl() {
    return req.getHttpURI().getPathQuery();
  }

  @Override
  public String contextPath() {
    String path = req.getContext().getContextPath();
    return path == null ? "" : path;
  }

  @Override
  public Context status(int statusCode) {
    res.setStatus(statusCode);
    return this;
  }

  @Override
  public int status() {
    return res.getStatus();
  }

  @Override
  public String contentType() {
    return req.getHeaders().get(HttpHeader.CONTENT_TYPE);
  }

  @Override
  public Context contentType(String contentType) {
    res.getHeaders().add(HttpHeader.CONTENT_TYPE, contentType);
    return this;
  }

  public Map<String, String> headerMap() {
    Map<String, String> map = new LinkedHashMap<>();
    for (HttpField header : req.getHeaders()) {
      map.put(header.getName(), header.getValue());
    }
    return map;
  }

  @Override
  public String responseHeader(String key) {
    return res.getHeaders().get(key);
  }

  @Override
  public String header(String key) {
    return req.getHeaders().get(key);
  }

  @Override
  public Context header(String key, String value) {
    res.getHeaders().add(key, value);
    return this;
  }

  @Override
  public String host() {
    return req.getHttpURI().getHost();
  }

  @Override
  public String ip() {
    return Request.getRemoteAddr(req);
  }

  @Override
  public boolean isMultipart() {
    final String type = header(HeaderKeys.CONTENT_TYPE);
    return type != null && type.toLowerCase().contains("multipart/");
  }

  @Override
  public boolean isMultipartFormData() {
    final String type = header(HeaderKeys.CONTENT_TYPE);
    return type != null && type.toLowerCase().contains("multipart/form-data");
  }

  @Override
  public String method() {
    return req.getMethod();
  }

  @Override
  public String path() {
    return req.getHttpURI().getDecodedPath();
  }

  @Override
  public int port() {
    return Request.getServerPort(req);
  }

  @Override
  public String protocol() {
    //req.getHttpURI().getScheme().
    throw new UnsupportedOperationException();
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

  /**
   * Write plain text content to the response.
   */
  @Override
  public Context text(String content) {
    contentType(TEXT_PLAIN);
    return write(content);
  }

  /**
   * Write html content to the response.
   */
  @Override
  public Context html(String content) {
    contentType(TEXT_HTML);
    return write(content);
  }

  @Override
  public Context write(String content) {
    Content.Sink.write(res, true, content, null);
    return this;
  }

  @Override
  public Context render(String name, Map<String, Object> model) {
    mgr.render(this, name, model);
    return this;
  }

  @Override
  public OutputStream outputStream() {
    return Content.Sink.asOutputStream(res);
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
}
