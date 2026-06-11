package com.xrayclient.model.builds;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xrayclient.model.common.Cve;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BuildIssue(
        @JsonProperty("issue_id") String issueId,
        @JsonProperty("summary") String summary,
        @JsonProperty("description") String description,
        @JsonProperty("issue_type") String issueType,
        @JsonProperty("severity") String severity,
        @JsonProperty("provider") String provider,
        @JsonProperty("cves") List<Cve> cves,
        @JsonProperty("components") List<Component> components
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Component(
            @JsonProperty("component_id") String componentId,
            @JsonProperty("fixed_versions") List<String> fixedVersions
    ) {}
}
