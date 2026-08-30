# Napkin Runbook

## Curation Rules
- Re-prioritize on every read.
- Keep recurring, high-value notes only.
- Max 10 items per category.
- Each item includes date + "Do instead".

## Execution & Validation (Highest Priority)
1. **[2026-06-18] Verify before claiming done**
   Do instead: run the required checks first, then report only what the evidence proves.

## Domain Behavior Guardrails
1. **[2026-06-18] V2 is Android offline-first**
   Do instead: treat `app/src/main/kotlin` as the active codebase and keep Firebase out of the MVP path.
2. **[2026-06-18] Workout history must stay snapshot-based**
   Do instead: preserve immutable session snapshots; never make history depend on mutable routine state.

## Shell & Command Reliability
1. **[2026-06-18] Prefer targeted reads over broad hunting**
   Do instead: inspect the project docs/README first, then only the files needed for the question.

## User Directives
1. **[2026-06-18] No emulator without explicit request**
   Do instead: do not launch an emulator or do manual device passes unless the user asks.
2. **[2026-06-18] Keep phase changes documented**
   Do instead: update project-progress, phase-log, and any reusable methodology notes when a phase closes.
