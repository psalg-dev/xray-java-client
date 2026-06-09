package com.xrayclient.model.builds;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IndexedBuild(
        @JsonProperty("build_name") String buildName,
        @JsonProperty("build_repo") String buildRepo
) {}
