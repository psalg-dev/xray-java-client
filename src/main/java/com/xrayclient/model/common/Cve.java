package com.xrayclient.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** A CVE identifier with its CVSS scores/vectors and any known fix versions. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Cve(
        @JsonProperty("cve") String cveId,
        @JsonProperty("cvss_v2_score") String cvssV2Score,
        @JsonProperty("cvss_v2_vector") String cvssV2Vector,
        @JsonProperty("cvss_v3_score") String cvssV3Score,
        @JsonProperty("cvss_v3_vector") String cvssV3Vector,
        @JsonProperty("fix_versions") List<String> fixVersions
) {}
