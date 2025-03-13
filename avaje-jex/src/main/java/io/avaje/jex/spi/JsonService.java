package io.avaje.jex.spi;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Service responsible for handling JSON-based request and response bodies.
 *
 * @see JexExtension SPI registration details.
 */
public non-sealed interface JsonService extends JexExtension {

  /**
   * **Writes a Java Object as JSON to an OutputStream**
   *
   * <p>Serializes a Java object into JSON format and writes the resulting JSON to the specified
   * output stream.
   *
   * @param bean the Java object to be serialized
   * @param os the output stream to write the JSON data to
   */
  void toJson(Object bean, OutputStream os);

  /**
   * **Writes a Java Object as a JSON string**
   *
   * <p>Serializes a Java object into JSON string format and writes the resulting JSON to the
   * specified output stream.
   *
   * @param bean the Java object to be serialized
   * @return the serialized JSON string
   */
  String toJsonString(Object bean);

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
  default <T> T fromJson(Class<T> type, InputStream is) {
    return fromJson((Type) type, is);
  }

  /**
   * **Reads JSON from an InputStream**
   *
   * <p>Reads a JSON-formatted input stream and deserializes it into a Java object of the specified
   * type.
   *
   * @param type the Type object of the desired type
   * @param is the input stream containing the JSON data
   * @return the deserialized object
   */
  <T> T fromJson(Type type, InputStream is);

  /**
   * Serializes a stream of Java objects into a JSON-Stream format, using the {@code x-json-stream}
   * media type. Each object in the stream is serialized as a separate JSON object, and the objects
   * are separated by newlines.
   *
   * @param iterator the stream of objects to be serialized
   * @param os the output stream to write the JSON-Stream data to
   */
  default <E> void toJsonStream(Iterator<E> iterator, OutputStream os) {
    throw new UnsupportedOperationException("toJsonStream is unimplemented in this JsonService");
  }
}
