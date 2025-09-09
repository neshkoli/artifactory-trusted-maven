## Architecture Overview

This repository contains the JFrog Artifactory Maven plugin. The plugin integrates with the Maven lifecycle to:

- Resolve project and plugin dependencies from Artifactory
- Capture build information (modules, artifacts, dependencies, environment) during the build
- Deploy artifacts to Artifactory and publish the resulting Build Info

The plugin’s single goal `publish` is bound to the `validate` phase to initialize early. It replaces default deploy behavior and wires custom resolution and deployment.


### High-Level Flow

1. Maven starts with the `validate` phase and executes the plugin goal `publish`.
2. The plugin:
   - Replaces `{{VAR|...|"default"}}` placeholders in configuration
   - Configures proxy from Maven `settings.xml` if none provided
   - Enforces resolution repositories based on `<resolver>` configuration
   - Skips Maven’s default deploy and registers a `BuildInfoRecorder` as the Maven `ExecutionListener`
3. During the build, the `BuildInfoRecorder`:
   - Tracks modules, artifacts, and dependencies
   - Applies include/exclude patterns and checksum calculation
   - Collects build-time dependencies via a `RepositoryListener` when `recordAllDependencies=true`
4. At session end, the `BuildInfoRecorder` builds a `BuildInfo` and invokes `BuildDeployer` to:
   - Deploy artifacts (parallelized per module)
   - Publish Build Info and (optionally) apply retention rules


### Key Components

- `org.jfrog.buildinfo.ArtifactoryMojo`
  - Entry point (`@Mojo(name = "publish", defaultPhase = VALIDATE)`)
  - Handles variable replacement, proxy setup, resolution enforcement, deployment enforcement
  - Registers `BuildInfoRecorder` as `ExecutionListener` and wires `RepositoryListener`

- `org.jfrog.buildinfo.deployment.BuildInfoRecorder`
  - Implements `ExecutionListener` and `BuildInfoExtractor<ExecutionEvent>`
  - Accumulates module artifacts and dependencies, filters by patterns
  - Determines target repository (snapshots vs releases)
  - On `sessionEnded`, extracts `BuildInfo` and triggers deployment

- `org.jfrog.buildinfo.deployment.BuildDeployer`
  - Uses `ArtifactoryManager` to deploy artifacts and publish Build Info
  - Parallel deploy per module via `ModuleParallelDeployHelper`
  - Honors flags: `publishArtifacts`, `publishBuildInfo`, `insecureTls`

- `org.jfrog.buildinfo.resolution.ResolutionRepoHelper`
  - Builds Maven `ArtifactRepository` instances from `<resolver>` config
  - Merges Maven system/user properties into Artifactory client config

- `org.jfrog.buildinfo.resolution.RepositoryListener`
  - Aether listener that captures resolved artifacts and forwards them to `BuildInfoRecorder`
  - Differentiates scopes (plugin vs project) via request context

- `org.jfrog.buildinfo.Config`
  - Thin wrapper mapping POM `<configuration>` sections to `ArtifactoryClientConfiguration` handlers
  - Sections: `<artifactory>`, `<resolver>`, `<publisher>`, `<buildInfo>`, `<proxy>`

- `org.jfrog.buildinfo.utils.Utils`
  - Helper utilities for checksum calculation, artifact name/path building, parsing `{{...}}` placeholders, Maven/plugin version discovery

- `org.jfrog.buildinfo.types.ModuleArtifacts`
  - Thread-local container for accumulating module artifacts and dependencies

- `src/main/resources/META-INF/plexus/components.xml`
  - Lifecycle mapping tying Maven `deploy` phase to `org.jfrog.buildinfo:artifactory-maven-plugin:publish`


### Configuration Surface (POM)

Under the plugin’s `<configuration>` you can set:
- `<artifactory>`: env var inclusion rules, timeout
- `<buildInfo>`: build metadata (name, number, URL, project, retention)
- `<deployProperties>`: matrix params attached to deployed artifacts
- `<publisher>`: server URL, credentials, repo keys (release/snapshot), include/exclude patterns, publish flags, retention, `recordAllDependencies`, checksum deploy threshold
- `<resolver>`: resolution repos (release/snapshot) and credentials
- `<proxy>`: host, port, credentials

Any value may include placeholders like `{{ENV|SYS|"default"}}`, resolved at runtime.


### Execution Model

- Default deploy is disabled via `maven.deploy.skip=true`
- Artifacts to deploy are determined per module and filtered by patterns
- Target repository is chosen by whether the artifact path contains `-SNAPSHOT`
- Checksums (md5/sha1) are calculated prior to deployment
- Build Info is enriched with filtered environment variables and system properties


### Tests

- Unit/integration tests under `src/test/java` validate:
  - Placeholder parsing and configuration wiring
  - Resolution override and repository auth
  - Recording artifacts/dependencies and correct deploy target selection
  - End-to-end plugin behavior against mock server projects


### Extension Points & Improvement Ideas

- Resolution:
  - Support additional repository layouts or custom resolution strategies
  - Pluggable authentication (e.g., OAuth, access tokens via env/providers)

- Deployment:
  - Configurable retry/backoff and per-artifact error handling
  - Advanced checksum deploy heuristics and resumable uploads
  - Fine-grained parallelism tuning and batching

- Build Info:
  - Additional metadata sources (SCM details, CI metadata, git commit SHA)
  - Custom property providers, uniform property precedence policy

- Configuration:
  - Stronger validation and user-facing error messages
  - Deeper secrets redaction and structured logging

- Observability:
  - Structured logs (JSON) and correlation IDs
  - Verbose/debug flags with consistent prefixes and summaries

- Compatibility:
  - Broadened Maven version matrix and Java compatibility checks at startup


### Directory Structure

- `src/main/java/org/jfrog/buildinfo/` — Mojo and configuration wrappers
- `src/main/java/org/jfrog/buildinfo/deployment/` — Build recording and deploy logic
- `src/main/java/org/jfrog/buildinfo/resolution/` — Resolution helper and repository listener
- `src/main/java/org/jfrog/buildinfo/types/` — Small types (e.g., `ModuleArtifacts`)
- `src/main/java/org/jfrog/buildinfo/utils/` — Shared utilities
- `src/main/resources/META-INF/plexus/components.xml` — lifecycle mapping
- `src/test/java/...` — unit and integration tests


### How It Hooks Into Maven

- The `@Mojo(name = "publish", defaultPhase = VALIDATE)` runs early and registers an `ExecutionListener`.
- `components.xml` maps the Maven `deploy` phase to call this plugin’s `publish` goal, ensuring custom deploy handling.


### Quick Start for Contributors

- Build/test: `mvn -U -B -e -ntp -DskipITs=false verify`
- Try locally: Use the README’s example, set `<publisher>` and run `mvn deploy`
- Common entry points to explore:
  - `ArtifactoryMojo.execute()`
  - `BuildInfoRecorder.projectSucceeded()` / `sessionEnded()`
  - `BuildDeployer.deploy()`
  - `ResolutionRepoHelper.getResolutionRepositories()`
