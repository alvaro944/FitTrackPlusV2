# Auditoria UI — Mapa de componentes y plan de re-etiquetado

Fecha: 2026-07-04
Metodo: dos revisiones read-only sobre el codigo real — inventario de `core/design/` y mapa pantalla por pantalla de `feature/` + `AppShell`. Evidencia por archivo y linea.
Objetivo: preparar una futura remodelacion visual de forma que cambiar un estilo (boton, card, dialogo) sea un cambio en UN sitio que propague a toda la app, con posibilidad de divergir estilos por contexto cuando tenga sentido.

Clasificacion de cada elemento:

- **DS** — componente del design system (`core/design/`): cambiarlo propaga.
- **RAW** — Material 3 crudo llamado directo en la pantalla: cambiar el estilo exige tocar cada call site.
- **AD-HOC** — construido a mano en la pantalla con estilos inline: igual que RAW pero ademas con valores duplicados.

---

## 1. Diagnostico

**Los cimientos (tokens) estan bien. La capa de componentes interactivos no existe.**

- Colores: dos esquemas estaticos light/dark completos + 10 tokens extendidos semanticos (`FitTrackPlusExtraColors`: surfaceAlt, primarySoft, accentWarm, errorSoft...). Adopcion buena.
- Espaciado: `FitSpacing` con 186 usos en features. Adopcion buena.
- `FitTrackCard` existe y se usa 30 veces... pero hay ~66 `Card`/containers crudos que lo puentean.
- **Botones: CERO componentes propios.** ~97 llamadas crudas a `Button`/`FilledTonalButton`/`OutlinedButton`/`TextButton` + 30+ `IconButton` repartidas por todas las pantallas. Propagacion de un restyle de boton hoy: **cero**.
- **Text fields: cero wrapper.** 18 `OutlinedTextField` crudos (solo Workout customiza colores, con una funcion privada suya).
- **Dialogos: cero patron comun.** 9 dialogos en dos estilos distintos (7 `AlertDialog` + 2 `Dialog(Surface)` custom en Workout).
- **Duplicacion real**: el mismo stepper implementado 3 veces, el selector de unidad 2 veces pixel-identico, el selector de tema 2 veces, el pill de hero 2 veces con distinto nombre, la fila de serie (numero+peso+reps) 2 veces con tamaños distintos, el "icono en caja de acento" 6 veces con 6 tamaños.

---

## 2. Inventario del design system actual

### 2.1 Tokens

| Categoria | Estado | Detalle |
|---|---|---|
| Color M3 | OK | `LightColors`/`DarkColors` estaticos en Theme.kt:21-53. Sin dynamic color |
| Color extendido | OK | `FitTrackPlusExtraColors` (Theme.kt:56-93): surfaceAlt, surfaceCard, borderLight, textTertiary, primaryDark, primarySoft, primaryMid, accentWarm, accentSoft, errorSoft |
| **success/warning** | **FALTA** | Por eso `Color(0xFF2E7D32)` hardcodeado 3x en WorkoutScreen (completion + hint de progresion) |
| Tipografia | OK parcial | `AppTypography` (Theme.kt:97-170), todo SansSerif de sistema. Faltan slots displayMedium/Small, headlineSmall, titleSmall |
| **Tipografia mono numerica** | **FALTA** | `FitTrackMetric` (Indicators.kt:59-63) y `FitTrackRadialTimer` (170-175) hardcodean Monospace + tamaños cada uno por su cuenta |
| Shapes | OK | `AppShapes` (Theme.kt:172-178). extraSmall == small (8dp): sin diferenciacion |
| Espaciado | OK | `FitSpacing` con aliases semanticos (card, section, screenHorizontal...). 4 aliases duplican valores numericos exactos; 3 tokens intermedios (tiny/smMd/mdLg) delatan escala con huecos |
| Elevacion | FALTA | Sin tokens. `shadowElevation` literal: 10dp en nav bar (AppShell:179), 6dp en dialogos custom, 2dp en menu button |
| Colores celebracion | FUERA DE PALETA | ConfettiAnimation.kt:88-93: 6 hex hardcodeados ajenos a la paleta |
| System bar | DUPLICADO | SystemBarAppearance.kt:5-6 duplica los backgrounds del scheme como constantes sueltas: drift asegurado si cambia el fondo |

