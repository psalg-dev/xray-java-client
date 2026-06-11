package com.xrayclient.api;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.xrayclient.exception.XrayApiException;
import com.xrayclient.model.builds.BuildSummary;
import com.xrayclient.model.builds.IndexedBuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XrayClientBuildsTest {

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
    void summary_on404_throwsXrayApiException() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/build"))
                .willReturn(aResponse().withStatus(404).withBody("Not Found")));

        assertThatThrownBy(() -> client.builds().summary("missing-build", "1"))
                .isInstanceOf(XrayApiException.class)
                .satisfies(ex -> assertThat(((XrayApiException) ex).getStatusCode()).isEqualTo(404));
    }

    @Test
    void summary_callsPostBuildSummaryEndpoint() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/build"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(buildSummaryResponse())));

        client.builds().summary("foo-batch-service", "42");

        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/summary/build")));
    }

    @Test
    void summary_sendsCorrectBuildNameAndNumber() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/build"))
                .withRequestBody(matchingJsonPath("$.name", equalTo("foo-batch-service")))
                .withRequestBody(matchingJsonPath("$.number", equalTo("42")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(buildSummaryResponse())));

        client.builds().summary("foo-batch-service", "42");

        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/summary/build"))
                .withRequestBody(matchingJsonPath("$.name", equalTo("foo-batch-service")))
                .withRequestBody(matchingJsonPath("$.number", equalTo("42"))));
    }

    @Test
    void summary_deserializesIssues() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/build"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(buildSummaryResponse())));

        BuildSummary summary = client.builds().summary("foo-batch-service", "42");

        assertThat(summary.issues()).hasSize(1);
        assertThat(summary.issues().getFirst().severity()).isEqualTo("Critical");
        assertThat(summary.issues().getFirst().issueType()).isEqualTo("security");
        assertThat(summary.issues().getFirst().summary()).isEqualTo("Remote code execution via Log4Shell");
    }

    @Test
    void summary_deserializesCvesWithScores() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/build"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(buildSummaryResponse())));

        BuildSummary summary = client.builds().summary("foo-batch-service", "42");

        var cves = summary.issues().getFirst().cves();
        assertThat(cves).hasSize(1);
        assertThat(cves.getFirst().cveId()).isEqualTo("CVE-2021-44228");
        assertThat(cves.getFirst().cvssV3Score()).isEqualTo("10.0");
    }

    @Test
    void summary_deserializesComponentsWithFixedVersions() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/build"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(buildSummaryResponse())));

        BuildSummary summary = client.builds().summary("foo-batch-service", "42");

        var components = summary.issues().getFirst().components();
        assertThat(components).hasSize(1);
        assertThat(components.getFirst().componentId()).isEqualTo("gav://org.apache.logging.log4j:log4j-core:2.14.1");
        assertThat(components.getFirst().fixedVersions()).containsExactly("2.15.0", "2.16.0");
    }

    @Test
    void summary_deserializesComponentSummaryCounts() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/build"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(buildSummaryResponse())));

        BuildSummary summary = client.builds().summary("foo-batch-service", "42");

        assertThat(summary.componentSummaryCounts().total()).isEqualTo(10);
        assertThat(summary.componentSummaryCounts().vulnerable()).isEqualTo(1);
    }

    @Test
    void list_callsGetBuildsEndpoint() {
        wm.stubFor(get(urlPathEqualTo("/xray/api/v1/builds"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(indexedBuildsResponse())));

        client.builds().list();

        wm.verify(getRequestedFor(urlPathEqualTo("/xray/api/v1/builds")));
    }

    @Test
    void list_returnsBuildNames() {
        wm.stubFor(get(urlPathEqualTo("/xray/api/v1/builds"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(indexedBuildsResponse())));

        List<IndexedBuild> builds = client.builds().list();

        assertThat(builds).hasSize(2);
        assertThat(builds.get(0).buildName()).isEqualTo("foo-batch-service");
        assertThat(builds.get(1).buildName()).isEqualTo("foo-api-service");
    }

    @Test
    void list_emptyRows_returnsEmptyList() {
        wm.stubFor(get(urlPathEqualTo("/xray/api/v1/builds"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"rows": [], "count": 0}
                                """)));

        List<IndexedBuild> builds = client.builds().list();

        assertThat(builds).isEmpty();
    }

    @Test
    void list_missingRowsField_returnsEmptyList() {
        wm.stubFor(get(urlPathEqualTo("/xray/api/v1/builds"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"count": 0}
                                """)));

        List<IndexedBuild> builds = client.builds().list();

        assertThat(builds).isEmpty();
    }

    // ---- helpers ----

    private String buildSummaryResponse() {
        return """
                {
                  "issues": [
                    {
                      "issue_id": "XRAY-12345",
                      "summary": "Remote code execution via Log4Shell",
                      "description": "Apache Log4j2 2.0-beta9 through 2.15.0 JNDI features...",
                      "issue_type": "security",
                      "severity": "Critical",
                      "provider": "JFrog",
                      "cves": [
                        {
                          "cve": "CVE-2021-44228",
                          "cvss_v3_score": "10.0",
                          "cvss_v3_vector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H"
                        }
                      ],
                      "components": [
                        {
                          "component_id": "gav://org.apache.logging.log4j:log4j-core:2.14.1",
                          "fixed_versions": ["2.15.0", "2.16.0"]
                        }
                      ]
                    }
                  ],
                  "component_summary_counts": {
                    "total": 10,
                    "vulnerable": 1
                  }
                }
                """;
    }

    private String indexedBuildsResponse() {
        return """
                {
                  "rows": [
                    {"build_name": "foo-batch-service", "build_repo": "artifactory-build-info"},
                    {"build_name": "foo-api-service",   "build_repo": "artifactory-build-info"}
                  ],
                  "count": 2
                }
                """;
    }
}
