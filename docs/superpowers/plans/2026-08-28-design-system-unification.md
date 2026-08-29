# Plan: Unificacion de componentes visuales repetidos en el design system

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `develop`**. Commits separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica local (`test` + `build`) antes de cada commit; NO lances el emulador (eso lo hace el dueño). No commits de WIP.

**Rama:** `refactor/design-system-unification` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-08-28-design-system-unification.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

**Esto es refactor puro de Compose UI.** No toca Room, DAOs, ViewModels mas alla de pasar los mismos datos a un componente distinto, ni el modelo de dominio. Si en algun punto parece que hace falta tocar persistencia, **para y avisa** — no deberia hacer falta.

**Regla de oro de esta rama:** cada tarea deja la app compilando y con el mismo comportamiento visible que antes, solo con el componente por debajo cambiado. Si una migracion no encaja limpiamente (ver notas de cada tarea), se documenta en el commit y se deja fuera en vez de forzarla.

---

## Tarea 0: Preparar rama

- [ ] `git checkout develop && git pull` si aplica
- [ ] `git checkout -b refactor/design-system-unification`

---

## Tarea 1: `FitTrackIconBadge`

**Archivos:**
- Nuevo: `core/design/IconBadge.kt`
- `feature/home/HomeScreen.kt` (`QuickActionCard`, pasos de onboarding)
- `feature/workout/WorkoutScreen.kt` (`WorkoutSetCompletionButton`)

- [ ] Crear `FitTrackIconBadge` segun R1 de la spec: tamaño fijo 40dp, `CircleShape`, `variant` (Icon/Number), `tone` (Soft/Outlined/Filled)
- [ ] Migrar `QuickActionCard` en Home a `variant = Icon, tone = Soft`
- [ ] Migrar los circulos numerados de los pasos de onboarding en Home a `variant = Number, tone = Soft`
- [ ] Migrar `WorkoutSetCompletionButton` a `variant = Number, tone = Outlined` (pendiente) / `tone = Filled` (completado, con check en vez de numero)
- [ ] Verificar que el `semantics`/accesibilidad de `WorkoutSetCompletionButton` (estado completado anunciado) se conserva
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract FitTrackIconBadge and migrate home/workout circles`

---

## Tarea 2: `FitTrackHeroCard`

**Archivos:**
- Nuevo: `core/design/HeroCard.kt` (o añadir a `Cards.kt`)
- `feature/home/HomeScreen.kt` (hero card inline)
- `feature/workout/WorkoutScreen.kt` (`WorkoutPreviewCard`)

- [ ] Crear `FitTrackHeroCard` segun R2: fondo `primaryDark`, radio `extraLarge`, padding `cardPadding` fijos; slots `badge`, `title`, `content` opcional, `cta` + `onCtaClick`
- [ ] Migrar el hero card inline de `HomeScreen.kt` (extraerlo como uso de `FitTrackHeroCard`, ya no como `Box` a mano)
- [ ] Migrar `WorkoutPreviewCard` — **debe dejar de usar `androidx.compose.material3.Card` en crudo**
- [ ] Confirmar visualmente (lectura de codigo) que ambos quedan con el mismo radio y padding
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract FitTrackHeroCard and migrate home/workout preview cards`

---

## Tarea 3: `FitTrackEntityListCard`

**Archivos:**
- Nuevo: `core/design/EntityListCard.kt` (o añadir a `Cards.kt`)
- `feature/routines/RoutinesScreen.kt` (`RoutineListItem`, `ArchivedRoutineListItem`)
- `feature/history/HistoryScreen.kt` (`HistorySessionCard`)

- [ ] Crear `FitTrackEntityListCard` segun R3: `leadingDot`, `title`, `badge`, `meta`, `actions`
- [ ] **El `leadingDot` va en la misma fila que `title`, no en fila propia** — esto es un ajuste explicito del dueño sobre la propuesta inicial, no te lo saltes
- [ ] Migrar `RoutineListItem` y `ArchivedRoutineListItem`
- [ ] Migrar `HistorySessionCard`, verificando que el punto rojo de "incompleta" queda en la misma linea que el titulo (criterio de aceptacion 6 de la spec)
- [ ] Evaluar las filas de `ExerciseAlternativesEditorDialog`: si el layout no encaja igual de limpio, **dejarlas fuera** y anotarlo en el mensaje de commit
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract FitTrackEntityListCard and migrate routines/history list rows`

---

## Tarea 4: `FitTrackKeyValueRow`

**Archivos:**
- Nuevo: `core/design/KeyValueRow.kt`
- `feature/history/HistoryScreen.kt` (`HistoryDetailSummary`)
- `feature/stats/StatsScreen.kt` (`RecordRow`, `ProgressPointDetails`, `SelectedDayDetail`)

- [ ] Crear `FitTrackKeyValueRow` segun R4: `style` = Flat / Pill
- [ ] Migrar `HistoryDetailSummary` a `style = Flat`
- [ ] Migrar `RecordRow` (dentro de `ExerciseRecordsCard`) a `style = Pill`
- [ ] Migrar `ProgressPointDetails` y `SelectedDayDetail` a `style = Pill`
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract FitTrackKeyValueRow and migrate history/stats detail rows`

