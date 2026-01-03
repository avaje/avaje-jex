package io.avaje.jex;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ShutDownTest {

  @Test
  void shutDownHooks() {

    List<String> results = new ArrayList<>();
    var jex = Jex.create().config(c -> c.socketBacklog(0));
    jex.lifecycle().onShutdown(() -> results.add("onShut"));
    jex.lifecycle().registerShutdownHook(() -> results.add("onHook"));
    var server = jex.start();

    server.onShutdown(() -> results.add("serverShutFirst"), Integer.MIN_VALUE);
    server.onShutdown(() -> results.add("serverShutLast"));
    server.shutdown();
    assertThat(results).hasSize(3); // 2 because jvm shutdown won't run in a junit
    assertThat(results).containsExactly("serverShutFirst", "onShut", "serverShutLast");
  }
}
