package dev.psalg.xray.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XrayHttpClientBuilderTest {

    @Test
    void basicAuth_setsAuthHeader() {
        XrayHttpClient client = XrayHttpClient.builder()
                .baseUrl("http://example.test")
                .basicAuth("user", "pass")
                .build();

        assertThat(field(client, "authHeader")).isEqualTo("Basic " + Base64.getEncoder()
                .encodeToString("user:pass".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void tokenAuth_setsAuthHeader() {
        XrayHttpClient client = XrayHttpClient.builder()
                .baseUrl("http://example.test")
                .tokenAuth("token")
                .build();

        assertThat(field(client, "authHeader")).isEqualTo("Bearer token");
    }

    @Test
    void apiKeyAuth_setsAuthHeader() {
        XrayHttpClient client = XrayHttpClient.builder()
                .baseUrl("http://example.test")
                .apiKeyAuth("api-key")
                .build();

        assertThat(field(client, "authHeader")).isEqualTo("api-key");
    }

    @Test
    void baseUrl_trimsTrailingSlash() {
        XrayHttpClient client = XrayHttpClient.builder()
                .baseUrl("http://example.test/")
                .basicAuth("user", "pass")
                .build();

        assertThat(field(client, "baseUrl")).isEqualTo("http://example.test");
    }

    @Test
    void objectMapper_setsMapper() {
        ObjectMapper mapper = new ObjectMapper();
        XrayHttpClient client = XrayHttpClient.builder()
                .baseUrl("http://example.test")
                .basicAuth("user", "pass")
                .objectMapper(mapper)
                .build();

        assertThat(field(client, "mapper")).isSameAs(mapper);
    }

    @Test
    void timeouts_areApplied() {
        XrayHttpClient client = XrayHttpClient.builder()
                .baseUrl("http://example.test")
                .basicAuth("user", "pass")
                .connectTimeout(7)
                .readTimeout(12)
                .build();

        assertThat(field(client, "readTimeout")).isEqualTo(Duration.ofSeconds(12));
        assertThat(((HttpClient) field(client, "http")).connectTimeout()).contains(Duration.ofSeconds(7));
    }

    @Test
    void build_withoutBaseUrl_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> XrayHttpClient.builder()
                .basicAuth("user", "pass")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void build_withoutAuth_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> XrayHttpClient.builder()
                .baseUrl("http://example.test")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("auth");
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
