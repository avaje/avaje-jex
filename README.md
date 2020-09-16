# avaje-jex

### Notes:

- PathParser - Has segment count which we use with RouteIndex
- RouteIndex - matching paths by method + number of segments
- Immutable routes on startup - no adding/removing routes after start()
- Context json() - call through to "ServiceManager" which has the JsonService (no static JavalinJson)
