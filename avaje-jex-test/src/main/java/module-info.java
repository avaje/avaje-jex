open module io.avaje.jex.test {

  exports io.avaje.jex.test;

  requires transitive io.avaje.jex;
  requires transitive io.avaje.http.client;
  requires static com.fasterxml.jackson.databind;
  requires static io.avaje.jsonb;
  requires static io.avaje.inject.test;
  requires static org.apiguardian.api; // stink man !!

  provides io.avaje.inject.test.Plugin with io.avaje.jex.test.JexInjectPlugin;
}
