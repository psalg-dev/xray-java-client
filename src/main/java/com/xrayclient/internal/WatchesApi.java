package com.xrayclient.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xrayclient.model.watches.Watch;

import java.util.List;

public final class WatchesApi {

    private static final String PATH = "/xray/api/v2/watches";
    private static final TypeReference<List<WatchResponse>> WATCH_LIST_TYPE = new TypeReference<>() {};

    private final XrayHttpClient http;

    public WatchesApi(XrayHttpClient http) {
        this.http = http;
    }

    public List<Watch> listWatches() {
        List<WatchResponse> raw = http.get(PATH, WATCH_LIST_TYPE);
        return raw.stream()
                .map(r -> new Watch(r.generalData().name(), r.generalData().description(), r.generalData().active()))
                .toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record WatchResponse(@JsonProperty("general_data") GeneralData generalData) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record GeneralData(
                @JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("active") boolean active
        ) {}
    }
}
