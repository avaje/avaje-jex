package io.avaje.jex.http;

/** Http Status codes */
public enum HttpStatus {

  // 1xx Informational
  CONTINUE_100(100, "Continue"),
  SWITCHING_PROTOCOLS_101(101, "Switching Protocols"),

  // 2xx Success
  OK_200(200, "OK"),
  CREATED_201(201, "Created"),
  ACCEPTED_202(202, "Accepted"),
  NON_AUTHORITATIVE_INFORMATION_203(203, "Non-Authoritative Information"),
  NO_CONTENT_204(204, "No Content"),
  RESET_CONTENT_205(205, "Reset Content"),
  PARTIAL_CONTENT_206(206, "Partial Content"),
  MULTI_STATUS_207(207, "Multi-Status"),

  // 3xx Redirection
  MOVED_PERMANENTLY_301(301, "Moved Permanently"),
  FOUND_302(302, "Found"),
  SEE_OTHER_303(303, "See Other"),
  NOT_MODIFIED_304(304, "Not Modified"),
  USE_PROXY_305(305, "Use Proxy"),
  TEMPORARY_REDIRECT_307(307, "Temporary Redirect"),
  PERMANENT_REDIRECT_308(308, "Permanent Redirect"),

  // 4xx Client Error
  BAD_REQUEST_400(400, "Bad Request"),
  UNAUTHORIZED_401(401, "Unauthorized"),
  PAYMENT_REQUIRED_402(402, "Payment Required"),
  FORBIDDEN_403(403, "Forbidden"),
  NOT_FOUND_404(404, "Not Found"),
  METHOD_NOT_ALLOWED_405(405, "Method Not Allowed"),
  NOT_ACCEPTABLE_406(406, "Not Acceptable"),
  PROXY_AUTHENTICATION_REQUIRED_407(407, "Proxy Authentication Required"),
  REQUEST_TIMEOUT_408(408, "Request Timeout"),
  CONFLICT_409(409, "Conflict"),
  GONE_410(410, "Gone"),
  LENGTH_REQUIRED_411(411, "Length Required"),
  PRECONDITION_FAILED_412(412, "Precondition Failed"),
  REQUEST_ENTITY_TOO_LARGE_413(413, "Request Entity Too Large"),
  REQUEST_URI_TOO_LONG_414(414, "Request-URI Too Long"),
  UNSUPPORTED_MEDIA_TYPE_415(415, "Unsupported Media Type"),
  REQUESTED_RANGE_NOT_SATISFIABLE_416(416, "Requested Range Not Satisfiable"),
  EXPECTATION_FAILED_417(417, "Expectation Failed"),
  I_AM_A_TEAPOT_418(418, "I'm A Teapot"),
  MISDIRECTED_REQUEST_421(421, "Misdirected Request"),
  UNPROCESSABLE_CONTENT_422(422, "Unprocessable Content"),
  LOCKED_423(423, "Locked"),
  FAILED_DEPENDENCY_424(424, "Failed Dependency"),
  UPGRADE_REQUIRED_426(426, "Upgrade Required"),
  PRECONDITION_REQUIRED_428(428, "Precondition Required"),
  TOO_MANY_REQUESTS_429(429, "Too Many Requests"),

  // 5xx Server Error
  INTERNAL_SERVER_ERROR_500(500, "Internal Server Error"),
  NOT_IMPLEMENTED_501(501, "Not Implemented"),
  BAD_GATEWAY_502(502, "Bad Gateway"),
  SERVICE_UNAVAILABLE_503(503, "Service Unavailable"),
  GATEWAY_TIMEOUT_504(504, "Gateway Timeout"),
  HTTP_VERSION_NOT_SUPPORTED_505(505, "HTTP Version Not Supported"),
  INSUFFICIENT_STORAGE_507(507, "Insufficient Storage"),
  LOOP_DETECTED_508(508, "Loop Detected"),
  NOT_EXTENDED_510(510, "Not Extended"),
  NETWORK_AUTHENTICATION_REQUIRED_511(511, "Network Authentication Required");

  private final int status;
  private final String message;

  HttpStatus(int status, String message) {
    this.status = status;
    this.message = message;
  }

  public int status() {
    return status;
  }

  public String message() {
    return message;
  }

  @Override
  public String toString() {
    return status + " " + message;
  }
}
