# xray-java-client

![CI](https://github.com/psalg-dev/xray-java-client/actions/workflows/ci.yml/badge.svg)

A lightweight Java client for the [JFrog Xray REST API](https://jfrog.com/help/r/jfrog-rest-apis/xray-rest-apis).
It provides a fluent, builder-style API for querying Xray policy
**violations**, **watches**, **builds**, and **artifact summaries**, with
automatic pagination and Jackson-based JSON mapping.

Requires Java 25.

## Installation

This library is published to GitHub Packages. GitHub Packages requires
authentication even for public repositories, so add a `<server>` entry with a
[personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)
that has the `read:packages` scope to your `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

### Maven

Add the repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/psalg-dev/xray-java-client</url>
    </repository>
</repositories>

<dependency>
    <groupId>dev.psalg</groupId>
    <artifactId>xray-java-client</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/psalg-dev/xray-java-client")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("dev.psalg:xray-java-client:1.0.1")
}
```

## Usage

```java
XrayClient client = XrayClient.builder()
        .baseUrl("https://my-artifactory.example.com")
        .basicAuth("user", "password")
        .build();

List<Violation> violations = client.violations()
        .forProject("my-project")
        .withSeverities(Severity.CRITICAL, Severity.HIGH)
        .activeOnly()
        .fetchAll();

for (Violation violation : violations) {
    System.out.println(violation.severity() + ": " + violation.description());
}
```

`fetchAll()` transparently pages through `POST /xray/api/v1/violations` and
returns every matching violation. Use `fetchPage(pageNum, pageSize)` if you
want to handle pagination yourself.

### Authentication

In addition to `basicAuth(user, password)`, the builder also supports:

```java
XrayClient.builder()
        .baseUrl("https://my-artifactory.example.com")
        .tokenAuth("access-token")   // Bearer token
        .build();

XrayClient.builder()
        .baseUrl("https://my-artifactory.example.com")
        .apiKey("artifactory-api-key")
        .build();
```

## Other query builders

```java
// Watches
List<Watch> watches = client.watches().list();

// Builds
List<IndexedBuild> builds = client.builds().list();
BuildSummary summary = client.builds().summary("my-build", "42");

// Artifact summaries
List<ArtifactSummary> artifacts = client.artifacts()
        .summaryByPath("libs-release-local/com/example/my-app/1.0.0/my-app-1.0.0.jar");
```

## Building from source

```bash
mvn verify
```

The build requires `--enable-preview` (already configured in `pom.xml`) and
JDK 25.
