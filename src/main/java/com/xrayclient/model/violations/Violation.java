package com.xrayclient.model.violations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xrayclient.model.common.Cve;

import java.time.OffsetDateTime;
import java.util.List;

/** A single policy violation returned by {@code POST /xray/api/v1/violations}. */
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
}
