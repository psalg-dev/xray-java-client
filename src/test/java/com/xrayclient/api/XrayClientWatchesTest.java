package com.xrayclient.api;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.xrayclient.model.watches.Watch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class XrayClientWatchesTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private XrayClient client;

    @BeforeEach
    void setUp() {
        client = XrayClient.builder()
                .baseUrl("http://localhost:" + wm.getPort())
                .basicAuth("admin", "password")
                .build();
    }

    @Test
    void listWatches_callsGetWatchesEndpoint() {
        wm.stubFor(get(urlPathEqualTo("/xray/api/v2/watches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(twoWatches())));

        client.watches().list();

        wm.verify(getRequestedFor(urlPathEqualTo("/xray/api/v2/watches")));
    }

    @Test
    void listWatches_returnsWatchNames() {
        wm.stubFor(get(urlPathEqualTo("/xray/api/v2/watches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(twoWatches())));

        List<Watch> watches = client.watches().list();

        assertThat(watches).hasSize(2);
        assertThat(watches.get(0).name()).isEqualTo("foo.scan.test.watch");
        assertThat(watches.get(1).name()).isEqualTo("bar.scan.prod.watch");
    }

    @Test
    void listWatches_returnsActiveStatus() {
        wm.stubFor(get(urlPathEqualTo("/xray/api/v2/watches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(twoWatches())));

        List<Watch> watches = client.watches().list();

        assertThat(watches.get(0).active()).isTrue();
        assertThat(watches.get(1).active()).isFalse();
    }

    @Test
    void listWatches_sendsBasicAuthHeader() {
        wm.stubFor(get(urlPathEqualTo("/xray/api/v2/watches"))
                .withHeader("Authorization", matching("Basic .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        client.watches().list();

        wm.verify(getRequestedFor(urlPathEqualTo("/xray/api/v2/watches"))
                .withHeader("Authorization", matching("Basic .*")));
    }

    // ---- helpers ----

    private String twoWatches() {
        return """
                [
                  {
                    "general_data": {
                      "name": "foo.scan.test.watch",
                      "description": "Foo team test artifacts",
                      "active": true
                    }
                  },
                  {
                    "general_data": {
                      "name": "bar.scan.prod.watch",
                      "description": "Bar team prod artifacts",
                      "active": false
                    }
                  }
                ]
                """;
    }
}
