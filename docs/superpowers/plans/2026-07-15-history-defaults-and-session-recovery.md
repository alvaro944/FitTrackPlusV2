# Plan: Historial por rutina activa, select-all en historial, recuperar sesiones incompletas

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `develop`** (no desde `main` ni `new-desing`). Commits separados por mejora, conventional commits, **sin Co-Authored-By**. Verifica local (`test` + `build`) antes de cada commit; NO lances el emulador (eso lo hace el dueño). No commits de WIP.

**Rama:** `feature/history-defaults-and-session-recovery` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-07-15-history-defaults-and-session-recovery.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

---

## Tarea 0: Preparar rama

- [ ] Confirmar que partes de `develop` actualizada (`git checkout develop && git pull` si aplica)
- [ ] `git checkout -b feature/history-defaults-and-session-recovery`

---

## Tarea 1: Historial arranca filtrado por la rutina activa

**Archivos:**
- `feature/history/HistoryViewModel.kt`
- (posible) `di/` si hay que exponer alguna dependencia nueva
- Test: `app/src/test/.../feature/history/`

- [ ] Inyectar en `HistoryViewModel` la fuente del id de rutina activa (mismo tipo que consume `WorkoutViewModel`: `activeRoutineId` de `UserPreferencesRepository`) y el modo de resolver id→nombre actual de la rutina (reutilizar consulta ya existente en `RoutineRepository`; no inventar una nueva si hay equivalente)
- [ ] En el `init`, observar la rutina activa y resolver su nombre actual; combinar con el stream de historial de forma que funcione en cualquier orden de emisión
- [ ] Añadir bandera interna `routineFilterInitialized` (o `hasUserOverriddenRoutineFilter`): el default solo se aplica una vez, mientras el usuario no toque el selector
- [ ] En `setRoutineFilter(...)`, marcar la bandera para que elegir "Todo" (`null`) NO se vuelva a pisar con la rutina activa en emisiones posteriores
- [ ] Caso `activeRoutineId == null` → default en "Todo" (comportamiento actual)
- [ ] Caso rutina activa sin sesiones → resultado vacío es correcto; confirmar que el empty state existente de `HistoryScreen.kt` se ve con sentido (no crear uno nuevo)
- [ ] Test del estado inicial / filtro por defecto siguiendo el patrón de los tests de `applyHistoryFilters`
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: default history filter to active routine`

---

## Tarea 2: Select-all al enfocar en los campos de edición del historial

**Archivos:**
- `feature/history/HistoryScreen.kt`

- [ ] Sustituir el `OutlinedTextField` de peso (`HistoryScreen.kt:744`) por `FitTrackSelectAllTextField`, manteniendo label/placeholder/singleLine/keyboardOptions/colors actuales
- [ ] Sustituir el `OutlinedTextField` de reps (`HistoryScreen.kt:752`) igual
- [ ] **NO** tocar el dropdown de rutina (`HistoryScreen.kt:322`)
- [ ] Confirmar que los callbacks al ViewModel (`updateSetWeight`/`updateSetReps`) siguen recibiendo `String` (cambio solo de UI)
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: select all text on focus in history set edit fields`

---

## Tarea 3: Guardar y recuperar entrenamientos incompletos

**Archivos (parte A — finalizar con incompletos):**
- `feature/workout/WorkoutViewModel.kt`
- `feature/workout/WorkoutScreen.kt`

- [ ] Mantener el descarte cuando `completedSetCount == 0` (sin cambios)
- [ ] Ajustar `finishDialogText` (`WorkoutScreen.kt:149`): cuando `0 < completedSetCount < totalSetCount`, avisar "Quedan series sin completar. Se guardará como incompleto y podrás recuperarlo desde el historial."; cuando `completedSetCount == totalSetCount`, texto normal de terminar
- [ ] Un solo botón (no crear un segundo botón "guardar")

