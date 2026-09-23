package io.avaje.jex.routes;

import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpFilter;

/** A filter that only applies to requests whose path matches the given path expression. */
public final class PathFilter implements HttpFilter {

  private final String path;
  private final HttpFilter delegate;
  private final PathParser matcher;

  /** Create an unbound PathFilter for the given path expression. */
  public PathFilter(String path, HttpFilter delegate) {
    this(path, delegate, null);
  }

  private PathFilter(String path, HttpFilter delegate, PathParser matcher) {
    this.path = path;
    this.delegate = delegate;
    this.matcher = matcher;
  }

  /** Return a PathFilter bound to the given context path. */
  PathFilter bind(String contextPath, boolean ignoreTrailingSlashes) {
    return new PathFilter(
        path, delegate, new PathParser(contextPath + path, ignoreTrailingSlashes));
  }

  /** Return the path expression. */
  public String path() {
    return path;
  }

  @Override
  public void filter(Context ctx, FilterChain chain) {
    if (matcher == null) {
      throw new IllegalStateException("PathFilter for " + path + " has not been bound to routes");
    }
    if (matcher.matches(ctx.path())) {
      delegate.filter(ctx, chain);
    } else {
      chain.proceed();
    }
  }

  @Override
  public String toString() {
    return "PathFilter{" + path + '}';
  }
}
