package io.avaje.jex.base;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.avaje.jex.Jex;
import io.avaje.jex.UploadedFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartFormPostTest {

  static TestPair pair = init();

  static final File helloFile = new File("src/test/resources/static-a/hello.txt");
  static final File hello2File = new File("src/test/resources/static-a/hello2.txt");

  static TestPair init() {
    final Jex app = Jex.create()
      .routing(routing -> routing
        .post("/simple", ctx -> {
          final UploadedFile file = ctx.uploadedFile("one");
          ctx.text("nm:" + file.name() + " fn:" + file.fileName() + " size:" + file.size());
        })
        .post("/both", ctx -> {
          final UploadedFile file = ctx.uploadedFile("one");
          ctx.text("nm:" + file.name() + " fn:" + file.fileName() + " size:" + file.size() + " paramMap:" + ctx.formParamMap());
        })
        .post("/multi", ctx -> {
          String out = "";
          final List<UploadedFile> files = ctx.uploadedFiles("one");
          for (UploadedFile file : files) {
            out += "file[nm:" + file.name() + " fn:" + file.fileName() + " size:" + file.size() + "]";
          }
          ctx.text(out + " paramMap:" + ctx.formParamMap());
        })
        .post("/delete", ctx -> {
          final UploadedFile file = ctx.uploadedFile("one");
          file.delete();
          ctx.text("withDelete nm:" + file.name() + " fn:" + file.fileName() + " size:" + file.size());
        })
      );

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void simple() throws UnirestException {
    final String baseUrl = pair.url();

    final com.mashape.unirest.http.HttpResponse<String> res =
      Unirest.post(baseUrl + "/simple")
        .field("one", helloFile)
        .asString();

    assertThat(res.getBody()).isEqualTo("nm:one fn:hello.txt size:18");
  }

  @Test
  void delete() throws UnirestException {
    final String baseUrl = pair.url();

    final com.mashape.unirest.http.HttpResponse<String> res =
      Unirest.post(baseUrl + "/delete")
        .field("one", helloFile)
        .asString();

    assertThat(res.getBody()).isEqualTo("withDelete nm:one fn:hello.txt size:18");
  }

  @Test
  void both() throws UnirestException {
    final String baseUrl = pair.url();

    final com.mashape.unirest.http.HttpResponse<String> res =
      Unirest.post(baseUrl + "/both")
        .field("a", "aval")
        .field("b", "bval")
        .field("one", helloFile)
        .asString();

    assertThat(res.getBody()).isEqualTo("nm:one fn:hello.txt size:18 paramMap:{a=[aval], b=[bval]}");
  }

  @Test
  void multipleFiles() throws UnirestException {
    final String baseUrl = pair.url();

    final com.mashape.unirest.http.HttpResponse<String> res =
      Unirest.post(baseUrl + "/multi")
        .field("a", "a1")
        .field("a", "a2")
        .field("b", "b1")
        .field("b", "b2")
        .field("c", "c1")
        .field("one", helloFile)
        .field("one", hello2File)
        .asString();

    assertThat(res.getBody()).isEqualTo("file[nm:one fn:hello.txt size:18]file[nm:one fn:hello2.txt size:28] paramMap:{a=[a1, a2], b=[b1, b2], c=[c1]}");
  }
}
