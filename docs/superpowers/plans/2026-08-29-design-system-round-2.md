# Plan: Unificacion de design system, ronda 2

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `develop`** (verifica primero que `refactor/design-system-unification` ya esta mergeada ahi; si no, para y avisa). Commits separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica local (`test` + `build`) antes de cada commit; NO lances el emulador. No commits de WIP.

**Rama:** `refactor/design-system-unification-round-2` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-08-29-design-system-round-2.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

**Esto sigue siendo refactor de Compose UI**, con UNA excepcion: la Tarea 7 (R7 de la spec) cambia la interaccion de escribir texto a tocar chips/stepper en las alternativas — es un cambio de comportamiento aceptado explicitamente por el dueño, no un descuido. Todo lo demas debe mantener el comportamiento actual, solo cambia el componente por debajo.

---

## Tarea 0: Preparar rama

- [ ] Verificar que `refactor/design-system-unification` esta mergeada en `develop`
- [ ] `git checkout develop && git pull` si aplica
- [ ] `git checkout -b refactor/design-system-unification-round-2`

---

## Tarea 1: Arreglar el olvido — badge de "rutina activa"

**Archivos:** `feature/routines/RoutinesScreen.kt` (~lineas 312-348)

- [ ] Migrar el `Box.background(primary, CircleShape) { Icon(...) }` del banner "rutina activa" a `FitTrackIconBadge`
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: migrate active-routine banner icon to FitTrackIconBadge`

---

## Tarea 2: Ficha de nombre de rutina dentro de `FitTrackCard`

**Archivos:** `feature/routines/RoutinesScreen.kt:673-685`

- [ ] Envolver el item del nombre de rutina en `FitTrackCard`, sin tocar validacion ni logica
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: wrap routine name field in FitTrackCard`

---

## Tarea 3: Campos de texto libre sueltos → `FitTrackSelectAllTextField`

**Archivos:** `feature/routines/RoutinesScreen.kt:674, 916, 1158`

- [ ] Sustituir los 3 `OutlinedTextField` crudos (nombre rutina, nombre dia, nombre ejercicio) por `FitTrackSelectAllTextField(selectAllOnFocus = false)`
- [ ] Mismo comportamiento — texto libre, sin seleccionar todo al enfocar
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: wrap free-text name fields in FitTrackSelectAllTextField`

---

## Tarea 4: `FitTrackReorderActions`

**Archivos:**
- Nuevo: `core/design/ReorderActions.kt`
- `feature/routines/RoutinesScreen.kt` (`RoutineDayEditor` ~869-914, `RoutineExerciseEditor` ~1106-1155)

- [ ] Crear `FitTrackReorderActions` segun R4: `canMoveUp`, `canMoveDown`, `canRemove`, `onMoveUp`, `onMoveDown`, `onDuplicate`, `onRemove`, slot opcional `extraAction`
- [ ] Migrar `RoutineDayEditor` sin `extraAction`
- [ ] Migrar `RoutineExerciseEditor` con `extraAction` = boton de alternativas
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract FitTrackReorderActions and migrate routine editors`

---

## Tarea 5: Fila de acciones de formulario en dialogos de alternativas

**Archivos:**
- Nuevo: `core/design/FormDialogActions.kt` (o añadir a `Dialogs.kt`)
- `feature/workout/WorkoutScreen.kt` (`ExerciseAlternativesDialog`, modo creacion, ~983-991)
- `feature/routines/RoutinesScreen.kt` (`ExerciseAlternativesEditorDialog`, modo edicion en linea, ~1313-1350)

