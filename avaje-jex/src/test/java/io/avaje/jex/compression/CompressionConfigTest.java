package io.avaje.jex.compression;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class CompressionConfigTest {

  static Compressor compressor(String encoding) {
    return new Compressor() {
      @Override
      public String encoding() {
        return encoding;
      }

      @Override
      public OutputStream compress(OutputStream out) {
        return out;
      }
    };
  }

  static Compressor ZSTD = compressor("zstd");
  static Compressor BR = compressor("br");

  @Test
  void serverPriorityWinsWhenClientQEqual() {
    var config = new CompressionConfig().compressor(ZSTD);
    // client lists gzip first but all q=1.0 — server prefers zstd
    var match = config.findMatchingCompressor(List.of("gzip, deflate, br, zstd"));
    assertThat(match).map(Compressor::encoding).contains("zstd");
  }

  @Test
  void higherClientQWins() {
    var config = new CompressionConfig().compressor(ZSTD);
    // client strongly prefers gzip over zstd
    var match = config.findMatchingCompressor(List.of("gzip;q=1.0, zstd;q=0.5"));
    assertThat(match).map(Compressor::encoding).contains("gzip");
  }

  @Test
  void qZeroRejectsEncoding() {
    var config = new CompressionConfig();
    // client explicitly rejects gzip
    var match = config.findMatchingCompressor(List.of("gzip;q=0"));
    assertThat(match).isEmpty();
  }

  @Test
  void qZeroDoesNotAffectOtherEncodings() {
    var config = new CompressionConfig().compressor(ZSTD);
    // client rejects gzip but accepts zstd
    var match = config.findMatchingCompressor(List.of("gzip;q=0, zstd"));
    assertThat(match).map(Compressor::encoding).contains("zstd");
  }

  @Test
  void wildcardMatchesAnyServerCompressor() {
    var config = new CompressionConfig();
    var match = config.findMatchingCompressor(List.of("*"));
    assertThat(match).map(Compressor::encoding).contains("gzip");
  }

  @Test
  void wildcardAsFallbackLosesToExplicitHigherQ() {
    var config = new CompressionConfig().compressor(ZSTD);
    // explicit gzip at q=1.0 beats wildcard at q=0.5 (which would match zstd)
    var match = config.findMatchingCompressor(List.of("gzip;q=1.0, *;q=0.5"));
    assertThat(match).map(Compressor::encoding).contains("gzip");
  }

  @Test
  void multiValueHeaderList() {
    var config = new CompressionConfig().compressor(ZSTD);
    // jetty-style multi-value headers
    var match = config.findMatchingCompressor(List.of("gzip", "zstd"));
    assertThat(match).map(Compressor::encoding).contains("zstd");
  }

  @Test
  void noMatchReturnsEmpty() {
    var config = new CompressionConfig();
    var match = config.findMatchingCompressor(List.of("br, zstd"));
    assertThat(match).isEmpty();
  }

  @Test
  void nullReturnsEmpty() {
    var config = new CompressionConfig();
    assertThat(config.findMatchingCompressor(null)).isEmpty();
  }

  @Test
  void serverRegistrationOrderTiebreaker() {
    // zstd registered first → wins over br when both q=1.0
    var config = new CompressionConfig().compressor(BR).compressor(ZSTD);
    var match = config.findMatchingCompressor(List.of("gzip, br, zstd"));
    assertThat(match).map(Compressor::encoding).contains("zstd");
  }
}