### 2.2 Componentes existentes y calidad de API

| Componente | Archivo | Calidad | Problemas |
|---|---|---|---|
| `FitTrackCard` | Cards.kt:17 | Buena | Padding interno no configurable; sin slot onClick |
| `FitTrackSectionLabel` | Labels.kt:20 | Buena | Usa `TextButton` crudo dentro: un restyle de botones no le llegaria |
| `FitTrackBadge` + tonos | Labels.kt:49 | Muy buena | Magic number `xs + 1.dp` |
| `FitTrackScreenHeader` | Labels.kt:83 | Muy buena | — |
| `FitTrackMetric` | Indicators.kt:33 | Media | Tipografia mono hardcodeada dentro |
| `FitTrackProgressBar` | Indicators.kt:82 | Buena | Altura 6dp fija |
| `FitTrackRadialTimer` | Indicators.kt:115 | Media | 132dp y tipografia fijos |
| `FitTrackEmptyState` | States.kt:22 | Buena | Icon box 56dp fijo (ver patron duplicado #10) |
| `FitTrackAppShell` | AppShell.kt:72 | Media | Contiene selectores duplicados con Settings (ver #2, #3) |
| `SkeletonBlock/Text/Card`, `shimmer` | components/ | Buena | Sin prefijo FitTrack |
| `HeatmapCalendar`, `LineChart`, `ConfettiAnimation` | components/ | Buena | Sin prefijo; textos en español hardcodeados dentro (HeatmapCalendar:150, LineChart:35) |

### 2.3 Naming actual

Conviven 4 convenciones: `FitTrack*` (componentes), `FitTrackPlus*` (tema), `Fit*` (FitSpacing), `App*` (AppTypography, AppShapes, AppThemeMode) y sin prefijo (Skeletons, charts). Los nombres son semanticos (bien): no hay `GreenCard` ni nombres presentacionales.

---

## 3. Mapa por pantalla

Resumen por pantalla; detalle de referencia con file:line en los informes de origen. Los porcentajes son sobre elementos interactivos/estilizados.

### 3.1 LaunchIntroScreen — 100% AD-HOC (deliberado)

Splash animado: 7 colores hex propios (`IntroBackground`, `IntroEmerald = #1F6B57`...), gradientes, wordmark, loader de barra fina propio (no usa `FitTrackProgressBar`). Es la unica pantalla donde el aislamiento es defendible (brand moment), pero **duplica los valores del tema** (`IntroEmerald` == `primary` light): si cambia la paleta, hay que acordarse de este archivo.

### 3.2 OnboardingScreen — mixta

| Elemento | Composable | Fuente |
|---|---|---|
| Card de pagina | `FitTrackCard` | DS |
| Boton saltar | `TextButton` | RAW |
| CTA siguiente/empezar | `Button` full-width | RAW |
| Icono en pill (72dp) | Box + primarySoft | AD-HOC (patron #10) |
| Dots indicator | Row + Box custom | AD-HOC |

### 3.3 HomeScreen — la mas rica y la mas mixta

| Zona | Elementos DS | Elementos RAW/AD-HOC |
|---|---|---|
| Header | — | Textos crudos con colores DS (bien tokenizados) |
| Semana (`WeekActivityStrip`) | FitTrackCard, FitTrackBadge, FitTrackProgressBar | `WeekDayCell` x7 AD-HOC (Home:346-378) |
| Hero CTA oscuro | FitTrackBadge, Skeletons | **Contenedor AD-HOC** `Box(primaryDark, extraLarge)` (Home:157-229), `MiniHeroTag` AD-HOC (472), `Button` RAW (206) |
| Accesos rapidos | FitTrackSectionLabel, FitTrackCard clickable | Icon box 44dp AD-HOC (503) |
| Recorrido base | FitTrackSectionLabel, FitTrackCard | Step badge 28dp AD-HOC (250) |

### 3.4 RoutinesScreen — la de mayor superficie de formulario

Lista: FitTrackScreenHeader, FitTrackCard (items, banner, plantillas), FitTrackBadge, FitTrackEmptyState, SkeletonCard — buena adopcion DS en contenedores. Pero: `FilterChip` x2 RAW (319-328), botones Edit/Archive/Activate/Restore/Usar todos RAW (`OutlinedButton`/`FilledTonalButton`), FABs RAW (151-173).

Editor: TODA la entrada es RAW/AD-HOC — `OutlinedTextField` x4+ (nombre rutina 785, dia 1039, ejercicio 1320, alternativas 1422-1445), `ExerciseSetsStepper` AD-HOC (1503), chips de reps RAW (1589), 8 `IconButton` crudos por dia (expandir, subir, bajar, duplicar, borrar...), footer `OutlinedButton`+`Button` (865-887).

Dialogos: 5 `AlertDialog` RAW (archivar 119, descartar 192, reps custom 1132, notas 1168, alternativas 1387).

### 3.5 WorkoutScreen — la critica; el mejor y el peor ejemplo a la vez

Buena adopcion DS en lo informativo: FitTrackScreenHeader, FitTrackCard (sesion activa 483, timer 577, ejercicio 785), FitTrackBadge x6, FitTrackMetric x2, FitTrackProgressBar, FitTrackRadialTimer, FitTrackEmptyState x2, Skeletons.

Todo lo interactivo es RAW/AD-HOC:

| Elemento | Composable | Fuente | Linea |
|---|---|---|---|
| Card preview (hero oscuro) | **`Card` crudo** con primaryDark — ni siquiera FitTrackCard | RAW | 412 |
| `HeroTag` pills | copia de MiniHeroTag de Home con otro nombre | AD-HOC | 462 |
| Iniciar / Finalizar | `Button` | RAW | 444, 541 |
| Controles del timer (pausa/reset/cancelar/60/90/120) | `FilledTonalButton` x6 | RAW | 659-710 |
| Campos peso/reps | `OutlinedTextField` + `workoutSetFieldColors()` privada | RAW custom | 1119, 1254, colores 1393 |
| Steppers peso/reps | `SetStepperButton` propio (28dp, long-press) | AD-HOC | 1345 |
| Circulo numero de serie | Box CircleShape 40dp | AD-HOC | 1199 |
| Label de completado | Icon + **`Color(0xFF2E7D32)`** | AD-HOC | 881-910 |
| Hint de progresion | IconButton + **`Color(0xFF2E7D32)`** | AD-HOC | 1311-1342 |
| Dialogo finalizar | `Dialog(Surface(extraLarge, 6dp))` custom | AD-HOC | 145 |
| Dialogo alternativas | mismo shell custom | AD-HOC | 926 |

### 3.6 HistoryScreen — la mas fiel al DS

FitTrackScreenHeader x2, FitTrackCard en todo (filtros, items, resumen, comparativa, ejercicios), FitTrackBadge (semana, deltas), FitTrackMetric, FitTrackEmptyState x2, Skeletons x3. RAW: FilterChips (259-278), IconButtons (editar/volver), OutlinedTextField x2 en modo edicion (671-686), AlertDialog descartar (301). AD-HOC: `HistorySetRow` con circulo de 30dp (651-706) — el gemelo no compartido del set row de Workout (40dp).

### 3.7 StatsScreen — buena en cards, custom en visualizacion

DS: FitTrackScreenHeader, FitTrackCard en todas las secciones, FitTrackMetric, FitTrackBadge, FitTrackProgressBar, HeatmapCalendar, LineChart, FitTrackEmptyState, Skeletons. RAW: FilterChips, IconButtons de navegacion de semana, `ExposedDropdownMenuBox` + OutlinedTextField readOnly (487-514), TextButton. AD-HOC: `DayBarColumn` (732-785, primo no compartido del WeekDayCell de Home), `SelectedDayDetail` (803), `ProgressPointDetails` (564), `RecordRow` (444) — todos containers `surfaceAlt`/`accentSoft` + shapes.large hechos a mano.

### 3.8 SettingsScreen — el nido de duplicados

DS: FitTrackSectionLabel x5, FitTrackCard x4. RAW: TopAppBar (unica pantalla que lo usa — inconsistencia de patron de cabecera con el resto, que usa FitTrackScreenHeader), Buttons full-width x3, OutlinedButton destructivo con color error inline (240-249), AlertDialog. AD-HOC: `UnitSelector`/`UnitSegment` (357-415), `ThemeModeSelector`/`ThemeModeOption` con radio custom de 22dp (418-509), `StepGoalStepper` (324) — los tres duplicados en otra parte.

### 3.9 AppShell — el chrome

DS: FitTrackSectionLabel, FitTrackBadge. RAW: NavigationBar + 5 items con colores inline (`indicatorColor = primary.copy(0.12f)`, 193), ModalNavigationDrawer. AD-HOC: `ShellMenuButton` (437-461), `DrawerActionRow` con icon box 36dp (297-352), `ThemeModeInlineSelector` (355, duplicado de Settings), `WeightUnitInlineSelector` (401, duplicado de Settings).

---

## 4. Censos transversales

### Botones (el hallazgo central)

| Rol semantico | DS | RAW | AD-HOC | Donde |
|---|---|---|---|---|
| CTA primario | 0 | 12 | 0 | Home hero, Onboarding, Routines crear/guardar, Workout iniciar/finalizar, Settings x3 |
| Secundario tonal | 0 | 10 | 0 | Routines activar/restaurar/usar, Workout timer x6 + crear alternativa |
| Outlined / cancelar | 0 | 6 | 0 | Routines editar/archivar/añadir x2/cancelar, Settings desconectar |
| Texto / accion de dialogo | 0 | 18+ | 0 | todos los dialogos, saltar, ocultar |
| Icon button | 0 | 30+ | 0 | expandir, volver, editar, mover, duplicar, borrar, refrescar, cerrar... |
| Stepper inline | 0 | 0 | 3 impl. | Workout, Routines, Settings — tres implementaciones distintas |
| FAB | 0 | 2 | 0 | Routines |
| Selector segmentado | 0 | 0 | 4 | unidad x2 + tema x2 (duplicados) |

**Total: 0% de botones pasan por el design system.**

### Text fields

18 `OutlinedTextField` crudos en 4 pantallas. Solo Workout customiza colores (funcion privada `workoutSetFieldColors`, WorkoutScreen:1393). Cero propagacion.

### Cards/containers

`FitTrackCard` domina el contenido informativo (bien). Fuera del sistema: hero oscuro x2 (Home como Box, Workout como Card crudo), superficies de formulario (`surfaceAlt` + shapes.large a mano en Routines editor, set rows, record rows, detalles de Stats), tiles de tema, filas de drawer.

### Dialogos

9 dialogos, 2 patrones: 7 `AlertDialog` estandar + 2 `Dialog(Surface(extraLarge, 6dp))` custom en Workout (el custom existe porque AlertDialog no soporta contenido scrollable + boton de cierre en cabecera). Sin componente comun.

---

## 5. Top 10 patrones duplicados (candidatos directos a componente)

1. **Stepper +/-** — 3 implementaciones: `SetStepperButton` (Workout:1345, con long-press), `ExerciseSetsStepper` (Routines:1531), `StepGoalStepper` (Settings:324)
2. **Selector segmentado de unidad** — pixel-identico en Settings:357 y AppShell:401
3. **Selector de modo de tema** — casi identico en Settings:418 y AppShell:355
4. **Pill sobre hero oscuro** — `MiniHeroTag` (Home:472) y `HeroTag` (Workout:462): mismo codigo, distinto nombre
5. **Cabecera expandible con chevron + acciones** — dia del editor (Routines:926) vs ejercicio de workout (Workout:789); uno usa IconButton para el chevron, el otro Icon a pelo
6. **Fila de serie** (circulo numero + peso + reps) — Workout:1160 (40dp, editable) vs History:641 (30dp, lectura)
7. **AlertDialog confirmar/cancelar con 2 TextButton** — 7 sitios en 4 pantallas
8. **Header con trailing IconButton** — bien abstraido en FitTrackScreenHeader pero compuesto inline distinto en cada pantalla
9. **Grupo de FilterChips en fila scrollable dentro de card** — History x2, Stats x1, Routines x1
10. **Icono en caja de acento** — 6 implementaciones con 6 tamaños (28/36/40/44/56/72dp): Home x2, Routines, Onboarding, AppShell, FitTrackEmptyState

---

## 6. Propuesta de re-etiquetado: taxonomia semantica

> **DECISION (2026-07-04)**: el enfoque ejecutable es **agrupar por funcion, no uniformar**. No se busca un boton unico para toda la app: se agrupan los botones que ya son funcionalmente lo mismo en 2+ sitios (añadir, CTA primario, tonal, outlined, acciones de dialogo) y se dejan en paz los unicos de cada pantalla (timer, FABs, icon buttons). La spec detallada para Codex esta en `docs/superpowers/specs/2026-07-04-ui-component-grouping.md` y REEMPLAZA a las secciones 6.4 y 7 de este documento como plan operativo. Las secciones siguientes se conservan como analisis de referencia.

### 6.1 Principio

Los componentes se nombran por **rol**, no por apariencia ni por pantalla. La divergencia por contexto (tu ejemplo: el boton de un login vs el de registrar un entreno) NO se resuelve creando `LoginButton` y `WorkoutButton`, sino con **variantes semanticas de un unico componente**: cuando quieras que diverjan, cambias la variante en un sitio; cuando quieras que converjan, comparten base. Un boton por pantalla es el mismo problema que hoy, con nombres mas bonitos.

### 6.2 Convencion de nombres (decision)

- Composables publicos del sistema: prefijo **`FitTrack`** (ya dominante). Renombrar al migrar: `SkeletonBlock/Text/Card` → `FitTrackSkeleton*`, `HeatmapCalendar` → `FitTrackHeatmap`, `LineChart` → `FitTrackLineChart`, `ConfettiAnimation` → `FitTrackConfetti`.
- Objetos de tokens: prefijo **`Fit`** (`FitSpacing` ya existe; añadir `FitElevation`, `FitTypographyExtras`). `AppTypography`/`AppShapes` pueden quedarse (internos del tema).
- Los enums de tono/variante viven junto al componente: `FitTrackButtonVariant`, como ya hacen `FitTrackBadgeTone` y `FitTrackMetricAccent` (patron correcto ya establecido — extenderlo).

### 6.3 Tokens nuevos (previos a cualquier componente)

| Token | Sustituye a |
|---|---|
| `success`, `onSuccess`, `successSoft` en ExtraColors | `Color(0xFF2E7D32)` x3 en Workout |
| `FitTypographyExtras.monoLarge/monoMedium` | Monospace hardcodeado en Metric e RadialTimer |
| `FitElevation.none/card/dialog/navBar` (0/0/6/10dp) | literales de elevacion dispersos |
| `celebrationColors` derivados de paleta | 6 hex del confetti |
| SystemBar refs al scheme | constantes duplicadas en SystemBarAppearance.kt:5-6 |

### 6.4 Primitivas nuevas (por radio de impacto)

| Componente | API minima | Sustituye |
|---|---|---|
| `FitTrackButton` | `variant: Primary / Secondary / Outlined / Text / Destructive`, `size: Default / Compact`, `fullWidth`, slots icono | ~46 botones de texto RAW |
| `FitTrackIconButton` | `icon`, `contentDescription`, `tone` | 30+ IconButton |
| `FitTrackTextField` | wrapper de OutlinedTextField con shape/colores del sistema; `style: Default / Numeric` (Numeric absorbe `workoutSetFieldColors`) | 18 campos |
| `FitTrackDialog` | shell unico: titulo + cierre opcional + contenido scrollable + fila de acciones (`FitTrackButton(Text)`) | 7 AlertDialog + 2 Dialog custom |
| `FitTrackConfirmDialog` | azucar sobre FitTrackDialog: title/message/confirm/dismiss | patron #7 (7 sitios) |
| `FitTrackStepper` | `onIncrement/onDecrement`, `supportsLongPress`, `size` | patron #1 (3 impl.) |
| `FitTrackSegmentedSelector` | lista de opciones + seleccionada | patrones #2 y #3 (4 impl.) |
| `FitTrackIconBox` | `icon`, `size: Sm/Md/Lg/Xl`, `tone` | patron #10 (6 impl.) |
| `FitTrackHeroCard` | contenedor primaryDark/extraLarge + slot contenido + `FitTrackHeroTag` | hero de Home (Box) + preview de Workout (Card crudo) + patron #4 |
| `FitTrackSetRow` | base compartida circulo+peso+reps con slot editable/lectura | patron #6 (Workout + History) |
| `FitTrackFilterChipGroup` | chips en fila scrollable | patron #9 (4 sitios) |
| `FitTrackFab` | wrapper FAB/ExtendedFAB | Routines |

### 6.5 Donde SI divergen los contextos

Con la taxonomia anterior, los puntos de divergencia legitima quedan asi:

- **CTA sobre hero oscuro** (iniciar/finalizar entreno, hero de Home): es `FitTrackButton(variant = Primary)` dentro de `FitTrackHeroCard` — si un dia el CTA de entreno debe ser mas gordo/urgente, se añade `variant = Hero` sin tocar el resto.
- **Acciones destructivas** (desconectar, borrar): `variant = Destructive` — hoy es un OutlinedButton con color error inline.
- **Futuro login/auth (fase Coach)**: usara `FitTrackButton(Primary)` de serie; si la pantalla de auth pide otra personalidad, sera una variante nueva o un theme override local — decision de UN punto, no una caceria.
- **Splash/LaunchIntro**: queda deliberadamente fuera del sistema (brand moment), pero debe leer los colores del tema en vez de duplicarlos.

---

## 7. Plan de migracion por fases (D = design)

Regla: **ningun cambio visual hasta D4 completada**. D1-D3 son refactors de equivalencia visual (mismos pixeles, distinta fuente), verificables con capturas antes/despues. Asi, cuando llegue la remodelacion real, es un cambio de tokens/variantes con toda la app respondiendo a la vez.

### D0 — Decisiones (este documento)
Confirmar: convencion de nombres (6.2), tokens (6.3), inventario de primitivas (6.4).

### D1 — Tokens (sin cambio visual, riesgo minimo)
success/mono/elevacion/celebracion/systembar. Incluye sustituir los 3 usos del verde hardcodeado. Rama corta.

### D2 — Primitivas de alto radio
`FitTrackButton`, `FitTrackIconButton`, `FitTrackTextField`, `FitTrackDialog`/`FitTrackConfirmDialog`. Crearlas + migrar UNA pantalla piloto (Settings: pequeña, contiene todos los tipos) como prueba del API.

### D3 — Primitivas de patron
`FitTrackStepper`, `FitTrackSegmentedSelector`, `FitTrackIconBox`, `FitTrackHeroCard`, `FitTrackSetRow`, `FitTrackFilterChipGroup`, `FitTrackFab`. Eliminar las implementaciones duplicadas (Settings/AppShell convergen aqui).

### D4 — Migracion pantalla a pantalla
Orden por riesgo creciente: Onboarding → Home → Stats → History → Routines → Workout (la critica, la ultima, con pasada manual obligatoria). Un commit por pantalla. Al terminar cada una: cero `Button(`/`OutlinedTextField(`/`AlertDialog(` crudos en esa pantalla (verificable con grep — criterio de done mecanico).

### D5 — La remodelacion real (el objetivo de todo esto)
Con D1-D4 hechas, el restyle es: tocar tokens + variantes de FitTrackButton/Card/Dialog y toda la app cambia coherentemente. Aqui es donde se decide la nueva direccion visual.

## 8. Encaje con el roadmap de release

Este plan NO bloquea R1-R4 (`docs/planning/roadmap-release.md`) y no debe adelantarse a ellas:

- D1 (tokens) puede colarse en cualquier rama que ya toque WorkoutScreen (R2 lo hara) — coste marginal minimo.
- D2-D4 son trabajo post-publicacion o de huecos: la remodelacion visual es fase futura por decision de producto (la vision dice "publicar primero").
- Regla anti-tentacion: si una tarea de R1-R3 toca una zona con duplicado conocido (p.ej. el dialogo de alternativas en R2), se aprovecha SOLO si el coste marginal es trivial; no se convierte una rama de fixes en una rama de refactor.
