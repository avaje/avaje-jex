package io.avaje.jex.jetty;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.UploadedFile;
import io.avaje.jex.http.RedirectResponse;
import io.avaje.jex.spi.HeaderKeys;
import io.avaje.jex.spi.SpiContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
  protected final HttpServletRequest req;
  private final HttpServletResponse res;
  private final Map<String, String> pathParams;
  private final String matchedPath;
  private String characterEncoding;
  private Routing.Type mode;
  private Map<String, List<String>> formParamMap;

  JexHttpContext(ServiceManager mgr, HttpServletRequest req, HttpServletResponse res, String matchedPath) {
    this.mgr = mgr;
    this.req = req;
    this.res = res;
    this.matchedPath = matchedPath;
    this.pathParams = emptyMap();
  }

  JexHttpContext(ServiceManager mgr, HttpServletRequest req, HttpServletResponse res, String matchedPath, Map<String, String> pathParams) {
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

  public HttpServletRequest req() {
    return req;
  }

  public HttpServletResponse res() {
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
    final jakarta.servlet.http.Cookie[] cookies = req.getCookies();
    if (cookies != null) {
      for (jakarta.servlet.http.Cookie cookie : cookies) {
        if (cookie.getName().equals(name)) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  @Override
  public Map<String, String> cookieMap() {
    final jakarta.servlet.http.Cookie[] cookies = req.getCookies();
    if (cookies == null) {
      return emptyMap();
    }
    final Map<String, String> map = new LinkedHashMap<>();
    for (jakarta.servlet.http.Cookie cookie : cookies) {
      map.put(cookie.getName(), cookie.getValue());
    }
    return map;
  }

  @Override
  public Context cookie(Cookie cookie) {
    final jakarta.servlet.http.Cookie newCookie = new jakarta.servlet.http.Cookie(cookie.name(), cookie.value());
    newCookie.setPath(cookie.path());
    if (newCookie.getPath() == null) {
      newCookie.setPath("/");
    }
    final String domain = cookie.domain();
    if (domain != null) {
      newCookie.setDomain(domain);
    }
    final Duration duration = cookie.maxAge();
    if (duration != null) {
      newCookie.setMaxAge((int)duration.toSeconds());
    }
    newCookie.setHttpOnly(cookie.httpOnly());
    newCookie.setSecure(cookie.secure());
    res.addCookie(newCookie);
    return this;
  }

  @Override
  public Context cookie(String name, String value, int maxAge) {
    final jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, value);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    res.addCookie(cookie);
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
    final jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, "");
    cookie.setPath(path);
    cookie.setMaxAge(0);
    res.addCookie(cookie);
    return this;
  }

  @Override
  public void redirect(String location) {
    redirect(location, HttpServletResponse.SC_MOVED_TEMPORARILY);
  }

  @Override
  public void redirect(String location, int statusCode) {
    res.setHeader(HeaderKeys.LOCATION, location);
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
    try {
      return req.getInputStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public <T> T bodyAsClass(Class<T> clazz) {
    return mgr.jsonRead(clazz, this);
  }

  @Override
  public byte[] bodyAsBytes() {
    return ContextUtil.readBody(req);
  }

  @Override
  public String body() {
    return new String(bodyAsBytes(), Charset.forName(characterEncoding()));
  }

  @Override
  public long contentLength() {
    return req.getContentLengthLong();
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
    final String[] vals = req.getParameterValues(name);
    if (vals == null || vals.length == 0) {
      return null;
    } else {
      return vals[0];
    }
  }

  @Override
  public List<String> queryParams(String name) {
    final String[] vals = req.getParameterValues(name);
    if (vals == null) {
      return emptyList();
    } else {
      return Arrays.asList(vals);
    }
  }

  @Override
  public Map<String, String> queryParamMap() {
    final Map<String, String> map = new LinkedHashMap<>();
    final Enumeration<String> names = req.getParameterNames();
    while (names.hasMoreElements()) {
      final String key = names.nextElement();
      map.put(key, queryParam(key));
    }
    return map;
  }

  @Override
  public String queryString() {
    return req.getQueryString();
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
      return mgr.multiPartForm(req);
    } else {
      return mgr.formParamMap(this, characterEncoding());
    }
  }

  @Override
  public String scheme() {
    return req.getScheme();
  }

  @Override
  public Context sessionAttribute(String key, Object value) {
    req.getSession().setAttribute(key, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T sessionAttribute(String key) {
    HttpSession session = req.getSession(false);
    return session == null ? null : (T) session.getAttribute(key);
  }

  @Override
  public Map<String, Object> sessionAttributeMap() {
    final Map<String, Object> map = new LinkedHashMap<>();
    final HttpSession session = req.getSession(false);
    if (session == null) {
      return emptyMap();
    }
    final Enumeration<String> names = session.getAttributeNames();
    while (names.hasMoreElements()) {
      final String name = names.nextElement();
      map.put(name, session.getAttribute(name));
    }
    return map;
  }

  @Override
  public String url() {
    return req.getRequestURL().toString();
  }

  @Override
  public String fullUrl() {
    final String url = url();
    final String qs = queryString();
    return qs == null ? url : url + "?" + qs;
  }

  @Override
  public String contextPath() {
    String path = req.getContextPath();
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
    return req.getContentType();
  }

  @Override
  public Context contentType(String contentType) {
    res.setContentType(contentType);
    return this;
  }

  public Map<String, String> headerMap() {
    Map<String, String> map = new LinkedHashMap<>();
    final Enumeration<String> names = req.getHeaderNames();
    while (names.hasMoreElements()) {
      final String name = names.nextElement();
      map.put(name, req.getHeader(name));
    }
    return map;
  }

  @Override
  public String responseHeader(String key) {
    return req.getHeader(key);
  }

  @Override
  public String header(String key) {
    return req.getHeader(key);
  }

  @Override
  public Context header(String key, String value) {
    res.setHeader(key, value);
    return this;
  }

  @Override
  public String host() {
    return req.getHeader(HeaderKeys.HOST);
  }

  @Override
  public String ip() {
    return req.getRemoteAddr();
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
    return req.getRequestURI();
  }

  @Override
  public int port() {
    return req.getServerPort();
  }

  @Override
  public String protocol() {
    return req.getProtocol();
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
    try {
      res.getWriter().write(content);
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
  public OutputStream outputStream() {
    try {
      return res.getOutputStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public UploadedFile uploadedFile(String name) {
    final List<UploadedFile> files = uploadedFiles(name);
    return files.isEmpty() ? null : files.get(0);
  }

  @Override
  public List<UploadedFile> uploadedFiles(String name) {
    if (!isMultipartFormData()) {
      return emptyList();
    } else {
      return mgr.uploadedFiles(req, name);
    }
  }

  @Override
  public List<UploadedFile> uploadedFiles() {
    if (!isMultipartFormData()) {
      return emptyList();
    } else {
      return mgr.uploadedFiles(req);
    }
  }
}
