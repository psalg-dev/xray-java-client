package dev.psalg.xray.model.violations;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeverityTest {

    @Test
    void getValue_returnsXrayApiString() {
        assertThat(Severity.CRITICAL.getValue()).isEqualTo("Critical");
        assertThat(Severity.HIGH.getValue()).isEqualTo("High");
    }

    @Test
    void fromValue_isCaseInsensitive() {
        assertThat(Severity.fromValue("critical")).isEqualTo(Severity.CRITICAL);
        assertThat(Severity.fromValue("HIGH")).isEqualTo(Severity.HIGH);
    }

    @Test
    void fromValue_unknownString_returnsUnknown() {
        assertThat(Severity.fromValue("not-a-severity")).isEqualTo(Severity.UNKNOWN);
    }
}
