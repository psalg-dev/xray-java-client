package dev.psalg.xray.api;

import dev.psalg.xray.internal.ViolationsApi;
import dev.psalg.xray.model.violations.Severity;
import dev.psalg.xray.model.violations.ViolationFilter;
import dev.psalg.xray.model.violations.Violation;
import dev.psalg.xray.model.violations.ViolationType;
import dev.psalg.xray.model.violations.ViolationsResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent builder for querying Xray violations.
 * Call {@link #fetchPage(int, int)} for one page or {@link #fetchAll()} to auto-paginate.
 */
public final class ViolationsQueryBuilder {

    private static final int DEFAULT_PAGE_SIZE = 25;

    private final ViolationsApi api;
    private final ViolationFilter.Builder filterBuilder = ViolationFilter.builder();
    private String projectKey;

    ViolationsQueryBuilder(ViolationsApi api) {
        this.api = api;
        filterBuilder.violationStatus("Active");
    }

    /** Scope results to a project key (Xray 3.21.2+). */
    public ViolationsQueryBuilder forProject(String projectKey) {
        this.projectKey = projectKey;
        return this;
    }

    /** Filter by one or more severities. */
    public ViolationsQueryBuilder withSeverities(Severity... severities) {
        filterBuilder.severities(Arrays.stream(severities).map(Severity::getValue).toList());
        return this;
    }

    /** Only return active violations (default). */
    public ViolationsQueryBuilder activeOnly() {
        filterBuilder.violationStatus("Active");
        return this;
    }

    /** Filter by violation type: security, license, operational_risk. */
    public ViolationsQueryBuilder ofType(String type) {
        filterBuilder.type(type);
        return this;
    }

    /** Filter by violation type. */
    public ViolationsQueryBuilder ofType(ViolationType type) {
        return ofType(type.getValue());
    }

    /** Filter by artifact name/prefix (server-side). */
    public ViolationsQueryBuilder forArtifact(String artifactName) {
        filterBuilder.artifact(artifactName);
        return this;
    }

    /** Filter by component name. */
    public ViolationsQueryBuilder forComponent(String componentName) {
        filterBuilder.component(componentName);
        return this;
    }

    /** Filter by watch names. */
    public ViolationsQueryBuilder watches(String... watchNames) {
        filterBuilder.watchNames(List.of(watchNames));
        return this;
    }

    /** Filter by policy names. */
    public ViolationsQueryBuilder policies(String... policyNames) {
        filterBuilder.policyNames(List.of(policyNames));
        return this;
    }

    /** Fetch a single page. pageNum is 1-based. */
    public ViolationsResponse fetchPage(int pageNum, int pageSize) {
        return api.getViolations(filterBuilder.build(), projectKey, pageNum, pageSize);
    }

    /** Fetch all violations across all pages (auto-paginated). */
    public List<Violation> fetchAll() {
        return fetchAll(DEFAULT_PAGE_SIZE);
    }

    /** Fetch all violations with a custom page size. */
    public List<Violation> fetchAll(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be positive: " + pageSize);
        }
        List<Violation> all = new ArrayList<>();
        int page = 1;
        while (true) {
            ViolationsResponse resp = fetchPage(page, pageSize);
            all.addAll(resp.violations());
            if (!resp.hasMore(page, pageSize)) break;
            page++;
        }
        return List.copyOf(all);
    }
}
