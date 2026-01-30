module io.avaje.jex.http3.flupke {
  exports io.avaje.jex.http3.flupke;
  exports io.avaje.jex.http3.flupke.webtransport;

  requires static io.avaje.spi;

  requires transitive io.avaje.jex.ssl;
  requires transitive tech.kwik.flupke;
  requires java.base;

  provides io.avaje.jex.spi.JexExtension
    with io.avaje.jex.http3.flupke.FlupkeJexPlugin;
}
