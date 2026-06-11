package dev.psalg.xray.api;

import dev.psalg.xray.internal.WatchesApi;
import dev.psalg.xray.model.watches.Watch;
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
