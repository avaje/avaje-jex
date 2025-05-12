package io.avaje.jex;

class Main {

  public static void main(String[] args) {

    Jex.create()
        .routing(
            routing ->
                routing
                    .get("/", ctx -> ctx.text("root"))
                    .get("/one", ctx -> ctx.text("one"))
                    .get(
                        "/two/{name}",
                        ctx -> {
                          System.out.println("pathParams: " + ctx.pathParamMap());
                          System.out.println("foo: " + ctx.queryParam("foo"));
                          ctx.text("two Yo " + ctx.pathParam("name"));
                        })
                    .post("one", ctx -> ctx.text("posted")))
        .port(7002)
        .start();
  }
}
