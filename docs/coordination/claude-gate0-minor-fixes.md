# Handoff Para Claude - Gate 0 Minor Fixes

Fecha: 2026-04-25

Origen: Codex
Destino: Claude
Rol esperado: ejecutar ajustes pequenos y acotados sobre problemas detectados en Gate 0.

## Estado

Completado por Claude — 2026-04-25.

Las 4 tareas se resolvieron en `WorkoutScreen.kt`, `WorkoutViewModel.kt` y `RoutinesViewModel.kt`.
Verificacion automatica ejecutada (`test` pasa; `build` con advertencias conocidas de AGP/D8).
Validacion manual en dispositivo pendiente por parte del usuario (dark mode, inputs, dialogo, preview).

## Resultado por Tarea

### Tarea 1 — Inputs de peso/reps: `0` -> `010`

Solucion: `onFocusChanged` en cada `OutlinedTextField` de `WorkoutSetRow`.
Al entrar en foco, si el valor actual es exactamente `"0"`, se llama a `onSetWeightChange` / `onSetRepsChange`
con cadena vacia. El usuario escribe directamente el nuevo valor sin prefijo.
No se toco ninguna regla de dominio ni el ViewModel.

### Tarea 2 — Bordes blancos en campos de peso/reps

Solucion: `OutlinedTextFieldDefaults.colors(...)` explicito en los dos `OutlinedTextField` de `WorkoutSetRow`.
Se mapearon `focusedBorderColor`, `unfocusedBorderColor`, `focusedLabelColor`, `unfocusedLabelColor`,
`focusedTextColor`, `unfocusedTextColor`, `cursorColor`, `focusedContainerColor` y `unfocusedContainerColor`
a tokens del `MaterialTheme.colorScheme` correspondientes (primary, outline, onSurface, onSurfaceVariant).
No se tocaron tokens globales del tema.

### Tarea 3 — Bordes blancos en dialogo de finalizar entrenamiento

Solucion: `AlertDialog` reemplazado por `Dialog { Surface { Column { ... } } }`.
`Surface` con `shape = MaterialTheme.shapes.extraLarge`, `color = MaterialTheme.colorScheme.surface`
y `tonalElevation = 6.dp`. Estructura interior: titulo, cuerpo y fila de botones con `TextButton`.
El flujo funcional (confirmar -> `viewModel.finishWorkout()`, cancelar -> cerrar dialogo) se mantiene identico.

### Tarea 4 — Preview de Entrenar no se refresca al editar rutina activa

Solucion: se inyecto `RoutineRepository` en `WorkoutViewModel` (nueva dependencia Hilt).
En el bloque `init` se anadio un segundo flow: `routineRepository.observeRoutines().drop(1)`.
`drop(1)` evita el reload doble en el arranque (ya cubierto por `activeRoutineId`).
Cuando el flow emite y no hay sesion activa, se llama a `loadWorkoutState(activeRoutineId)`.
Se importaron `RoutineRepository` y `kotlinx.coroutines.flow.drop`.

### Extra — Detekt ReturnCount en `RoutinesViewModel.isValidTargetReps`

Problema descubierto durante `build`: Detekt limita a 4 returns por funcion;
`isValidTargetReps` tenia 6. Se refactorizo con expresion `when` anidada, reduciendo a 2 returns.
La logica de validacion (AMRAP, entero 1-99, rango X-Y, RPE N) queda identica y cubierta por tests existentes.

## Contexto

Gate 0 del Roadmap 2.1 se valido en telefono fisico. El flujo principal queda aceptado por el usuario:

- Intro / arranque: OK.
- Rutinas: OK.
- Historial: OK.
- Datos: OK.
- Snapshots historicos: OK. Al editar una rutina, el historico conserva el snapshot antiguo.

Codex ya aplico estos ajustes y pasaron verificacion automatica:

- `HistoryScreen`: `BackHandler` para volver de detalle a listado.
- `RoutinesScreen`: acciones de rutinas inactivas reorganizadas para evitar texto partido.
- `WorkoutScreen`: colores/shape del dialogo de finalizar entrenamiento.

Verificacion ejecutada por Codex:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado: ambos comandos pasan. El build mantiene warnings conocidos de AGP/compileSdk 35 y D8/Kotlin metadata.

## Tareas (Referencia — Ver "Resultado por Tarea" para resolucion)

### 1. Inputs de peso/reps en Entrenar

Problema validado por el usuario:

- Al tocar un campo con valor `0`, el valor no se selecciona ni se borra.
- Si el usuario escribe `10`, queda `010`.
- Ocurre en peso y reps.

Objetivo:

- Al enfocar por primera vez un campo cuyo texto actual es `0`, facilitar reemplazo directo por el nuevo valor.
- Evitar que la experiencia normal produzca valores tipo `010`.

Restricciones:

- No cambiar reglas de dominio ni schema.
- No mover logica de negocio a Compose.
- Mantener el alcance en `WorkoutScreen` / manejo de input existente, salvo que sea imprescindible tocar ViewModel.
- No implementar validaciones grandes de Fase 2.1A todavia.

