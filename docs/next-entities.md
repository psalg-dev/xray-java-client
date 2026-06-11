# Next Entities to Support

Survey of JFrog Xray REST API entities not yet covered by this client (current
coverage: Violations, Watches, Builds, Artifacts summary). Ranked by expected
real-world value for security automation/reporting use cases. Each entity
follows the existing `internal.XxxApi` + `api.XxxQueryBuilder` + `model.xxx`
package pattern.

## 1. Reports (Vulnerabilities / Licenses / Violations / Operational Risk)

**What it is:** Xray's async report engine. A report is requested with a name,
scope (repositories, builds, release bundles, projects) and filters, runs in
the background, and is then polled and downloaded once complete.

**Key endpoints:**
- `POST /xray/api/v1/reports/vulnerabilities` — start a vulnerabilities report
  (also `/reports/licenses`, `/reports/violations`, `/reports/operational_risk`)
- `GET /xray/api/v1/reports?direction=...&page_num=...&num_of_rows=...` — list
  reports and check `status` (`in progress` / `completed` / `failed`)
- `POST /xray/api/v1/reports/export/{report_id}` — download content as
  PDF, CSV, or JSON
- `DELETE /xray/api/v1/reports/{report_id}` — clean up

**Why useful:** This is the mechanism behind scheduled compliance/audit
reporting (e.g. "monthly critical-vulnerability report for repo X"). A
`ReportsQueryBuilder` that wraps generate -> poll -> download -> delete into a
single `fetchAll()`/`download()` call would be the highest-value addition for
the "reporting" half of this client.

**Complexity: Complex.** Async polling loop, four report types each with their
own filter/scope schema, and a multi-step lifecycle (generate, status,
content, delete). Needs its own `model.reports` package and a dedicated
`ReportsApi`/`ReportsQueryBuilder`.

## 2. Ignore Rules

**What it is:** Rules that suppress specific violations (by CVE, license,
component, artifact, build, watch, etc.), optionally with an expiry date.
Available since Xray 3.11.

**Key endpoints:**
- `GET /xray/api/v1/ignore_rules` — list/filter ignore rules (by CVE,
  vulnerability ID, license, policy, watch, component, build, `projectKey`,
  plus `order_by`/`direction`/`page_num`/`num_of_rows` paging)
- `GET /xray/api/v1/ignore_rules/{ignore_rule_id}` — get a single rule
- `POST /xray/api/v1/ignore_rules` — create a rule (`ignore_filters` +
  optional `expires_at`, RFC 3339)
- `DELETE /xray/api/v1/ignore_rules/{ignore_rule_id}` — remove a rule

**Why useful:** Lets automation manage policy exceptions programmatically —
audit which ignore rules exist and whether they have an expiry, flag
"permanent" exceptions for security review, or bulk-create scoped/expiring
exceptions during onboarding. Pairs naturally with the existing Violations API
(an ignored violation simply stops appearing there).

**Complexity: Medium.** Straightforward CRUD + list/filter, but the
`ignore_filters`/scope object is a fairly wide nested DTO (mirrors the
Violations filter shape already modeled in `ViolationFilter`).

## 3. Search Resources by Vulnerability / CVE

**What it is:** Given one or more CVE IDs (or a vulnerability/component
search), returns every artifact, build, or release bundle in the system that
contains the affected component.

**Key endpoints:**
- `POST /xray/api/v1/component/searchByCves` — find components/artifacts
  impacted by a list of CVEs
- `POST /xray/api/v1/vulnerabilities` — search vulnerabilities by component
  name/version (the "raw" CVE data behind a violation, independent of any
  watch/policy)

**Why useful:** This is the "Log4Shell moment" query — when a new CVE drops,
teams need an immediate answer to "which of our artifacts/builds are
affected?" across the whole instance, not just within a configured watch. High
value for incident response and ad-hoc reporting.

**Complexity: Simple–Medium.** Single POST with a small request body
(`{"cves": [...]}` or `{"component_name": ..., "component_version": ...}`) and
a response shape similar to the existing `Violation`/`ImpactedArtifact`
models — likely reuses `model.common.Cve` and `ImpactedArtifact`.

## 4. Policies

**What it is:** The security/license/operational-risk policies that watches
enforce — each policy is a named set of rules (criteria + actions, e.g.
"CVSS >= 7 -> fail build").

**Key endpoints:**
- `GET /xray/api/v2/policies` — list all policies
- `GET /xray/api/v2/policies/{policy_name}` — get one policy
- `POST /xray/api/v1/policies` — create (`name`, `type`, `description`,
  `rules[]`, optional `projectKey`)
- `PUT /xray/api/v1/policies/{policy_name}` — update
- `DELETE /xray/api/v1/policies/{policy_name}` — delete

**Why useful:** Enables policy-as-code workflows — diffing policy
configuration across projects/environments, auditing for drift, or
provisioning a standard set of security/license policies for new projects via
automation instead of the UI.

**Complexity: Medium.** Full CRUD, and the `rules[].criteria`/`actions` object
is a moderately complex nested DTO, but no async/pagination concerns.

## 5. SBOM / Component Export (CycloneDX)

**What it is:** Exports a CycloneDX (or SPDX) Software Bill of Materials for a
build, release bundle, or repository path, optionally including vulnerability
and license data (VEX).

**Key endpoints:**
- `POST /xray/api/v2/component/exportDetails` — export details for one or
  more components/artifacts; `cyclonedx: true` + `cyclonedx_format: "json" |
  "xml"` controls SBOM output

**Why useful:** Increasingly required for supply-chain compliance (SLSA,
Executive Order 14028, customer security questionnaires). A
`sbomFor(buildName, buildNumber)` convenience method would let consumers pull
a CycloneDX document straight into their reporting pipeline.

**Complexity: Medium.** Single POST, but the response is a large nested
CycloneDX document — modeling it fully is a bigger lift than the other
entities, though a "pass-through as JSON/raw string" first cut would be
simple.

---

## Suggested order of implementation

1. **Ignore Rules** — smallest, self-contained CRUD; immediately useful
   alongside the existing Violations API.
2. **Search by CVE** — simple POST, very high "wow factor" for incident
   response, reuses existing `Cve`/`ImpactedArtifact` models.
3. **Policies** — moderate CRUD, complements Watches (already supported).
4. **Reports** — highest value but highest complexity; worth tackling once
   the simpler entities have validated the async-friendly patterns needed
   (polling helpers, file/byte-stream responses for export).
5. **SBOM Export** — can start as a thin "raw JSON passthrough" once Reports
   groundwork (export/download handling) exists.
