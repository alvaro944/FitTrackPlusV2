# Spec: Unificacion de componentes visuales repetidos en el design system

**Fecha:** 2026-08-28
**Origen:** auditoria de UI de las 5 pestañas principales (Home, Rutinas, Entrenar, Historial, Datos), hecha con 5 subagentes de exploracion en paralelo.
**Plan de ejecucion:** `docs/superpowers/plans/2026-08-28-design-system-unification.md`
**Prerequisito:** ninguno. No toca persistencia ni dominio, solo Compose UI.

---

## Por que

`core/design/` ya tiene una base solida (`FitTrackCard`, `FitTrackBadge`, `FitTrackMetric`, `FitTrackScreenHeader`, `FitTrackEmptyState`, dialogos, skeletons) y las 5 features la usan de forma consistente para lo grande. El problema esta en un escalon mas abajo: **patrones visuales pequeños que cada feature ha resuelto por su cuenta**, con pequeñas inconsistencias de tamaño, radio o color entre ellos. No es un problema de arquitectura, es deuda de repeticion: mismo concepto visual, 2-3 implementaciones distintas, cada una con su propio ajuste fino.

Esto se detecto lanzando un subagente de exploracion de solo lectura por pestaña (Home, Rutinas, Entrenar, Historial, Datos) mas un inventario de `core/design/`, y cruzando los 5 informes. El analisis completo con mockups antes/despues esta publicado en un Artifact (canvas de diseño) que el dueño ya reviso y aprobo.

## Estado actual verificado

### 1. Circulo con icono/numero (avatar/badge)
- `feature/home/HomeScreen.kt` — `QuickActionCard`: caja 44dp, radio `shapes.large` (16dp), icono.
- `feature/home/HomeScreen.kt` — pasos de onboarding "Recorrido base": circulo 28dp con numero, fondo `primarySoft`.
- `feature/workout/WorkoutScreen.kt` — `WorkoutSetCompletionButton`: circulo 32dp, borde 1.5dp o relleno `primary` + check, logica de color inline.
- `feature/history/HistoryScreen.kt` — `HistorySetRow`: circulo 30dp, fondo `surface` con borde 1dp.
- Tres tamaños distintos (44/32/30/28dp), sin componente compartido.

### 2. Hero card oscura (badge + titulo + CTA)
- `feature/home/HomeScreen.kt` — hero card inline (no extraida a composable): `Box.clip(shapes.extraLarge).background(primaryDark)`, padding `FitSpacing.cardPadding` (22dp), texto `Color.White` a mano.
- `feature/workout/WorkoutScreen.kt` — `WorkoutPreviewCard`: usa `androidx.compose.material3.Card` en crudo (no `FitTrackCard`), radio `shapes.extraLarge` pero padding y boton con esquina distintos al de Home.

### 3. Tarjeta de item de lista (titulo + badge + meta + acciones)
- `feature/routines/RoutinesScreen.kt` — `RoutineListItem` y `ArchivedRoutineListItem`: casi duplicados entre si.
- `feature/routines/RoutinesScreen.kt` — filas de `ExerciseAlternativesEditorDialog`: mismo patron visual reimplementado una tercera vez.
- `feature/history/HistoryScreen.kt` — `HistorySessionCard`: mismo layout (titulo + meta + badge), con un punto rojo de "incompleta" **en la misma linea que el titulo** (dueño confirmo mantener asi).

### 4. Fila clave-valor / pill de dato
- `feature/history/HistoryScreen.kt` — `HistoryDetailSummary`: pares label/valor como `Text`/`Text` sueltos, sin contenedor.
- `feature/stats/StatsScreen.kt` — `RecordRow`: pill con `Box.background(accentSoft, shapes.large)`, repetida 4 veces a mano dentro de `ExerciseRecordsCard`.
- `feature/stats/StatsScreen.kt` — `ProgressPointDetails` y `SelectedDayDetail`: mismo `Box.background(accentSoft, shapes.large)` para un bloque de detalle destacado.

