module example.jdk {

  requires transitive io.avaje.jex.jdk;
  requires transitive org.slf4j;

  exports org.example to com.fasterxml.jackson.databind;
}
