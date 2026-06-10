# Plan: UX Improvements

> **Para Codex:** Lee las tres specs antes de empezar. Todo el trabajo va en una sola rama. No hagas commits hasta que cada tarea este verificada localmente.

**Rama:** `codex/ux-improvements`
**Specs de referencia:**
- `docs/superpowers/specs/2026-06-10-routine-editor-ux.md`
- `docs/superpowers/specs/2026-06-10-weight-progression-hints.md`
- `docs/superpowers/specs/2026-06-10-workout-entry-ux.md`

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room, Hilt

---

## Reglas de esta rama

- Crea la rama `codex/ux-improvements` desde `main`
- Todo el trabajo de las tres mejoras va en esta rama
- Verifica localmente con `./gradlew test` y `./gradlew build` antes de cada commit
- Haz pasada manual en emulador antes del commit final
- Un commit limpio por tarea (o uno por grupo si las tareas son pequenas)
- Push solo cuando todo el plan este done
- No amplies el alcance sin confirmar con el usuario

---

## Tarea 1: Editor de rutinas — dias colapsables y jerarquia visual

**Archivos:**
- `feature/routines/RoutinesScreen.kt`
- `feature/routines/RoutinesViewModel.kt` (si hace falta estado de expansion)

- [ ] Anadir estado de expansion por dia en el ViewModel o con `remember` local
- [ ] Modificar el composable de dia para que colapse/expanda su contenido
- [ ] Por defecto todos los dias abren colapsados
- [ ] Cabecera del dia colapsado muestra nombre + conteo de ejercicios
- [ ] Ajustar estilos para diferenciar visualmente dia > ejercicio > config
- [ ] Verificar con `test` + `build`

---

## Tarea 2: Editor de rutinas — confirmacion al salir con cambios

**Archivos:**
- `feature/routines/RoutinesViewModel.kt`
- `feature/routines/RoutinesScreen.kt`

- [ ] Anadir flag `hasUnsavedChanges` al estado del ViewModel (se activa con cualquier edicion, se limpia al guardar)
- [ ] Interceptar navegacion hacia atras y cambio de pestana cuando hay cambios pendientes
- [ ] Mostrar dialogo de confirmacion: "Descartar" o "Seguir editando"
- [ ] Verificar que navegar sin cambios no muestra el dialogo
- [ ] Verificar con `test` + `build`

---

## Tarea 3: Editor de rutinas — boton guardar flotante

**Archivos:**
- `feature/routines/RoutinesScreen.kt`

- [ ] Anadir un `FloatingActionButton` o `ExtendedFloatingActionButton` en la parte inferior del `Scaffold`
- [ ] Solo visible cuando `hasUnsavedChanges == true`
- [ ] Al pulsarlo llama al mismo metodo de guardado que el boton del header
- [ ] Verificar con `test` + `build`

---

## Tarea 4: Recomendaciones de carga — use case y logica

**Archivos:**
- Create: `domain/usecase/GetProgressionHintUseCase.kt`
- Create: `domain/model/ProgressionHint.kt` (enum: UP, DOWN, NONE)
- Create: `app/src/test/.../domain/usecase/GetProgressionHintUseCaseTest.kt`

- [ ] Escribir el test primero (casos: suficientes sesiones con rango superado, por debajo, sin datos, rango no parseable)
- [ ] Implementar `GetProgressionHintUseCase` segun la spec (logica de 3 sesiones, parsing de rango)
- [ ] Hacer pasar los tests
- [ ] Verificar con `test` + `build`

---

## Tarea 5: Recomendaciones de carga — integracion en WorkoutViewModel y WorkoutScreen

**Archivos:**
- `feature/workout/WorkoutViewModel.kt`
- `feature/workout/WorkoutScreen.kt`

- [ ] Exponer `hints: Map<Long, ProgressionHint>` en `WorkoutUiState` (clave: workoutExerciseId)
- [ ] En `loadActiveSession`, calcular hints para cada ejercicio de la sesion
- [ ] En `ExerciseBlock`, mostrar el icono de hint junto al nombre del ejercicio
- [ ] El icono desaparece cuando la primera serie del ejercicio esta completada
- [ ] Al pulsar el icono mostrar tooltip
- [ ] Verificar con `test` + `build` + pasada manual

---

## Tarea 6: Entrada de series — steppers de reps y peso

**Archivos:**
- `feature/workout/WorkoutScreen.kt`
- `feature/workout/WorkoutViewModel.kt` (si hace falta para el step de peso)

- [ ] Anadir botones `−` y `+` en la fila de serie para reps (minimo 40dp)
- [ ] El campo de texto sigue editable
- [ ] Valor inicial de reps: ultima serie registrada del mismo ejercicio, o minimo del rango si es primera vez
- [ ] Anadir steppers para peso con incremento de 2.5 kg
- [ ] Long-press en +/− del peso incrementa de 5 en 5
- [ ] Verificar con `test` + `build`

---

## Tarea 7: Entrada de series — feedback visual al completar

**Archivos:**
- `feature/workout/WorkoutScreen.kt`

- [ ] Fila de serie completada: fondo diferenciado + checkmark visible
- [ ] Vibracion haptica corta al completar (ya existe el canal de hapticos en el ViewModel, reutilizar)
- [ ] Verificar en emulador que el feedback es perceptible pero no invasivo

---

## Tarea 8: Verificacion final y commit

- [ ] `./gradlew test --no-daemon --console=plain` — todos los tests en verde
- [ ] `./gradlew build --no-daemon --console=plain` — build limpio
- [ ] Pasada manual completa en emulador: editar rutina, entrar en entreno, completar series
- [ ] Commit limpio con mensaje descriptivo
- [ ] Push a `origin/codex/ux-improvements`
- [ ] Avisar al usuario para revision y merge a main
