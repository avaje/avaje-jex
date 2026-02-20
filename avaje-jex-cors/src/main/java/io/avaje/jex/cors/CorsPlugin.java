package io.avaje.jex.cors;

import java.util.List;
import java.util.function.Consumer;

import io.avaje.jex.Jex;
import io.avaje.jex.cors.CorsConfig.CorsData;
import io.avaje.jex.http.Context;
import io.avaje.jex.spi.JexPlugin;

/**
 * Plugin that handles the functionality to set CORS headers for some or all origins as required.
 */
public class CorsPlugin implements JexPlugin {

  private static final String ORIGIN = "Origin";
  private static final String VARY = "Vary";
  private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
  private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
  private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
  private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
  private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
  private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
  private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
  private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

  private final CorsConfig config;

  private CorsPlugin(Consumer<CorsConfig> userConfig) {
    this.config = new CorsConfig();
    userConfig.accept(this.config);
    if (config.rules.isEmpty()) {
      throw new IllegalArgumentException(
          "At least one cors config has to be provided. Use CorsPluginConfig.addRule() to add one.");
    }
  }

  /**
   * Create a new instance of the CorsPlugin with the provided configuration.
   *
   * @param userConfig
   * @return a new instance of the CorsPlugin
   */
  public static CorsPlugin create(Consumer<CorsConfig> userConfig) {
    return new CorsPlugin(userConfig);
  }

  @Override
  public void apply(Jex jex) {
    for (final var corsRule : config.rules) {
      final var origins = corsRule.allowedOrigins();

      if (origins.isEmpty() && !corsRule.reflectClientOrigin()) {
        throw new IllegalArgumentException(
            "Origins cannot be empty if `reflectClientOrigin` is false.");
      }
      if (!origins.isEmpty() && corsRule.reflectClientOrigin()) {
        throw new IllegalArgumentException(
            "Cannot set `allowedOrigins` if `reflectClientOrigin` is true");
      }
      if (origins.contains("*") && corsRule.allowCredentials()) {
        throw new IllegalArgumentException(
            """
             Cannot use `anyHost()` / Origin: * if `allowCredentials` is true as that is rejected by all browsers.
             Please use either an explicit list of allowed origins via `allowHost()` or use `reflectClientOrigin = true` without any origins.""");
      }
      jex.options(corsRule.path(), ctx -> handleCors(ctx, corsRule));
    }

    jex.filter(
        (ctx, chain) -> {
          // If it's a preflight, OPTIONS call option handler
          if ("OPTIONS".equals(ctx.method())) {
            chain.proceed();
            return;
          }
          final var path = ctx.path();
          for (final var rule : config.rules) {
            if (matchesPath(path, rule.path())) {
              handleCors(ctx, rule);
              break;
            }
          }
          chain.proceed();
        });
  }

  private boolean matchesPath(String requestPath, String rulePath) {
    if ("*".equals(rulePath)) return true;
    if (rulePath.endsWith("*")) {
      return requestPath.startsWith(rulePath.substring(0, rulePath.length() - 1));
    }
    return requestPath.equals(rulePath);
  }

  private void handleCors(Context ctx, CorsData cfg) {
    final var clientOrigin = ctx.header(ORIGIN);
    if (clientOrigin == null || !CorsUtils.isValidOrigin(clientOrigin, true)) return;

    final var isOptions = "OPTIONS".equalsIgnoreCase(ctx.method());

    if (isOptions) {
      var requestedHeader = false;

      final var requestHeaders = ctx.header(ACCESS_CONTROL_REQUEST_HEADERS);
      if (requestHeaders != null) {
        ctx.header(ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders);
        requestedHeader = true;
      }
      final var requestMethod = ctx.header(ACCESS_CONTROL_REQUEST_METHOD);
      if (requestMethod != null) {
        ctx.header(ACCESS_CONTROL_ALLOW_METHODS, requestMethod);
        requestedHeader = true;
      }
      if (requestedHeader && cfg.maxAge() >= 0) {
        ctx.header(ACCESS_CONTROL_MAX_AGE, String.valueOf(cfg.maxAge()));
      }
    }

    final var origins = cfg.allowedOrigins();
    String allowOriginValue;

    if (origins.contains("*")) {
      allowOriginValue = "*";
    } else if ("null".equals(clientOrigin)) {
      return;
    } else if (cfg.reflectClientOrigin() || matchOrigin(clientOrigin, origins)) {
      allowOriginValue = clientOrigin;
    } else {
      ctx.writeEmpty(400);
      return;
    }

    ctx.header(ACCESS_CONTROL_ALLOW_ORIGIN, allowOriginValue);
    ctx.header(VARY, ORIGIN);

    if (cfg.allowCredentials()) {
      ctx.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    }

    final var headersToExpose = cfg.headersToExpose();
    if (!headersToExpose.isEmpty()) {
      ctx.header(ACCESS_CONTROL_EXPOSE_HEADERS, String.join(",", headersToExpose));
    }
  }

  private boolean matchOrigin(String clientOrigin, List<String> origins) {
    final var clientOriginPart = CorsUtils.parseAsOriginParts(clientOrigin);
    return origins.stream()
        .map(CorsUtils::parseAsOriginParts)
        .anyMatch(serverPart -> CorsUtils.originsMatch(clientOriginPart, serverPart));
  }

  record OriginParts(String scheme, String host, int port) {}
}