- [ ] Crear `FitTrackFormDialogActions(cancelLabel, confirmLabel, onCancel, onConfirm, confirmEnabled)`
- [ ] Migrar el modo creacion de `ExerciseAlternativesDialog` (Entrenar) a este componente — mismo "Cancelar"/"Guardar y usar"
- [ ] En `ExerciseAlternativesEditorDialog` (Rutinas): dar al modo edicion en linea un cancelar real. Esto requiere que el ViewModel pueda **descartar** los cambios del campo en edicion (volver al valor guardado) al pulsar cancelar, no solo cerrar el modo edicion dejando lo escrito. Revisar `RoutinesViewModel` para ver si ya existe un snapshot del valor previo o hay que añadirlo
- [ ] Sustituir el boton unico "Listo" por `FitTrackFormDialogActions("Cancelar", "Guardar", onCancel, onConfirm)`
- [ ] El modo lista de Entrenar y el "Cerrar" del dialogo completo de Rutinas **no cambian**
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: standardize form dialog actions and add cancel to inline alternative edit`

---

## Tarea 6: Resaltado visual del item actual/predeterminado

**Archivos:** `feature/routines/RoutinesScreen.kt` (`ExerciseAlternativesEditorDialog`)

- [ ] Cuando la tarjeta (ejercicio base o alternativa) sea el `defaultVariantKey` actual, aplicar el mismo borde verde que usa Entrenar para "en uso ahora" en `FitTrackCard`
- [ ] No cambiar la logica de accion (sigue siendo un boton, no se vuelve clickeable-toda-la-tarjeta como en Entrenar)
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: highlight default variant card in routines alternatives editor`

---

## Tarea 7: Unificar el campo "series + reps objetivo" — CAMBIO DE INTERACCION

**Archivos:**
- Nuevo: `core/design/TargetPrescriptionFields.kt`
- `feature/routines/RoutinesScreen.kt` (`RoutineExerciseEditor` ~1187/1217, alternativa en Rutinas ~1323/1330)
- `feature/workout/WorkoutScreen.kt` (alternativa en Entrenar ~955/963)

**Aviso:** esta tarea cambia como el usuario introduce series y reps en las alternativas — de escribir numeros a tocar +/- y chips, igual que ya funciona para el ejercicio principal. Es una decision del dueño, no la simplifiques ni la dejes "a medias" sin avisar si algo no encaja.

- [ ] Extraer de `RoutineExerciseEditor` el par `FitTrackStepper` (series, clamp 1-99) + selector de reps (`FlowRow` de `FilterChip` + chip "+" que abre `FitTrackInputDialog` para valor personalizado) a `FitTrackTargetPrescriptionFields`, parametrizado por valores actuales y callbacks
- [ ] Migrar `RoutineExerciseEditor` a usar el componente extraido — mismo comportamiento, es la misma UI movida
- [ ] Migrar el par series+reps de la alternativa en Rutinas (`FitTrackSelectAllTextField` x2) al componente extraido
- [ ] Migrar el par series+reps de la alternativa en Entrenar (`FitTrackSelectAllTextField` x2) al componente extraido
- [ ] Verificar que el `ExerciseAlternativeDraftUiState`/equivalentes en los ViewModels puedan representar el reps-target como el mismo tipo de dato que usa el ejercicio principal (texto de rango tipo "8-12"), no como texto libre sin validar
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: unify target sets/reps input across exercises and alternatives`

---

## Verificacion final antes de avisar al dueño

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] Repaso del diff: cero `OutlinedTextField` crudo quedando en `RoutinesScreen.kt` para nombres
- [ ] Cero `FitTrackSelectAllTextField` quedando para series/reps objetivo en los dialogos de alternativas
- [ ] Confirmar que el modo edicion en linea de Rutinas puede cancelar sin dejar el cambio escrito
- [ ] Confirmar que el resaltado del item actual/predeterminado se ve en las dos pantallas
- [ ] Push y avisar, marcando claramente la Tarea 7 como el cambio que necesita pasada manual mas cuidadosa

**Pendiente de pasada manual (la hace el dueño):**
1. Crear una rutina nueva y comprobar que la ficha de nombre ya tiene tarjeta.
2. Reordenar/duplicar/eliminar dias y ejercicios en el editor de rutinas — comportamiento identico al de antes.
3. Crear y editar una alternativa desde Rutinas: comprobar que se puede cancelar sin perder el ejercicio original, y que fijar una como predeterminada se ve resaltada.
4. Crear una alternativa durante un entrenamiento activo: comprobar que series/reps ahora se eligen con chips/stepper en vez de escribirse, y que sigue aplicandose a la sesion al guardar.

**Merge:** a `develop` cuando compile y pasen los tests. A `main` solo tras la pasada manual del dueño.
