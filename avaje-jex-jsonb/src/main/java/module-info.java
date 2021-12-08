import io.avaje.jex.jsonb.JsonbJsonService;
import io.avaje.jex.spi.JsonService;

module io.avaje.jex.jsonb {

  opens io.avaje.jex.jsonb;

  requires io.avaje.jex;
  requires transitive io.avaje.jsonb;

  provides JsonService with JsonbJsonService;
}
