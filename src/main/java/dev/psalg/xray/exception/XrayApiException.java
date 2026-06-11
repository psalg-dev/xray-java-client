package dev.psalg.xray.exception;

import lombok.Getter;

/**
 * Thrown when an Xray API call fails: either a non-2xx HTTP response
 * ({@code statusCode}/{@code responseBody} populated) or a transport-level
 * failure ({@code statusCode == -1}, cause populated).
 */
@Getter
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
}
