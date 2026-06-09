package com.xrayclient.api;

import com.xrayclient.internal.WatchesApi;
import com.xrayclient.model.watches.Watch;

import java.util.List;

public final class WatchesQueryBuilder {

    private final WatchesApi api;

    WatchesQueryBuilder(WatchesApi api) {
        this.api = api;
    }

    public List<Watch> list() {
        return api.listWatches();
    }
}