**Archivos (parte B — LED derivado en historial):**
- `domain/model/HistoryModels.kt`
- `domain/usecase/ObserveWorkoutHistoryUseCase.kt`
- `domain/usecase/GetWorkoutHistoryDetailUseCase.kt`
- `feature/history/HistoryViewModel.kt`
- `feature/history/HistoryScreen.kt`

- [ ] `WorkoutHistorySummary`: añadir `isComplete: Boolean` derivado; calcularlo en `ObserveWorkoutHistoryUseCase.toHistorySummary()` como `sets.all { it.isCompleted }`
- [ ] `HistorySessionUiState` (`HistoryViewModel.kt:350`): añadir `isComplete` y mapearlo en `WorkoutHistorySummary.toUiState()` (`:438`)
- [ ] Arrastrar `isCompleted` al detalle: añadir a `WorkoutHistorySet` (`HistoryModels.kt:34`) y a `HistorySetUiState` (`HistoryViewModel.kt:428`); mapear en `GetWorkoutHistoryDetailUseCase.kt:48-51` y en `WorkoutHistorySet.toUiState()` (`:556`)
- [ ] Lista del historial (`HistoryScreen.kt`, item de sesión): mostrar punto/LED discreto cuando `!isComplete`, con `contentDescription`/semantics ("Entrenamiento incompleto"), colores de `core/design/Theme.kt`
- [ ] Detalle de sesión incompleta: mostrar el estado + botón de recuperar (parte C)

**Archivos (parte C — acción recuperar):**
- `data/local/dao/WorkoutDao.kt`
- `data/repository/WorkoutRepository.kt` + `DefaultWorkoutRepository.kt`
- `domain/usecase/ReopenWorkoutSessionUseCase.kt` (nuevo)
- `feature/history/HistoryViewModel.kt` + `HistoryScreen.kt`
- `core/navigation/` (revisar, no cablear a mano)

- [ ] `WorkoutDao`: `@Query("UPDATE workout_sessions SET finishedAt = NULL WHERE id = :sessionId")`
- [ ] `WorkoutRepository` + impl: `suspend fun reopenSession(sessionId: Long)`
- [ ] `ReopenWorkoutSessionUseCase`: si `getActiveSessionWithExercises()` != null → resultado "bloqueado"; si no → `reopenSession` y "ok"
- [ ] UI: botón "Recuperar entrenamiento" en el detalle de sesión incompleta → invoca el use case; ok → navegar a Entrenar (reutilizar patrón de `core/navigation`); bloqueado → mensaje "Termina primero tu entrenamiento en curso"
- [ ] Confirmar que al reabrir, la sesión sale de historial/stats hasta re-finalizar (consecuencia del invariante, no bug)
- [ ] Test de `ReopenWorkoutSessionUseCase` (guarda de sesión activa) si encaja con el patrón de tests de use cases
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: allow finishing and recovering incomplete workout sessions` (1 o 2 commits a criterio del ejecutor: data/domain separado de UI si ayuda; sin WIP)

---

## Tarea 4: Docs y cierre

- [ ] Actualizar `docs/progress/project-progress.md` y `docs/progress/phase-log.md`: las tres mejoras, verificado con `test`/`build`, pendiente de pasada manual (mejoras 2 y 3 + caveat del LED en datos viejos)
- [ ] Si queda limpio, corregir en `CLAUDE.md` el dato "DB v2" → v3; si no, dejar nota en progress
- [ ] `test` + `build` finales en verde
- [ ] Merge a `develop` cuando compile y tests pasen; `main` solo tras pasada manual del dueño
- [ ] Push y avisar al dueño con la checklist de pasada manual:
  - Historial abre filtrado por la rutina activa; cambiar a "Todo" no se vuelve a pisar
  - Editar reps "8"→"10" en el historial reemplaza (no "810"); ídem peso
  - Finalizar dejando series sin marcar → aviso de incompleto + LED en historial
  - Recuperar desde el detalle → vuelve a Entrenar con la sesión; teniendo ya una activa → mensaje de bloqueo
  - Revisar si el LED ensucia sesiones antiguas (caveat de datos previos a la feature)
