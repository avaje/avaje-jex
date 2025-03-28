package io.avaje.jex.security;

import java.util.HashMap;
import java.util.Map;

import io.avaje.config.Config;
import io.avaje.jex.http.Context;

public enum AppRole implements Role {
  ANYONE("", ""),
  USER(Config.get("roles.user.username", "test"), Config.get("roles.user.password", "test"));

  private final String username;
  private final String password;

  AppRole(String username, String password) {
    this.username = username;
    this.password = password;
  }

  private static final Map<String, AppRole> ROLE_MAP = createRoleMap();

  private static Map<String, AppRole> createRoleMap() {
    Map<String, AppRole> map = new HashMap<>();
    for (AppRole role : AppRole.values()) {
      if (!ANYONE.equals(role)) {
        map.put(role.username + ":" + role.password, role);
      }
    }
    return map;
  }

  public static AppRole getRole(Context ctx) {

    final var auth = ctx.basicAuthCredentials();
    return ROLE_MAP.getOrDefault(auth.userName() + ":" + auth.password(), ANYONE);
  }
}