### 5. Selector desplegable (`ExposedDropdownMenuBox`)
- `feature/history/HistoryScreen.kt` — `RoutineFilterDropdown`: label flotante arriba, borde 1dp.
- `feature/stats/StatsScreen.kt` — `StatsDropdown` (privado): sin label flotante, borde 1.5dp.
- `feature/stats/StatsScreen.kt` — `ProgressChartCard`: reimplementa el mismo dropdown **otra vez**, sin reutilizar `StatsDropdown` que esta en el mismo fichero.

### 6. Fila de serie (set row: circulo numerado + campos)
- `feature/workout/WorkoutScreen.kt` — `WorkoutSetRow`: circulo 30dp, fondo `primarySoft` con borde cuando la fila esta activa, campos editables (`FitTrackStepper` + `OutlinedTextField` con `workoutSetFieldColors()` inline).
- `feature/history/HistoryScreen.kt` — `HistorySetRow`: mismo circulo (mismo tamaño, 30dp), fondo `surfaceAlt` sin borde, texto plano en vez de campos editables.

### Hallazgo adicional (no es duplicacion de patron, es un componente sin usar)
- `feature/stats/StatsScreen.kt` — `MonthConsistencyGrid`/`ConsistencyDayCell`: calendario mensual hecho a mano desde cero. Ya existe `core/design/components/HeatmapCalendar.kt` sin ningun consumidor. Esto no es "unificar dos versiones", es sustituir una reimplementacion por el componente que ya existe.

## Decisiones de diseño ya tomadas (aprobadas por el dueño sobre el canvas)

- En la tarjeta de sesion de Historial (patron 3), **el punto rojo de "incompleta" va en la misma linea que el titulo**, no en su propia fila. Es el unico ajuste que pidio el dueño sobre la propuesta inicial del canvas.
- El resto de propuestas del canvas (tamaños, props, nombres) se aceptan tal cual estan publicadas.

## Requisitos

### R1 — `FitTrackIconBadge`

Nuevo componente en `core/design/` (fichero nuevo, p.ej. `IconBadge.kt`).

- Tamaño unico: 40dp, forma circulo (`CircleShape`), sin excepciones por feature.
- `variant`: `Icon` (recibe un `ImageVector` o `@Composable` slot) o `Number` (recibe un `Int`/`String`).
- `tone`: `Soft` (fondo `primarySoft`, contenido `primaryDark`), `Outlined` (borde `primary`, sin relleno), `Filled` (fondo `primary`, contenido `onPrimary`, para el estado "completado").
- Migrar a este componente: `QuickActionCard` (icono, tone Soft), pasos de onboarding en Home (numero, tone Soft), `WorkoutSetCompletionButton` (numero, tone Outlined/Filled segun estado).
- **No** migrar `HistorySetRow` aqui: su circulo se resuelve en R6 como parte de `FitTrackSetRow`.

### R2 — `FitTrackHeroCard`

Nuevo componente en `core/design/Cards.kt` o fichero nuevo.

- Fondo `primaryDark`, radio `shapes.extraLarge` (20dp), padding `FitSpacing.cardPadding` (22dp) — fijos, no configurables por quien lo usa.
- Slots: `badge: String?`, `title: @Composable ColumnScope.() -> Unit` (o `String` simple si no hace falta mas flexibilidad), `content` opcional para lineas intermedias, `cta: String` + `onCtaClick`.
- Migrar: hero card inline de `HomeScreen.kt` y `WorkoutPreviewCard` de `WorkoutScreen.kt`. `WorkoutPreviewCard` deja de usar `androidx.compose.material3.Card` en crudo.

### R3 — `FitTrackEntityListCard`

Nuevo componente en `core/design/Cards.kt` o fichero nuevo.

- Slots: `leadingDot: Color?` (null = sin punto), `title: String`, `badge: (text: String, tone: FitTrackBadgeTone)?`, `meta: String?` (linea secundaria), `actions: List<@Composable RowScope.() -> Unit>` (0 o mas).
- **El `leadingDot`, cuando existe, se pinta en la misma fila que `title`**, no en una fila propia (decision del dueño sobre el canvas).
- Migrar: `RoutineListItem`, `ArchivedRoutineListItem` (`RoutinesScreen.kt`), `HistorySessionCard` (`HistoryScreen.kt`). Evaluar tambien las filas de `ExerciseAlternativesEditorDialog`; si su forma no encaja exactamente, dejarlas fuera y anotarlo en el PR en vez de forzar el encaje.

