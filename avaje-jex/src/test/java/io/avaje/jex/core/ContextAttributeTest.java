package io.avaje.jex.core;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContextAttributeTest {

  static final UUID uuid = UUID.randomUUID();

  static TestPair pair = init();

  static TestPair attrPair;
  static UUID attrUuid;

  static TestPair init() {
    var app =
        Jex.create()
            .routing(
                routing ->
                    routing
                        .filter(
                            (ctx, chain) -> {
                              ctx.attribute("oneUuid", uuid)
                                  .attribute(TestPair.class.getName(), pair);
                              chain.proceed();
                            })
                        .get(
                            "/",
                            ctx -> {
                              attrUuid = ctx.attribute("oneUuid");
                              attrPair = ctx.attribute(TestPair.class.getName());

                              assert attrUuid == uuid;
                              assert attrPair == pair;

                              //          ctx.attributeMap() is not supported
                              //          final Map<String, Object> attrMap = ctx.attributeMap();
                              //          final Object mapUuid = attrMap.get("oneUuid");
                              //          assert mapUuid == uuid;
                              //
                              //          final Object mapPair =
                              // attrMap.get(TestPair.class.getName());
                              //          assert mapPair == pair;
                              ctx.text("all-good");
                            }));
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("all-good");

    assertThat(attrPair).isSameAs(pair);
    assertThat(attrUuid).isSameAs(uuid);
  }
}
