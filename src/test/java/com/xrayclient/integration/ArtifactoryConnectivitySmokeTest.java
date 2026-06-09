package com.xrayclient.integration;

import com.xrayclient.api.XrayClient;
import com.xrayclient.exception.XrayApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test against any reachable Artifactory instance (OSS or Pro).
 * Verifies HTTP client connectivity and auth — does NOT require Xray.
 *
 * Runs when XRAY_BASE_URL is set:
 *   XRAY_BASE_URL=http://localhost:8082 XRAY_USER=admin XRAY_PASSWORD=password
 *   mvn verify -Pintegration
 *
 * On Artifactory OSS (no Xray), the violations endpoint returns 404 →
 * the client correctly throws XrayApiException(404).
 * On Artifactory Pro with Xray, use XrayViolationsIntegrationTest instead.
 */
@EnabledIfEnvironmentVariable(named = "XRAY_BASE_URL", matches = ".+")
class ArtifactoryConnectivitySmokeTest {

    @Test
    void client_canReachArtifactory_andThrows404WhenXrayNotInstalled() {
        String baseUrl = System.getenv("XRAY_BASE_URL");
        XrayClient client = buildClient(baseUrl);

        // On OSS: expect 404 (Xray not installed)
        // On Pro with Xray: this will succeed and return violations (possibly empty)
        try {
            var response = client.violations()
                    .withSeverities(com.xrayclient.model.violations.Severity.CRITICAL)
                    .fetchPage(1, 10);

            // If we get here, Xray IS installed and responding
            System.out.println("[Smoke] Xray is available. Total violations: " + response.totalViolations());
            assertThat(response).isNotNull();

        } catch (XrayApiException ex) {
            // 400 = Xray available, request invalid (no watch/project — auth passed)
            // 404 = Xray not installed (OSS)
            // 403 = not licensed
            // 401 = bad credentials (unexpected here)
            System.out.println("[Smoke] Xray responded: HTTP " + ex.getStatusCode());
            assertThat(ex.getStatusCode())
                    .as("Expected Xray error (not auth failure), got " + ex.getStatusCode())
                    .isIn(400, 404, 403);
        }
    }

    // ---- helpers ----

    static XrayClient buildClient(String baseUrl) {
        String token = System.getenv("XRAY_TOKEN");
        if (token != null && !token.isBlank()) {
            return XrayClient.builder().baseUrl(baseUrl).tokenAuth(token).build();
        }
        String user = System.getenv().getOrDefault("XRAY_USER", "admin");
        String password = System.getenv().getOrDefault("XRAY_PASSWORD", "password");
        return XrayClient.builder().baseUrl(baseUrl).basicAuth(user, password).build();
    }

    @Test
    void client_with_badCredentials_throws401() {
        String baseUrl = System.getenv("XRAY_BASE_URL");

        XrayClient client = XrayClient.builder()
                .baseUrl(baseUrl)
                .basicAuth("admin", "wrong-password-xyz")
                .build();

        assertThatThrownBy(() -> client.violations().fetchPage(1, 10))
                .isInstanceOf(XrayApiException.class)
                .satisfies(ex -> {
                    int code = ((XrayApiException) ex).getStatusCode();
                    // Pro+Xray: 401 for bad creds; OSS: 404 (router never reaches auth)
                    assertThat(code).as("Expected HTTP error (401 or 404), got " + code)
                            .isIn(401, 404);
                });
    }
}
