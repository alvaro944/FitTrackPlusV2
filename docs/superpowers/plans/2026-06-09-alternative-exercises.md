# Alternative Exercises Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Añadir variantes discretas por ejercicio para usarlas puntualmente en entrenos y gestionarlas desde la edición de rutinas sin contaminar estadísticas.

**Architecture:** Se mantendrá el ejercicio base de la rutina como slot principal y se añadirá una tabla separada de alternativas asociadas. La sesión de entreno seguirá trabajando con snapshots, pero podrá sustituir el ejercicio activo por una alternativa antes de registrar series, manteniendo el histórico separado por variante real.

**Tech Stack:** Kotlin, Room, Jetpack Compose, Material 3, Hilt, JUnit

---

### Task 1: Modelar alternativas en Room y dominio

**Files:**
- Create: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/local/entity/RoutineExerciseAlternativeEntity.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/database/FitTrackPlusDatabase.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/local/entity/WorkoutExerciseEntity.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/local/dao/RoutineDao.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/domain/model/RoutineModels.kt`
- Test: `app/src/test/java/com/alvarocervantes/fittrackplus/data/repository/DefaultRoutineRepositoryTest.kt`

- [ ] **Step 1: Write the failing repository/domain test**
- [ ] **Step 2: Run the focused test to verify the new alternative model is missing**
- [ ] **Step 3: Add the alternative entity, database registration, DAO queries and domain snapshots/drafts**
- [ ] **Step 4: Re-run the focused test and make it pass**

### Task 2: Persist alternatives in routine repository

**Files:**
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/repository/RoutineRepository.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/repository/DefaultRoutineRepository.kt`
- Test: `app/src/test/java/com/alvarocervantes/fittrackplus/data/repository/DefaultRoutineRepositoryTest.kt`

- [ ] **Step 1: Write failing tests for create, read and promote-default flows**
- [ ] **Step 2: Run the focused repository tests and verify failure**
- [ ] **Step 3: Implement repository methods for listing, creating and promoting alternatives**
- [ ] **Step 4: Re-run repository tests and make them pass**

### Task 3: Exponer alternativas en el editor de rutinas

**Files:**
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/routines/RoutinesViewModel.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/routines/RoutinesScreen.kt`
- Test: `app/src/test/java/com/alvarocervantes/fittrackplus/feature/routines/RoutineEditorUiStateTest.kt`
- Test: `app/src/test/java/com/alvarocervantes/fittrackplus/feature/routines/RoutinesViewModelTest.kt`

- [ ] **Step 1: Write failing ViewModel/UI-state tests for opening the alternatives sheet, creating an alternative and setting one as default**
- [ ] **Step 2: Run the focused editor tests and verify failure**
- [ ] **Step 3: Add editor state, events and Compose UI for the discreet alternatives action and bottom sheet**
- [ ] **Step 4: Re-run the focused editor tests and make them pass**

### Task 4: Permitir sustitución puntual en entreno

**Files:**
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/local/dao/WorkoutDao.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/repository/WorkoutRepository.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/repository/DefaultWorkoutRepository.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/workout/WorkoutViewModel.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/workout/WorkoutScreen.kt`
- Test: `app/src/test/java/com/alvarocervantes/fittrackplus/feature/workout/WorkoutViewModelTest.kt`

- [ ] **Step 1: Write failing tests for selecting an alternative before data entry and blocking the action after data entry**
- [ ] **Step 2: Run the focused workout tests and verify failure**
- [ ] **Step 3: Implement session-level substitution, previous-weight refresh and bottom sheet UI in workout**
- [ ] **Step 4: Re-run the focused workout tests and make them pass**

### Task 5: Mantener estadísticas separadas por variante

**Files:**
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/domain/usecase/ObserveWorkoutStatsUseCase.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/repository/DefaultWorkoutRepository.kt`
- Test: `app/src/test/java/com/alvarocervantes/fittrackplus/domain/usecase/ObserveWorkoutStatsUseCaseTest.kt`

- [ ] **Step 1: Write failing stats tests covering barbell vs dumbbell progress separation**
- [ ] **Step 2: Run the focused stats tests and verify failure**
- [ ] **Step 3: Implement grouping by real performed variant while preserving current snapshot behavior**
- [ ] **Step 4: Re-run the focused stats tests and make them pass**

### Task 6: Verificar y documentar cierre de fase

**Files:**
- Modify: `README.md` (only if behavior/user-facing notes need mention)
- Modify: `docs/progress/project-progress.md`
- Modify: `docs/progress/phase-log.md`
- Modify: `docs/methodology/work-methodology/README.md` or relevant file under `docs/methodology/work-methodology/`

- [ ] **Step 1: Run `./gradlew test --no-daemon --console=plain`**
- [ ] **Step 2: Run `./gradlew build --no-daemon --console=plain`**
- [ ] **Step 3: Hacer pasada manual en rutina/entreno/stats**
- [ ] **Step 4: Actualizar progreso, phase log y metodología con aprendizajes de la fase**
