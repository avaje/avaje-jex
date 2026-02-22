package io.avaje.jex.cors;

import io.avaje.jex.cors.CorsPlugin.OriginParts;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsUtilsTest {

  @Test
  void isValidOrigin_emptyString_returnsFalse() {
    assertThat(CorsUtils.isValidOrigin("", true)).isFalse();
    assertThat(CorsUtils.isValidOrigin("", false)).isFalse();
  }

  @Test
  void isValidOrigin_nullString_returnsTrue() {
    assertThat(CorsUtils.isValidOrigin("null", true)).isTrue();
    assertThat(CorsUtils.isValidOrigin("null", false)).isTrue();
  }

  @Test
  void isValidOrigin_validHttpOrigin_returnsTrue() {
    assertThat(CorsUtils.isValidOrigin("http://example.com", true)).isTrue();
    assertThat(CorsUtils.isValidOrigin("https://example.com", false)).isTrue();
  }

  @Test
  void isValidOrigin_originWithPath_returnsFalse() {
    assertThat(CorsUtils.isValidOrigin("https://example.com/path", true)).isFalse();
  }

  @Test
  void isValidOrigin_originWithQuery_returnsFalse() {
    assertThat(CorsUtils.isValidOrigin("https://example.com?foo=bar", true)).isFalse();
  }

  @Test
  void isValidOrigin_originWithFragment_returnsFalse() {
    assertThat(CorsUtils.isValidOrigin("https://example.com#section", true)).isFalse();
  }

  @Test
  void isValidOrigin_wildcardOriginAsServer_returnsTrue() {
    assertThat(CorsUtils.isValidOrigin("https://*.example.com", false)).isTrue();
  }

  @Test
  void isValidOrigin_wildcardOriginAsClient_returnsFalse() {
    assertThat(CorsUtils.isValidOrigin("https://*.example.com", true)).isFalse();
  }

  @Test
  void isValidOrigin_invalidUri_returnsFalse() {
    assertThat(CorsUtils.isValidOrigin("not-a-url", true)).isFalse();
  }

  @Test
  void parseAsOriginParts_httpsDefaultPort() {
    OriginParts parts = CorsUtils.parseAsOriginParts("https://example.com");
    assertThat(parts.scheme()).isEqualTo("https");
    assertThat(parts.host()).isEqualTo("example.com");
    assertThat(parts.port()).isEqualTo(443);
  }

  @Test
  void parseAsOriginParts_httpDefaultPort() {
    OriginParts parts = CorsUtils.parseAsOriginParts("http://example.com");
    assertThat(parts.scheme()).isEqualTo("http");
    assertThat(parts.host()).isEqualTo("example.com");
    assertThat(parts.port()).isEqualTo(80);
  }

  @Test
  void parseAsOriginParts_explicitPort() {
    OriginParts parts = CorsUtils.parseAsOriginParts("https://example.com:8443");
    assertThat(parts.port()).isEqualTo(8443);
  }

  @Test
  void parseAsOriginParts_wildcardSubdomain() {
    OriginParts parts = CorsUtils.parseAsOriginParts("https://*.example.com");
    assertThat(parts.scheme()).isEqualTo("https");
    assertThat(parts.host()).isEqualTo("*.example.com");
    assertThat(parts.port()).isEqualTo(443);
  }

  @Test
  void parseAsOriginParts_invalidOrigin_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> CorsUtils.parseAsOriginParts("not-a-url"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void addSchemeIfMissing_hostWithoutScheme_prependsDefault() {
    assertThat(CorsUtils.addSchemeIfMissing("example.com", "https"))
        .isEqualTo("https://example.com");
  }

  @Test
  void addSchemeIfMissing_hostWithScheme_unchanged() {
    assertThat(CorsUtils.addSchemeIfMissing("http://example.com", "https"))
        .isEqualTo("http://example.com");
  }

  @Test
  void addSchemeIfMissing_asterisk_unchanged() {
    assertThat(CorsUtils.addSchemeIfMissing("*", "https")).isEqualTo("*");
  }

  @Test
  void addSchemeIfMissing_nullString_unchanged() {
    assertThat(CorsUtils.addSchemeIfMissing("null", "https")).isEqualTo("null");
  }

  @Test
  void addSchemeIfMissing_trailingSlash_isStripped() {
    assertThat(CorsUtils.addSchemeIfMissing("https://example.com/", "https"))
        .isEqualTo("https://example.com");
  }

  @Test
  void addSchemeIfMissing_uppercaseIsLowercased() {
    assertThat(CorsUtils.addSchemeIfMissing("EXAMPLE.COM", "https"))
        .isEqualTo("https://example.com");
  }

  @Test
  void originsMatch_exactMatch_returnsTrue() {
    OriginParts a = new OriginParts("https", "example.com", 443);
    OriginParts b = new OriginParts("https", "example.com", 443);
    assertThat(CorsUtils.originsMatch(a, b)).isTrue();
  }

  @Test
  void originsMatch_differentHost_returnsFalse() {
    OriginParts a = new OriginParts("https", "foo.com", 443);
    OriginParts b = new OriginParts("https", "bar.com", 443);
    assertThat(CorsUtils.originsMatch(a, b)).isFalse();
  }

  @Test
  void originsMatch_differentScheme_returnsFalse() {
    OriginParts a = new OriginParts("http", "example.com", 80);
    OriginParts b = new OriginParts("https", "example.com", 443);
    assertThat(CorsUtils.originsMatch(a, b)).isFalse();
  }

  @Test
  void originsMatch_differentPort_returnsFalse() {
    OriginParts a = new OriginParts("https", "example.com", 443);
    OriginParts b = new OriginParts("https", "example.com", 8443);
    assertThat(CorsUtils.originsMatch(a, b)).isFalse();
  }

  @Test
  void originsMatch_wildcardServer_matchingSubdomain_returnsTrue() {
    OriginParts client = new OriginParts("https", "sub.example.com", 443);
    OriginParts server = new OriginParts("https", "*.example.com", 443);
    assertThat(CorsUtils.originsMatch(client, server)).isTrue();
  }

  @Test
  void originsMatch_wildcardServer_nonMatchingSubdomain_returnsFalse() {
    OriginParts client = new OriginParts("https", "sub.other.com", 443);
    OriginParts server = new OriginParts("https", "*.example.com", 443);
    assertThat(CorsUtils.originsMatch(client, server)).isFalse();
  }

  @Test
  void originsMatch_wildcardServer_clientHasNoDot_returnsFalse() {
    OriginParts client = new OriginParts("https", "example", 443);
    OriginParts server = new OriginParts("https", "*.example.com", 443);
    assertThat(CorsUtils.originsMatch(client, server)).isFalse();
  }

  @Test
  void wildcardRequirements_noWildcard() {
    assertThat(CorsUtils.originFulfillsWildcardRequirements("https://example.com"))
        .isEqualTo(WildcardResult.NoWildcardDetected);
  }

  @Test
  void wildcardRequirements_validWildcard() {
    assertThat(CorsUtils.originFulfillsWildcardRequirements("https://*.example.com"))
        .isEqualTo(WildcardResult.WildcardOkay);
  }

  @Test
  void wildcardRequirements_wildcardNotAtStart() {
    assertThat(CorsUtils.originFulfillsWildcardRequirements("https://example.*.com"))
        .isEqualTo(WildcardResult.WildcardNotAtTheStartOfTheHost);
  }

  @Test
  void wildcardRequirements_tooManyWildcards() {
    assertThat(CorsUtils.originFulfillsWildcardRequirements("https://*.*.example.com"))
        .isEqualTo(WildcardResult.TooManyWildcards);
  }
}
