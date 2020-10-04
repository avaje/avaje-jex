package io.avaje.jex.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper to create model map of key value pairs.
 */
public class Model {

  /**
   * Convert the arguments into a map of String keys to Object values.
   *
   * @param key    The first key
   * @param val    The first value
   * @param others Other key values where the key should be a string
   * @return The map of key value pairs
   */
  public static Map<String, Object> toMap(String key, Object val, Object... others) {
    if (others.length % 2 != 0) {
      throw new IllegalArgumentException("Must have even number of args (key, value)");
    }
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(key, val);
    if (others.length > 2) {
      for (int i = 0; i < others.length; i = i + 2) {
        map.put(others[i].toString(), others[i + 1]);
      }
    }
    return map;
  }
}
