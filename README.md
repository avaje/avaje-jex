[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)
[![Build](https://github.com/avaje/avaje-jex/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-jex/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-jex/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-jex/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-jex/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jex.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jex)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-jex/javadoc.svg?color=purple)](https://javadoc.io/doc/io.avaje/avaje-jex)

# avaje-jex

Java cut down version of https://javalin.io

```java
var app = Jex.create()
  .routing(routing -> routing
    .get("/", ctx -> ctx.text("hello"))
    .get("/one/{id}", ctx -> ctx.text("one-" + ctx.pathParam("id")))
  )
  .staticFiles().addClasspath("/static", "content")
  .staticFiles().addExternal("/other", "/external")
  .port(8080)
  .start();

```

### Goals / intention

- Help progress converting Javalin internals from Kotlin to Java
    - Convert bits of Javalin internals to Java
    - Maybe get feedback from David if there is design impact
    - Prepare small PR's to Javalin (this is going to take time)

- Another goal is to explore some options for Javalin along the lines of
    - matching routes (making use of path segment count)
    - organisation of internals to reduce some statics (JavalinJson)
    - modularisation of internals using ServiceLoader (for templating implementation, websockets and sse - make these all optional dependencies keeping core small)

### Design Notes (different to Javalin):
- Context is an interface
- Routing, ErrorHandling, StaticFileConfig are interfaces
- PathParser - Has segment count which we use with RouteIndex
- RouteIndex - matching paths by method + number of segments
- Immutable routes on startup - no adding/removing routes after start()
- Context json() - call through to "ServiceManager" which has the JsonService (no static JavalinJson)

### Differences to Javalin
- Uses `{}` rather than `:` for defining path parameters
- Supports use of regex in path segments e.g `{id:[0-9]+}` (provides tighter path matching)
- Added ctx.text(...) for plain text response
- Method name change to use ctx.write(...) rather than ctx.result(...)

### TODO
- cookie store
- app attributes
- basicAuthCredentials/basicAuthCredentialsExist
- plugin api
- render in progress - FreeMarker and Mustache done
- web sockets
- sse

### Intentionally excluded features
-


### To Review
- Javalin uses int getContentLength() rather than long getContentLengthLong()
- Javalin removeCookie should set null path to "/"
- endpointHandlerPath()
- bodyValidator
