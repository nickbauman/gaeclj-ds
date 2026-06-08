# gaeclj-ds Modernization Plan

## Goal

Modernize gaeclj-ds from a legacy App Engine runtime/dependency stack to the latest supported Google Cloud Datastore tooling while preserving existing DSL behavior and test coverage.

## Current Baseline

- Test status is green.
- Baseline command: `lein test`
- Baseline result: 9 tests, 48 assertions, 0 failures, 0 errors.

## Guiding Principles

1. Preserve public DSL behavior first; refactor internals second.
2. Upgrade in small, reversible batches.
3. Keep tests passing after every batch.
4. Add tests before changing behavior.
5. Prefer official, current Google Cloud libraries over deprecated App Engine APIs.

## High-Level Phases

## Phase 1: Lock Baseline and Branch Strategy

1. Create a dedicated branch for modernization work.
2. Freeze current behavior by documenting expected outputs and known edge cases.
3. Add a CI workflow (if missing) that runs `lein test` on every push.
4. Capture dependency and runtime inventory from `project.clj`.

Exit criteria:
- Branch created.
- CI runs tests automatically.
- Baseline behavior documented.

## Phase 2: Dependency Audit and Target Stack Selection

1. Audit all dependencies in `project.clj` and classify them:
   - App Engine legacy APIs
   - Google API client dependencies
   - Clojure/core utilities
2. Select target libraries for migration:
   - Move away from `com.google.appengine/appengine-api-1.0-sdk` where possible.
   - Adopt modern Google Cloud Datastore client libraries.
3. Define compatibility target:
   - Java version target: support Java 21 and maintain Java 17 compatibility.
   - Clojure compatibility target.
4. Produce a migration mapping table (current library -> replacement library).

Exit criteria:
- Approved dependency mapping table.
- Target Java/runtime version finalized.

## Phase 3: Build and Runtime Modernization

1. Update Java compilation target and runtime assumptions in `project.clj`.
2. Upgrade non-breaking dependencies first (logging, utility libs).
3. Introduce replacement datastore client dependencies.
4. Remove/phase out deprecated App Engine-specific dependencies.
5. Keep feature flags or adapter shims while transitioning internals.

Exit criteria:
- Project builds with modern runtime targets.
- Tests still pass with transitional adapter layer.

## Phase 4: Datastore API Migration (Core Work)

1. Identify all direct usages of legacy App Engine datastore classes in `src/gaeclj/ds.clj` and related files.
2. Create an internal adapter boundary:
   - Existing DSL surface remains unchanged.
   - New datastore implementation sits behind the boundary.
3. Migrate operations incrementally:
   - Entity create/save/update
   - Get by key
   - Delete
   - Query filters and compound predicates
   - Sort/order
   - Ancestor queries
   - Transactions (including cross-group behavior)
4. Verify each migrated operation using existing tests plus focused new tests.

Exit criteria:
- No direct dependency on legacy App Engine datastore API in core path.
- Existing DSL API preserved.

## Phase 5: Test Hardening and Regression Safety

1. Expand tests for edge cases currently under-covered:
   - Validation failures
   - Transaction semantics and error paths
   - Query combination behavior (`and`, `or`, inequality rules)
   - Repeated property behavior
2. Add characterization tests for current DSL behavior before any behavior changes.
3. Add smoke tests for representative user flows from README examples.

Exit criteria:
- Coverage stable or improved.
- Critical datastore behaviors explicitly tested.

## Phase 6: Documentation and Release Preparation

1. Update README with modern runtime/dependency requirements.
2. Add migration notes for existing users.
3. Bump version with clear changelog entries.
4. Publish release candidate and run downstream validation.

Exit criteria:
- README and changelog updated.
- Release candidate validated.

## Phase 7: Clojure Template Packaging

1. Define template structure and generated project layout.
2. Provide sensible defaults for datastore configuration and testing.
3. Include a minimal sample entity and query usage.
4. Add template validation checks (generated project compiles/tests).

Exit criteria:
- Template generates a runnable project.
- Generated project tests pass out of the box.

## Risk Register

1. API semantic mismatches between legacy App Engine datastore and modern client libraries.
2. Transaction behavior differences, especially cross-group scenarios.
3. Query edge cases changing silently.
4. Increased complexity from dual-stack transition period.

Mitigation:
- Keep adapter boundary small and well-tested.
- Migrate one operation class at a time.
- Use characterization tests to catch semantic drift.

## Execution Checklist (Initial)

1. Create modernization branch.
2. Add CI test workflow.
3. Build dependency mapping table.
4. Choose target Java/runtime versions.
5. Implement datastore adapter boundary.
6. Migrate create/get/delete operations.
7. Migrate query and transaction operations.
8. Expand regression tests.
9. Update docs and publish release.
10. Package into Clojure template.

## Immediate Next Action

Run this sequence first:

1. Create branch: `git checkout -b chore/modernize-datastore-runtime`
2. Confirm baseline: `lein test`
3. Start dependency mapping from `project.clj` into a new migration checklist document.
