package com.xrayclient.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViolationsQueryBuilderTest {

    private final ViolationsQueryBuilder builder = new ViolationsQueryBuilder(null);

    @Test
    void fetchAll_zeroPageSize_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> builder.fetchAll(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pageSize must be positive");
    }

    @Test
    void fetchAll_negativePageSize_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> builder.fetchAll(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pageSize must be positive");
    }
}
