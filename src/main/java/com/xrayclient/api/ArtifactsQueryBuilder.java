package com.xrayclient.api;

import com.xrayclient.internal.ArtifactsApi;
import com.xrayclient.model.artifacts.ArtifactSummary;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Fluent builder for querying Xray vulnerability summaries for artifacts.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ArtifactsQueryBuilder {

    private final ArtifactsApi api;

    /** Returns vulnerability summaries for the artifacts at the given repository paths. */
    public List<ArtifactSummary> summaryByPath(String... paths) {
        return api.getSummaryByPaths(List.of(paths));
    }

    /** Returns vulnerability summaries for the artifacts matching the given checksums (sha1/sha256/md5). */
    public List<ArtifactSummary> summaryByChecksum(String... checksums) {
        return api.getSummaryByChecksums(List.of(checksums));
    }
}
