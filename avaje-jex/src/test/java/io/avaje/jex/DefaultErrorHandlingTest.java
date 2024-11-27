package io.avaje.jex;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.DirectoryIteratorException;

import org.junit.jupiter.api.Test;

import io.avaje.jex.core.ExceptionManager;

class DefaultErrorHandlingTest {

  private final ExceptionHandler<RuntimeException> rt = new RT();
  private final ExceptionHandler<IllegalStateException> ise = new ISE();

  @Test
  void exception() {

    Routing router = new DefaultRouting();
    router.error(RuntimeException.class, rt);

    var handling = new ExceptionManager(router.errorHandlers());

    assertThat(handling.find(RuntimeException.class)).isSameAs(rt);
    assertThat(handling.find(IllegalStateException.class)).isSameAs(rt);
    assertThat(handling.find(DirectoryIteratorException.class)).isSameAs(rt);
  }

  @Test
  void exception_expect_highestMatch() {
    Routing router = new DefaultRouting();
    router.error(RuntimeException.class, rt);
    router.error(IllegalStateException.class, ise);

    var handling = new ExceptionManager(router.errorHandlers());

    assertThat(handling.find(IllegalStateException.class)).isSameAs(ise);
    assertThat(handling.find(RuntimeException.class)).isSameAs(rt);
    assertThat(handling.find(DirectoryIteratorException.class)).isSameAs(rt);
  }

  private static class RT implements ExceptionHandler<RuntimeException> {

    @Override
    public void handle(Context ctx, RuntimeException exception) {}
  }

  private static class ISE implements ExceptionHandler<IllegalStateException> {

    @Override
    public void handle(Context ctx, IllegalStateException exception) {}
  }
}
