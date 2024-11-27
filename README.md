[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)
[![Build](https://github.com/avaje/avaje-jex/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-jex/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-jex/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-jex/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-jex/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jex.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jex)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-jex/javadoc.svg?color=purple)](https://javadoc.io/doc/io.avaje/avaje-jex)

# Avaje-Jex

Lightweight (~120KB) wrapper over the JDK's [`jdk.httpserver`](https://docs.oracle.com/en/java/javase/23/docs/api/jdk.httpserver/module-summary.html) `HttpServer` classes.

Features:

- Static resource handling
- Compression SPI (will use gzip by default)
- Json SPI (will use either Avaje JsonB or Jackson if available)
- Virtual threads enabled by default
- [Context](https://javadoc.io/doc/io.avaje/avaje-jex/latest/io.avaje.jex/io/avaje/jex/Context.html) abstraction over `HttpExchange` to easily retrieve and send request/response data.

```java
var app = Jex.create()
  .routing(routing -> routing
    .get("/", ctx -> ctx.text("hello"))
    .get("/one/{id}", ctx -> ctx.text("one-" + ctx.pathParam("id")))
    .filter(
        (ctx, chain) -> {
          System.out.println("before request");
          chain.proceed();
          System.out.println("after request");
        }))
  .staticResource(
        b ->
          b.httpPath("/myResource")
           .resource("/public")
           .directoryIndex("index.html"))
  .port(8080)
  .start();
```

### Alternate `HttpServer` Implementations

As the JDK provides an SPI to swap the underlying `HttpServer`, you can easily use jex with alternate implementations by adding them as a dependency.

An example would be [@robaho's implementation](https://github.com/robaho/httpserver?tab=readme-ov-file#performance) where performance seems to be increased by 10x in certain benchmarks.

```xml
<dependency>
  <groupId>io.github.robaho</groupId>
  <artifactId>httpserver</artifactId>
  <version>1.0.9</version>
</dependency>
```

See also:

- [Javalin](https://github.com/javalin/javalin) (A lightweight wrapper over Jetty)
