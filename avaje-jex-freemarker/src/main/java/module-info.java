open module io.avaje.jex.freemarker {

  requires transitive io.avaje.jex;
  requires transitive freemarker;
  requires java.net.http;

  requires static io.avaje.jex.jetty;

  provides io.avaje.jex.TemplateRender with io.avaje.jex.render.freemarker.FreeMarkerRender;
}
