package io.avaje.jex.spi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 *
 * <p>Service responsible for handling JSON-based request and response bodies.
 *
 * @see {@link JexExtension} for SPI registration details.
 */
public non-sealed interface JsonService extends JexExtension {

  /**
   * **Reads JSON from an InputStream**
   *
   * <p>Reads a JSON-formatted input stream and deserializes it into a Java object of the specified
   * type.
   *
   * @param type the Class object of the desired type
   * @param is the input stream containing the JSON data
   * @return the deserialized object
   */
  <T> T jsonRead(Class<T> type, InputStream is);

  /**
   * **Writes a Java Object as JSON to an OutputStream**
   *
   * <p>Serializes a Java object into JSON format and writes the resulting JSON to the specified
   * output stream.
   *
   * @param bean the Java object to be serialized
   * @param os the output stream to write the JSON data to
   */
  void jsonWrite(Object bean, OutputStream os);

  /**
   * Serializes a stream of Java objects into a JSON-Stream format, using the {@code x-json-stream}
   * media type. Each object in the stream is serialized as a separate JSON object, and the objects
   * are separated by newlines.
   *
   * @param iterator the stream of objects to be serialized
   * @param os the output stream to write the JSON-Stream data to
   */
  <E> void jsonWriteStream(Iterator<E> iterator, OutputStream os);
}
