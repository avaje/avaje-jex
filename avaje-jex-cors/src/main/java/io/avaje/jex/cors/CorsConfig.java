package io.avaje.jex.cors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


/** Configuration for {@link CorsPlugin}. */
public final class CorsConfig {
  final List<CorsData> rules = new ArrayList<>();

  public CorsConfig addRule(Consumer<CorsRule> consumer) {
    final var rule = new CorsRule();
    consumer.accept(rule);
    rules.add(rule.build());
    return this;
  }

  /**
   * Builder for {@link CorsData}. Provides a fluent API for constructing CORS rules with
   * various options.
   */
  public static final class CorsRule {

    private boolean allowCredentials = false;
    private boolean reflectClientOrigin = false;
    private String defaultScheme = "https";
    private String path = "*";
    private int maxAge = -1;

    private final List<String> allowedOrigins = new ArrayList<>();
    private final List<String> headersToExpose = new ArrayList<>();

    /** Allow requests to carry credentials (cookies, auth headers). */
    public CorsRule allowCredentials(boolean allowCredentials) {
      this.allowCredentials = allowCredentials;
      return this;
    }

    /** Reflect the client's Origin header back instead of a fixed value. */
    public CorsRule reflectClientOrigin(boolean reflectClientOrigin) {
      this.reflectClientOrigin = reflectClientOrigin;
      return this;
    }

    /** Default scheme prepended when a host is given without one (default: {@code "https"}). */
    public CorsRule defaultScheme(String defaultScheme) {
      this.defaultScheme = defaultScheme;
      return this;
    }

    /** Path pattern this rule applies to (default: {@code "*"}). */
    public CorsRule path(String path) {
      this.path = path;
      return this;
    }

    /** Preflight {@code Access-Control-Max-Age} in seconds ({@code -1} to omit). */
    public CorsRule maxAge(int maxAge) {
      this.maxAge = maxAge;
      return this;
    }

    /** Allow all origins ({@code Access-Control-Allow-Origin: *}). */
    public CorsRule anyHost() {
      allowedOrigins.add("*");
      return this;
    }

    /**
     * Allow one or more specific hosts, with optional scheme (defaults to {@link #defaultScheme}).
     */
    public CorsRule allowHost(String host, String... others) {
      final List<String> origins = new ArrayList<>();
      origins.add(host);
      Collections.addAll(origins, others);

      for (var idx = 0; idx < origins.size(); idx++) {
        final var raw = origins.get(idx);
        final var normalized = CorsUtils.addSchemeIfMissing(raw, defaultScheme);

        if ("null".equals(normalized)) {
          throw new IllegalArgumentException(
              "Adding the string null as an allowed host is forbidden. Consider calling anyHost() instead");
        }

        final var wildcardResult = CorsUtils.originFulfillsWildcardRequirements(normalized);
        if (wildcardResult == WildcardResult.TooManyWildcards) {
          throw new IllegalArgumentException(
              "Too many wildcards detected inside '"
                  + raw
                  + "'. Only one at the start of the host is allowed!");
        }
        if (wildcardResult  == WildcardResult.WildcardNotAtTheStartOfTheHost) {
          throw new IllegalArgumentException(
              "The wildcard must be at the start of the passed in host. The value '"
                  + raw
                  + "' violates this requirement!");
        }
        if (!CorsUtils.isValidOrigin(normalized, false)) {
          throw new IllegalArgumentException(
              "The given value '" + raw + "' could not be transformed into a valid origin");
        }

        allowedOrigins.add(normalized);
      }
      return this;
    }

    /** Expose an additional response header to the browser. */
    public CorsRule exposeHeader(String header) {
      headersToExpose.add(header);
      return this;
    }

    CorsData build() {
      return new CorsData(
          allowCredentials,
          reflectClientOrigin,
          defaultScheme,
          path,
          maxAge,
          List.copyOf(allowedOrigins),
          List.copyOf(headersToExpose));
    }
  }

  /**
   * Immutable CORS rule. Construct via {@link CorsRule}.
   *
   * <pre>{@code
   * new CorsRule.Builder()
   *     .path("/api/*")
   *     .allowHost("example.com")
   *     .allowCredentials(true)
   *     .build();
   * }</pre>
   */
  static final record CorsData(
      boolean allowCredentials,
      boolean reflectClientOrigin,
      String defaultScheme,
      String path,
      int maxAge,
      List<String> allowedOrigins,
      List<String> headersToExpose) {}
}