Sugerencia tecnica:

- Revisar `WorkoutSetRow` en:
  - `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/workout/WorkoutScreen.kt`
- Considerar usar estado local de foco/seleccion o limpiar `0` en `onFocusChanged` solo para el campo visual.
- Mantener persistencia actual mediante `onSetWeightChange` y `onSetRepsChange`.

Criterio de aceptacion:

- Con un campo mostrando `0`, tocarlo y escribir `10` deja `10`, no `010`.
- No se rompe el registro de series.
- La sesion finalizada sigue llegando a Historial y Datos.

### 2. Bordes blancos en campos de peso/reps

Problema validado por el usuario:

- En Entrenar, al poner el cursor en peso/reps aparecen bordes blancos.
- Es especialmente visible en dark mode.

Objetivo:

- Alinear colores de `OutlinedTextField` con el sistema visual dark/light.
- Evitar bordes blancos agresivos al enfocar.

Restricciones:

- No redisenar toda la pantalla.
- No cambiar tokens globales salvo que sea claramente reusable y seguro.
- No introducir dependencias.

Sugerencia tecnica:

- Revisar los `OutlinedTextField` de `WorkoutSetRow`.
- Usar `OutlinedTextFieldDefaults.colors(...)` con colores de `MaterialTheme.colorScheme.primary`, `outline`, `onSurface`, `onSurfaceVariant` o tokens existentes.

Criterio de aceptacion:

- En dark mode, el borde enfocado es verde/acento del sistema, no blanco.
- Texto y labels siguen siendo legibles.
- Light mode no queda degradado.

### 3. Bordes blancos en dialogo de finalizar entrenamiento

Problema validado por el usuario:

- El dialogo de finalizar entrenamiento sigue mostrando bordes/esquinas blancas en dark mode.
- Codex intento fijar `shape` y colores, pero el usuario confirma que persiste.

Objetivo:

- El dialogo debe verse limpio en dark mode, sin esquinas o bordes blancos.

Restricciones:

- Mantener dialogo simple.
- No cambiar el flujo funcional de finalizar.
- No introducir componente complejo salvo que `AlertDialog` sea la causa clara del artefacto.

Sugerencias tecnicas:

- Revisar si el artefacto viene de `AlertDialog` Material 3 con edge-to-edge/theme.
- Probar una de estas opciones:
  - ajustar `DialogProperties`/container si aplica;
  - usar `BasicAlertDialog` o `Dialog` + `Surface` con `MaterialTheme.shapes.extraLarge`;
  - envolver contenido en `Surface` controlando `shape`, `color` y `tonalElevation`.

Criterio de aceptacion:

- En dark mode, al pulsar `Finalizar entrenamiento`, el dialogo no muestra bordes blancos.
- Botones `Seguir entrenando` y `Finalizar` siguen visibles y accionables.
- Confirmar finalizacion guarda la sesion.

### 4. Rutina editada no refresca inmediatamente en Entrenar

Problema observado por el usuario:

- Tras editar el nombre de una rutina activa, la vista de Entrenar no recoge el cambio inmediatamente.
- Para que lo detecte, hay que seleccionar otra rutina como activa y luego volver a seleccionar la editada.
- El historico conserva correctamente el nombre antiguo, que es el comportamiento esperado.

Objetivo:

- Si la rutina activa editable cambia, Entrenar debe refrescar su preview sin obligar a cambiar de rutina activa.

Restricciones:

- No romper el invariante historico de snapshots.
- No modificar sesiones historicas.
- No meter Firebase/sync.
- Mantener repositorios ocultando Room/DataStore a UI.

Sugerencia tecnica:

- Revisar:
  - `WorkoutViewModel`
  - `GetNextWorkoutPreviewUseCase`
  - `RoutineRepository`
  - observacion de rutina activa / rutinas desde DataStore + Room
- Buscar si `WorkoutViewModel` solo refresca cuando cambia `activeRoutineId`, pero no cuando cambia el contenido de la rutina activa.

Criterio de aceptacion:

- Editar nombre de rutina activa.
- Volver a Entrenar.
- Preview muestra el nombre actualizado sin cambiar de rutina activa.
- Historial de sesiones antiguas mantiene el snapshot anterior.

## Verificacion

Automatica ejecutada por Claude:

```powershell
.\gradlew.bat test --no-daemon --console=plain   # EXIT 0
.\gradlew.bat build --no-daemon --console=plain  # EXIT 0 (warnings conocidos AGP/D8)
```

Detekt extra (detectado): `isValidTargetReps` refactorizada; `test` + `build` pasan tras el fix.

Validacion manual pendiente de usuario (en dispositivo fisico):

- Dark mode activo.
- Entrenar con rutina activa.
- Editar peso/reps desde `0`.
- Abrir/cerrar dialogo de finalizar.
- Finalizar sesion.
- Revisar Historial y Datos.
- Editar rutina activa y comprobar preview de Entrenar.

## Fuera De Alcance

- Fase 2.1A completa.
- Nuevas features.
- Firebase/sync.
- Refactor transversal.
- Cambios de schema.
- Reescribir sistema visual completo.
