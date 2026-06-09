package com.xrayclient.model.violations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ViolationFilter(
        @JsonProperty("type") String type,
        @JsonProperty("watch_names") List<String> watchNames,
        @JsonProperty("watch_patterns") List<String> watchPatterns,
        @JsonProperty("policy_names") List<String> policyNames,
        @JsonProperty("violation_status") String violationStatus,
        @JsonProperty("severities") List<String> severities,
        @JsonProperty("component") String component,
        @JsonProperty("artifact") String artifact
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String type;
        private List<String> watchNames;
        private List<String> watchPatterns;
        private List<String> policyNames;
        private String violationStatus;
        private List<String> severities;
        private String component;
        private String artifact;

        private Builder() {}

        public Builder type(String type) { this.type = type; return this; }
        public Builder watchNames(List<String> names) { this.watchNames = names; return this; }
        public Builder watchPatterns(List<String> patterns) { this.watchPatterns = patterns; return this; }
        public Builder policyNames(List<String> names) { this.policyNames = names; return this; }
        public Builder violationStatus(String status) { this.violationStatus = status; return this; }
        public Builder severities(List<String> severities) { this.severities = severities; return this; }
        public Builder component(String component) { this.component = component; return this; }
        public Builder artifact(String artifact) { this.artifact = artifact; return this; }

        public ViolationFilter build() {
            return new ViolationFilter(type, watchNames, watchPatterns, policyNames,
                    violationStatus, severities, component, artifact);
        }
    }
}
