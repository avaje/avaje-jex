package io.avaje.jex.staticcontent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContentDispositionTest {

  @Test
  void noFilename() {
    assertThat(ContentDisposition.attachment().headerValue()).isEqualTo("attachment");
    assertThat(ContentDisposition.inline().headerValue()).isEqualTo("inline");
  }

  @Test
  void asciiFilename() {
    assertThat(ContentDisposition.attachment("report.pdf").headerValue())
        .isEqualTo("attachment; filename=\"report.pdf\"");
  }

  @Test
  void nonAsciiFilenameAddsExtValue() {
    assertThat(ContentDisposition.inline("naïve café.txt").headerValue())
        .isEqualTo("inline; filename=\"na_ve caf_.txt\"; filename*=UTF-8''na%C3%AFve%20caf%C3%A9.txt");
  }

  @Test
  void stripsPathComponents() {
    assertThat(ContentDisposition.attachment("/etc/passwd").filename()).isEqualTo("passwd");
    assertThat(ContentDisposition.attachment("..\\..\\secret.txt").filename()).isEqualTo("secret.txt");
  }

  @Test
  void stripsControlCharactersPreventingHeaderInjection() {
    var disposition = ContentDisposition.attachment("evil\r\nX-Injected: yes.txt");
    assertThat(disposition.filename()).isEqualTo("evilX-Injected: yes.txt");
    assertThat(disposition.headerValue()).doesNotContain("\r").doesNotContain("\n");
  }

  @Test
  void escapesQuotes() {
    assertThat(ContentDisposition.attachment("we\"ird.txt").headerValue())
        .isEqualTo("attachment; filename=\"we\\\"ird.txt\"");
  }

  @Test
  void prefixesWindowsDeviceNames() {
    assertThat(ContentDisposition.attachment("con.txt").filename()).isEqualTo("_con.txt");
    assertThat(ContentDisposition.attachment("LPT1").filename()).isEqualTo("_LPT1");
    assertThat(ContentDisposition.attachment("console.txt").filename()).isEqualTo("console.txt");
  }

  @Test
  void emptyOrDotFilenameFallsBack() {
    assertThat(ContentDisposition.attachment("   ").filename()).isEqualTo("download");
    assertThat(ContentDisposition.attachment("..").filename()).isEqualTo("download");
  }
}
