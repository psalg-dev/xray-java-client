package com.xrayclient.exception;

public class XrayApiException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public XrayApiException(int statusCode, String responseBody) {
        super("Xray API error: HTTP " + statusCode + " - " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public XrayApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }

    public int getStatusCode() { return statusCode; }
    public String getResponseBody() { return responseBody; }
}
