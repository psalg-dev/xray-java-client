package com.xrayclient.model.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xrayclient.model.builds.BuildIssue;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArtifactSummary(
        @JsonProperty("name") String name,
        @JsonProperty("path") String path,
        @JsonProperty("pkg_type") String pkgType,
        @JsonProperty("sha256") String sha256,
        @JsonProperty("issues") List<BuildIssue> issues
) {}
