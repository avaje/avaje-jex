package io.avaje.jex;

import org.junit.jupiter.api.Test;

import java.nio.file.DirectoryIteratorException;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultErrorHandlingTest {

  private final ExceptionHandler rt = new RT();
  private final ExceptionHandler ise = new ISE();

  @Test
  void exception() {

    DefaultErrorHandling handling = new DefaultErrorHandling();
    handling.exception(RuntimeException.class, rt);

    assertThat(handling.find(RuntimeException.class)).isSameAs(rt);
    assertThat(handling.find(IllegalStateException.class)).isSameAs(rt);
    assertThat(handling.find(DirectoryIteratorException.class)).isSameAs(rt);
  }

  @Test
  void exception_expect_highestMatch() {

    DefaultErrorHandling handling = new DefaultErrorHandling();
    handling.exception(RuntimeException.class, rt);
    handling.exception(IllegalStateException.class, ise);

    assertThat(handling.find(IllegalStateException.class)).isSameAs(ise);
    assertThat(handling.find(RuntimeException.class)).isSameAs(rt);
    assertThat(handling.find(DirectoryIteratorException.class)).isSameAs(rt);
  }

  private static class RT implements ExceptionHandler<RuntimeException> {

    @Override
    public void handle(RuntimeException exception, Context ctx) {
    }
  }

  private static class ISE implements ExceptionHandler<IllegalStateException> {

    @Override
    public void handle(IllegalStateException exception, Context ctx) {
    }
  }
}
