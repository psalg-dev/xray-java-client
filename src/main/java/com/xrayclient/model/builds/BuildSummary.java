package com.xrayclient.model.builds;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BuildSummary(
        @JsonProperty("issues") List<BuildIssue> issues,
        @JsonProperty("component_summary_counts") ComponentSummaryCounts componentSummaryCounts
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ComponentSummaryCounts(
            @JsonProperty("total") int total,
            @JsonProperty("vulnerable") int vulnerable
    ) {}
}
