package dev.psalg.xray.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.psalg.xray.model.watches.Watch;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class WatchesApi {

    private static final String PATH = "/xray/api/v2/watches";
    private static final TypeReference<List<WatchResponse>> WATCH_LIST_TYPE = new TypeReference<>() {};

    private final XrayHttpClient http;

    public List<Watch> listWatches() {
        List<WatchResponse> raw = http.get(PATH, WATCH_LIST_TYPE);
        if (raw == null) {
            return List.of();
        }
        return raw.stream()
                .filter(r -> r.generalData() != null)
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
