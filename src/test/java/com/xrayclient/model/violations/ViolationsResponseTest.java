package com.xrayclient.model.violations;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationsResponseTest {

    @Test
    void compactConstructor_nullViolations_becomesEmptyList() {
        ViolationsResponse response = new ViolationsResponse(null, 0);

        assertThat(response.violations()).isEmpty();
    }

    @Test
    void hasMore_whenMorePagesRemain_returnsTrue() {
        ViolationsResponse response = new ViolationsResponse(List.of(), 10);

        assertThat(response.hasMore(1, 5)).isTrue();
    }

    @Test
    void hasMore_onLastPage_returnsFalse() {
        ViolationsResponse response = new ViolationsResponse(List.of(), 10);

        assertThat(response.hasMore(2, 5)).isFalse();
    }

    @Test
    void hasMore_whenTotalIsZero_returnsFalse() {
        ViolationsResponse response = new ViolationsResponse(List.of(), 0);

        assertThat(response.hasMore(1, 25)).isFalse();
    }

    @Test
    void hasMore_partialFinalPage_stopsAfterLastPage() {
        // total=11, pageSize=5 -> pages of 5, 5, 1; page 3 is the last
        ViolationsResponse response = new ViolationsResponse(List.of(), 11);

        assertThat(response.hasMore(2, 5)).isTrue();
        assertThat(response.hasMore(3, 5)).isFalse();
    }
}
