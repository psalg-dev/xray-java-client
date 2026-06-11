package dev.psalg.xray.model.builds;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** A build indexed by Xray, identified by its name and the build-info repository it is stored in. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IndexedBuild(
        @JsonProperty("build_name") String buildName,
        @JsonProperty("build_repo") String buildRepo
) {}
