package io.avaje.jex.file.upload;

import java.util.function.Consumer;

import io.avaje.jex.Jex;
import io.avaje.jex.spi.JexPlugin;

/**
 * A plugin for handling file uploads within the Jex framework.
 *
 * <p>This plugin sets up a {@link FileUploadService} accessible via the request context, which
 * simplifies the process of handling multipart form data.
 *
 * @see MultipartConfig
 * @see FileUploadService
 */
public class FileUploadPlugin implements JexPlugin {

  private final MultipartConfig multipartConfig;

  private FileUploadPlugin(MultipartConfig multipartConfig) {
    this.multipartConfig = multipartConfig;
  }

  /**
   * Creates and configures a new FileUploadPlugin using a consumer.
   *
   * @param consumer A consumer to configure the {@link MultipartConfig}.
   * @return A new FileUploadPlugin instance.
   */
  public static FileUploadPlugin create(Consumer<MultipartConfig> consumer) {
    var config = new MultipartConfig();
    consumer.accept(config);
    return new FileUploadPlugin(config);
  }

  /**
   * Creates a new FileUploadPlugin with default settings.
   *
   * @return A new FileUploadPlugin instance with default configuration.
   */
  public static FileUploadPlugin create() {
    return new FileUploadPlugin(new MultipartConfig());
  }

  /**
   * Applies the plugin to the Jex instance.
   *
   * <p>This method registers a 'before' handler that creates and adds a {@link FileUploadService}
   * instance to the request attributes for each incoming request.
   *
   * @param jex The Jex instance to which the plugin is being applied.
   */
  @Override
  public void apply(Jex jex) {

    jex.before(
        ctx ->
            ctx.attribute(FileUploadService.class, new DFileUploadService(multipartConfig, ctx)));
  }
}
