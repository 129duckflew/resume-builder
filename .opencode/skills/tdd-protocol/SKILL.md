---
name: tdd-protocol
description: Agentic TDD Red-Green-Refactor-Verify-Commit protocol. Use when implementing a new feature or fixing a bug to ensure disciplined test-first development with atomic commits.
---

# Agentic TDD Protocol: Red → Green → Refactor → Verify → Commit

## Phase 0: Environment Discovery & Tool Sync
Before mutation, sync with the project's testing capabilities.
- [ ] Locate test suites: `find . -name "*Test*" -o -name "*.test.*" -o -name "test.sh" -o -name "Makefile" | grep -v node_modules | grep -v .mvn`
- [ ] Identify "Run All" command: `cd backend && mvn test` / `cd frontend && npm test`
- [ ] Identify "Run Specific" command pattern: `mvn test -Dtest=XxxTest` / `npm test -- --run src/Xxx.test.ts`
- [ ] Record baseline test count before starting

## Phase 1: RED — Failure Establishment
- [ ] Write a failing test that captures the desired behavior
- [ ] Run the **specific** test to confirm red
- [ ] Capture and diff the error output
- [ ] Verify you can explain **exactly** why it fails and what the expected output is

## Phase 2: GREEN — Minimal Functional Mutation
- [ ] Modify target file(s) with the simplest possible implementation
- [ ] Prefer hardcoded return values or minimal logic to bridge the gap
- [ ] Rerun the **specific** test — it must pass (✅)
- [ ] Do not optimize or clean up yet

## Phase 3: REFACTOR — Structural Refinement
- [ ] Generalize: replace hardcoded logic with proper implementation
- [ ] Align: verify code follows project patterns (layering, naming, REST conventions)
- [ ] Cleanup: remove temporary logs, dead code, debug comments
- [ ] Rerun the **specific** test — must still pass (✅)

## Phase 4: VERIFY — Regression Guard
- [ ] Run the **full** test suite
- [ ] Compare counts against Phase 0 baseline
- [ ] If regressions found, revert to last GREEN state and re-evaluate refactor
- [ ] Zero regressions required to proceed

## Phase 5: ATOMIC COMMIT — State Finalization
- [ ] Update tracking artifacts if any (progress.md)
- [ ] Commit with message format: `feat: <description>` (impl + test in one commit)
- [ ] Ensure commit is functional — every commit must pass the full suite

## Macro Cycle: History & Debt Management
For large-scale shifts (history purge, global refactors):
- [ ] Identify debt: `git rev-list --max-parents=0 HEAD` or `git log --oneline -100`
- [ ] Purge: use `git filter-repo` for global changes
- [ ] Reset: update `.gitignore` to prevent re-introduction
- [ ] Resync: verify HEAD remains compliant with full suite
