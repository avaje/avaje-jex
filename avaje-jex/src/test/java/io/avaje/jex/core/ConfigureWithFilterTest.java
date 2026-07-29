package io.avaje.jex.core;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.http.HttpFilter;
import io.avaje.jex.spi.JexPlugin;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigureWithFilterTest {

  @Test
  void httpFilterBean_isAutoCollected() {
    AtomicInteger filterHits = new AtomicInteger();

    HttpFilter filter =
        (ctx, chain) -> {
          filterHits.incrementAndGet();
          ctx.header("x-filter", "applied");
          chain.proceed();
        };

    try (BeanScope scope = BeanScope.builder().bean(HttpFilter.class, filter).build()) {

      Jex app = Jex.create().configureWith(scope).routing(r -> r.get("/", ctx -> ctx.text("ok")));

      try (TestPair pair = TestPair.create(app)) {
        HttpResponse<String> res = pair.request().GET().asString();

        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body()).isEqualTo("ok");
        assertThat(res.headers().firstValue("x-filter")).get().isEqualTo("applied");
        assertThat(filterHits.get()).isEqualTo(1);
      }
    }
  }

  @Test
  void httpFilterBean_and_jexPluginBean_bothApplied() {
    AtomicInteger filterHits = new AtomicInteger();
    AtomicInteger pluginFilterHits = new AtomicInteger();

    HttpFilter filter =
        (ctx, chain) -> {
          filterHits.incrementAndGet();
          ctx.header("x-filter", "applied");
          chain.proceed();
        };

    JexPlugin plugin =
        jex ->
            jex.filter(
                (ctx, chain) -> {
                  pluginFilterHits.incrementAndGet();
                  ctx.header("x-plugin-filter", "applied");
                  chain.proceed();
                });

    try (BeanScope scope =
        BeanScope.builder()
            .bean(HttpFilter.class, filter)
            .bean(JexPlugin.class, plugin)
            .build()) {

      Jex app = Jex.create().configureWith(scope).routing(r -> r.get("/", ctx -> ctx.text("ok")));

      try (TestPair pair = TestPair.create(app)) {
        HttpResponse<String> res = pair.request().GET().asString();

        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.headers().firstValue("x-filter")).get().isEqualTo("applied");
        assertThat(res.headers().firstValue("x-plugin-filter")).get().isEqualTo("applied");
        assertThat(filterHits.get()).isEqualTo(1);
        assertThat(pluginFilterHits.get()).isEqualTo(1);
      }
    }
  }
}
