package dev.psalg.xray.model.violations;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationFilterTest {

    @Test
    void builder_setsAllFields() {
        ViolationFilter filter = ViolationFilter.builder()
                .type("security")
                .watchNames(List.of("watch1"))
                .watchPatterns(List.of("pattern*"))
                .policyNames(List.of("policy1"))
                .violationStatus("Active")
                .severities(List.of("Critical", "High"))
                .component("log4j")
                .artifact("foo-batch-service")
                .build();

        assertThat(filter.type()).isEqualTo("security");
        assertThat(filter.watchNames()).containsExactly("watch1");
        assertThat(filter.watchPatterns()).containsExactly("pattern*");
        assertThat(filter.policyNames()).containsExactly("policy1");
        assertThat(filter.violationStatus()).isEqualTo("Active");
        assertThat(filter.severities()).containsExactly("Critical", "High");
        assertThat(filter.component()).isEqualTo("log4j");
        assertThat(filter.artifact()).isEqualTo("foo-batch-service");
    }

    @Test
    void builder_unsetFields_areNull() {
        ViolationFilter filter = ViolationFilter.builder().build();

        assertThat(filter.type()).isNull();
        assertThat(filter.watchNames()).isNull();
        assertThat(filter.violationStatus()).isNull();
        assertThat(filter.component()).isNull();
        assertThat(filter.artifact()).isNull();
    }
}
