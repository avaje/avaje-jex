package io.avaje.jex.staticcontent;

import java.util.List;
import java.util.Map;

record CachedResource(Map<String, List<String>> headers, byte[] bytes) {}
