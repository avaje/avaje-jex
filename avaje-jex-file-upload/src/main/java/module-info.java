/**
 * Provides a plugin for handling multipart file uploads in the Jex framework.
 * <h3>Usage</h3>
 * <p>
 * First, create and register the plugin. You can use the default configuration or
 * customize it with a {@link io.avaje.jex.file.upload.MultipartConfig}.
 * </p>
 * <pre>{@code
 * // Example: Registering the plugin with a custom configuration
 * var jex = Jex.create();
 * var uploadPlugin = FileUploadPlugin.create(config -> {
 * // Set the maximum file size for a single upload to 50 MB
 * config.maxFileSize(50, SizeUnit.MB);
 * // Set the cache directory for large files
 * config.cacheDirectory("/var/tmp/jex-uploads");
 * });
 * jex.register(uploadPlugin);
 * }</pre>
 *
 * <p>
 * Within your route handlers, you can then retrieve the {@link io.avaje.jex.file.upload.FileUploadService}
 * from the context attributes and use it to access the uploaded files.
 * </p>
 * <pre>{@code
 * // Example: Handling a file upload in a POST route
 * jex.post("/upload", ctx -> {
 * // Get the FileUploadService from the request context
 * FileUploadService service = ctx.attribute(FileUploadService.class);
 * // Retrieve the uploaded file using its form field name
 * MultiPart uploadedFile = service.uploadedFile("image-file");
 *
 * if (uploadedFile != null) {
 * System.out.println("File name: " + uploadedFile.filename());
 * System.out.println("File: " + uploadedFile.file());
 *
 * }
 * ctx.status(200).result("File upload successful!");
 * });
 * }</pre>
 *
 * @see io.avaje.jex.file.upload.FileUploadPlugin
 * @see io.avaje.jex.file.upload.MultipartConfig
 * @see io.avaje.jex.file.upload.FileUploadService
 */
module io.avaje.jex.file.upload {

  exports io.avaje.jex.file.upload;

  requires io.avaje.jex;
}
