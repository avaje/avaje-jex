open module io.avaje.jex.mustache {

  requires transitive io.avaje.jex;
  requires transitive com.github.mustachejava;
  requires java.net.http;

  provides io.avaje.jex.TemplateRender with io.avaje.jex.render.mustache.MustacheRender;
}
