package io.avaje.jex.ssl;

import static java.util.Base64.getDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

final class KeyStoreUtil {
  private static final Pattern CERT_PATTERN =
      Pattern.compile("-----BEGIN CERTIFICATE-----(.+?)-----END CERTIFICATE-----", Pattern.DOTALL);

  private static final Pattern PRIVATE_KEY_PATTERN =
      Pattern.compile(
          "-----BEGIN (?:RSA )?PRIVATE KEY-----(.+?)-----END (?:RSA )?PRIVATE KEY-----",
          Pattern.DOTALL);

  /** Load a KeyStore from an InputStream. Automatically detects JKS and PKCS12 formats. */
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
    try {
      var keyStore = KeyStore.getInstance(type);
      try (var bis = new ByteArrayInputStream(data)) {
        keyStore.load(bis, password);
        return keyStore;
      }
    } catch (Exception e) {
      // Ignore and try next format
      return null;
    }
  }

  static X509ExtendedKeyManager loadIdentityFromPem(
      InputStream certificateInputStream, String privateKeyContent, char[] password) {
    try {
      var certificates = parseCertificates(certificateInputStream);

      var privateKey = parsePrivateKey(privateKeyContent, password);

      // Create a KeyStore with the certificate and private key
      var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, null);

      var certChain = certificates.toArray(new Certificate[0]);
      var alias = "identity";
      var keyPassword = password != null ? password : new char[0];

      keyStore.setKeyEntry(alias, privateKey, keyPassword, certChain);

      var kmf =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keyStore, keyPassword);

      for (var km : kmf.getKeyManagers()) {
        if (km instanceof X509ExtendedKeyManager m) {
          return m;
        }
      }

      throw new SslConfigException("No X509ExtendedKeyManager found");

    } catch (Exception e) {
      throw new SslConfigException("Failed to create KeyManager from PEM content", e);
    }
  }

  static List<Certificate> parseCertificates(String content, Pattern certPattern) {

    List<Certificate> certs = new ArrayList<>();
    CertificateFactory factory = null;
    try {
      factory = CertificateFactory.getInstance("X.509");

      var matcher = certPattern.matcher(content);
      while (matcher.find()) {
        var base64Cert = matcher.group(1).replaceAll("\\s", "");

        var certBytes = Base64.getDecoder().decode(base64Cert);
        try (var bis = new ByteArrayInputStream(certBytes)) {
          var cert = factory.generateCertificate(bis);
          certs.add(cert);
        }
      }
    } catch (Exception e) {
      throw new SslConfigException("Failed to parse PEM certificate", e);
    }

    if (certs.isEmpty()) {
      throw new SslConfigException("No valid certificate found in PEM content");
    }

    return certs;
  }

  static PrivateKey parsePrivateKey(String privateKeyContent, char[] password) {
    try {
      var matcher = PRIVATE_KEY_PATTERN.matcher(privateKeyContent);
      if (!matcher.find()) {
        throw new IllegalArgumentException("No valid private key found in PEM content");
      }

      var base64Key = matcher.group(1).replaceAll("\\s+", "");
      var keyBytes = getDecoder().decode(base64Key);

      // TODO add decryption if enough people ask
      if (password != null && password.length > 0) {
        throw new UnsupportedOperationException(
            "Encrypted private keys not supported in this implementation. Please decrypt the key first.");
      }

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

  static List<Certificate> parseCertificates(InputStream inputStream) {
    List<Certificate> certs = new ArrayList<>();

    // Read all bytes from the input stream
    byte[] data = null;
    try {
      data = inputStream.readAllBytes();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Try to parse as PEM first (check if it contains PEM markers)
    var content = new String(data, StandardCharsets.UTF_8);

    if (content.contains("-----BEGIN CERTIFICATE-----")) {
      certs.addAll(parseCertificates(content, CERT_PATTERN));
    } else {
      // Try to parse as DER format
      try (var bis = new ByteArrayInputStream(data)) {
        var factory = CertificateFactory.getInstance("X.509");
        var parsedCerts = factory.generateCertificates(bis);
        certs.addAll(parsedCerts);
      } catch (CertificateException | IOException e) {
        e.printStackTrace();
      }
    }

    return certs;
  }

  static List<Certificate> parseCertificates(String cert) {
    return parseCertificates(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
  }
}
