package io.avaje.jex.cors;

enum WildcardResult {
  NoWildcardDetected,
  WildcardOkay,
  TooManyWildcards,
  WildcardNotAtTheStartOfTheHost;
}
