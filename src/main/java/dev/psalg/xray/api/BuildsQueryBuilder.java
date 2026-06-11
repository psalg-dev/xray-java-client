package dev.psalg.xray.api;

import dev.psalg.xray.internal.BuildsApi;
import dev.psalg.xray.model.builds.BuildSummary;
import dev.psalg.xray.model.builds.IndexedBuild;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Fluent builder for querying Xray-indexed builds and their vulnerability summaries.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class BuildsQueryBuilder {

    private final BuildsApi api;

    /** Returns the vulnerability and license summary for a specific build. */
    public BuildSummary summary(String buildName, String buildNumber) {
        return api.getBuildSummary(buildName, buildNumber);
    }

    /** Lists all builds indexed by Xray. */
    public List<IndexedBuild> list() {
        return api.listBuilds();
    }
}
