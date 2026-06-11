package dev.psalg.xray.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.psalg.xray.api.ArtifactsQueryBuilder;
import dev.psalg.xray.api.BuildsQueryBuilder;
import dev.psalg.xray.api.WatchesQueryBuilder;
import dev.psalg.xray.model.artifacts.ArtifactSummary;
import dev.psalg.xray.model.builds.BuildSummary;
import dev.psalg.xray.model.builds.IndexedBuild;
import dev.psalg.xray.model.watches.Watch;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import static org.assertj.core.api.Assertions.assertThat;

class QueryBuilderCoverageTest {

    @Test
    void artifactsQueryBuilder_summaryByPath_delegatesToArtifactsApi() throws Exception {
        RecordingHttpClient transport = new RecordingHttpClient(artifactSummaryResponse());
        XrayHttpClient http = new XrayHttpClient(
                transport,
                "http://example.test",
                "Bearer token",
                new ObjectMapper(),
                Duration.ofSeconds(1)
        );
        ArtifactsApi api = new ArtifactsApi(http);
        ArtifactsQueryBuilder builder = newBuilder(ArtifactsQueryBuilder.class, api);

        List<ArtifactSummary> summaries = builder.summaryByPath("repo/path/1.0/artifact.jar");

        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst().name()).isEqualTo("artifact.jar");
        assertThat(transport.requests).hasSize(1);
        RecordingHttpClient.Request request = transport.requests.get(0);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo("/xray/api/v1/summary/artifact");
        assertThat(request.body()).contains("\"paths\":[\"repo/path/1.0/artifact.jar\"]");
        assertThat(request.body()).contains("\"checksums\":null");
    }

    @Test
    void artifactsQueryBuilder_summaryByChecksum_delegatesToArtifactsApi() throws Exception {
        RecordingHttpClient transport = new RecordingHttpClient(artifactSummaryResponse());
        XrayHttpClient http = new XrayHttpClient(
                transport,
                "http://example.test",
                "Bearer token",
                new ObjectMapper(),
                Duration.ofSeconds(1)
        );
        ArtifactsApi api = new ArtifactsApi(http);
        ArtifactsQueryBuilder builder = newBuilder(ArtifactsQueryBuilder.class, api);

        List<ArtifactSummary> summaries = builder.summaryByChecksum("sha256:abc123");

        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst().path()).isEqualTo("repo/path/1.0");
        assertThat(transport.requests).hasSize(1);
        RecordingHttpClient.Request request = transport.requests.get(0);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo("/xray/api/v1/summary/artifact");
        assertThat(request.body()).contains("\"paths\":null");
        assertThat(request.body()).contains("\"checksums\":[\"sha256:abc123\"]");
    }

    @Test
    void buildsQueryBuilder_summary_delegatesToBuildsApi() throws Exception {
        RecordingHttpClient transport = new RecordingHttpClient(buildSummaryResponse());
        XrayHttpClient http = new XrayHttpClient(
                transport,
                "http://example.test",
                "Bearer token",
                new ObjectMapper(),
                Duration.ofSeconds(1)
        );
        BuildsApi api = new BuildsApi(http);
        BuildsQueryBuilder builder = newBuilder(BuildsQueryBuilder.class, api);

        BuildSummary summary = builder.summary("build-name", "42");

        assertThat(summary.componentSummaryCounts().total()).isZero();
        assertThat(transport.requests).hasSize(1);
        RecordingHttpClient.Request request = transport.requests.get(0);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo("/xray/api/v1/summary/build");
        assertThat(request.body()).contains("\"name\":\"build-name\"", "\"number\":\"42\"");
    }

    @Test
    void buildsQueryBuilder_list_delegatesToBuildsApi() throws Exception {
        RecordingHttpClient transport = new RecordingHttpClient(indexedBuildsResponse());
        XrayHttpClient http = new XrayHttpClient(
                transport,
                "http://example.test",
                "Bearer token",
                new ObjectMapper(),
                Duration.ofSeconds(1)
        );
        BuildsApi api = new BuildsApi(http);
        BuildsQueryBuilder builder = newBuilder(BuildsQueryBuilder.class, api);

        List<IndexedBuild> builds = builder.list();

        assertThat(builds).containsExactly(new IndexedBuild("build-name", "build-repo"));
        assertThat(transport.requests).hasSize(1);
        RecordingHttpClient.Request request = transport.requests.get(0);
        assertThat(request.method()).isEqualTo("GET");
        assertThat(request.path()).isEqualTo("/xray/api/v1/builds");
    }

    @Test
    void watchesQueryBuilder_list_delegatesToWatchesApi() throws Exception {
        RecordingHttpClient transport = new RecordingHttpClient(watchesResponse());
        XrayHttpClient http = new XrayHttpClient(
                transport,
                "http://example.test",
                "Bearer token",
                new ObjectMapper(),
                Duration.ofSeconds(1)
        );
        WatchesApi api = new WatchesApi(http);
        WatchesQueryBuilder builder = newBuilder(WatchesQueryBuilder.class, api);

        List<Watch> watches = builder.list();

        assertThat(watches).containsExactly(new Watch("watch-name", "watch description", true));
        assertThat(transport.requests).hasSize(1);
        RecordingHttpClient.Request request = transport.requests.get(0);
        assertThat(request.method()).isEqualTo("GET");
        assertThat(request.path()).isEqualTo("/xray/api/v2/watches");
    }

    private static <T> T newBuilder(Class<T> builderType, Object api) throws Exception {
        Constructor<T> constructor = builderType.getDeclaredConstructor(api.getClass());
        constructor.setAccessible(true);
        return constructor.newInstance(api);
    }

    private static String artifactSummaryResponse() {
        return """
                {
                  "artifacts": [
                    {
                      "general": {
                        "name": "artifact.jar",
                        "path": "repo/path/1.0",
                        "pkg_type": "maven",
                        "sha256": "sha256"
                      },
                      "issues": []
                    }
                  ]
                }
                """;
    }

    private static String buildSummaryResponse() {
        return """
                {
                  "issues": [],
                  "component_summary_counts": {
                    "total": 0,
                    "vulnerable": 0
                  }
                }
                """;
    }

    private static String indexedBuildsResponse() {
        return """
                {
                  "rows": [
                    {"build_name": "build-name", "build_repo": "build-repo"}
                  ],
                  "count": 1
                }
                """;
    }

    private static String watchesResponse() {
        return """
                [
                  {
                    "general_data": {
                      "name": "watch-name",
                      "description": "watch description",
                      "active": true
                    }
                  }
                ]
                """;
    }

    static final class RecordingHttpClient extends HttpClient {
        private final HttpClient delegate = HttpClient.newHttpClient();
        private final List<Request> requests = new ArrayList<>();
        private final String responseBody;

        RecordingHttpClient(String responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return delegate.cookieHandler();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return delegate.connectTimeout();
        }

        @Override
        public Redirect followRedirects() {
            return delegate.followRedirects();
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return delegate.proxy();
        }

        @Override
        public SSLContext sslContext() {
            return delegate.sslContext();
        }

        @Override
        public SSLParameters sslParameters() {
            return delegate.sslParameters();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return delegate.authenticator();
        }

        @Override
        public Version version() {
            return delegate.version();
        }

        @Override
        public Optional<Executor> executor() {
            return delegate.executor();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> handler)
                throws IOException, InterruptedException {
            requests.add(new Request(request.method(), request.uri().getPath(), readBody(request)));
            HttpHeaders headers = HttpHeaders.of(Map.of("Content-Type", List.of("application/json")), (name, values) -> !name.isBlank());
            HttpResponse.ResponseInfo responseInfo = new HttpResponse.ResponseInfo() {
                @Override
                public int statusCode() {
                    return 200;
                }

                @Override
                public HttpHeaders headers() {
                    return headers;
                }

                @Override
                public Version version() {
                    return Version.HTTP_1_1;
                }
            };
            BodySubscriber<T> subscriber = handler.apply(responseInfo);
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                }

                @Override
                public void cancel() {
                }
            });
            subscriber.onNext(List.of(ByteBuffer.wrap(responseBody.getBytes(StandardCharsets.UTF_8))));
            subscriber.onComplete();
            T body = subscriber.getBody().toCompletableFuture().join();
            return response(request, headers, body);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> handler) {
            try {
                return CompletableFuture.completedFuture(send(request, handler));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                BodyHandler<T> handler,
                PushPromiseHandler<T> pushPromiseHandler
        ) {
            return sendAsync(request, handler);
        }

        private static String readBody(HttpRequest request) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            request.bodyPublisher().ifPresent(publisher -> publisher.subscribe(new Subscriber<>() {
                @Override
                public void onSubscribe(Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(ByteBuffer item) {
                    out.write(item.array(), item.position(), item.remaining());
                    item.position(item.limit());
                }

                @Override
                public void onError(Throwable throwable) {
                    throw new RuntimeException(throwable);
                }

                @Override
                public void onComplete() {
                }
            }));
            return out.toString(StandardCharsets.UTF_8);
        }

        private static <T> HttpResponse<T> response(HttpRequest request, HttpHeaders headers, T body) {
            return new HttpResponse<>() {
                @Override
                public int statusCode() {
                    return 200;
                }

                @Override
                public HttpRequest request() {
                    return request;
                }

                @Override
                public Optional<HttpResponse<T>> previousResponse() {
                    return Optional.empty();
                }

                @Override
                public HttpHeaders headers() {
                    return headers;
                }

                @Override
                public T body() {
                    return body;
                }

                @Override
                public Optional<SSLSession> sslSession() {
                    return Optional.empty();
                }

                @Override
                public URI uri() {
                    return request.uri();
                }

                @Override
                public Version version() {
                    return Version.HTTP_1_1;
                }
            };
        }

        record Request(String method, String path, String body) {}
    }
}
