package io.avaje.jex.grizzly;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.UploadedFile;
import io.avaje.jex.http.RedirectResponse;
import io.avaje.jex.spi.HeaderKeys;
import io.avaje.jex.spi.SpiContext;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.ContentType;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

class GrizzlyContext implements Context, SpiContext {

  private static final ContentType JSON = ContentType.newContentType(APPLICATION_JSON);
  private static final ContentType JSON_STREAM = ContentType.newContentType(APPLICATION_X_JSON_STREAM);
  private static final ContentType HTML_UTF8 = ContentType.newContentType("text/html","utf-8");
  private static final ContentType PLAIN_UTF8 = ContentType.newContentType("text/plain","utf-8");

  private static final String UTF8 = "UTF8";
  private static final int SC_MOVED_TEMPORARILY = 302;
  private final ServiceManager mgr;
  private final String path;
  private final Map<String, String> pathParams;
  private final Request request;
  private final Response response;
  private Routing.Type mode;
  private Map<String, List<String>> formParams;
  private Map<String, List<String>> queryParams;
  private Map<String, String> cookieMap;

  GrizzlyContext(ServiceManager mgr, Request request, Response response, String path, Map<String, String> pathParams) {
    this.mgr = mgr;
    this.request = request;
    this.response = response;
    this.path = path;
    this.pathParams = pathParams;
  }

  /**
   * Create when no route matched.
   */
  GrizzlyContext(ServiceManager mgr, Request request, Response response, String path) {
    this.mgr = mgr;
    this.request = request;
    this.response = response;
    this.path = path;
    this.pathParams = null;
  }

  @Override
  public String matchedPath() {
    return path;
  }

