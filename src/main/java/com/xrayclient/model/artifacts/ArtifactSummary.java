package com.xrayclient.model.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xrayclient.model.common.BuildIssue;

import java.util.List;

/** Vulnerability summary for a single artifact, as returned by {@code /xray/api/v1/summary/artifact}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArtifactSummary(
        @JsonProperty("name") String name,
        @JsonProperty("path") String path,
        @JsonProperty("pkg_type") String pkgType,
        @JsonProperty("sha256") String sha256,
        @JsonProperty("issues") List<BuildIssue> issues
) {}
