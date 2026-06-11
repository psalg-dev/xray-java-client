package com.xrayclient.model.violations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** An artifact impacted by a violation, including any infected files found within it. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ImpactedArtifact(
        @JsonProperty("name") String name,
        @JsonProperty("display_name") String displayName,
        @JsonProperty("path") String path,
        @JsonProperty("pkg_type") String pkgType,
        @JsonProperty("sha256") String sha256,
        @JsonProperty("infected_files") List<InfectedFile> infectedFiles
) {
    /** A file within an impacted artifact (e.g. inside an archive) that triggered the violation. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InfectedFile(
            @JsonProperty("name") String name,
            @JsonProperty("path") String path,
            @JsonProperty("sha256") String sha256,
            @JsonProperty("depth") int depth,
            @JsonProperty("parent_sha") String parentSha
    ) {}
}
