module io.avaje.jex.mustache {

  requires transitive io.avaje.jex;
  requires transitive com.github.mustachejava;
  requires java.net.http;

  requires static io.avaje.spi;

  provides io.avaje.jex.spi.JexExtension with io.avaje.jex.render.mustache.MustacheRender;
}
