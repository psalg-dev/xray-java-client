package dev.psalg.xray.api;

import dev.psalg.xray.internal.XrayHttpClient;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void build_appliesBaseUrlAndBasicAuth() {
        XrayClient client = XrayClient.builder()
                .baseUrl("http://example.test/")
                .basicAuth("user", "pass")
                .build();

        XrayHttpClient http = httpClient(client);
        assertThat(field(http, "baseUrl")).isEqualTo("http://example.test");
        assertThat(field(http, "authHeader")).isEqualTo("Basic " + Base64.getEncoder()
                .encodeToString("user:pass".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void build_appliesTokenAuth() {
        XrayClient client = XrayClient.builder()
                .baseUrl("http://example.test")
                .tokenAuth("token")
                .build();

        assertThat(field(httpClient(client), "authHeader")).isEqualTo("Bearer token");
    }

    @Test
    void build_appliesApiKeyAuth() {
        XrayClient client = XrayClient.builder()
                .baseUrl("http://example.test")
                .apiKey("api-key")
                .build();

        assertThat(field(httpClient(client), "authHeader")).isEqualTo("api-key");
    }

    @Test
    void build_appliesTimeouts() {
        XrayClient client = XrayClient.builder()
                .baseUrl("http://example.test")
                .basicAuth("user", "pass")
                .connectTimeout(7)
                .readTimeout(12)
                .build();

        XrayHttpClient http = httpClient(client);
        assertThat(field(http, "readTimeout")).isEqualTo(Duration.ofSeconds(12));
        assertThat(((HttpClient) field(http, "http")).connectTimeout()).contains(Duration.ofSeconds(7));
    }

    private static XrayHttpClient httpClient(XrayClient client) {
        return (XrayHttpClient) field(client, "httpClient");
    }

    private static Object field(Object target, String name) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
