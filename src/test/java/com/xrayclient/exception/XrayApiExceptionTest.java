package com.xrayclient.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XrayApiExceptionTest {

    @Test
    void statusCodeConstructor_setsStatusCodeAndResponseBody() {
        XrayApiException ex = new XrayApiException(404, "Not Found");

        assertThat(ex.getStatusCode()).isEqualTo(404);
        assertThat(ex.getResponseBody()).isEqualTo("Not Found");
        assertThat(ex.getMessage()).isEqualTo("Xray API error: HTTP 404 - Not Found");
    }

    @Test
    void causeConstructor_setsMessageAndCauseWithoutStatusCode() {
        RuntimeException cause = new RuntimeException("boom");
        XrayApiException ex = new XrayApiException("HTTP request failed", cause);

        assertThat(ex.getStatusCode()).isEqualTo(-1);
        assertThat(ex.getResponseBody()).isNull();
        assertThat(ex.getMessage()).isEqualTo("HTTP request failed");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
