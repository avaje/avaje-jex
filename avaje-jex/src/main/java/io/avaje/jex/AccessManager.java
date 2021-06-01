/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */
package io.avaje.jex;


import java.util.Set;

/**
 * Provide access check for routes that have permitted roles assigned to them.
 *
 * <p>
 * An example implementation might look like the code below.
 *
 * <pre>{@code
 *
 *   var app = Jex.create()
 *     .accessManager((handler, ctx, permittedRoles) -> {
 *
 *         // obtain current users role(s)
 *         final String userRole = ...
 *
 *         if (userRole == null || !permittedRoles.contains(AppRoles.valueOf(userRole))) {
 *           ctx.status(401).text("Unauthorized");
 *         } else {
 *           // allow
 *           handler.handle(ctx);
 *         }
 *       })
 *
 * }</pre>
 */
@FunctionalInterface
public interface AccessManager {

  /**
   * Check that the current user has one of the required roles.
   * <p>
   * Implementations should call the handler if the user has one of
   * the permitted roles.
   *
   * @param handler        The handler to call if the user has an appropriate role.
   * @param ctx            The context
   * @param permittedRoles The permitted roles for the endpoint
   */
  void manage(Handler handler, Context ctx, Set<Role> permittedRoles);
}
