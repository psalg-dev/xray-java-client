package dev.psalg.xray.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XrayClientBuilderTest {

    @Test
    void build_withoutBaseUrl_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> XrayClient.builder()
                .basicAuth("admin", "password")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void build_withoutAuth_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> XrayClient.builder()
                .baseUrl("http://localhost:8080")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("auth");
    }
}
