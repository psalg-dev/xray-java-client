package dev.psalg.xray.model.violations;

import com.fasterxml.jackson.annotation.JsonValue;

/** Categories of policy violations reported by Xray. */
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
