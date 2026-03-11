package io.avaje.jex.cors;

import java.util.ArrayList;
import java.util.List;

/** Builder of {@link CorsPlugin} instances. */
public final class CorsPluginBuilder {
  final List<CorsData> rules;

  CorsPluginBuilder() {
    rules = new ArrayList<>();
  }

  public RuleBuilder createRule() {
    return new RuleBuilder();
  }

  public CorsPlugin build() {
    if (rules.isEmpty()) {
      throw new IllegalArgumentException(
          "At least one cors config has to be provided. Use CorsPluginConfig.addRule() to add one.");
    }
    return new CorsPlugin(this);
  }

  /** Provides a fluent API for constructing CORS rules with various options. */
  public final class RuleBuilder {
    private boolean allowCredentials = false;
    private boolean reflectClientOrigin = false;
    private String defaultScheme = "https";
    private String path = "*";
    private int maxAge = -1;

    private final List<String> allowedOrigins = new ArrayList<>();
    private final List<String> headersToExpose = new ArrayList<>();

    private RuleBuilder() {}

    /** Allow requests to carry credentials (cookies, auth headers). */
    public RuleBuilder allowCredentials(boolean allowCredentials) {
      this.allowCredentials = allowCredentials;
      return this;
    }

    /** Reflect the client's Origin header back instead of a fixed value. */
    public RuleBuilder reflectClientOrigin(boolean reflectClientOrigin) {
      this.reflectClientOrigin = reflectClientOrigin;
      return this;
    }

    /** Default scheme prepended when a host is given without one (default: {@code "https"}). */
    public RuleBuilder defaultScheme(String defaultScheme) {
      this.defaultScheme = defaultScheme;
      return this;
    }

    /** Path pattern this rule applies to (default: {@code "*"}). */
    public RuleBuilder path(String path) {
      this.path = path;
      return this;
    }

    /** Preflight {@code Access-Control-Max-Age} in seconds ({@code -1} to omit). */
    public RuleBuilder maxAge(int maxAge) {
      this.maxAge = maxAge;
      return this;
    }

    /** Allow all origins ({@code Access-Control-Allow-Origin: *}). */
    public RuleBuilder anyHost() {
      allowedOrigins.add("*");
      return this;
    }

    /**
     * Allow one or more specific hosts, with optional scheme (defaults to {@link #defaultScheme}).
     */
    public RuleBuilder allowHost(String... others) {
      final List<String> origins = List.of(others);

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
        if (wildcardResult == WildcardResult.WildcardNotAtTheStartOfTheHost) {
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
    public RuleBuilder exposeHeader(String header) {
      headersToExpose.add(header);
      return this;
    }

    /**
     * Create and append this CORS rule.
     * @return The builder this rule is associated with.
     */
    CorsPluginBuilder buildRule() {
      CorsData built = new CorsData(
          allowCredentials,
          reflectClientOrigin,
          defaultScheme,
          path,
          maxAge,
          List.copyOf(allowedOrigins),
          List.copyOf(headersToExpose));

      CorsPluginBuilder.this.rules.add(built);
      return CorsPluginBuilder.this;
    }
  }

  record CorsData(
      boolean allowCredentials,
      boolean reflectClientOrigin,
      String defaultScheme,
      String path,
      int maxAge,
      List<String> allowedOrigins,
      List<String> headersToExpose) {}
}
