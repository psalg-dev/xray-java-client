package dev.psalg.xray.model.violations;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationTypeTest {

    @Test
    void getValue_returnsXrayApiString() {
        assertThat(ViolationType.SECURITY.getValue()).isEqualTo("security");
        assertThat(ViolationType.LICENSE.getValue()).isEqualTo("license");
        assertThat(ViolationType.OPERATIONAL_RISK.getValue()).isEqualTo("operational_risk");
    }
}
