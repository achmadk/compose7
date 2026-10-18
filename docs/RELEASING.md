# Releasing

Releases publish every library module to **Maven Central** from a Git tag. No
local credentials are needed to cut a release; nothing is published from a
maintainer's machine.

## Versioning

`VERSION_NAME` in [gradle.properties](../gradle.properties) is the default for
local builds and snapshots. A release overrides it with the tag name, so:

| Tag | Published version |
|---|---|
| `v0.2.0` | `0.2.0` |
| `v0.2.0-beta01` | `0.2.0-beta01` |

Modules release together today. If a single module needs its own track later,
add module-specific coordinates before the first split — consumers should never
see a version that silently skipped a target.

## Preflight

1. CI is green on `dev`/`main`, including both Compose Multiplatform matrix
   entries (`1.12.2` and the previous stable release).
2. `CHANGELOG.md` has an entry for the version, with the compare link updated.
3. Golden images match (`./gradlew desktopTest`), with any intentional visual
   change committed alongside its regenerated baselines.
4. `./gradlew publishToMavenLocal -PVERSION_NAME=<version>` succeeds and the
   artifacts appear under `~/.m2/repository/io/github/compose7/`.
5. A dry run of the POM is sane: name, description, license (MIT), developers,
   SCM URL, and no `SNAPSHOT` in the version.

## Cutting a release

```bash
git tag v0.2.0
git push origin v0.2.0
```

The tag triggers [`.github/workflows/release.yml`](../.github/workflows/release.yml):

1. resolve the version from the tag (and reject anything that is not
   semver-shaped);
2. build and test every module that ships;
3. run `publishToMavenCentral`, which uploads signed artifacts through the
   Central Portal and releases the deployment.

`workflow_dispatch` with a `version` input does the same thing without a tag —
use it only to recover from a failed tag run, never to publish a version that
has no matching tag in history.

### Required repository secrets

| Secret | Purpose |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Central Portal user token name |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal user token |
| `SIGNING_IN_MEMORY_KEY` | ASCII-armored GPG private key |
| `SIGNING_KEY_ID` | Key id (last 8/16 hex chars) |
| `SIGNING_KEY_PASSWORD` | Key passphrase |

The publish step maps them to the Gradle properties
`mavenCentralUsername`, `mavenCentralPassword`, `signingInMemoryKey`,
`signingInMemoryKeyId`, and `signingInMemoryKeyPassword`. Signing stays
optional for local builds (the convention plugin only signs when a key is
present), so `publishToMavenLocal` keeps working on a clean machine.

The `io.github.compose7` group must be verified in the Central Portal before
the first publish; verification is a one-time namespace check, not a per-release
step.

## After publishing

- Confirm the version appears on Maven Central (indexing can lag ~15 minutes).
- Create the GitHub release from the same tag and paste the CHANGELOG entry.
- If the deployment is still open in the Central Portal, it can be dropped;
  once released, a bad version must be superseded by a new patch — Maven
  Central artifacts are immutable.

## Handing over

This document is also the succession path (PRD §18). A successor needs:

1. **Repository admin**: transfer the repository to an organization (the
   planned transition trigger is securing sponsorship/funding) and add at least
   two maintainers with publish access.
2. **Secrets**: rotate the five secrets above when access changes. The GPG key
   is the only irreplaceable item — export it to the new maintainers and keep a
   password-manager copy. Losing it means new artifacts must be signed with a
   new key.
3. **Nothing swallowed by a machine**: because every release is reproducible
   from a tag with `./gradlew publishToMavenCentral`, a successor does not need
   anyone's laptop state.

If the project ever goes dormant, the MIT license and the tag history mean a
fork can publish the same code under a new coordinate — the failure mode
`compose-cupertino` hit (PRD §2.3) is at least recoverable rather than fatal.
