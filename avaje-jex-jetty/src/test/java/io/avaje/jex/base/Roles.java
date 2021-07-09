package io.avaje.jex.base;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Create an app specific Role annotation that uses the
 * app specific role enum.
 */
@Target(value={METHOD, TYPE})
@Retention(value=RUNTIME)
public @interface Roles {

  /**
   * Specify the permitted roles (using app specific enum).
   */
  AppRoles[] value() default {};
}
