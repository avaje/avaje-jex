package io.avaje.jex;

import io.avaje.jex.Context.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class CookieTest {

  @Test
  void format() {
    assertEquals("key=val", Cookie.of("key", "val").toString());
    assertEquals("key=val; Domain=dom", Cookie.of("key", "val").domain("dom").toString());
    assertEquals("key=val; Path=/pt", Cookie.of("key", "val").path("/pt").toString());
    assertEquals("key=val; Max-Age=10", Cookie.of("key", "val").maxAge(Duration.ofSeconds(10)).toString());
    assertEquals("key=val; Secure", Cookie.of("key", "val").secure(true).toString());
    assertEquals("key=val; HttpOnly", Cookie.of("key", "val").httpOnly(true).toString());
    assertEquals("key=val; Partitioned", Cookie.of("key", "val").partitioned(true).toString());
    assertEquals("key=val; SameSite=Strict", Cookie.of("key", "val").sameSite(Cookie.SameSite.Strict).toString());
    assertEquals("key=val; Secure; HttpOnly", Cookie.of("key", "val").httpOnly(true).secure(true).toString());
  }

  @Test
  void partitioned() {
    var c = Cookie.of("k", "v").secure(true).partitioned(true);
    assertTrue(c.partitioned());

    c.partitioned(false);
    assertFalse(c.partitioned());

    assertEquals("k=v; Secure; Partitioned", Cookie.of("k", "v").secure(true).partitioned(true).toString());
    assertEquals("k=v; Secure", Cookie.of("k", "v").secure(true).partitioned(false).toString());

  }
  @Test
  void sameSite() {

    var c = Cookie.of("k", "v");
    assertNull(c.sameSite());
    c.sameSite(Cookie.SameSite.Strict);
    assertEquals(Cookie.SameSite.Strict, c.sameSite());
    c.sameSite(Cookie.SameSite.Lax);
    assertEquals(Cookie.SameSite.Lax, c.sameSite());
    c.sameSite(Cookie.SameSite.None);
    assertEquals(Cookie.SameSite.None, c.sameSite());
    c.sameSite(null);
    assertNull(c.sameSite());

    assertEquals("key=val; SameSite=Strict; Secure", Cookie.of("key", "val").secure(true).sameSite(Cookie.SameSite.Strict).toString());
    assertEquals("key=val; SameSite=Lax; Secure", Cookie.of("key", "val").secure(true).sameSite(Cookie.SameSite.Lax).toString());
    assertEquals("key=val; SameSite=None; Secure", Cookie.of("key", "val").secure(true).sameSite(Cookie.SameSite.None).toString());
    assertEquals("key=val; Secure", Cookie.of("key", "val").secure(true).sameSite(null).toString());
  }

  @Test
  void format_all() {
    var cookie = Cookie.of("key", "val")
      .domain("dom")
      .path("/pt")
      .secure(true)
      .httpOnly(true)
      .partitioned(true)
      .sameSite(Cookie.SameSite.Strict);

    assertTrue(cookie.secure());
    assertTrue(cookie.httpOnly());
    assertTrue(cookie.partitioned());

    assertEquals("key=val; Domain=dom; Path=/pt; SameSite=Strict; Secure; HttpOnly; Partitioned",
      cookie.toString());
  }
}
