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

### Notes:

- PathParser - Has segment count which we use with RouteIndex
- RouteIndex - matching paths by method + number of segments
- Immutable routes on startup - no adding/removing routes after start()
- Context json() - call through to "ServiceManager" which has the JsonService (no static JavalinJson)


### TODO
- formParam, formParams(key: String), formParamMap()
- Uploaded files
- cookie store
- app attributes

### Intentionally excluded features
-


### To Review
- endpointHandlerPath()
- bodyValidator
