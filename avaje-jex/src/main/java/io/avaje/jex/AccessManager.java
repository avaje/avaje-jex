/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.avaje.jex;


import java.util.Set;

/**
 *
 */
@FunctionalInterface
public interface AccessManager {

  void manage(Handler handler, Context ctx, Set<Role> permittedRoles) throws Exception;
}
