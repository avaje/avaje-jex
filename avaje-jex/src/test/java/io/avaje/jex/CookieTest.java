package io.avaje.jex;

import io.avaje.jex.Context.Cookie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CookieTest {

  @Test
  void format() {
    assertEquals("key=val", Cookie.of("key", "val").toString());
    assertEquals("key=val; Domain=dom", Cookie.of("key", "val").domain("dom").toString());
    assertEquals("key=val; Path=/pt", Cookie.of("key", "val").path("/pt").toString());
    //assertEquals("key=val; Path=/; Max-Age=10", Cookie.of("key", "val").maxAge(10).format());
    assertEquals("key=val; Secure", Cookie.of("key", "val").secure(true).toString());
    assertEquals("key=val; HttpOnly", Cookie.of("key", "val").httpOnly(true).toString());
    assertEquals("key=val; Secure; HttpOnly", Cookie.of("key", "val").httpOnly(true).secure(true).toString());
  }

  @Test
  void format_all() {
    assertEquals("key=val; Domain=dom; Path=/pt; Secure; HttpOnly", Cookie.of("key", "val")
      .domain("dom").path("/pt").secure(true).httpOnly(true).toString());
  }
}
