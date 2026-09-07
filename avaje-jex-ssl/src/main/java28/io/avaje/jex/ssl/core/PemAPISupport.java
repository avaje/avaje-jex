package io.avaje.jex.ssl.core;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.nio.charset.StandardCharsets;
import java.security.PEMDecoder;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/** Handles PEM files using the finalized {@code java.security.PEMDecoder} API (JEP 542) */
final class PemAPISupport {

  private static final PEMDecoder DECODER = PEMDecoder.of();

  private PemAPISupport() {}

  static List<Certificate> certificates(String pemContent) {
    var certs = new ArrayList<Certificate>();
    try (var is = new ByteArrayInputStream(pemContent.getBytes(StandardCharsets.UTF_8))) {
      while (is.available() > 0) {
        try {
          certs.add(DECODER.decode(is, X509Certificate.class));
        } catch (EOFException eof) {
          break;
        }
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to decode PEM certificate(s)", e);
    }
    return certs;
  }

  static PrivateKey privateKey(String pemContent, char[] password) {
    var decoder =
        password != null && password.length > 0 ? DECODER.withDecryption(password) : DECODER;
    try {
      return decoder.decode(pemContent, PrivateKey.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to decode PEM private key", e);
    }
  }
}
