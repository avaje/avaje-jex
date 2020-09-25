package io.avaje.jex.core;

import io.avaje.jex.Context;
import io.avaje.jex.spi.IORuntimeException;
import io.avaje.jex.spi.SpiContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class JexHttpContext implements SpiContext {

  private final ServiceManager mgr;
  protected final HttpServletRequest req;
  private final HttpServletResponse res;
  private final Map<String, String> pathParams;
  private final String matchedPath;

  private String characterEncoding;

  JexHttpContext(ServiceManager mgr, HttpServletRequest req, HttpServletResponse res, Map<String, String> pathParams, String matchedPath) {
    this.mgr = mgr;
    this.req = req;
    this.res = res;
    this.pathParams = pathParams;
    this.matchedPath = matchedPath;
  }

  private String characterEncoding() {
    if (characterEncoding == null) {
      characterEncoding = ContextUtil.getRequestCharset(this);
    }
    return characterEncoding;
  }

  @Override
  public HttpServletRequest req() {
    return req;
  }

  @Override
  public HttpServletResponse res() {
    return res;
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
  public Map<String, Object> attributeMap() {
    final Map<String, Object> map = new LinkedHashMap<>();
    final Enumeration<String> names = req.getAttributeNames();
    while (names.hasMoreElements()) {
      final String name = names.nextElement();
      map.put(name, req.getAttribute(name));
    }
    return map;
  }

  @Override
  public String matchedPath() {
    return matchedPath;
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
      return Collections.emptyList();
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
    return (T) req.getSession().getAttribute(key);
  }

  @Override
  public Map<String, Object> sessionAttributeMap() {
    final Map<String, Object> map = new LinkedHashMap<>();
    final HttpSession session = req.getSession();
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
    final String qs = queryString();
    return qs == null ? url() : url() + "?" + qs;
  }

  @Override
  public String contextPath() {
    return req.getContextPath();
  }

  @Override
  public String userAgent() {
    return req.getHeader(HeaderKeys.USER_AGENT);
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
  public String header(String key) {
    return req.getHeader(key);
  }

  @Override
  public void header(String key, String value) {
    res.setHeader(key, value);
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
  public Context text(String content) {
    res.setContentType("text/plain");
    return write(content);
  }

  @Override
  public Context html(String content) {
    res.setContentType("text/html");
    return write(content);
  }

  @Override
  public Context json(Object bean) {
    contentType("application/json");
    mgr.jsonWrite(bean, this);
    return this;
  }

  @Override
  public Context write(String content) {
    try {
      res.getWriter().write(content);
      return this;
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  @Override
  public OutputStream outputStream() {
    try {
      return res.getOutputStream();
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

}
