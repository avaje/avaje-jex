package io.avaje.jex.ssl.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import io.avaje.jex.ssl.SslConfigException;

final class KeyStoreUtil {

  static KeyStore loadKeyStore(InputStream inputStream, char[] password) {
    // Read all bytes first so we can try different formats
    byte[] data;
    try {
      data = inputStream.readAllBytes();
    } catch (IOException e) {
      throw new SslConfigException("Unable to load KeyStore", e);
    }

    // Try PKCS12 first (more common for modern applications)
    var keyStore = tryLoadKeyStore(data, "PKCS12", password);
    if (keyStore != null) {
      return keyStore;
    }

    keyStore = tryLoadKeyStore(data, "JKS", password);
    if (keyStore != null) {
      return keyStore;
    }

    keyStore = tryLoadKeyStore(data, KeyStore.getDefaultType(), password);
    if (keyStore != null) {
      return keyStore;
    }

    throw new SslConfigException(
        "Unable to load KeyStore - format not recognized or invalid password");
  }

  private static KeyStore tryLoadKeyStore(byte[] data, String type, char[] password) {
    try (var bis = new ByteArrayInputStream(data)) {
      var keyStore = KeyStore.getInstance(type);
      keyStore.load(bis, password);
      return keyStore;
    } catch (Exception e) {
      // Ignore and try next format
      return null;
    }
  }

  static KeyStore loadIdentityFromPem(
      InputStream certificateInputStream, String privateKeyContent, char[] password) {
    try {
      var certificates = parseCertificates(certificateInputStream);
      var privateKey = parsePrivateKey(privateKeyContent, password);
      var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, null);

      var certChain = certificates.toArray(new Certificate[0]);
      var alias = "identity";
      var keyPassword = password != null ? password : new char[0];
      keyStore.setKeyEntry(alias, privateKey, keyPassword, certChain);

      return keyStore;
    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
      throw new SslConfigException("Failed to create KeyManager from PEM content", e);
    }
  }

  static List<Certificate> parsePemCertificates(String content) {
    var certs = PemAPISupport.certificates(content);
    if (certs.isEmpty()) {
      throw new SslConfigException("No valid certificate found in PEM content");
    }
    return certs;
  }

  static PrivateKey parsePrivateKey(String privateKeyContent, char[] password) {
    return PemAPISupport.privateKey(privateKeyContent, password);
  }

  static List<Certificate> parseCertificates(InputStream inputStream) {
    List<Certificate> certs = new ArrayList<>();

    // Read all bytes from the input stream
    byte[] data = null;
    try (inputStream) {
      data = inputStream.readAllBytes();
    } catch (IOException e) {
      throw new SslConfigException("Unable to load KeyStore", e);
    }

    // Try to parse as PEM first (check if it contains PEM markers)
    var content = new String(data, StandardCharsets.UTF_8);
    if (content.contains("-----BEGIN CERTIFICATE-----")) {
      certs.addAll(parsePemCertificates(content));
    } else {
      // Try to parse as DER format
      try (var bis = new ByteArrayInputStream(data)) {
        var factory = CertificateFactory.getInstance("X.509");
        var parsedCerts = factory.generateCertificates(bis);
        certs.addAll(parsedCerts);
      } catch (CertificateException | IOException e) {
        throw new SslConfigException("Unable to load KeyStore", e);
      }
    }
    return certs;
  }

  static List<Certificate> parseCertificates(String cert) {
    return parseCertificates(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
  }
}
