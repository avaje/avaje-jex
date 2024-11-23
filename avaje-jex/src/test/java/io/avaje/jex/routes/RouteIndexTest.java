package io.avaje.jex.routes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.avaje.jex.Routing;

class RouteIndexTest {

  private static final Routing.Entry routingEntry = Mockito.mock(Routing.Entry.class);

  @Test
  void match() {
    RouteIndex index = new RouteIndex();
    index.add(entry("/"));
    index.add(entry("/a/b/c"));
    index.add(entry("/a/b/d"));
    index.add(entry("/a/b/d/e"));
    index.add(entry("/a/b/d/e/f"));
    index.add(entry("/a/b/d/e/f/g"));
    index.add(entry("/a/b/d/e/f/g/h"));
    index.add(entry("/a/b/d/e/f/g2/h"));

    assertThat(index.match("/").matchPath()).isEqualTo("/");
    assertThat(index.match("/a/b/d/e/f/g2/h").matchPath()).isEqualTo("/a/b/d/e/f/g2/h");
  }

  @Test
  void match_args() {
    RouteIndex index = new RouteIndex();
    index.add(entry("/"));
    index.add(entry("/{id}"));
    index.add(entry("/{id}/a"));
    index.add(entry("/{id}/b"));
    index.add(entry("/a/{id}/c"));
    index.add(entry("/a/{name}/d"));
    index.add(entry("/a/b/d/e"));
    index.add(entry("/a/b/d/e/f"));
    index.add(entry("/a/b/d/e/f/g"));
    index.add(entry("/a/b/d/e/f/g/h"));
    index.add(entry("/a/b/d/e/f/g2/h"));

    assertThat(index.match("/").matchPath()).isEqualTo("/");
    assertThat(index.match("/42").matchPath()).isEqualTo("/{id}");
    assertThat(index.match("/99").matchPath()).isEqualTo("/{id}");
    assertThat(index.match("/99/a").matchPath()).isEqualTo("/{id}/a");
    assertThat(index.match("/99/b").matchPath()).isEqualTo("/{id}/b");
    assertThat(index.match("/99/c")).isNull();
  }

  @Test
  void match_splat() {
    RouteIndex index = new RouteIndex();
    index.add(entry("/"));
    index.add(entry("/{id}"));
    index.add(entry("/{id}/a"));
    index.add(entry("/{id}/*"));

    assertThat(index.match("/").matchPath()).isEqualTo("/");
    assertThat(index.match("/42").matchPath()).isEqualTo("/{id}");
    assertThat(index.match("/42/a").matchPath()).isEqualTo("/{id}/a");
    assertThat(index.match("/42/banana").matchPath()).isEqualTo("/{id}/*");
    assertThat(index.match("/42/banana/apple/grape/bean/nut").matchPath()).isEqualTo("/{id}/*");
  }

  private SpiRoutes.Entry entry(String path) {
    return new RouteEntry(new PathParser(path, true), routingEntry.getHandler(), Set.of());
  }
}
