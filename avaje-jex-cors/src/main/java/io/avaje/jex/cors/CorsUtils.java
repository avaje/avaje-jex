package io.avaje.jex.cors;

import java.net.URI;
import java.util.Locale;

import io.avaje.jex.cors.CorsPlugin.OriginParts;

final class CorsUtils {

  private CorsUtils() {}

  static boolean isValidOrigin(String origin, boolean client) {
    if (origin.isEmpty()) return false;
    if ("null".equals(origin)) return true;

    final var wildcardSnippet = "://*.";
    final var hasWildcard = origin.contains(wildcardSnippet);
    if (client && hasWildcard) return false;

    final var originToAnalyze = hasWildcard ? origin.replace(wildcardSnippet, "://") : origin;
    try {
      final var uri = new URI(originToAnalyze).parseServerAuthority();
      if (uri.getPath() != null && !uri.getPath().isEmpty()
          || uri.getUserInfo() != null && !uri.getUserInfo().isEmpty()) return false;
      if (uri.getQuery() != null && !uri.getQuery().isEmpty()
          || uri.getFragment() != null && !uri.getFragment().isEmpty()) return false;
      return true;
    } catch (final Exception e) {
      return false;
    }
  }

  static OriginParts parseAsOriginParts(String origin) {
    final var wildcardSnippet = "://*.";
    final var hasWildcard = origin.contains(wildcardSnippet);
    final var originWithoutWildcard = origin.replace(wildcardSnippet, "://");

    try {
      final var uri = new URI(originWithoutWildcard).parseServerAuthority();
      if (uri.getScheme() == null) throw new IllegalArgumentException("Scheme is required!");

      final var host = hasWildcard ? "*." + uri.getHost() : uri.getHost();

      int port;
      if ("https".equals(uri.getScheme()) && uri.getPort() == -1) {
        port = 443;
      } else if ("http".equals(uri.getScheme()) && uri.getPort() == -1) {
        port = 80;
      } else {
        port = uri.getPort();
      }

      return new OriginParts(uri.getScheme(), host, port);
    } catch (final IllegalArgumentException e) {
      throw e;
    } catch (final Exception e) {
      throw new IllegalArgumentException("Failed to parse origin: " + origin, e);
    }
  }

  static String addSchemeIfMissing(String host, String defaultScheme) {
    String hostWithScheme;
    if ("*".equals(host) || "null".equals(host) || host.contains("://")) {
      hostWithScheme = host;
    } else {
      hostWithScheme = defaultScheme + "://" + host;
    }
    final var result = hostWithScheme.toLowerCase(Locale.ROOT);
    return result.endsWith("/") ? result.substring(0, result.length() - 1) : result;
  }

  static boolean originsMatch(OriginParts clientOrigin, OriginParts serverOrigin) {
    if (clientOrigin.equals(serverOrigin)) return true;
    if (!clientOrigin.scheme().equals(serverOrigin.scheme())
        || clientOrigin.port() != serverOrigin.port()
        || !serverOrigin.host().startsWith("*.")
        || !clientOrigin.host().contains(".")) return false;

    final var serverHostBase = serverOrigin.host().substring(2); // remove "*."
    final var clientHostBase = clientOrigin.host().substring(clientOrigin.host().indexOf('.') + 1);
    return serverHostBase.equals(clientHostBase);
  }

  static WildcardResult originFulfillsWildcardRequirements(String origin) {
    final var count = origin.chars().filter(c -> c == '*').count();

    if (count == 0) return WildcardResult.NoWildcardDetected;

    if (count == 1) {
      if (origin.contains("://*.")) {
        return WildcardResult.WildcardOkay;
      }
      return WildcardResult.WildcardNotAtTheStartOfTheHost;
    }

    return switch ((int) count) {
      case 0 -> WildcardResult.NoWildcardDetected;
      case 1 ->
          origin.contains("://*.")
              ? WildcardResult.WildcardOkay
              : WildcardResult.WildcardNotAtTheStartOfTheHost;
      default -> WildcardResult.TooManyWildcards;
    };
  }
}
