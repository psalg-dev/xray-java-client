package com.xrayclient.model.violations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Violation(
        @JsonProperty("violation_details_url") String violationDetailsUrl,
        @JsonProperty("watch_name") String watchName,
        @JsonProperty("policy_name") String policyName,
        @JsonProperty("rule_name") String ruleName,
        @JsonProperty("created") OffsetDateTime created,
        @JsonProperty("updated") OffsetDateTime updated,
        @JsonProperty("description") String description,
        @JsonProperty("type") String type,
        @JsonProperty("severity") String severity,
        @JsonProperty("impacted_artifacts") List<ImpactedArtifact> impactedArtifacts,
        @JsonProperty("cves") List<Cve> cves
) {
    public Severity severityEnum() {
        return Severity.fromValue(severity);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cve(
            @JsonProperty("cve") String cveId,
            @JsonProperty("cvss_v2_score") String cvssV2Score,
            @JsonProperty("cvss_v2_vector") String cvssV2Vector,
            @JsonProperty("cvss_v3_score") String cvssV3Score,
            @JsonProperty("cvss_v3_vector") String cvssV3Vector,
            @JsonProperty("fix_versions") List<String> fixVersions
    ) {}
}
