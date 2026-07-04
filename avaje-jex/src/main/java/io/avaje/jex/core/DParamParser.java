package io.avaje.jex.core;

import io.avaje.jex.ParamParser;
import io.avaje.jex.PathParser;
import io.avaje.jex.Routing;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.http.HttpStatus;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * This implements spec-compliant parsing of query, path, and body params
 */
public final class DParamParser extends ParamParser {

  @Override
  public Map<String, List<String>> decodeQueryParams(Context ctx, Charset charset) {
    return decodeListAsUrlEncoded(ctx.exchange().getRequestURI().getRawQuery(), charset);
  }

  @Override
  public Map<String, List<String>> decodeBody(Context ctx, Charset charset) {
    if (!"application/x-www-form-urlencoded".equals(ctx.contentType())) {
      throw new HttpResponseException(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
    }
    return decodeListAsUrlEncoded(ctx.body(), charset);
  }

  @Override
  public PathParser createPathParser(String path, Routing.Entry routeEntry, boolean ignoreTrailingSlashes) {
    return defaultPathParser(path, ignoreTrailingSlashes);
  }
}
