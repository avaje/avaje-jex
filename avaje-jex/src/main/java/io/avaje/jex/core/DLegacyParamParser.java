package io.avaje.jex.core;

import io.avaje.jex.ParamParser;
import io.avaje.jex.PathParser;
import io.avaje.jex.Routing;
import io.avaje.jex.http.Context;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * This implementation is for the pre-4.0 behaviour regarding parsing query/form params
 */
public final class DLegacyParamParser extends ParamParser {
  @Override
  public Map<String, List<String>> decodeQueryParams(Context ctx, Charset charset) {
    return decodeListAsRfc3986(ctx.queryString(), charset);
  }

  @Override
  public Map<String, List<String>> decodeBody(Context ctx, Charset charset) {
    return decodeListAsRfc3986(ctx.body(), charset);
  }

  @Override
  public PathParser createPathParser(String path, Routing.Entry routeEntry, boolean ignoreTrailingSlashes) {
    return defaultPathParser(path, ignoreTrailingSlashes);
  }
}