### R4 — `FitTrackKeyValueRow`

Nuevo componente en `core/design/` (fichero nuevo o junto a `Labels.kt`).

- `style`: `Flat` (fila `label`/`value` en texto plano, `SpaceBetween`) o `Pill` (`Box.background(accentSoft, shapes.large)` con `label` pequeño arriba y `value` debajo, como el `RecordRow` actual).
- Migrar: `HistoryDetailSummary` (style Flat), `RecordRow` en `ExerciseRecordsCard` (style Pill), `ProgressPointDetails` y `SelectedDayDetail` (style Pill) — todos en `StatsScreen.kt`.

### R5 — `FitTrackDropdownField`

Nuevo componente en `core/design/` (fichero nuevo, p.ej. `Dropdown.kt`), envolviendo `ExposedDropdownMenuBox`.

- Props: `label: String`, `value: String`, `options: List<String>` (o generico con `T` + `labelOf`), `onSelect: (T) -> Unit`.
- Label flotante arriba (el estilo de `RoutineFilterDropdown`, que es el mas completo de los dos actuales).
- Migrar: `RoutineFilterDropdown` (`HistoryScreen.kt`), `StatsDropdown` y el dropdown duplicado dentro de `ProgressChartCard` (`StatsScreen.kt`) — las tres pasan a usar el mismo componente; `StatsDropdown` como composable propio desaparece.

### R6 — `FitTrackSetRow`

Nuevo componente en `core/design/` (fichero nuevo, p.ej. `SetRow.kt`).

- Circulo numerado unico: 30dp (tamaño que ya usan ambas versiones actuales, se mantiene).
- `mode`: `Edit` (fondo `primarySoft` + borde cuando esta activa, campos `weight`/`reps` editables via `FitTrackStepper`) o `ReadOnly` (fondo `surfaceAlt`, texto plano `"{weight} x {reps}"`).
- La logica de color de campo (`workoutSetFieldColors()`, hoy inline en `WorkoutScreen.kt`) se mueve dentro de este componente, no se duplica.
- Migrar: `WorkoutSetRow` (`WorkoutScreen.kt`, mode Edit) y `HistorySetRow` (`HistoryScreen.kt`, mode ReadOnly o Edit segun si la fila esta en edicion del historial).

### R7 — DESCARTADA: el calendario de Stats no se toca

Revision posterior (2026-08-29): `HeatmapCalendar` (tira de 53 semanas sin navegacion, estilo GitHub) y `ConsistencyCalendarCard`/`MonthConsistencyGrid` (selector de un mes con flechas y seleccion de dia) resuelven UX distintas sobre el mismo dato, no el mismo patron reimplementado dos veces. Decision: no fusionar. `StatsScreen.kt` no se toca en esta rama.

## Fuera de alcance

- Cualquier cambio de dominio, persistencia o Room. Esta rama es Compose puro.
- El bar chart a medida de `WeeklyStepsCard` (Stats) — es un tipo de grafico nuevo, no una duplicacion; no entra en esta ronda.
- `RoutineDayEditor`/`RoutineExerciseEditor`: el toolbar de reordenar/duplicar/eliminar y la cabecera expandible con barra de acento (hallazgos de la auditoria de Rutinas) quedan para una ronda posterior — no estaban en el canvas aprobado por el dueño.
- Cambiar `OutlinedTextField` crudo por un wrapper de design system (hallazgo menor, no bloqueante).

## Criterios de aceptacion

1. `test` y `build` en verde.
2. Los 6 componentes nuevos (R1-R6) existen en `core/design/` y no en ninguna feature.
3. Cero regresiones visuales fuera de lo descrito: mismo contenido, mismo texto, mismo comportamiento de click en cada sitio migrado.
4. `WorkoutPreviewCard` ya no usa `androidx.compose.material3.Card` en crudo.
5. `StatsDropdown` como composable independiente desaparece de `StatsScreen.kt`; queda un unico dropdown reutilizado 3 veces.
6. El punto rojo de `HistorySessionCard` sigue en la misma linea que el titulo tras la migracion a `FitTrackEntityListCard`.
7. Pasada manual del dueño en emulador/dispositivo de las 5 pestañas antes de mergear a `main`.
