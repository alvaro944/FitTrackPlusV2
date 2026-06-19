# CLAUDE.md

This file provides guidance to Claude Code when working in this repository.

## Project

FitTrackPlus v2 is a native Android app built with Kotlin, Jetpack Compose, Material 3, Room, Hilt, and DataStore. It is used to build gym routines, log real workouts, and keep a history that stays consistent even when routines change later.

Current branch: `main` (stable, in daily use).

Current status:

- Phase 6 visual pass complete and merged.
- Alternative exercises feature complete and merged (DB v2).
- Next: UX improvements — routine editor, workout entry, progression hints.
- Firebase and sync are still out of scope.

## Roles — regla critica

Este proyecto usa dos agentes con roles separados. La separacion es estricta.

### Claude = planificador y disenador

Claude puede:

- leer codigo y documentacion
- analizar el estado del repo
- proponer cambios y discutir opciones
- escribir specs y planes de tareas para Codex
- revisar codigo que Codex ha escrito

Claude NO puede:

- escribir ni modificar codigo de produccion
- hacer commits (salvo encargo explicito del usuario)
- crear ramas ni hacer push
- ejecutar builds o tests salvo para diagnosticar un problema puntual

### Codex = ejecutor

Codex ejecuta lo que Claude ha especificado:

- lee la spec en `docs/superpowers/specs/` y el plan en `docs/superpowers/plans/`
- crea la rama indicada en el plan
- implementa y prueba localmente (`test` + `build`) — NO lanza el emulador, eso lo hace el usuario
- hace commit solo cuando el codigo esta verificado — nunca commits de WIP
- hace push y avisa al usuario
- cuando el usuario aprueba, hace merge a main y push

La referencia canonica del modelo de colaboracion es `docs/methodology/work-methodology/agent-collaboration.md`.

## Multi-platform handoff

This repo may be reviewed from more than one editor or AI platform. To avoid overlap:

- Only one platform should execute code or shared docs changes in each iteration.
- Other platforms may analyze, design, review, or leave backlog notes in their own files.
- Before resuming work from another platform, re-check `git status`, re-read the live docs, and reopen the concrete files being touched.
- Treat files such as `docs/design/mejoras-claude.md` as proposal backlog unless the user explicitly promotes items into execution.

## Required reading before editing

Per `AGENTS.md`, always re-read these before touching code:

1. `README.md`
2. `docs/methodology/project-methodology/README.md`
3. `docs/planning/project-plan.md`
4. `docs/progress/project-progress.md`
5. `docs/progress/phase-log.md`
6. `docs/architecture/overview.md`
7. `docs/methodology/work-methodology/README.md`
8. The concrete area being modified

When present, also read:

- `CLAUDE.md`
- `docs/design/mejoras-claude.md`
- any external design/backlog note that is shaping the current iteration

## Build and test

Use the Gradle wrapper:

```bash
./gradlew.bat test
./gradlew.bat build
./gradlew.bat connectedAndroidTest
```

Run a single JVM test class:

```bash
./gradlew.bat :app:test --tests "com.alvarocervantes.fittrackplus.<FqcnOrPattern>"
```

Minimum verification for any code change:

- `test`
- `build`

For UI changes, also do a manual pass in emulator/device when possible. `adb` is not currently on PATH in this workspace, so manual validation may remain pending in `docs/progress/project-progress.md`.

## Source layout rule

The v2 code lives in `app/src/main/kotlin/`.

The Gradle source set points at `src/main/kotlin`, so anything under `app/src/main/java/` is legacy local code and intentionally outside the v2 build. Do not edit it and do not reintroduce XML layouts, fragments, or menus from there.

The stray top-level `MainActivity.kt` at repo root is also legacy residue, not the real app entry point.

## Architecture

Package root: `com.alvarocervantes.fittrackplus`

Feature-first, pragmatic modern Android architecture:

- `feature/{home,routines,workout,history,stats,settings}`: Compose screens and ViewModels
- `domain/{model,usecase}`: domain models and selected use cases
- `data/{local,preferences,repository}`: Room, DataStore, repositories
- `core/{database,datastore,design,navigation,util}`: shared infrastructure
- `di/`: Hilt modules

Compose paints state and emits events only. Business rules stay outside UI.

### Snapshot invariant

This is the load-bearing rule of the app and must not be broken:

- Routines are editable.
- Workout history stores snapshots of routine, day, exercise, and target reps at session start.
- History detail reads from snapshots, never from live routines.
- Stats are computed from finished sessions plus snapshots.
- Stats group exercises by normalized snapshot name.
- Open sessions must not appear in History or Stats.

## Persistence

Room is the local source of truth.

Schemas are exported to `app/schemas/`. Commit schema changes when entities change.

DataStore stores only small preferences such as active routine id, weight unit, and simple settings.

## Debug seeding

A demo Push/Pull/Legs dataset is seeded only when:

- the build is debuggable
- the database is empty

Do not rely on it for release flows and do not let it leak into tests or non-debug contexts.

## Workflow y ramas

El proyecto ya no trabaja en fases cerradas. Ahora trabaja en grupos de mejoras:

- cada grupo de mejoras relacionadas va en una sola rama: `codex/<nombre-del-grupo>`
- no se crea una rama por cada pequeño cambio
- todo el trabajo del grupo se hace en esa rama hasta que esta completo
- Codex verifica localmente antes de hacer ningun commit
- se hace commit limpio cuando el grupo esta verificado, no antes
- se hace push y merge a main cuando todo el grupo esta done

Ejemplo de rama correcta: `codex/ux-improvements`
Ejemplo de rama incorrecta: `codex/fix-button-color`, `codex/minor-tweak`

Una mejora esta done cuando: compila, tests pasan, flujo manual verificado en emulador, docs alineados.

## Phase closeout

On phase close, update:

1. `docs/progress/project-progress.md`
2. `docs/progress/phase-log.md`
3. `docs/methodology/work-methodology/`
4. `README.md` or `AGENTS.md` if process changed
5. `docs/planning/roadmap-2.1.md` for deferred ideas

Then tell the user:

- what was done
- what was verified
- what remains pending

## Skills disponibles

Las siguientes skills de Claude Code estan instaladas a nivel usuario y disponibles en este proyecto:

| Skill | Uso principal |
|---|---|
| `humanizer` | Naturalizar textos de UI u onboarding |
| `napkin` | Diagramas rapidos de arquitectura o flujos |
| `skill-forge` | Crear nuevas skills personalizadas |
| `caveman` / `caveman-commit` / `caveman-review` / `caveman-compress` | Simplificar explicaciones, commits y revisiones |
| `token-optimizer` / `token-coach` / `token-dashboard` / `fleet-auditor` | Optimizar uso de tokens en sesiones largas |

Referencia completa con criterios de uso en `docs/methodology/work-methodology/available-skills.md`.

## Language and tone

Project docs and commit messages use Spanish without accents when possible. Match that style in `docs/`. Keep code identifiers in English.
