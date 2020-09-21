package io.avaje.jex.core;

import io.avaje.jex.Context;
import io.avaje.jex.spi.IORuntimeException;
import io.avaje.jex.spi.SpiContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedHashMap;
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
  public Map<String, String> pathParams() {
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
  public Context status(int statusCode) {
    res.setStatus(statusCode);
    return this;
  }

  @Override
  public Context contentType(String contentType) {
    res.setContentType(contentType);
    return this;
  }

  public Map<String, String> headerMap() {
    Map<String,String> map = new LinkedHashMap<>();
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
  public Context text(String one) {
    res.setContentType("text/plain");
    try {
      res.getWriter().write(one);
      return this;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Context json(Object bean) {
    contentType("application/json");
    mgr.jsonWrite(bean, this);
    return this;
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
