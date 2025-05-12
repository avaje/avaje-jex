open module io.avaje.jex.test {
  exports io.avaje.jex.test;

  requires transitive io.avaje.jex;
  requires transitive io.avaje.http.client;
  requires transitive com.fasterxml.jackson.databind;
  requires static io.avaje.inject.test;

  provides io.avaje.inject.test.Plugin with
      io.avaje.jex.test.JexInjectPlugin;
}
