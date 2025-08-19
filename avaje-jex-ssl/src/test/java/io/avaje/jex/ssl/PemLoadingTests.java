package io.avaje.jex.ssl;

import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.avaje.jex.ssl.cert.Server;

class PemLoadingTests extends IntegrationTestClass {

  @Test
  void loadingPasswordlessPemFileFromStringWorks() {
    assertSslWorks(
        config ->
            config.pemFromString(Server.CERTIFICATE_AS_STRING, Server.NON_ENCRYPTED_KEY_AS_STRING));
  }

  @Test
  void loadingInvalidKeyPemFileFromStringFails() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(config -> config.pemFromString(Server.CERTIFICATE_AS_STRING, "invalid"));
        });
  }

  @Test
  void loadingInvalidCertificatePemFileFromStringFails() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config -> config.pemFromString("invalid", Server.NON_ENCRYPTED_KEY_AS_STRING));
        });
  }

  @Test
  void loadingPemFileWithWrongPasswordFromStringFails() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromString(
                      Server.CERTIFICATE_AS_STRING, Server.ENCRYPTED_KEY_AS_STRING, "invalid"));
        });
  }

  @Test
  void loadingEncryptedPemFileFromString() {

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromString(
                      Server.CERTIFICATE_AS_STRING,
                      Server.ENCRYPTED_KEY_AS_STRING,
                      Server.KEY_PASSWORD));
        });
  }

  @Test
  void loadingPasswordlessPemFileFromClasspathWorks() {
    assertSslWorks(
        config ->
            config.pemFromClasspath(
                Server.CERTIFICATE_FILE_NAME, Server.NON_ENCRYPTED_KEY_FILE_NAME));
  }

  @Test
  void loadingEncryptedPemFileFromClasspathWorks() {

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromClasspath(
                      Server.CERTIFICATE_FILE_NAME,
                      Server.ENCRYPTED_KEY_FILE_NAME,
                      Server.KEY_PASSWORD));
        });
  }

  @Test
  void loadingPemFileWithWrongPasswordFromClasspathFails() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromClasspath(
                      Server.CERTIFICATE_FILE_NAME, Server.ENCRYPTED_KEY_FILE_NAME, "invalid"));
        });
  }

  @Test
  void loadingPemFileFromInvalidClasspathCertLocationFails() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> {
          assertSslWorks(
              config -> config.pemFromClasspath("invalid", Server.NON_ENCRYPTED_KEY_FILE_NAME));
        });
  }

  @Test
  void loadingPemFileFromInvalidClasspathKeyLocationFails() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> {
          assertSslWorks(
              config -> config.pemFromClasspath(Server.CERTIFICATE_FILE_NAME, "invalid"));
        });
  }

  @Test
  void loadingPasswordlessPemFileFromPathWorks() {
    assertSslWorks(
        config -> config.pemFromPath(Server.CERTIFICATE_PATH, Server.NON_ENCRYPTED_KEY_PATH));
  }

  @Test
  void loadingEncryptedPemFileFromPath() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromPath(
                      Server.CERTIFICATE_PATH, Server.ENCRYPTED_KEY_PATH, Server.KEY_PASSWORD));
        });
  }

  @Test
  void loadingPemFileWithWrongPasswordFromPathFails() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromPath(
                      Server.CERTIFICATE_PATH, Server.ENCRYPTED_KEY_PATH, "invalid"));
        });
  }

  @Test
  void loadingPemFileFromInvalidCertPathFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(config -> config.pemFromPath("invalid", Server.NON_ENCRYPTED_KEY_PATH));
        });
  }

  @Test
  void loadingPemFileFromInvalidKeyPathFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(config -> config.pemFromPath(Server.CERTIFICATE_PATH, "invalid"));
        });
  }

  @Test
  void loadingPasswordlessPemFileFromInputStreamWorks() {
    assertSslWorks(
        config ->
            config.pemFromInputStream(
                Server.CERTIFICATE_INPUT_STREAM_SUPPLIER.get(),
                Server.NON_ENCRYPTED_KEY_INPUT_STREAM_SUPPLIER.get()));
  }

  @Test
  void loadingEncryptedPemFileFromInputStreamWorks() {

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromInputStream(
                      Server.CERTIFICATE_INPUT_STREAM_SUPPLIER.get(),
                      Server.ENCRYPTED_KEY_INPUT_STREAM_SUPPLIER.get(),
                      Server.KEY_PASSWORD));
        });
  }

  @Test
  void loadingPemFileWithWrongPasswordFromInputStreamFails() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromInputStream(
                      Server.CERTIFICATE_INPUT_STREAM_SUPPLIER.get(),
                      Server.ENCRYPTED_KEY_INPUT_STREAM_SUPPLIER.get(),
                      "invalid"));
        });
  }

  @Test
  void loadingPemFileFromInvalidCertInputStreamFails() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromInputStream(
                      InputStream.nullInputStream(),
                      Server.NON_ENCRYPTED_KEY_INPUT_STREAM_SUPPLIER.get()));
        });
  }

  @Test
  void loadingPemFileFromInvalidKeyInputStreamFails() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.pemFromInputStream(
                      Server.ENCRYPTED_KEY_INPUT_STREAM_SUPPLIER.get(),
                      InputStream.nullInputStream()));
        });
  }
}
