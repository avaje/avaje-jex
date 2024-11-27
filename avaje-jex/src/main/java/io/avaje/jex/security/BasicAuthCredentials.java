package io.avaje.jex.security;

/**
 * Http Basic Auth credentials
 *
 * @param userName the username
 * @param password the password
 */
public record BasicAuthCredentials(String userName, String password) {}
