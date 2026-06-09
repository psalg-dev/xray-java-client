package com.xrayclient.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xrayclient.model.violations.ViolationFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
record ViolationsRequest(
        @JsonProperty("filters") ViolationFilter filters
) {}
