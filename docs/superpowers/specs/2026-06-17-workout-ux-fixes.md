# Spec: Workout UX Fixes

Fecha: 2026-06-17
Rama sugerida: `codex/workout-ux-fixes`

---

## Fix 1 — Completar serie al rellenar el último campo (peso o reps)

### Contexto

Actualmente una serie se marca como completada únicamente cuando el usuario escribe
repeticiones y el valor es > 0. Si el usuario escribe primero las reps y después el
peso, la serie nunca se completa automáticamente.

### Comportamiento deseado

La serie se completa cuando **ambos campos tienen valor válido** y el usuario acaba de
rellenar el **último de los dos**. El trigger debe dispararse desde cualquiera de los
dos handlers, no solo desde `onSetRepsChange`.

### Regla exacta

```
set.isCompleted = weightKg > 0 AND reps > 0
```

El trigger de auto-complete se activa en `onSetWeightChange` si tras el cambio
`weight > 0 && reps > 0` y el set no estaba ya completado.
El trigger ya existente en `onSetRepsChange` sigue igual.

### Archivos afectados

- `feature/workout/WorkoutViewModel.kt` — función `onSetWeightChange`: añadir la misma
  lógica de auto-complete que existe en `onSetRepsChange` (disparar el timer de descanso
  y marcar completada).

---

## Fix 2 — Mostrar repeticiones de la sesión anterior por serie

### Contexto

Al iniciar un entrenamiento, cada serie muestra el peso de la última sesión
(`"Ultima vez: 50 kg"`). No muestra las repeticiones de esa misma serie.

### Comportamiento deseado

Mostrar peso **y** repeticiones de la última sesión en cada fila de serie:

```
Ultima vez: 50 kg · 12 reps
```

Si alguno de los dos no existe (primera vez que se registra ese ejercicio), mostrar
solo el que esté disponible. Si ninguno existe, no mostrar nada (comportamiento actual).

### Cambios necesarios

**Repository (Room):**
- Añadir `suspend fun getLastRepsForExerciseSet(variantKey: String, setNumber: Int): Int?`
  en `WorkoutRepository` y su implementación en `WorkoutRepositoryImpl`.
- La query busca la sesión terminada más reciente que contenga ese `variantKey` y ese
  `setNumber`, y devuelve las `reps` del snapshot correspondiente.

**ViewModel:**
- En `WorkoutSetUiState` añadir `val previousReps: Int? = null`.
- En la función `withPreviousWeights` (o equivalente), poblar también `previousReps`
  usando `getLastRepsForExerciseSet`.

**UI:**
- En `WorkoutScreen.kt`, donde se muestra `"Ultima vez: $previousWeight kg"`,
  añadir las reps si están disponibles:
  `"Ultima vez: $previousWeight kg · $previousReps reps"`.
- Si solo hay peso: `"Ultima vez: $previousWeight kg"`.
- Si solo hay reps: `"Ultima vez: $previousReps reps"`.

---

## Fix 3 — Teclado aparece al hacer over-scroll en la lista de entrenamiento

### Contexto

Al llegar al final de la lista de entrenamiento y seguir deslizando hacia abajo, el
teclado aparece. Esto ocurre porque el over-scroll activa el foco en el último
`OutlinedTextField` visible.

### Comportamiento deseado

El over-scroll debe rebotar visualmente sin activar ningún teclado ni cambiar el foco.

### Investigar y verificar

Comprobar si el mismo comportamiento ocurre en:
- `WorkoutScreen` (confirmado por el usuario)
- `RoutinesScreen` (editor de rutinas)
- `HistoryScreen` (edit mode)

### Solución propuesta

Añadir `Modifier.focusProperties { canFocus = false }` en la columna raíz o usar
`LocalFocusManager.current.clearFocus(force = true)` cuando el scroll llega a su
límite inferior. Alternativamente, envolver el `LazyColumn` con
`Modifier.pointerInput` que descarte los eventos de toque que ocurran fuera de los
bounds de los items.

La solución más limpia: añadir en el `LazyColumn` el modificador
`overscrollEffect(null)` o un `ScrollBehavior` personalizado que no propague el foco.
Si eso no funciona, usar `FocusRequester` explícito en cada `OutlinedTextField` y no
dejar el foco libre al rebotar.

> Codex debe probar el fix en el emulador antes de commitear.

---

## Fix 4 — Dialog de ejercicios alternativos: UX de cierre y selección

### Contexto

Cuando el usuario abre el diálogo de alternativas estando ya en una alternativa
seleccionada, la pantalla parece "bloqueada": no hay un botón de cierre visible y
tocar la opción ya seleccionada llama a `onApplyVariant` pero no da feedback visual
ni cierra el diálogo.

### Comportamiento deseado

1. **Botón de cierre (X)** visible en la esquina superior derecha del diálogo.
2. Tocar la opción **ya seleccionada** cierra el diálogo sin cambiar nada
   (equivalente a confirmar la selección actual).
3. Tocar una opción **diferente** aplica el cambio y cierra el diálogo (comportamiento
   actual, que ya funciona).
4. Tocar fuera del diálogo lo cierra (ya funciona vía `onDismissRequest`).

### Archivos afectados

- `feature/workout/WorkoutScreen.kt` — `ExerciseAlternativesDialog`:
  - Añadir `IconButton` con `Icons.Default.Close` en la esquina superior derecha del
    header del diálogo.
  - En el `onClick` de cada opción: si `option.variantKey == currentVariantKey` llamar
    a `onDismiss` en lugar de `onApplyVariant`.
  - Para saber cuál es la opción actualmente activa, el `ExerciseAlternativesUiState`
    debe exponer un campo `currentVariantKey: String?`. Verificar si ya existe o
    añadirlo en el ViewModel.
