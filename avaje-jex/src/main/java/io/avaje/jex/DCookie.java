package io.avaje.jex;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

final class DCookie implements Context.Cookie {

  private static final ZonedDateTime EXPIRED = ZonedDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0, 0), ZoneId.of("GMT"));
  private static final DateTimeFormatter RFC_1123_DATE_TIME = DateTimeFormatter.RFC_1123_DATE_TIME;
  private static final String PARAM_SEPARATOR = "; ";
  private final String name; // NAME= ... "$Name" style is reserved
  private final String value; // value of NAME
  private String domain; // ;Domain=VALUE ... domain that sees cookie
  private ZonedDateTime expires;
  private Duration maxAge;// = -1; // ;Max-Age=VALUE ... cookies auto-expire
  private String path; // ;Path=VALUE ... URLs that see the cookie
  private SameSite sameSite; // ;SameSite=Strict|Lax|None
  private boolean secure; // ;Secure ... e.g. use SSL
  private boolean httpOnly;
  private boolean partitioned;

  private DCookie(String name, String value) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("name required");
    }
    this.name = name;
    this.value = value;
  }

  static Context.Cookie expired(String name) {
    return new DCookie(name, "").expires(EXPIRED);
  }

  static Context.Cookie of(String name, String value) {
    return new DCookie(name, value);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public String domain() {
    return domain;
  }

  @Override
  public Context.Cookie domain(String domain) {
    this.domain = domain;
    return this;
  }

  @Override
  public Duration maxAge() {
    return maxAge;
  }

  @Override
  public Context.Cookie maxAge(Duration maxAge) {
    this.maxAge = maxAge;
    return this;
  }

  @Override
  public ZonedDateTime expires() {
    return expires;
  }

  @Override
  public Context.Cookie expires(ZonedDateTime expires) {
    this.expires = expires;
    return this;
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public Context.Cookie path(String path) {
    this.path = path;
    return this;
  }

  @Override
  public boolean secure() {
    return secure;
  }

  @Override
  public Context.Cookie secure(boolean secure) {
    this.secure = secure;
    return this;
  }

  @Override
  public boolean httpOnly() {
    return httpOnly;
  }

  @Override
  public Context.Cookie httpOnly(boolean httpOnly) {
    this.httpOnly = httpOnly;
    return this;
  }

  @Override
  public boolean partitioned() {
    return partitioned;
  }

  @Override
  public Context.Cookie partitioned(boolean partitioned) {
    this.partitioned = partitioned;
    return this;
  }

  @Override
  public SameSite sameSite() {
    return sameSite;
  }

  @Override
  public Context.Cookie sameSite(SameSite sameSite) {
    this.sameSite = sameSite;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(60);
    result.append(name).append('=').append(value);
    if (expires != null) {
      result.append(PARAM_SEPARATOR).append("Expires=").append(expires.format(RFC_1123_DATE_TIME));
    }
    if ((maxAge != null) && !maxAge.isNegative() && !maxAge.isZero()) {
      result.append(PARAM_SEPARATOR).append("Max-Age=").append(maxAge.getSeconds());
    }
    if (domain != null) {
      result.append(PARAM_SEPARATOR).append("Domain=").append(domain);
    }
    if (path != null) {
      result.append(PARAM_SEPARATOR).append("Path=").append(path);
    }
    if (sameSite != null) {
      result.append(PARAM_SEPARATOR).append("SameSite=").append(sameSite);
    }
    if (secure) {
      result.append(PARAM_SEPARATOR).append("Secure");
    }
    if (httpOnly) {
      result.append(PARAM_SEPARATOR).append("HttpOnly");
    }
    if (partitioned) {
      result.append(PARAM_SEPARATOR).append("Partitioned");
    }
    return result.toString();
  }
}
