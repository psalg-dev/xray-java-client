package dev.psalg.xray.model.violations;

import com.fasterxml.jackson.annotation.JsonValue;

/** Xray violation severity levels, as used in policy filters and reported on violations. */
public enum Severity {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    UNKNOWN("Unknown"),
    INFORMATION("Information");

    private final String value;

    Severity(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Severity fromValue(String value) {
        for (Severity s : values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        return UNKNOWN;
    }
}