---

## Tarea 5: `FitTrackDropdownField`

**Archivos:**
- Nuevo: `core/design/Dropdown.kt`
- `feature/history/HistoryScreen.kt` (`RoutineFilterDropdown`)
- `feature/stats/StatsScreen.kt` (`StatsDropdown`, dropdown inline de `ProgressChartCard`)

- [ ] Crear `FitTrackDropdownField` segun R5: label flotante arriba (estilo de `RoutineFilterDropdown`, que es el mas completo), `label`, `value`, `options`, `onSelect`
- [ ] Migrar `RoutineFilterDropdown` a usar el nuevo componente por debajo (o sustituirlo directamente si no aporta nada propio)
- [ ] Migrar `StatsDropdown` — **debe desaparecer como composable independiente** (criterio de aceptacion 5)
- [ ] Migrar el dropdown duplicado dentro de `ProgressChartCard` al mismo componente
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract FitTrackDropdownField and remove duplicated dropdowns`

---

## Tarea 6: `FitTrackSetRow`

**Archivos:**
- Nuevo: `core/design/SetRow.kt`
- `feature/workout/WorkoutScreen.kt` (`WorkoutSetRow`, `workoutSetFieldColors()`)
- `feature/history/HistoryScreen.kt` (`HistorySetRow`)

- [ ] Crear `FitTrackSetRow` segun R6: circulo numerado 30dp, `mode` = Edit / ReadOnly
- [ ] Mover `workoutSetFieldColors()` dentro del componente (modo Edit), no dejarla suelta en `WorkoutScreen.kt`
- [ ] Migrar `WorkoutSetRow` a `mode = Edit`
- [ ] Migrar `HistorySetRow` al modo que corresponda segun si esa fila esta en edicion de historial o no
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract FitTrackSetRow and migrate workout/history set rows`

---

## Tarea 7: Sustituir el calendario de Stats por `HeatmapCalendar` — DESCARTADA

**Decision (2026-08-29), tras revisar el codigo real y comparar visualmente ambos componentes:** `HeatmapCalendar` (tira de 53 semanas estilo GitHub, sin navegacion) y `ConsistencyCalendarCard`/`MonthConsistencyGrid` (selector de un mes con flechas y seleccion de dia que abre detalle) **no son el mismo patron duplicado** — son dos UX distintas para el mismo dato de actividad. Forzar la fusion complicaria `HeatmapCalendar` sin necesidad real.

**No tocar `StatsScreen.kt` en esta rama.** `ConsistencyCalendarCard` se queda como esta. Esto no es deuda pendiente, es una decision de diseño tomada.

---

## Verificacion final antes de avisar al dueño

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] Repaso del diff: ninguna feature define ya su propia version de los 6 patrones migrados
- [ ] Confirmar que `WorkoutPreviewCard` no usa `Card` de M3 en crudo
- [ ] Confirmar que `StatsDropdown` no existe como composable independiente
- [ ] Confirmar que el punto rojo de `HistorySessionCard` sigue en la misma linea que el titulo
- [ ] Push y avisar, indicando explicitamente si la Tarea 3 (alternativas) se dejo fuera y por que

**Pendiente de pasada manual (la hace el dueño):**
1. Recorrer las 5 pestañas (Home, Rutinas, Entrenar, Historial, Datos) comparando con el canvas aprobado.
2. Verificar en Entrenar que completar/descompletar series y sets sigue funcionando igual.
3. Verificar en Historial que la tarjeta de sesion incompleta muestra el punto rojo junto al titulo.
4. Verificar en Datos que los 3 dropdowns (filtro de rutina, filtro de dia, metrica de progreso) siguen filtrando igual.

**Merge:** a `develop` cuando compile y pasen los tests. A `main` solo tras la pasada manual del dueño.
