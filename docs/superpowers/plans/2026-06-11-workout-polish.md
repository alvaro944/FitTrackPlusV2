# Plan: Workout Polish

> **Para Codex:** Lee las dos specs antes de empezar. Todo el trabajo va en una sola rama. No hagas commits hasta que cada tarea esté verificada localmente.

**Rama:** `codex/workout-polish`
**Specs de referencia:**
- `docs/superpowers/specs/2026-06-11-workout-screen-polish.md`
- `docs/superpowers/specs/2026-06-11-data-integrity.md`

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room, Hilt

---

## Reglas de esta rama

- Crea la rama `codex/workout-polish` desde `main`
- Todo el trabajo va en esa rama — no crees ramas adicionales
- Verifica con `./gradlew test` y `./gradlew build` antes de cada commit
- Pasada manual en emulador antes del commit final
- Push solo cuando todo el plan esté done

---

## Tarea 1: Scroll con teclado abierto

**Archivos:**
- `feature/workout/WorkoutScreen.kt`

- [ ] Añadir `Modifier.imePadding()` al `LazyColumn` principal de la sesión activa
- [ ] Verificar que el `Scaffold` no absorbe los insets antes de que lleguen al scroll
- [ ] Probar en emulador con 7+ ejercicios y teclado abierto — el último ejercicio debe ser accesible sin cerrar el teclado
- [ ] Verificar con `test` + `build`

---

## Tarea 2: Teclado numérico + separador decimal con coma

**Archivos:**
- `feature/workout/WorkoutScreen.kt`

- [ ] Campo de peso: `KeyboardOptions(keyboardType = KeyboardType.Decimal)`
- [ ] Campo de reps: `KeyboardOptions(keyboardType = KeyboardType.Number)`
- [ ] En `onValueChange` del campo de peso: reemplazar `.` por `,` en el texto mostrado
- [ ] En el parsing del valor: reemplazar `,` por `.` antes de `toDoubleOrNull()`
- [ ] Ignorar silenciosamente caracteres no válidos — nunca mostrar error de formato
- [ ] Verificar con `test` + `build`

---

## Tarea 3: Alineación horizontal de columnas

**Archivos:**
- `feature/workout/WorkoutScreen.kt`

- [ ] Definir anchos fijos como constantes para columna de peso, columna de reps y columna de check
- [ ] Aplicar los mismos anchos en cabecera (si existe) y en cada `SetRow`
- [ ] Verificar alineación visual en emulador con series de distintos valores (ej: 100 kg vs 7.5 kg)
- [ ] Verificar con `test` + `build`

---

## Tarea 4: Ejercicios colapsables en entreno activo

**Archivos:**
- `feature/workout/WorkoutScreen.kt`
- `feature/workout/WorkoutViewModel.kt`

- [ ] Añadir `expandedExerciseId: Long?` al `WorkoutUiState` (null = todos colapsados excepto el primero con series pendientes)
- [ ] Exponer `fun expandExercise(id: Long)` en el ViewModel (accordion: expandir uno colapsa el anterior)
- [ ] Al cargar la sesión, inicializar `expandedExerciseId` al primer ejercicio con series pendientes
- [ ] En `WorkoutScreen`, `ExerciseBlock` muestra contenido solo si `id == expandedExerciseId`
- [ ] Cabecera del ejercicio colapsado muestra: nombre + `X/Y series` + checkmark si completado
- [ ] Al completar la última serie de un ejercicio, esperar ~600ms y colapsar automáticamente (usar `delay` en una corrutina del ViewModel o con `LaunchedEffect`)
- [ ] Tras el auto-colapso, expandir automáticamente el siguiente ejercicio con series pendientes
- [ ] Verificar con `test` + `build` + pasada manual

---

## Tarea 5: Sesiones vacías descartadas

**Archivos:**
- `feature/workout/WorkoutViewModel.kt`
- `data/repository/WorkoutRepository.kt`
- `data/repository/DefaultWorkoutRepository.kt`
- Test: `app/src/test/.../feature/workout/WorkoutViewModelTest.kt` o use case test

- [ ] Escribir test: finalizar sesión con 0 series completadas → la sesión no existe en el repositorio
- [ ] En `WorkoutViewModel.finishWorkout()`: contar series completadas antes de llamar al use case
- [ ] Si `completedSets == 0`: llamar a un método `cancelSession(sessionId)` en el repositorio y volver al estado de preview con mensaje "Sesión descartada"
- [ ] Añadir `cancelSession(sessionId: Long)` a `WorkoutRepository` e implementarlo en `DefaultWorkoutRepository` (DELETE de la sesión)
- [ ] Hacer pasar el test
- [ ] Verificar con `test` + `build`

---

## Tarea 6: Gráficas sin huecos para días no entrenados

**Archivos:**
- `domain/usecase/ObserveWorkoutStatsUseCase.kt`
- Test: `app/src/test/.../domain/usecase/ObserveWorkoutStatsUseCaseTest.kt`

- [ ] Revisar si hay lógica que rellena días vacíos con 0 — si la hay, eliminarla
- [ ] Asegurarse de que los puntos de gráfica se generan solo de sesiones finalizadas con al menos 1 serie
- [ ] Escribir test: periodo con entrenos el día 1 y día 4, verificar que solo hay 2 puntos (no puntos a 0 en días 2 y 3)
- [ ] Hacer pasar el test
- [ ] Verificar con `test` + `build`

---

## Tarea 7: Verificación final y commit

- [ ] `./gradlew test --no-daemon --console=plain` — todos en verde
- [ ] `./gradlew build --no-daemon --console=plain` — build limpio
- [ ] Pasada manual en emulador:
  - Entreno con teclado abierto: scroll hasta el último ejercicio
  - Escribir `82.5` en un campo de peso → se muestra `82,5`
  - Todos los ejercicios colapsados al entrar, se expanden al tocar
  - Completar todas las series de un ejercicio → se colapsa solo
  - Iniciar y finalizar sin series → no aparece en historial
- [ ] Commit limpio con mensaje descriptivo
- [ ] Push a `origin/codex/workout-polish`
- [ ] Avisar al usuario para revisión y merge a main
