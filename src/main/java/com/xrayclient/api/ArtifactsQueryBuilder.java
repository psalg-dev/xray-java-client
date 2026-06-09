package com.xrayclient.api;

import com.xrayclient.internal.ArtifactsApi;
import com.xrayclient.model.artifacts.ArtifactSummary;

import java.util.List;

public final class ArtifactsQueryBuilder {

    private final ArtifactsApi api;

    ArtifactsQueryBuilder(ArtifactsApi api) {
        this.api = api;
    }

    public List<ArtifactSummary> summaryByPath(String... paths) {
        return api.getSummaryByPaths(List.of(paths));
    }

    public List<ArtifactSummary> summaryByChecksum(String... checksums) {
        return api.getSummaryByChecksums(List.of(checksums));
    }
}
