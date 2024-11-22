import io.avaje.jex.render.freemarker.FreeMarkerRender;
import io.avaje.jex.spi.JexExtension;

open module io.avaje.jex.freemarker {

  requires transitive io.avaje.jex;
  requires transitive freemarker;
  requires java.net.http;

  requires static io.avaje.spi;

  provides JexExtension with FreeMarkerRender;
}
