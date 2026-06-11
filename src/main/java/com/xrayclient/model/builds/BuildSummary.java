package com.xrayclient.model.builds;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xrayclient.model.common.BuildIssue;

import java.util.List;

/** Vulnerability and license summary for a single build, as returned by {@code /xray/api/v1/summary/build}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BuildSummary(
        @JsonProperty("issues") List<BuildIssue> issues,
        @JsonProperty("component_summary_counts") ComponentSummaryCounts componentSummaryCounts
) {
    /** Aggregate counts of components scanned vs. found vulnerable. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ComponentSummaryCounts(
            @JsonProperty("total") int total,
            @JsonProperty("vulnerable") int vulnerable
    ) {}
}
