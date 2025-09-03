package io.avaje.jex.websocket;

import io.avaje.jex.websocket.exception.CloseCode;

/**
 * Represents a WebSocket connection, providing methods to interact with the connection.
 * Allows sending and receiving messages, pinging, and closing the connection.
 */
public interface WebSocket {

  /**
   * Checks if the WebSocket connection is open.
   *
   * @return true if the connection is open, false otherwise
   */
  boolean isOpen();

  /**
   * Closes the WebSocket connection with the specified close code and reason.
   *
   * @param code the close code indicating the reason for closure
   * @param reason the reason for closing the connection
   * @param initiatedByRemote true if the close was initiated by the remote endpoint, false otherwise
   */
  void close(CloseCode code, String reason, boolean initiatedByRemote);

  /**
   * Sends a ping frame with the specified payload to the remote endpoint.
   *
   * @param payload the ping payload as a byte array
   */
  void ping(byte[] payload);

  /**
   * Sends a binary message to the remote endpoint.
   *
   * @param payload the binary payload as a byte array
   */
  void send(byte[] payload);

  /**
   * Sends a text message to the remote endpoint.
   *
   * @param payload the text payload as a string
   */
  void send(String payload);
}