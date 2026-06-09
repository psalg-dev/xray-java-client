package com.xrayclient.api;

import com.xrayclient.internal.BuildsApi;
import com.xrayclient.model.builds.BuildSummary;
import com.xrayclient.model.builds.IndexedBuild;

import java.util.List;

public final class BuildsQueryBuilder {

    private final BuildsApi api;

    BuildsQueryBuilder(BuildsApi api) {
        this.api = api;
    }

    public BuildSummary summary(String buildName, String buildNumber) {
        return api.getBuildSummary(buildName, buildNumber);
    }

    public List<IndexedBuild> list() {
        return api.listBuilds();
    }
}
