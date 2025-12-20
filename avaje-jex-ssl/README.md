# SSL/TLS Configuration
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jex-ssl.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jex-ssl)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-jex-ssl/javadoc.svg?color=purple)](https://javadoc.io/doc/io.avaje/avaje-jex-ssl)

SSL plugin for configuring HTTPS with support for loading key stores, PEM certificates, and mutual TLS (mTLS).

## Installation

Add the SSL dependency to your project:
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jex-ssl</artifactId>
  <version>${avaje.jex.version}</version>
</dependency>
```

## SSL Plugin

The `SslPlugin` can be configured using a fluent configuration API:
```java
var sslPlugin = SslPlugin.create(config ->
    config.keystoreFromClasspath("keystore.p12", "password"));

Jex.create()
    .plugin(sslPlugin)
    .get("/", ctx -> ctx.text("Hello Secure World"))
    .port(8443)
    .start();
```

## Key Store Configuration

The SSL configuration supports loading key stores from multiple sources with optional separate identity passwords:
```java
var sslPlugin = SslPlugin.create(config -> {
    // From file system
    config.keystoreFromPath("/path/to/keystore.p12", "keystorePassword", "identityPassword");

    // From classpath
    config.keystoreFromClasspath("ssl/keystore.jks", "password");

    // From input stream
    config.keystoreFromInputStream(inputStream, "password");
});
```

## PEM Certificate Configuration

For PEM-formatted certificates and private keys, the plugin supports various sources and optional private key passwords:
```java
var sslPlugin = SslPlugin.create(config -> {
    // From file system
    config.pemFromPath("/path/to/cert.pem", "/path/to/private-key.pem", "keyPassword");

    // From classpath
    config.pemFromClasspath("ssl/certificate.pem", "ssl/private-key.pem");

    // From strings (useful for environment variables or external config)
    config.pemFromString(certPemString, privateKeyPemString);

    // From input streams
    config.pemFromInputStream(certInputStream, keyInputStream, "password");
});
```

## Mutual TLS (mTLS) Configuration

For client certificate authentication, configure trust settings using the `TrustConfig` interface:
```java
var sslPlugin = SslPlugin.create(config -> {
    // Configure server identity
    config.keystoreFromClasspath("server-keystore.p12", "serverPassword");

    // Configure client certificate trust
    config.withTrustConfig(trust -> {
        // Trust specific client certificates
        trust.certificateFromClasspath("client-cert.pem")
             .certificateFromPath("/path/to/another-client-cert.crt");

        // Or use a trust store
        trust.trustStoreFromClasspath("truststore.jks", "trustPassword");

        // Mix different certificate formats
        trust.certificateFromString(pemCertString)
             .certificateFromInputStream(certInputStream);
    });
});
```
