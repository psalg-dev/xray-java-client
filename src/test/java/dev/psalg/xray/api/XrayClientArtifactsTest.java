package dev.psalg.xray.api;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.psalg.xray.exception.XrayApiException;
import dev.psalg.xray.model.artifacts.ArtifactSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("wiremock")
class XrayClientArtifactsTest {

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
    void summaryByPath_callsPostArtifactSummaryEndpoint() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/artifact"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(artifactSummaryResponse())));

        client.artifacts().summaryByPath("docker-local/foo-batch-service/1.0.0/manifest.json");

        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/summary/artifact")));
    }

    @Test
    void summaryByPath_sendsPathsInRequestBody() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/artifact"))
                .withRequestBody(matchingJsonPath("$.paths[0]", equalTo("docker-local/foo-batch-service/1.0.0/manifest.json")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(artifactSummaryResponse())));

        client.artifacts().summaryByPath("docker-local/foo-batch-service/1.0.0/manifest.json");

        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/summary/artifact"))
                .withRequestBody(matchingJsonPath("$.paths[0]", equalTo("docker-local/foo-batch-service/1.0.0/manifest.json"))));
    }

    @Test
    void summaryByPath_deserializesArtifactGeneralInfo() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/artifact"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(artifactSummaryResponse())));

        List<ArtifactSummary> summaries = client.artifacts()
                .summaryByPath("docker-local/foo-batch-service/1.0.0/manifest.json");

        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst().name()).isEqualTo("manifest.json");
        assertThat(summaries.getFirst().path()).isEqualTo("docker-local/foo-batch-service/1.0.0");
        assertThat(summaries.getFirst().pkgType()).isEqualTo("docker");
        assertThat(summaries.getFirst().sha256()).isEqualTo("abc123def456");
    }

    @Test
    void summaryByPath_deserializesIssues() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/artifact"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(artifactSummaryResponse())));

        List<ArtifactSummary> summaries = client.artifacts()
                .summaryByPath("docker-local/foo-batch-service/1.0.0/manifest.json");

        var issues = summaries.getFirst().issues();
        assertThat(issues).hasSize(1);
        assertThat(issues.getFirst().severity()).isEqualTo("Critical");
        assertThat(issues.getFirst().summary()).isEqualTo("Remote code execution via Log4Shell");
        assertThat(issues.getFirst().cves().getFirst().cveId()).isEqualTo("CVE-2021-44228");
        assertThat(issues.getFirst().components().getFirst().fixedVersions()).containsExactly("2.15.0");
    }

    @Test
    void summaryByPath_emptyArtifactsResponse_returnsEmptyList() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/artifact"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"artifacts": []}
                                """)));

        List<ArtifactSummary> summaries = client.artifacts()
                .summaryByPath("docker-local/missing/1.0.0/manifest.json");

        assertThat(summaries).isEmpty();
    }

    @Test
    void summaryByPath_on500_throwsXrayApiException() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/artifact"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        assertThatThrownBy(() -> client.artifacts().summaryByPath("docker-local/foo/1.0.0/manifest.json"))
                .isInstanceOf(XrayApiException.class)
                .satisfies(ex -> assertThat(((XrayApiException) ex).getStatusCode()).isEqualTo(500));
    }

    @Test
    void summaryByChecksum_sendsChecksumInRequestBody() {
        wm.stubFor(post(urlPathEqualTo("/xray/api/v1/summary/artifact"))
                .withRequestBody(matchingJsonPath("$.checksums[0]", equalTo("sha256:abc123")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(artifactSummaryResponse())));

        client.artifacts().summaryByChecksum("sha256:abc123");

        wm.verify(postRequestedFor(urlPathEqualTo("/xray/api/v1/summary/artifact"))
                .withRequestBody(matchingJsonPath("$.checksums[0]", equalTo("sha256:abc123"))));
    }

    // ---- helpers ----

    private String artifactSummaryResponse() {
        return """
                {
                  "artifacts": [
                    {
                      "general": {
                        "name": "manifest.json",
                        "path": "docker-local/foo-batch-service/1.0.0",
                        "pkg_type": "docker",
                        "sha256": "abc123def456"
                      },
                      "issues": [
                        {
                          "issue_id": "XRAY-12345",
                          "summary": "Remote code execution via Log4Shell",
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
                              "fixed_versions": ["2.15.0"]
                            }
                          ]
                        }
                      ],
                      "licenses": []
                    }
                  ]
                }
                """;
    }
}
