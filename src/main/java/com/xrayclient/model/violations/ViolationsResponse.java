package com.xrayclient.model.violations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViolationsResponse(
        @JsonProperty("violations") List<Violation> violations,
        @JsonProperty("total_violations") int totalViolations
) {
    public ViolationsResponse {
        violations = violations != null ? List.copyOf(violations) : Collections.emptyList();
    }

    public boolean hasMore(int currentPage, int pageSize) {
        return (long) currentPage * pageSize < totalViolations;
    }
}
