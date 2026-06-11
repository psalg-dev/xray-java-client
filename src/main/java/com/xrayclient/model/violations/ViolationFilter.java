package com.xrayclient.model.violations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder(builderClassName = "Builder")
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
) {}
