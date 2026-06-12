# Plan: Workout Entry Fixes

> **Para Codex:** Lee la spec antes de empezar. Todo el trabajo va en la rama actual. No crees ramas nuevas. Un commit limpio al final cuando todo esté verificado.

**Rama:** `codex/ux-improvements` (continuar en la rama actual)
**Spec:** `docs/superpowers/specs/2026-06-12-workout-entry-fixes.md`

---

## Tarea 1: Campo de reps estrecho

**Archivos:**
- `feature/workout/WorkoutScreen.kt`

- [ ] Reducir tamaño visual de `SetStepperButton` en la columna de reps (icon a 16dp, ancho mínimo a 28dp)
- [ ] Ajustar proporciones de columnas si es necesario para que el campo muestre 2+ dígitos
- [ ] Verificar con valores 1, 10, 100 en emulador pantalla vertical
- [ ] Verificar con `test` + `build`

---

## Tarea 2: Eliminar auto-colapso

**Archivos:**
- `feature/workout/WorkoutViewModel.kt`

- [ ] Eliminar `exerciseAdvanceJob: Job?` y todas sus referencias
- [ ] Eliminar `EXERCISE_AUTO_COLLAPSE_DELAY_MILLIS`
- [ ] Eliminar el bloque `viewModelScope.launch { delay(...); expandedExerciseId = nextPending }` que se dispara al completar sets
- [ ] Mantener el accordion (expandir uno colapsa el anterior) — ese comportamiento se queda
- [ ] Verificar que completar todas las series no cierra el ejercicio
- [ ] Verificar con `test` + `build`

---

## Tarea 3: Bug reps compartidas entre ejercicios

**Archivos:**
- `feature/workout/WorkoutViewModel.kt`
- `feature/workout/WorkoutScreen.kt`
- Test: `app/src/test/.../feature/workout/`

- [ ] Revisar `stepSetReps`: confirmar que usa `setId` único, no índice
- [ ] Revisar si `repsText` en el composable usa `remember { mutableStateOf(...) }` con una clave que pueda colisionar entre ejercicios — si es así, mover el estado al ViewModel o usar `key(set.id) { ... }` en el composable
- [ ] Escribir test: `stepSetReps(setIdA, +1)` no modifica el `repsText` del set B
- [ ] Hacer pasar el test
- [ ] Verificar manualmente en emulador con 2+ ejercicios
- [ ] Verificar con `test` + `build`

---

## Tarea 4: Seleccionar todo al tocar campo

**Archivos:**
- `feature/workout/WorkoutScreen.kt`

- [ ] En `WeightFieldColumn`: cambiar el `OutlinedTextField` a usar `TextFieldValue` con `onFocusChanged` que hace `selectAll` al ganar foco
- [ ] Aplicar el mismo patrón al campo de reps
- [ ] Patrón a usar:
  ```kotlin
  var fieldValue by remember { mutableStateOf(TextFieldValue(text)) }
  OutlinedTextField(
      value = fieldValue,
      onValueChange = { fieldValue = it; onSetWeightChange(setId, it.text) },
      modifier = Modifier.onFocusChanged { focus ->
          if (focus.isFocused) {
              fieldValue = fieldValue.copy(
                  selection = TextRange(0, fieldValue.text.length)
              )
          }
      }
  )
  ```
- [ ] Verificar que los botones +/− actualizan el `TextFieldValue` correctamente
- [ ] Verificar con `test` + `build`

---

## Tarea 5: Commit final

- [ ] `./gradlew test --no-daemon --console=plain` — todos en verde
- [ ] `./gradlew build --no-daemon --console=plain` — build limpio
- [ ] Pasada manual en emulador: campo reps visible, no auto-colapso, reps independientes, tap selecciona todo
- [ ] Commit limpio
- [ ] Avisar al usuario
