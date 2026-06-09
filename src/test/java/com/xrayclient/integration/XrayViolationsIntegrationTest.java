package com.xrayclient.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xrayclient.api.XrayClient;
import com.xrayclient.model.violations.Severity;
import com.xrayclient.model.violations.Violation;
import com.xrayclient.model.violations.ViolationsResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * End-to-end integration test against a real JFrog Platform instance.
 *
 * Requires environment variables:
 *   XRAY_BASE_URL    — e.g. http://localhost:8082
 *   XRAY_USER        — e.g. admin
 *   XRAY_PASSWORD    — e.g. password
 *
 * The test auto-provisions:
 *   - A local Maven repository (xray-e2e-repo)
 *   - An Xray security policy that blocks log4j CVE-2021-44228 (CRITICAL)
 *   - A watch on the repository
 *   - Uploads a known-vulnerable log4j 2.14.1 Maven artifact
 *   - Triggers an Xray scan
 *   - Queries violations and asserts at least one CRITICAL violation exists
 *   - Cleans up all created resources on exit
 *
 * Run via: mvn verify -Pintegration
 *   (or directly: XRAY_BASE_URL=... mvn test -Dgroups=integration)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "XRAY_BASE_URL", matches = ".+")
class XrayViolationsIntegrationTest {

    private static final String REPO_KEY = "xray-e2e-repo";
    private static final String POLICY_NAME = "xray-e2e-policy";
    private static final String WATCH_NAME = "xray-e2e-watch";

    private String baseUrl;
    private String authHeader;
    private HttpClient http;
    private ObjectMapper mapper;
    private XrayClient xrayClient;

    @BeforeAll
    void setup() throws Exception {
        baseUrl = System.getenv("XRAY_BASE_URL").replaceAll("/+$", "");
        String token = System.getenv("XRAY_TOKEN");
        String user = System.getenv().getOrDefault("XRAY_USER", "admin");
        String password = System.getenv().getOrDefault("XRAY_PASSWORD", "password");

        if (token != null && !token.isBlank()) {
            authHeader = "Bearer " + token;
        } else {
            authHeader = "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
        }

        http = HttpClient.newHttpClient();
        mapper = new ObjectMapper();

        XrayClient.Builder builder = XrayClient.builder().baseUrl(baseUrl);
        if (token != null && !token.isBlank()) {
            builder.tokenAuth(token);
        } else {
            builder.basicAuth(user, password);
        }
        xrayClient = builder.build();

        assumeXrayAvailable();
        createRepository();
        createSecurityPolicy();
        createWatch();
        uploadVulnerableArtifact();
        triggerScanAndWait();
    }

    @AfterAll
    void cleanup() {
        silently(this::deleteWatch);
        silently(this::deletePolicy);
        silently(this::deleteRepository);
    }

    @Test
    void violations_returnsAtLeastOneCriticalOrHighViolation() {
        ViolationsResponse response = xrayClient.violations()
                .watches(WATCH_NAME)
                .withSeverities(Severity.CRITICAL, Severity.HIGH)
                .activeOnly()
                .fetchPage(1, 25);

        assertThat(response.totalViolations())
                .as("Expected at least one CRITICAL/HIGH violation from log4j 2.14.1")
                .isGreaterThan(0);

        assertThat(response.violations())
                .as("Violations list should not be empty")
                .isNotEmpty();

        List<String> severities = response.violations().stream()
                .map(Violation::severity)
                .toList();
        assertThat(severities)
                .as("All returned violations should be CRITICAL or HIGH")
                .allMatch(s -> s.equalsIgnoreCase("Critical") || s.equalsIgnoreCase("High"));
    }

    @Test
    void violations_fetchAll_autopaginates() {
        List<Violation> all = xrayClient.violations()
                .watches(WATCH_NAME)
                .withSeverities(Severity.CRITICAL, Severity.HIGH)
                .activeOnly()
                .fetchAll(5);

        assertThat(all)
                .as("fetchAll should return all violations across pages")
                .isNotEmpty();
    }

    // ---- availability check ----

