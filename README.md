[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)
[![Build](https://github.com/avaje/avaje-jex/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-jex/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-jex/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-jex/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-jex/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jex.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jex)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-jex/javadoc.svg?color=purple)](https://javadoc.io/doc/io.avaje/avaje-jex)

# avaje-jex

Javalin style Wrapper over the JDK's `jdk.httpserver` server.

```java
var app = Jex.create()
  .routing(routing -> routing
    .get("/", ctx -> ctx.text("hello"))
    .get("/one/{id}", ctx -> ctx.text("one-" + ctx.pathParam("id")))
  )
  .port(8080)
  .start();
```

### TODO
- static file configuration
