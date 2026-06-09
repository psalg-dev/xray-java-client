package com.xrayclient.model.violations;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ViolationType {
    SECURITY("security"),
    LICENSE("license"),
    OPERATIONAL_RISK("operational_risk");

    private final String value;

    ViolationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
