package dev.psalg.xray.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.psalg.xray.model.violations.ViolationFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
record ViolationsRequest(
        @JsonProperty("filters") ViolationFilter filters
) {}
