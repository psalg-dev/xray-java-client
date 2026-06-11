package com.xrayclient.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xrayclient.model.builds.BuildSummary;
import com.xrayclient.model.builds.IndexedBuild;

import java.util.List;

public final class BuildsApi {

    private static final String SUMMARY_PATH = "/xray/api/v1/summary/build";
    private static final String LIST_PATH = "/xray/api/v1/builds";
    private static final TypeReference<IndexedBuildsResponse> INDEXED_BUILDS_TYPE = new TypeReference<>() {};

    private final XrayHttpClient http;

    public BuildsApi(XrayHttpClient http) {
        this.http = http;
    }

    public BuildSummary getBuildSummary(String buildName, String buildNumber) {
        return http.post(SUMMARY_PATH, new BuildSummaryRequest(buildName, buildNumber), BuildSummary.class);
    }

    public List<IndexedBuild> listBuilds() {
        IndexedBuildsResponse response = http.get(LIST_PATH, INDEXED_BUILDS_TYPE);
        return response.rows() != null ? response.rows() : List.of();
    }

    record BuildSummaryRequest(
            @JsonProperty("name") String name,
            @JsonProperty("number") String number
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IndexedBuildsResponse(
            @JsonProperty("rows") List<IndexedBuild> rows,
            @JsonProperty("count") int count
    ) {}
}
