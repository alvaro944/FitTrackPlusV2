# Spec: Calendario home y orden de secciones en rutinas

**Fecha:** 2026-06-12
**Rama:** `codex/home-routines-fixes`
**Estado:** aprobada

---

## 1. Bug — Calendario home marca días incorrectos

**Problema:** El widget de días de la semana en HomeScreen usa `sessionsThisWeek` (un entero con el conteo de sesiones) para marcar como completados los primeros N días desde el lunes. Si el usuario entrenó miércoles y jueves, el código pinta lunes y martes porque `index < completedSlots` no tiene en cuenta qué días reales se entrenó.

**Código actual (HomeScreen.kt ~línea 314):**
```kotlin
val completedSlots = sessionsThisWeek.coerceIn(0, 7)
val isCompleted = index < completedSlots  // INCORRECTO — marca siempre desde el lunes
```

**Solución:**

En `HomeViewModel`:
- Cambiar `sessionsThisWeek: Int` por `trainedDaysThisWeek: Set<Int>` en `HomeUiState`
  donde cada Int es el índice del día (0 = lunes, 1 = martes, …, 6 = domingo)
- Calcular los días reales: para cada sesión finalizada esta semana, extraer el día de semana de `startedAt` con `Calendar` y mapearlo al índice 0–6 (lunes=0)
- Mantener `sessionsThisWeek: Int` solo para el texto "N sesiones esta semana" (puede ser `trainedDaysThisWeek.size`)

En `HomeScreen`:
- Cambiar `val isCompleted = index < completedSlots` por `val isCompleted = index in trainedDaysThisWeek`

**Criterio de aceptación:**
- [ ] Entrenar miércoles y jueves → los días X y J quedan marcados, L y M no
- [ ] Entrenar lunes → solo L queda marcado
- [ ] El conteo de texto "N sesiones esta semana" sigue siendo correcto
- [ ] Test unitario: sesión el miércoles → `trainedDaysThisWeek` contiene índice 2

---

## 2. Cambio de orden — Biblioteca antes que Plantillas en Rutinas

**Problema:** En la pantalla de Rutinas, la sección "Plantillas" aparece antes que "Biblioteca". El usuario quiere lo contrario: primero Biblioteca (rutinas guardadas), luego Plantillas.

**Solución:**
En `RoutinesScreen.kt`, intercambiar el bloque que renderiza "Plantillas" (línea ~377) con el bloque que renderiza "Biblioteca" (línea ~391). Solo es un reordenado de composables, no hay cambio de lógica ni de datos.

**Criterio de aceptación:**
- [ ] En la pantalla de Rutinas, "Biblioteca" aparece antes que "Plantillas"
- [ ] El contenido y funcionalidad de ambas secciones no cambia
