package com.xrayclient.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xrayclient.model.artifacts.ArtifactSummary;
import com.xrayclient.model.common.BuildIssue;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class ArtifactsApi {

    private static final String SUMMARY_PATH = "/xray/api/v1/summary/artifact";

    private final XrayHttpClient http;

    public List<ArtifactSummary> getSummaryByPaths(List<String> paths) {
        return getSummary(new ArtifactSummaryRequest(paths, null));
    }

    public List<ArtifactSummary> getSummaryByChecksums(List<String> checksums) {
        return getSummary(new ArtifactSummaryRequest(null, checksums));
    }

    private List<ArtifactSummary> getSummary(ArtifactSummaryRequest request) {
        ArtifactSummaryResponse response = http.post(SUMMARY_PATH, request, ArtifactSummaryResponse.class);
        if (response.artifacts() == null) {
            return List.of();
        }
        return response.artifacts().stream()
                .map(a -> new ArtifactSummary(
                        a.general().name(),
                        a.general().path(),
                        a.general().pkgType(),
                        a.general().sha256(),
                        a.issues() != null ? a.issues() : List.of()))
                .toList();
    }

    record ArtifactSummaryRequest(
            @JsonProperty("paths") List<String> paths,
            @JsonProperty("checksums") List<String> checksums
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ArtifactSummaryResponse(
            @JsonProperty("artifacts") List<ArtifactEntry> artifacts
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ArtifactEntry(
            @JsonProperty("general") GeneralInfo general,
            @JsonProperty("issues") List<BuildIssue> issues
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeneralInfo(
            @JsonProperty("name") String name,
            @JsonProperty("path") String path,
            @JsonProperty("pkg_type") String pkgType,
            @JsonProperty("sha256") String sha256
    ) {}
}
