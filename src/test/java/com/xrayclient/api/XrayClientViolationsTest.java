package com.xrayclient.api;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.xrayclient.exception.XrayApiException;
import com.xrayclient.model.violations.Severity;
import com.xrayclient.model.violations.Violation;
import com.xrayclient.model.violations.ViolationsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XrayClientViolationsTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private XrayClient client;

    @BeforeEach
    void setUp() {
        client = XrayClient.builder()
                .baseUrl("http://localhost:" + wm.getPort())
                .basicAuth("admin", "password")
                .build();
    }

    @Test
    void fetchPage_returnsViolations() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .withQueryParam("num_of_rows", equalTo("25"))
                .withQueryParam("page_num", equalTo("1"))
                .withQueryParam("direction", equalTo("asc"))
                .withQueryParam("order_by", equalTo("updated"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(singleViolationPage(1, "Critical"))));

        ViolationsResponse response = client.violations()
                .withSeverities(Severity.CRITICAL, Severity.HIGH)
                .fetchPage(1, 25);

        assertThat(response.violations()).hasSize(1);
        assertThat(response.totalViolations()).isEqualTo(1);
        assertThat(response.violations().getFirst().severity()).isEqualTo("Critical");
    }

    @Test
    void fetchPage_withProjectKey_includesProjectKeyQueryParam() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .withQueryParam("projectKey", equalTo("my-project"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(singleViolationPage(1, "High"))));

        ViolationsResponse response = client.violations()
                .forProject("my-project")
                .withSeverities(Severity.CRITICAL, Severity.HIGH)
                .fetchPage(1, 25);

        assertThat(response.violations()).hasSize(1);
        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/violations"))
                .withQueryParam("projectKey", equalTo("my-project")));
    }

    @Test
    void fetchAll_autopaginates_untilNoMore() {
        // total=3, pageSize=2 -> page1 returns 2 items, page2 returns 1 item
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .withQueryParam("page_num", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(violationPage(3, "Critical", "High"))));

        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .withQueryParam("page_num", equalTo("2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(singleViolationPage(3, "High"))));

        List<Violation> all = client.violations()
                .withSeverities(Severity.CRITICAL, Severity.HIGH)
                .fetchAll(2);

        assertThat(all).hasSize(3);
        wm.verify(2, postRequestedFor(urlPathEqualTo("/xray/api/v1/violations")));
    }

    @Test
    void fetchAll_singlePage_doesNotRequestNextPage() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(singleViolationPage(1, "Critical"))));

        List<Violation> all = client.violations().fetchAll();

        assertThat(all).hasSize(1);
        wm.verify(1, postRequestedFor(urlPathEqualTo("/xray/api/v1/violations")));
    }

    @Test
    void fetchPage_sendsBasicAuthHeader() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .withHeader("Authorization", matching("Basic .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyPage())));

        client.violations().fetchPage(1, 25);

        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/violations"))
                .withHeader("Authorization", matching("Basic .*")));
    }

    @Test
    void fetchPage_on401_throwsXrayApiException() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .willReturn(aResponse().withStatus(401).withBody("Unauthorized")));

        assertThatThrownBy(() -> client.violations().fetchPage(1, 25))
                .isInstanceOf(XrayApiException.class)
                .satisfies(ex -> assertThat(((XrayApiException) ex).getStatusCode()).isEqualTo(401));
    }

    @Test
    void fetchPage_on500_throwsXrayApiException() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        assertThatThrownBy(() -> client.violations().fetchPage(1, 25))
                .isInstanceOf(XrayApiException.class)
                .satisfies(ex -> assertThat(((XrayApiException) ex).getStatusCode()).isEqualTo(500));
    }

    @Test
    void violations_filterBySeverityValues_serializedCorrectly() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .withRequestBody(matchingJsonPath("$.filters.severities[0]", equalTo("Critical")))
                .withRequestBody(matchingJsonPath("$.filters.severities[1]", equalTo("High")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyPage())));

        client.violations()
                .withSeverities(Severity.CRITICAL, Severity.HIGH)
                .fetchPage(1, 25);

        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/violations"))
                .withRequestBody(matchingJsonPath("$.filters.severities[0]", equalTo("Critical")))
                .withRequestBody(matchingJsonPath("$.filters.severities[1]", equalTo("High"))));
    }

    @Test
    void violations_activeOnlyFilter_setsByDefault() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .withRequestBody(matchingJsonPath("$.filters.violation_status", equalTo("Active")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyPage())));

        client.violations().fetchPage(1, 25);

        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/violations"))
                .withRequestBody(matchingJsonPath("$.filters.violation_status", equalTo("Active"))));
    }

    @Test
    void tokenAuth_sendsBearerHeader() {
        XrayClient tokenClient = XrayClient.builder()
                .baseUrl("http://localhost:" + wm.getPort())
                .tokenAuth("my-token")
                .build();

        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/violations"))
                .withHeader("Authorization", equalTo("Bearer my-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyPage())));

        tokenClient.violations().fetchPage(1, 25);

        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/violations"))
                .withHeader("Authorization", equalTo("Bearer my-token")));
    }

    // ---- helpers ----

    private String singleViolationPage(int total, String severity) {
        return """
                {
                  "violations": [
                    {
                      "watch_name": "security-watch",
                      "policy_name": "security-policy",
                      "rule_name": "block-critical",
                      "type": "security",
                      "severity": "%s",
                      "description": "CVE-2021-44228"
                    }
                  ],
                  "total_violations": %d
                }
                """.formatted(severity, total);
    }

    private String violationPage(int total, String... severities) {
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < severities.length; i++) {
            if (i > 0) items.append(",");
            items.append("""
                    {"watch_name":"w","policy_name":"p","rule_name":"r","type":"security","severity":"%s"}
                    """.formatted(severities[i]));
        }
        return """
                {"violations":[%s],"total_violations":%d}
                """.formatted(items, total);
    }

    private String emptyPage() {
        return """
                {"violations":[],"total_violations":0}
                """;
    }
}
