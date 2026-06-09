package com.xrayclient.model.violations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ImpactedArtifact(
        @JsonProperty("name") String name,
        @JsonProperty("display_name") String displayName,
        @JsonProperty("path") String path,
        @JsonProperty("pkg_type") String pkgType,
        @JsonProperty("sha256") String sha256,
        @JsonProperty("infected_files") List<InfectedFile> infectedFiles
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InfectedFile(
            @JsonProperty("name") String name,
            @JsonProperty("path") String path,
            @JsonProperty("sha256") String sha256,
            @JsonProperty("depth") int depth,
            @JsonProperty("parent_sha") String parentSha
    ) {}
}
