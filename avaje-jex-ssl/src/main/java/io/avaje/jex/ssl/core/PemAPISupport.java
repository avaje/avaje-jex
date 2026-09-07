package io.avaje.jex.ssl.core;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import io.avaje.jex.ssl.SslConfigException;

/**
 * Manual regex/Base64/DER parsing.
 *
 * <p>Overridden by the JDK 28 multi-release variant which uses the finalized {@code
 * java.security.PEMDecoder} API (JEP 542) instead, notably adding support for password-encrypted
 * PEM private keys which this implementation cannot decrypt.
 */
final class PemAPISupport {

  private static final Pattern CERT_PATTERN =
      Pattern.compile("-----BEGIN CERTIFICATE-----(.+?)-----END CERTIFICATE-----", Pattern.DOTALL);

  private static final Pattern PRIVATE_KEY_PATTERN =
      Pattern.compile(
          "-----BEGIN (?:RSA )?PRIVATE KEY-----(.+?)-----END (?:RSA )?PRIVATE KEY-----",
          Pattern.DOTALL);

  private PemAPISupport() {}

  static List<Certificate> certificates(String pemContent) {
    List<Certificate> certs = new ArrayList<>();
    var matcher = CERT_PATTERN.matcher(pemContent);
    while (matcher.find()) {
      certs.add(decodeCertificate(matcher.group(1)));
    }
    return certs;
  }

  private static Certificate decodeCertificate(String base64Body) {
    try {
      var certBytes = Base64.getDecoder().decode(base64Body.replaceAll("\\s", ""));
      var factory = CertificateFactory.getInstance("X.509");
      try (var bis = new ByteArrayInputStream(certBytes)) {
        return factory.generateCertificate(bis);
      }
    } catch (Exception e) {
      throw new SslConfigException("Failed to parse PEM certificate", e);
    }
  }

  static PrivateKey privateKey(String pemContent, char[] password) {
    try {
      var matcher = PRIVATE_KEY_PATTERN.matcher(pemContent);
      if (!matcher.find()) {
        throw new IllegalArgumentException("No valid private key found in PEM content");
      }

      var base64Key = matcher.group(1).replaceAll("\\s+", "");
      var keyBytes = Base64.getDecoder().decode(base64Key);

      // Try different algorithms
      String[] algorithms = {"RSA", "EC", "DSA"};
      for (String algorithm : algorithms) {
        try {
          var keyFactory = KeyFactory.getInstance(algorithm);
          var keySpec = new PKCS8EncodedKeySpec(keyBytes);
          return keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
          // Try next algorithm
        }
      }

      throw new SslConfigException("Unable to parse private key with any supported algorithm");

    } catch (NoSuchAlgorithmException e) {
      throw new SslConfigException("Failed to parse private key", e);
    }
  }
}
