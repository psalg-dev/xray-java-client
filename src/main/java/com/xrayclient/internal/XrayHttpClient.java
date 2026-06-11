package com.xrayclient.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xrayclient.exception.XrayApiException;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

@Slf4j
public final class XrayHttpClient {

    private final HttpClient http;
    private final String baseUrl;
    private final String authHeader;
    private final ObjectMapper mapper;
    private final Duration readTimeout;

    private XrayHttpClient(Builder builder) {
        this.baseUrl = builder.baseUrl.replaceAll("/+$", "");
        this.authHeader = builder.authHeader;
        this.mapper = builder.mapper;
        this.readTimeout = Duration.ofSeconds(builder.readTimeoutSeconds);
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(builder.connectTimeoutSeconds))
                .build();
    }

    public <T> T get(String path, Class<T> responseType) {
        String body = executeGet(path);
        try {
            return mapper.readValue(body, responseType);
        } catch (Exception e) {
            throw new XrayApiException("Failed to parse response", e);
        }
    }

    public <T> T get(String path, TypeReference<T> responseType) {
        String body = executeGet(path);
        try {
            return mapper.readValue(body, responseType);
        } catch (Exception e) {
            throw new XrayApiException("Failed to parse response", e);
        }
    }

    private String executeGet(String path) {
        try {
            log.debug("GET {}", baseUrl + path);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Authorization", authHeader)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(readTimeout)
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new XrayApiException(response.statusCode(), response.body());
            }
            return response.body();
        } catch (XrayApiException e) {
            throw e;
        } catch (Exception e) {
            throw new XrayApiException("HTTP request failed", e);
        }
    }

    public <T> T post(String path, Object requestBody, Class<T> responseType) {
        try {
            String bodyJson = requestBody != null ? mapper.writeValueAsString(requestBody) : "{}";
            log.debug("POST {} body={}", baseUrl + path, bodyJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                    .timeout(readTimeout)
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new XrayApiException(response.statusCode(), responseBody);
            }
            return mapper.readValue(responseBody, responseType);
        } catch (XrayApiException e) {
            throw e;
        } catch (Exception e) {
            throw new XrayApiException("HTTP request failed", e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String baseUrl;
        private String authHeader;
        private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        private int connectTimeoutSeconds = 10;
        private int readTimeoutSeconds = 30;

        private Builder() {}

        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }

        public Builder basicAuth(String username, String password) {
            this.authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes());
            return this;
        }

        public Builder tokenAuth(String token) {
            this.authHeader = "Bearer " + token;
            return this;
        }

        public Builder apiKeyAuth(String apiKey) {
            this.authHeader = apiKey;
            return this;
        }

        public Builder objectMapper(ObjectMapper mapper) { this.mapper = mapper; return this; }
        public Builder connectTimeout(int seconds) { this.connectTimeoutSeconds = seconds; return this; }
        public Builder readTimeout(int seconds) { this.readTimeoutSeconds = seconds; return this; }

        public XrayHttpClient build() {
            if (baseUrl == null || baseUrl.isBlank()) throw new IllegalArgumentException("baseUrl required");
            if (authHeader == null || authHeader.isBlank()) throw new IllegalArgumentException("auth required");
            return new XrayHttpClient(this);
        }
    }
}
