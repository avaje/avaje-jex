package io.avaje.jex.file.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.file.upload.MultipartConfig.SizeUnit;
import io.avaje.jex.test.TestPair;

class MultipartUploadTest {

  private static final String BOUNDARY = "ThisSeemsLikeAPrettyGoodBoundary";

  @Test
  void testNonMultipart() {

    var jex =
        Jex.create()
            .post(
                "/upload",
                ctx -> {
                  var part = ctx.attribute(FileUploadService.class).uploadedFile("file");

                  assert part == null;
                })
            .plugin(FileUploadPlugin.create());
    var pair = TestPair.create(jex);

    var response =
        pair.request()
            .requestTimeout(Duration.ofDays(1))
            .path("upload")
            .body("hi")
            .POST()
            .asDiscarding();

    pair.shutdown();
    assertEquals(204, response.statusCode());
  }

  @Test
  void testMultipartUpload() throws IOException {

    var jex =
        Jex.create()
            .post(
                "/upload",
                ctx -> {
                  var part = ctx.attribute(FileUploadService.class).uploadedFile("file");

                  assert part.file().exists();
                })
            .plugin(FileUploadPlugin.create());
    var pair = TestPair.create(jex);

    // Create a dummy file for the upload
    Path tempFile = Path.of("test_file.txt");
    Files.writeString(tempFile, "This is a test file content.");

    // Construct the multipart body
    String requestBody =
        "--"
            + BOUNDARY
            + "\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\""
            + tempFile.getFileName()
            + "\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + Files.readString(tempFile)
            + "\r\n"
            + "--"
            + BOUNDARY
            + "--\r\n";

    Files.delete(tempFile);

    var response =
        pair.request()
            .requestTimeout(Duration.ofDays(1))
            .path("upload")
            .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
            .body(requestBody)
            .POST()
            .asDiscarding();

    pair.shutdown();
    assertEquals(204, response.statusCode());
  }

  @Test
  void testTooBig() throws IOException {

    var jex =
        Jex.create()
            .post(
                "/upload",
                ctx -> {
                  var part = ctx.attribute(FileUploadService.class).uploadedFile("file");

                  assert part.file().exists();
                })
            .plugin(FileUploadPlugin.create(c -> c.maxFileSize(1, SizeUnit.BYTES)));
    var pair = TestPair.create(jex);

    // Create a dummy file for the upload
    Path tempFile = Path.of("test_file.txt");
    Files.writeString(tempFile, "This is a test file content.");

    // Construct the multipart body
    String requestBody =
        "--"
            + BOUNDARY
            + "\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\""
            + tempFile.getFileName()
            + "\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + Files.readString(tempFile)
            + "\r\n"
            + "--"
            + BOUNDARY
            + "--\r\n";

    Files.delete(tempFile);

    var response =
        pair.request()
            .requestTimeout(Duration.ofDays(1))
            .path("upload")
            .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
            .body(requestBody)
            .POST()
            .asString();

    pair.shutdown();
    assertEquals(413, response.statusCode());
    assertEquals("Uploaded file exceeds max size of 1 bytes", response.body());
  }

  @Test
  void testRequestTooBig() throws IOException {

    var jex =
        Jex.create()
            .post(
                "/upload",
                ctx -> {
                  var part = ctx.attribute(FileUploadService.class).uploadedFile("file");

                  assert part.file().exists();
                })
            .plugin(FileUploadPlugin.create(c -> c.maxRequestSize(1, SizeUnit.BYTES)));
    var pair = TestPair.create(jex);

    // Create a dummy file for the upload
    Path tempFile = Path.of("test_file.txt");
    Files.writeString(tempFile, "This is a test file content.");

    // Construct the multipart body
    String requestBody =
        "--"
            + BOUNDARY
            + "\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\""
            + tempFile.getFileName()
            + "\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + Files.readString(tempFile)
            + "\r\n"
            + "--"
            + BOUNDARY
            + "--\r\n";

    Files.delete(tempFile);

    var response =
        pair.request()
            .requestTimeout(Duration.ofDays(1))
            .path("upload")
            .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
            .body(requestBody)
            .POST()
            .asString();

    pair.shutdown();
    assertEquals(413, response.statusCode());
    assertEquals("Request exceeds max size of 1 bytes", response.body());
  }

  @Test
  void testMultipleFiles() throws IOException {
    var jex =
        Jex.create()
            .post(
                "/upload/multiple",
                ctx -> {
                  var service = ctx.attribute(FileUploadService.class);
                  var file1 = service.uploadedFile("file1");
                  var file2 = service.uploadedFile("file2");

                  // Assert both files exist on disk
                  assert file1.file().exists();
                  assert file2.file().exists();

                  // Optional: verify content or other properties
                  assertEquals("content of file 1", Files.readString(file1.file().toPath()));
                  assertEquals("content of file 2", Files.readString(file2.file().toPath()));
                })
            .plugin(FileUploadPlugin.create());

    var pair = TestPair.create(jex);

    Path tempFile1 = Path.of("test_file1.txt");
    Path tempFile2 = Path.of("test_file2.txt");
    Files.writeString(tempFile1, "content of file 1");
    Files.writeString(tempFile2, "content of file 2");

    String requestBody =
        "--"
            + BOUNDARY
            + "\r\n"
            + "Content-Disposition: form-data; name=\"file1\"; filename=\""
            + tempFile1.getFileName()
            + "\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + Files.readString(tempFile1)
            + "\r\n"
            + "--"
            + BOUNDARY
            + "\r\n"
            + "Content-Disposition: form-data; name=\"file2\"; filename=\""
            + tempFile2.getFileName()
            + "\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + Files.readString(tempFile2)
            + "\r\n"
            + "--"
            + BOUNDARY
            + "--\r\n";

    Files.delete(tempFile1);
    Files.delete(tempFile2);

    var response =
        pair.request()
            .requestTimeout(Duration.ofDays(1))
            .path("upload/multiple")
            .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
            .body(requestBody)
            .POST()
            .asDiscarding();

    pair.shutdown();
    assertEquals(204, response.statusCode());
  }
}