  @Override
  public Context attribute(String key, Object value) {
    request.setAttribute(key, value);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T attribute(String key) {
    return (T) request.getAttribute(key);
  }

  @Override
  public Map<String, Object> attributeMap() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, String> cookieMap() {
    if (cookieMap == null) {
      cookieMap = new LinkedHashMap<>();
      final org.glassfish.grizzly.http.Cookie[] cookies = request.getCookies();
      for (org.glassfish.grizzly.http.Cookie cookie : cookies) {
        cookieMap.put(cookie.getName(), cookie.getValue());
      }
    }
    return cookieMap;
  }

  @Override
  public String cookie(String name) {
    return cookieMap().get(name);
  }

  @Override
  public Context cookie(Cookie cookie) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context cookie(String name, String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context cookie(String name, String value, int maxAge) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context removeCookie(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context removeCookie(String name, String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void redirect(String location) {
    redirect(location, SC_MOVED_TEMPORARILY);
  }

  @Override
  public void redirect(String location, int statusCode) {
    status(statusCode);
    if (mode == Routing.Type.BEFORE) {
      header(HeaderKeys.LOCATION, location);
      throw new RedirectResponse(statusCode);
    } else {
      try {
        response.sendRedirect(location);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  @Override
  public void performRedirect() {
    // TODO check this
  }

  @Override
  public <T> T bodyAsClass(Class<T> beanType) {
    return mgr.jsonRead(beanType, this);
  }

  @Override
  public byte[] bodyAsBytes() {
      return ContextUtil.requestBodyAsBytes(request);
  }

  private String characterEncoding() {
    String encoding = request.getCharacterEncoding();
    return encoding != null ? encoding : UTF8;
  }

  @Override
  public String body() {
    return ContextUtil.requestBodyAsString(request);
  }

  @Override
  public long contentLength() {
    return request.getContentLengthLong();
  }

  @Override
  public String contentType() {
    return request.getContentType();
  }

  @Override
  public String responseHeader(String key) {
    return response.getHeader(key);
  }

  @Override
  public Context contentType(String contentType) {
    response.setContentType(contentType);
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
    final List<String> values = queryParams(name);
    return values == null || values.isEmpty() ? null : values.get(0);
  }

  private Map<String, List<String>> queryParams() {
    if (queryParams == null) {
      queryParams = mgr.parseParamMap(queryString(), characterEncoding());
    }
    return queryParams;
  }

  @Override
  public List<String> queryParams(String name) {
    final List<String> values = queryParams().get(name);
    return values == null ? emptyList() : values;
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
    return request.getQueryString();
  }

  /**
   * Return the first form param value for the specified key or null.
   */
  @Override
  public String formParam(String key) {
    return request.getParameter(key);
  }

  /**
   * Return the first form param value for the specified key or the default value.
   */
  @Override
  public String formParam(String key, String defaultValue) {
    String value = request.getParameter(key);
    return value == null ? defaultValue : value;
  }

  /**
   * Return the form params for the specified key, or empty list.
   */
  @Override
  public List<String> formParams(String key) {
    final String[] values = request.getParameterValues(key);
    return values == null ? emptyList() : asList(values);
  }

  @Override
  public Map<String, List<String>> formParamMap() {
    if (formParams == null) {
      formParams = initFormParamMap();
    }
    return formParams;
  }

  private Map<String, List<String>> initFormParamMap() {
    final Map<String, String[]> parameterMap = request.getParameterMap();
    if (parameterMap.isEmpty()) {
      return emptyMap();
    }
    final Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();
    Map<String, List<String>> map = new LinkedHashMap<>(entries.size());
    for (Map.Entry<String, String[]> entry : entries) {
      map.put(entry.getKey(), asList(entry.getValue()));
    }
    return map;
  }

  @Override
  public String scheme() {
    return request.getScheme();
  }

  @Override
  public Context sessionAttribute(String key, Object value) {
    request.getSession().setAttribute(key, value);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T sessionAttribute(String key) {
    return (T) request.getSession().getAttribute(key);
  }

  @Override
  public Map<String, Object> sessionAttributeMap() {
    return request.getSession().attributes();
  }

  @Override
  public String url() {
    return scheme() + "://" + host() + ":" + port() + path;
  }

  @Override
  public String contextPath() {
    return mgr.contextPath();
  }

  @Override
  public Context status(int statusCode) {
    response.setStatus(statusCode);
    return this;
  }

  @Override
  public int status() {
    return response.getStatus();
  }


  @Override
  public Context json(Object bean) {
    response.setContentType(JSON);
    mgr.jsonWrite(bean, this);
    return this;
  }

  @Override
  public <E> Context jsonStream(Stream<E> stream) {
    response.setContentType(JSON_STREAM);
    mgr.jsonWriteStream(stream, this);
    return this;
  }

  @Override
  public <E> Context jsonStream(Iterator<E> iterator) {
    response.setContentType(JSON_STREAM);
    mgr.jsonWriteStream(iterator, this);
    return this;
  }

  @Override
  public Context text(String content) {
    response.setContentType(PLAIN_UTF8);
    return write(content);
  }

  @Override
  public Context html(String content) {
    response.setContentType(HTML_UTF8);
    return write(content);
  }

  @Override
  public Context write(String content) {
    try {
      response.getOutputBuffer().write(content);
      return this;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Context render(String name, Map<String, Object> model) {
    mgr.render(this, name, model);
    return this;
  }

  @Override
  public Map<String, String> headerMap() {
    Map<String, String> map = new LinkedHashMap<>();
    for (String headerName : request.getHeaderNames()) {
      map.put(headerName, request.getHeader(headerName));
    }
    return map;
  }

  @Override
  public String header(String key) {
    return request.getHeader(key);
  }

  @Override
  public Context header(String key, String value) {
    response.setHeader(key, value);
    return this;
  }

  @Override
  public String host() {
    return request.getRemoteHost();
  }

  @Override
  public String ip() {
    return request.getRemoteAddr();
  }

  @Override
  public boolean isMultipart() {
    // TODO
    return false;
  }

  @Override
  public boolean isMultipartFormData() {
    // TODO
    return false;
  }

  @Override
  public String method() {
    return request.getMethod().getMethodString();
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public int port() {
    return request.getServerPort();
  }

  @Override
  public String protocol() {
    return request.getProtocol().getProtocolString();
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
    return response.getOutputStream();
  }

  @Override
  public InputStream inputStream() {
    return request.getInputStream();
  }

  @Override
  public void setMode(Routing.Type type) {
    this.mode = type;
  }

}
