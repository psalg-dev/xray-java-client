package com.xrayclient.api;

import com.xrayclient.internal.WatchesApi;
import com.xrayclient.model.watches.Watch;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Fluent builder for querying Xray watches.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class WatchesQueryBuilder {

    private final WatchesApi api;

    /** Lists all watches configured in Xray. */
    public List<Watch> list() {
        return api.listWatches();
    }
}
