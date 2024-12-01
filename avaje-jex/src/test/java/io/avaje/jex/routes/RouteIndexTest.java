package io.avaje.jex.routes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class RouteIndexTest {

  @Test
  void match() {
    var indexBuild = new RouteIndexBuild();
    indexBuild.add(entry("/"));
    indexBuild.add(entry("/a/b/c"));
    indexBuild.add(entry("/a/b/d"));
    indexBuild.add(entry("/a/b/d/e"));
    indexBuild.add(entry("/a/b/d/e/f"));
    indexBuild.add(entry("/a/b/d/e/f/g"));
    indexBuild.add(entry("/a/b/d/e/f/g/h"));
    indexBuild.add(entry("/a/b/d/e/f/g2/h"));

    RouteIndex index = indexBuild.build();

    assertThat(index.match("/").matchPath()).isEqualTo("/");
    assertThat(index.match("/a/b/d/e/f/g2/h").matchPath()).isEqualTo("/a/b/d/e/f/g2/h");
  }

  @Test
  void matchMulti() {
    var indexBuild = new RouteIndexBuild();
    indexBuild.add(entry("/hi/{id}"));
    indexBuild.add(entry("/a/b/c"));
    indexBuild.add(entry("/hi/{id}"));
    indexBuild.add(entry("/b"));

    RouteIndex index = indexBuild.build();

    SpiRoutes.Entry entry = index.match("/hi/42");
    assertThat(entry).isNotNull();
  }

  @Test
  void match_args() {
    var indexBuild = new RouteIndexBuild();
    indexBuild.add(entry("/"));
    indexBuild.add(entry("/{id}"));
    indexBuild.add(entry("/{id}/a"));
    indexBuild.add(entry("/{id}/b"));
    indexBuild.add(entry("/a/{id}/c"));
    indexBuild.add(entry("/a/{name}/d"));
    indexBuild.add(entry("/a/b/d/e"));
    indexBuild.add(entry("/a/b/d/e/f"));
    indexBuild.add(entry("/a/b/d/e/f/g"));
    indexBuild.add(entry("/a/b/d/e/f/g/h"));
    indexBuild.add(entry("/a/b/d/e/f/g2/h"));

    var index = indexBuild.build();
    assertThat(index.match("/").matchPath()).isEqualTo("/");
    assertThat(index.match("/42").matchPath()).isEqualTo("/{id}");
    assertThat(index.match("/99").matchPath()).isEqualTo("/{id}");
    assertThat(index.match("/99/a").matchPath()).isEqualTo("/{id}/a");
    assertThat(index.match("/99/b").matchPath()).isEqualTo("/{id}/b");
    assertThat(index.match("/99/c")).isNull();
  }

  @Test
  void match_splat() {
    var indexBuild = new RouteIndexBuild();
    indexBuild.add(entry("/"));
    indexBuild.add(entry("/{id}"));
    indexBuild.add(entry("/{id}/a"));
    indexBuild.add(entry("/{id}/*"));

    var index = indexBuild.build();
    assertThat(index.match("/").matchPath()).isEqualTo("/");
    assertThat(index.match("/42").matchPath()).isEqualTo("/{id}");
    assertThat(index.match("/42/a").matchPath()).isEqualTo("/{id}/a");
    assertThat(index.match("/42/banana").matchPath()).isEqualTo("/{id}/*");
    assertThat(index.match("/42/banana/apple/grape/bean/nut").matchPath()).isEqualTo("/{id}/*");
  }

  private SpiRoutes.Entry entry(String path) {
    return new RouteEntry(new PathParser(path, true), null, Set.of());
  }

}
