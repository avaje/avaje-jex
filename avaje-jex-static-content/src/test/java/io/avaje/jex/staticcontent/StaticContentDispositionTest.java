package io.avaje.jex.staticcontent;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;

class StaticContentDispositionTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app =
        Jex.create()
            .plugin(
                StaticContent.ofClassPath("/public")
                    .directoryIndex("index.html")
                    .route("/attach/*")
                    .contentDisposition(ContentDisposition.Type.ATTACHMENT)
                    .build())
            .plugin(
                StaticContent.ofFile("src/test/resources/public")
                    .directoryIndex("index.html")
                    .route("/attachFile/*")
                    .contentDisposition(ContentDisposition.Type.ATTACHMENT)
                    .build())
            .plugin(
                StaticContent.ofClassPath("/public")
                    .directoryIndex("index.html")
                    .route("/custom/*")
                    .contentDisposition(
                        (ctx, filename) ->
                            filename.endsWith(".css")
                                ? ContentDisposition.inline(filename)
                                : null)
                    .build())
            .plugin(
                StaticContent.ofClassPath("/public")
                    .directoryIndex("index.html")
                    .route("/cached/*")
                    .preCompress()
                    .contentDisposition(ContentDisposition.Type.ATTACHMENT)
                    .build())
            .plugin(
                StaticContent.ofClassPath("/logback.xml")
                    .route("/single")
                    .contentDisposition(ContentDisposition.Type.ATTACHMENT)
                    .build());

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  private static String disposition(HttpResponse<String> res) {
    return res.headers().firstValue("Content-Disposition").orElse(null);
  }

  @Test
  void classPathAttachment() {
    HttpResponse<String> res = pair.request().path("attach/sus.txt").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(disposition(res)).isEqualTo("attachment; filename=\"sus.txt\"");
  }

  @Test
  void fileAttachment() {
    HttpResponse<String> res = pair.request().path("attachFile/sus.txt").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(disposition(res)).isEqualTo("attachment; filename=\"sus.txt\"");
  }

  @Test
  void directoryIndexAttachment() {
    HttpResponse<String> res = pair.request().path("attach/").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(disposition(res)).isEqualTo("attachment; filename=\"index.html\"");
  }

  @Test
  void singleFileAttachment() {
    HttpResponse<String> res = pair.request().path("single").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(disposition(res)).isEqualTo("attachment; filename=\"logback.xml\"");
  }

  @Test
  void filenameWithSpaceIsQuoted() {
    HttpResponse<String> res =
        pair.request().path("attach/Extinction%20Party.txt").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(disposition(res)).isEqualTo("attachment; filename=\"Extinction Party.txt\"");
  }

  @Test
  void headRequestGetsDisposition() {
    HttpResponse<String> res = pair.request().path("attach/sus.txt").HEAD().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(disposition(res)).isEqualTo("attachment; filename=\"sus.txt\"");
  }

  @Test
  void nullDispositionSendsNoHeader() {
    HttpResponse<String> res = pair.request().path("custom/sus.txt").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(disposition(res)).isNull();

    res = pair.request().path("custom/bundle.css").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(disposition(res)).isEqualTo("inline; filename=\"bundle.css\"");
  }

  @Test
  void preCompressedIsNotDuplicatedOnCacheHit() {
    for (int i = 0; i < 3; i++) {
      HttpResponse<String> res = pair.request().path("cached/bundle.css").GET().asString();
      assertThat(res.statusCode()).isEqualTo(200);
      assertThat(res.headers().allValues("Content-Disposition"))
          .containsExactly("attachment; filename=\"bundle.css\"");
    }
  }
}
