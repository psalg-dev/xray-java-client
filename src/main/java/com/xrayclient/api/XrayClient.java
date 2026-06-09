package com.xrayclient.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xrayclient.internal.ViolationsApi;
import com.xrayclient.internal.WatchesApi;
import com.xrayclient.internal.XrayHttpClient;

/**
 * Entry point for the Xray Java client.
 *
 * <pre>{@code
 * XrayClient client = XrayClient.builder()
 *     .baseUrl("https://my-artifactory.example.com")
 *     .basicAuth("user", "password")
 *     .build();
 *
 * List<Violation> violations = client.violations()
 *     .forProject("my-project")
 *     .withSeverities(Severity.CRITICAL, Severity.HIGH)
 *     .activeOnly()
 *     .fetchAll();
 * }</pre>
 */
public final class XrayClient {

    private final XrayHttpClient httpClient;

    private XrayClient(Builder builder) {
        this.httpClient = builder.httpClientBuilder.build();
    }

    /** Returns a builder for querying violations. */
    public ViolationsQueryBuilder violations() {
        return new ViolationsQueryBuilder(new ViolationsApi(httpClient));
    }

    /** Returns a builder for querying watches. */
    public WatchesQueryBuilder watches() {
        return new WatchesQueryBuilder(new WatchesApi(httpClient));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final XrayHttpClient.Builder httpClientBuilder = XrayHttpClient.builder();

        private Builder() {}

        public Builder baseUrl(String baseUrl) {
            httpClientBuilder.baseUrl(baseUrl);
            return this;
        }

        public Builder basicAuth(String username, String password) {
            httpClientBuilder.basicAuth(username, password);
            return this;
        }

        public Builder tokenAuth(String accessToken) {
            httpClientBuilder.tokenAuth(accessToken);
            return this;
        }

        /** Use an Artifactory API key as auth header value. */
        public Builder apiKey(String apiKey) {
            httpClientBuilder.apiKeyAuth(apiKey);
            return this;
        }

        public Builder connectTimeout(int seconds) {
            httpClientBuilder.connectTimeout(seconds);
            return this;
        }

        public Builder readTimeout(int seconds) {
            httpClientBuilder.readTimeout(seconds);
            return this;
        }

        public XrayClient build() {
            return new XrayClient(this);
        }
    }
}