    private void assumeXrayAvailable() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/xray/api/v1/system/ping"))
                .header("Authorization", authHeader)
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        assumeTrue(resp.statusCode() == 200,
                "Xray not available at " + baseUrl + " (HTTP " + resp.statusCode() + ") — skipping Xray tests");
    }

    // ---- provisioning helpers ----

    private void createRepository() throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("key", REPO_KEY);
        body.put("rclass", "local");
        body.put("packageType", "maven");
        body.put("description", "Xray integration test repo — safe to delete");
        body.put("xrayIndex", true);

        put("/artifactory/api/repositories/" + REPO_KEY, body.toString());

        // Enable Xray indexing so the repo is visible to watches
        ObjectNode xrayBody = mapper.createObjectNode();
        xrayBody.put("xrayIndex", true);
        post("/artifactory/api/repositories/" + REPO_KEY, xrayBody.toString());
    }

    private void createSecurityPolicy() throws Exception {
        ObjectNode rule = mapper.createObjectNode();
        rule.put("name", "block-critical-cve");
        rule.put("priority", 1);

        ObjectNode criteria = mapper.createObjectNode();
        criteria.put("min_severity", "High");
        rule.set("criteria", criteria);

        ObjectNode blockDownload = mapper.createObjectNode();
        blockDownload.put("unscanned", false);
        blockDownload.put("active", true);

        ObjectNode actions = mapper.createObjectNode();
        actions.set("block_download", blockDownload);
        rule.set("actions", actions);

        ArrayNode rules = mapper.createArrayNode();
        rules.add(rule);

        ObjectNode body = mapper.createObjectNode();
        body.put("name", POLICY_NAME);
        body.put("type", "security");
        body.put("description", "E2E test policy — safe to delete");
        body.set("rules", rules);

        post("/xray/api/v2/policies", body.toString());
    }

    private void createWatch() throws Exception {
        ObjectNode repoItem = mapper.createObjectNode();
        repoItem.put("type", "repository");
        repoItem.put("name", REPO_KEY);
        repoItem.put("bin_mgr_id", "default");

        ArrayNode resourceList = mapper.createArrayNode();
        resourceList.add(repoItem);

        ObjectNode projectResources = mapper.createObjectNode();
        projectResources.set("resources", resourceList);

        ObjectNode assignedPolicy = mapper.createObjectNode();
        assignedPolicy.put("name", POLICY_NAME);
        assignedPolicy.put("type", "security");
        ArrayNode policies = mapper.createArrayNode();
        policies.add(assignedPolicy);

        ObjectNode general = mapper.createObjectNode();
        general.put("name", WATCH_NAME);
        general.put("description", "E2E test watch — safe to delete");
        general.put("active", true);

        ObjectNode watchBody = mapper.createObjectNode();
        watchBody.set("general_data", general);
        watchBody.set("project_resources", projectResources);
        watchBody.set("assigned_policies", policies);

        post("/xray/api/v2/watches", watchBody.toString());
    }

    private void uploadVulnerableArtifact() throws Exception {
        // Download log4j-core 2.14.1 JAR from Maven Central, then upload to Artifactory
        // CVE-2021-44228 (Log4Shell, CVSS 10.0) is present in this version
        String mavenCentralUrl =
                "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.14.1/log4j-core-2.14.1.jar";

        HttpResponse<byte[]> download = http.send(
                HttpRequest.newBuilder().uri(URI.create(mavenCentralUrl)).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(download.statusCode()).as("Failed to download log4j JAR from Maven Central").isEqualTo(200);

        byte[] jarBytes = download.body();
        System.out.println("[E2E] Downloaded log4j-core-2.14.1.jar (" + jarBytes.length + " bytes)");

        String jarPath = "/artifactory/" + REPO_KEY
                + "/org/apache/logging/log4j/log4j-core/2.14.1/log4j-core-2.14.1.jar";

        HttpRequest upload = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + jarPath))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/java-archive")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(jarBytes))
                .build();

        HttpResponse<String> response = http.send(upload, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
                .as("JAR upload failed: " + response.body())
                .isBetween(200, 299);
        System.out.println("[E2E] Uploaded log4j-core-2.14.1.jar to " + REPO_KEY);
    }

    private void triggerScanAndWait() throws Exception {
        // Ask Xray to scan the artifact explicitly
        String componentPath = "default//" + REPO_KEY
                + ":org/apache/logging/log4j/log4j-core/2.14.1/log4j-core-2.14.1.jar";
        ObjectNode scanBody = mapper.createObjectNode();
        scanBody.put("component_name", componentPath);
        try {
            post("/xray/api/v1/scanArtifact", scanBody.toString());
            System.out.println("[E2E] Triggered explicit Xray scan");
        } catch (Exception ignored) {
            System.out.println("[E2E] Explicit scan trigger failed; Xray will scan automatically");
        }

        // Poll until at least one violation appears (max 5 min)
        System.out.println("[E2E] Waiting for Xray to scan and detect violations...");
        for (int i = 0; i < 60; i++) {
            ViolationsResponse check = xrayClient.violations()
                    .watches(WATCH_NAME)
                    .withSeverities(Severity.CRITICAL, Severity.HIGH)
                    .fetchPage(1, 1);
            if (check.totalViolations() > 0) {
                System.out.println("[E2E] Violations detected after ~" + (i * 5) + "s");
                return;
            }
            Thread.sleep(5_000);
        }
        System.out.println("[E2E] Warning: no violations found after 5 min — test assertions may fail");
    }

    // ---- cleanup helpers ----

    private void deleteWatch() throws Exception {
        delete("/xray/api/v2/watches/" + WATCH_NAME);
    }

    private void deletePolicy() throws Exception {
        delete("/xray/api/v2/policies/" + POLICY_NAME);
    }

    private void deleteRepository() throws Exception {
        delete("/artifactory/api/repositories/" + REPO_KEY);
    }

    // ---- raw HTTP helpers ----

    private void post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            System.err.println("POST " + path + " -> " + response.statusCode() + ": " + response.body());
        }
    }

    private void put(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            System.err.println("PUT " + path + " -> " + response.statusCode() + ": " + response.body());
        }
    }

    private void delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", authHeader)
                .DELETE()
                .build();
        http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void silently(ThrowingRunnable r) {
        try { r.run(); } catch (Exception ignored) {}
    }

    @FunctionalInterface
    interface ThrowingRunnable { void run() throws Exception; }
}
