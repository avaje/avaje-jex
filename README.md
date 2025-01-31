![Supported JVM Versions](https://img.shields.io/badge/JVM-21+-brightgreen.svg?&logo=openjdk)
[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)
[![Build](https://github.com/avaje/avaje-jex/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-jex/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-jex/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-jex/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-jex/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jex.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jex)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-jex/javadoc.svg?color=purple)](https://javadoc.io/doc/io.avaje/avaje-jex)

# [Avaje-Jex](https://avaje.io/jex/)
Lightweight (~110KB) wrapper over the JDK's built-in [HTTP server](https://docs.oracle.com/en/java/javase/23/docs/api/jdk.httpserver/module-summary.html).

Features:

- [Context](https://javadoc.io/doc/io.avaje/avaje-jex/latest/io.avaje.jex/io/avaje/jex/Context.html) abstraction over `HttpExchange` to easily retrieve and send request/response data.
- Fluent API
- Static resource handling
- Compression SPI
- Json SPI
- Virtual threads enabled by default

## Quick Start

Add dependency:
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jex</artifactId>
  <version>${jex.version}</version>
</dependency>
```
Create Server:
```java
    Jex.create()
        .get("/", ctx -> ctx.text("hello"))
        .get("/one/{id}", ctx -> ctx.text("one-" + ctx.pathParam("id")))
        .filter(
            (ctx, chain) -> {
              System.out.println("before request");
              chain.proceed();
              System.out.println("after request");
            })
        .error(
            IllegalStateException.class,
            (ctx, exception) -> ctx.status(500).text(exception.getMessage()))
        .port(8080)
        .start();
```

## Use with [Avaje Http](https://avaje.io/http/)

If you find yourself pining for the JAX-RS style of controllers, you can have avaje http generate jex adapters for your annotated classes.

### Add dependencies

[![Avaje-HTTP](https://img.shields.io/maven-central/v/io.avaje/avaje-http-api.svg?label=avaje.http.version)](https://mvnrepository.com/artifact/io.avaje/avaje-jex)
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jex</artifactId>
  <version>${jex.version}</version>
</dependency>

<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-api</artifactId>
  <version>${avaje.http.version}</version>
</dependency>

<!-- Annotation processor -->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-jex-generator</artifactId>
  <version>${avaje.http.version}</version>
  <scope>provided</scope>
  <optional>true</optional>
</dependency>
```

#### JDK 23+

In JDK 23+, annotation processors are disabled by default, you will need to add a flag to re-enable.
```xml
<properties>
  <maven.compiler.proc>full</maven.compiler.proc>
</properties>
```

### Define a Controller
```java
package org.example.hello;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import java.util.List;

@Controller("/widgets")
public class WidgetController {
  private final HelloComponent hello;
  public WidgetController(HelloComponent hello) {
    this.hello = hello;
  }

  @Get("/{id}")
  Widget getById(int id) {
    return new Widget(id, "you got it"+ hello.hello());
  }

  @Get()
  List<Widget> getAll() {
    return List.of(new Widget(1, "Rob"), new Widget(2, "Fi"));
  }

  record Widget(int id, String name){};
}
```

This will generate routing code that we can register using any JSR-330 compliant DI:

```java
@Generated("avaje-jex-generator")
@Singleton
public class WidgetController$Route implements Routing.HttpService {

  private final WidgetController controller;

  public WidgetController$Route(WidgetController controller) {
    this.controller = controller;
  }

  @Override
  public void add(Routing routing) {
    routing.get("/widgets/{id}", this::_getById);
    routing.get("/widgets", this::_getAll);
  }

  private void _getById(Context ctx) throws IOException {
    ctx.status(200);
    var id = asInt(ctx.pathParam("id"));
    ctx.json(controller.getById(id));
  }

  private void _getAll(Context ctx) throws IOException {
    ctx.status(200);
    ctx.json(controller.getAll());
  }

}
```

### JSR-330 DI Usage
You can use whatever DI library you like.

```java
public class Main {

  public static void main(String[] args ) {

    List<Routing.HttpService> services = // Retrieve HttpServices via DI;
    Jex.create().routing(services).start();
  }
}
```


## Alternate `HttpServer` Implementations

The JDK provides an SPI to swap the underlying `HttpServer`, so you can easily use jex with alternate implementations by adding them as a dependency.

### Robaho

[@robaho's httpserver](https://github.com/robaho/httpserver?tab=readme-ov-file#performance) is a zero-dependency implementation that seems to increase performance by 10x over the built-in implementation, and 5x over Jetty in certain benchmarks.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.robaho/httpserver.svg?label=robaho.version)](https://mvnrepository.com/artifact/io.github.robaho/httpserver)
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jex</artifactId>
  <version>${jex.version}</version>
</dependency>

<dependency>
  <groupId>io.github.robaho</groupId>
  <artifactId>httpserver</artifactId>
  <version>${robaho.version}</version>
</dependency>
```

### Eclipse Jetty

[Jetty](https://jetty.org/) is a classic embedded server with a long and distinguished history.

[![Maven Central](https://img.shields.io/maven-central/v/org.eclipse.jetty/jetty-http-spi.svg?label=jetty.version)](https://mvnrepository.com/artifact/org.eclipse.jetty/jetty-http-spi)
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jex</artifactId>
  <version>${jex.version}</version>
</dependency>

<dependency>
  <groupId>org.eclipse.jetty</groupId>
  <artifactId>jetty-server</artifactId>
  <version>${jetty.version}</version>
</dependency>

<dependency>
  <groupId>org.eclipse.jetty</groupId>
  <artifactId>jetty-http-spi</artifactId>
  <version>${jetty.version}</version>
</dependency>
```

See also:

- [Javalin](https://github.com/javalin/javalin) (A lightweight wrapper over Jetty)
