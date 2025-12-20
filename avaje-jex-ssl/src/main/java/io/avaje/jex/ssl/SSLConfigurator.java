package io.avaje.jex.ssl;

import java.security.KeyStore;

import javax.net.ssl.SSLContext;

/** SSL KeyStore configurator that configures the underlying server. */
public interface SSLConfigurator {

  /** The {@link SSLContext} for this {@code HttpsConfigurator}. */
  SSLContext getSSLContext();

  /** The configured KeyStore */
  KeyStore keyStore();

  /** The configured KeyStore password */
  String password();
}
