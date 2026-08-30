# Plan: Corregir perdida y corrupcion de datos (P0)

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `develop`** — las rondas 1+2 de unificacion de design system y `structured-target-reps` (Room v5) ya estan mergeadas ahi, verificado. Commits separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica local (`test` + `build`) antes de cada commit; NO lances el emulador. No commits de WIP.

**Rama:** `fix/data-loss` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-08-29-fix-data-loss.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

**Esto es la maxima prioridad del proyecto ahora mismo.** Son bugs que borran o corrompen datos de entrenamientos reales, hoy. Si al implementar cualquier tarea descubres que el problema es mas profundo de lo que dice la spec (por ejemplo, que no hay forma de distinguir un ejercicio de peso corporal en el dominio), **para y avisa** en vez de improvisar una solucion a medias.

---

## Tarea 0: Preparar rama

- [ ] Confirmar que `develop` tiene las rondas 1+2 y `structured-target-reps` (Room v5) mergeadas
- [ ] `git checkout develop && git pull`
- [ ] `git checkout -b fix/data-loss`

---

## Tarea 1 (R1): Sesiones de peso corporal no se borran

**Archivos:** `feature/workout/WorkoutViewModel.kt:982-993, 409-422`

- [ ] Investigar si el dominio ya distingue ejercicios de peso corporal (categoria, flag, o inferencia). Si no existe ninguna señal, documentar la decision de aceptar `reps > 0` como suficiente sin exigir peso
- [ ] Modificar `isWorkoutSetCompleted` para que una serie con `reps > 0` y peso vacio/0 cuente como completada
- [ ] Verificar que `finishWorkout`/`shouldDiscardSession` ya no borra sesiones que tengan series con reps registradas
- [ ] Test: sesion con 3 series de dominadas (reps 8,8,6, sin peso) se guarda al finalizar, no se descarta
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: count bodyweight sets as completed without requiring weight`

---

## Tarea 2 (R2): Las notas no se seleccionan enteras al tocar

**Archivos:** `feature/routines/RoutinesScreen.kt:1278`, `feature/workout/WorkoutScreen.kt:962`

- [ ] Añadir `selectAllOnFocus = false` a los dos campos de notas
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: stop notes fields from selecting all text on focus`

---

## Tarea 3 (R3): Historial deja de escribir en cada tecla

**Archivos:** `feature/history/HistoryViewModel.kt:272-298,215`, `feature/history/HistoryScreen.kt:351-361`

- [ ] Cambiar el flujo de edicion para que los cambios de peso/reps se acumulen en estado local del editor y solo se persistan al pulsar "Guardar"
- [ ] "Descartar cambios" no debe escribir nada en BD (ni siquiera para deshacer una escritura previa, porque ya no habra escrituras previas)
- [ ] Corregir el orden/posicion de los botones de confirmar/descartar segun la convencion del resto de la app
- [ ] Test: editar peso de una serie, pulsar "Descartar" → la BD conserva el valor original
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: buffer history set edits until save instead of writing per keystroke`

---

## Tarea 4 (R4): Cerrar el dialogo de alternativas sin pulsar Cancelar tambien revierte

**Archivos:** `feature/routines/RoutinesScreen.kt:1013-1022`

- [ ] En el `onDismiss` del `FitTrackDialog` completo, si `editingAlternativeIndex != null`, llamar a `cancelInlineAlternativeEdit` (no `finishInlineAlternativeEdit`) antes de cerrar
- [ ] Test/verificacion manual: editar el nombre de una alternativa, cerrar con la X sin pulsar Cancelar → el nombre vuelve al valor guardado
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: revert unsaved alternative edits when dialog is dismissed`

---

## Tarea 5 (R5): Editar una serie completada limpia su marca de PR

**Archivos:** `feature/workout/WorkoutViewModel.kt:996-1001, 504-513`

- [ ] `updateWorkoutSetWeightInput`/`updateWorkoutSetRepsInput`: añadir `prType = null` junto a `isCompleted = false`
- [ ] Test: completar una serie que es PR (prCount sube a 1), editarla, volver a completarla con el mismo valor → prCount solo sube si `detectPersonalRecord` la detecta de nuevo como PR, sin duplicar el conteo de antes
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: clear PR flag when a completed set is edited`

---

## Tarea 6 (R6): Guardar rutina no se puede disparar dos veces

**Archivos:** `feature/routines/RoutinesScreen.kt:148-154`, `feature/routines/RoutinesViewModel.kt:359-361`

- [ ] FAB de guardar: `enabled = !state.isSaving` (o el nombre real del flag)
- [ ] `saveEditor()`: guarda temprana `if (_uiState.value.isSaving) return`, ademas de `editor.canSave`
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: prevent duplicate routine saves from rapid double-tap`

---

## Tarea 7 (R7): El estado de los dialogos de rutina sigue al elemento, no a la posicion

**Archivos:** `feature/routines/RoutinesScreen.kt:694,910,618`

- [ ] Añadir `key` estable (id del dia/ejercicio, no el indice) a los bucles `itemsIndexed`/`forEachIndexed` señalados
- [ ] Verificar manualmente: abrir confirmacion de borrado de un ejercicio, reordenarlo, comprobar que la confirmacion sigue apuntando al ejercicio correcto (o se cierra, pero nunca se aplica a otro)
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: key routine day/exercise items by id to stop dialog state drift on reorder`

---

## Tarea 8 (R8): Eliminar un dia pide confirmacion

**Archivos:** `feature/routines/RoutinesViewModel.kt:161`, `feature/routines/RoutinesScreen.kt`

- [ ] Añadir `FitTrackConfirmDialog` (destructivo) antes de `removeDay`, mismo patron que eliminar ejercicio
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: require confirmation before removing a routine day`

---

## Tarea 9 (R9): Confirmar antes de eliminar una alternativa — recortada durante la implementacion

**Decision (2026-08-29):** construir "Deshacer" real (snapshot + canal de eventos ViewModel→SnackbarHost) es tamaño de feature; no existe hoy ninguna infraestructura de snapshot/transporte para ello. Se recorta a cerrar el unico hueco sin proteccion: eliminar alternativa.

**Archivos:** `feature/routines/RoutinesScreen.kt:1058-1060`

- [ ] Añadir `FitTrackConfirmDialog` (destructivo) antes de `onRemoveAlternative`, mismo patron que dia (T8) y ejercicio
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: require confirmation before removing an exercise alternative`

---

## Verificacion final antes de avisar al dueño

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] Repaso manual de los 11 criterios de aceptacion de la spec, uno por uno
- [ ] Push y avisar

**Pendiente de pasada manual (la hace el dueño):**
1. Entrenar una sesion completa de solo ejercicios de peso corporal y finalizarla — debe guardarse.
2. Escribir una nota, cerrar y reabrir, tocarla y escribir — no debe borrarse el contenido previo.
3. Editar una serie en Historial y descartar — el valor original debe seguir en BD.
4. Editar una alternativa y cerrar con la X — el cambio no debe quedar aplicado.
5. Completar una serie PR, editarla, recompletarla — el contador de PRs no debe inflarse.
6. Pulsar guardar rutina dos veces rapido — no debe crear una rutina duplicada.

**Merge:** a `develop` cuando compile y pasen los tests (junto con las rondas 1+2, ya que esta rama parte de ahi). A `main` solo tras la pasada manual del dueño.
