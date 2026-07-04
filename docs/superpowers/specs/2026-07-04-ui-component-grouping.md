# Spec: Agrupacion de componentes UI (fases D1-D4)

Fecha: 2026-07-04
Ejecutor: Codex
Rama: `refactor/ui-component-grouping` (una sola rama para todo el grupo, commits por fase)
Contexto completo: `docs/design/ui-component-audit.md` (auditoria con evidencia file:line)

## Filosofia (leer antes de tocar nada)

NO se busca que todos los botones de la app sean iguales. Se busca **agrupar los que ya son funcionalmente lo mismo** y estan implementados N veces por separado. Botones distintos en pantallas distintas estan BIEN si son cosas distintas. El criterio:

- Si el mismo boton (misma funcion, mismo aspecto) aparece en 2+ sitios → componente compartido.
- Si es unico de una pantalla → se queda como esta. No lo toques.
- El objetivo es que la futura remodelacion visual pueda cambiar "el boton de añadir" o "los botones de dialogo" en UN sitio, y que el dueño luego vaya pantalla por pantalla ajustando lo que quiera.

**Regla de oro: equivalencia visual.** Salvo las convergencias explicitamente listadas en cada tarea, la app debe verse IGUAL antes y despues. Esto es re-etiquetado, no restyle. Cualquier duda entre dos aspectos → conservar el actual y anotar en el commit.

**Que NO tocar en toda la spec** (fuera de alcance, deliberadamente):

- `LaunchIntroScreen.kt` completo (brand moment aislado).
- Los 30+ `IconButton` sueltos (volver, refrescar, expandir, cerrar, mover, duplicar, borrar...). Riesgo bajo para el restyle; se agruparan mas adelante si hace falta.
- Los 2 FABs de RoutinesScreen.
- `TextButton` unicos: saltar onboarding (OnboardingScreen:83), "Ocultar" (StatsScreen:583).
- Las filas de serie (WorkoutSetRow / HistorySetRow), el icon-box de 6 tamaños y el resto de patrones del top-10 de la auditoria que no esten en D4.
- Cualquier logica de negocio, ViewModels, navegacion.

Verificacion minima por fase: `./gradlew test` y `./gradlew build` en verde + los greps de "done" de cada tarea. Commit por fase con conventional commits (sin Co-Authored-By). El usuario hace la pasada manual en emulador al final.

Convenciones de codigo: identificadores en ingles, componentes publicos con prefijo `FitTrack`, cada componente nuevo en `core/design/` (archivo indicado en cada tarea). KDoc breve en ingles por componente. Los enums de variante junto al componente (patron ya existente: `FitTrackBadgeTone`).

---

## Fase D1 — Tokens que faltan (sin cambio visual)

Commit sugerido: `refactor: add missing design tokens (success, mono, elevation)`

### D1.1 Color semantico de exito

**Archivo**: `core/design/Theme.kt`

1. Añadir a `FitTrackPlusExtraColors` (data class, Theme.kt:56-67) tres campos: `success: Color`, `onSuccess: Color`, `successSoft: Color`.
2. Valores en `LightExtraColors`: `success = Color(0xFF2E7D32)` (el verde que hoy esta hardcodeado — asi el cambio es invisible), `onSuccess = Color(0xFFFFFFFF)`, `successSoft = Color(0xFFDCEDDD)`.
3. Valores en `DarkExtraColors`: `success = Color(0xFF66BB6A)`, `onSuccess = Color(0xFF10130F)`, `successSoft = Color(0xFF223524)`. (Dark hoy tambien pinta `0xFF2E7D32`, que en fondo oscuro tiene poco contraste; este es el UNICO cambio visual permitido de la fase y es una mejora de accesibilidad. Anotarlo en el commit.)
4. Exponer las tres extension properties de `ColorScheme` igual que las existentes (Theme.kt:207-245).

**Migrar los 3 usos del verde hardcodeado**:

| Sitio | Linea | Cambio |
|---|---|---|
| `ExerciseCompletionLabel` | WorkoutScreen.kt:901 y 907 | `Color(0xFF2E7D32)` → `MaterialTheme.colorScheme.success` |
| `ProgressionHintButton` tinte UP | WorkoutScreen.kt:1328 | idem |

**Done**: `rg "0xFF2E7D32" app/src/main/kotlin` devuelve cero resultados.

### D1.2 Tipografia mono numerica

**Archivo nuevo**: `core/design/TypographyExtras.kt`

```kotlin
object FitTypographyExtras {
    val monoLarge: TextStyle  // FontFamily.Monospace, fontSize 30.sp, lineHeight 34.sp, FontWeight.SemiBold
    val monoMedium: TextStyle // FontFamily.Monospace, fontSize 24.sp, lineHeight 28.sp, FontWeight.SemiBold
    val monoTimer: TextStyle  // FontFamily.Monospace, fontSize 28.sp, lineHeight 32.sp, FontWeight.SemiBold
}
```

Copiar los valores EXACTOS que hoy estan inline en `FitTrackMetric` (Indicators.kt:59-63: normal 30sp / compact 24sp) y `FitTrackRadialTimer` (Indicators.kt:170-175: 28sp) — comprobar pesos y lineHeight reales en el codigo antes de escribir el objeto; los de arriba son orientativos. Sustituir los estilos inline de ambos componentes por referencias a estos tokens.

**Done**: `rg "FontFamily.Monospace" app/src/main/kotlin` solo aparece en TypographyExtras.kt.

### D1.3 Tokens de elevacion

**Archivo**: `core/design/Spacing.kt` (añadir al final) o archivo nuevo `Elevation.kt`:

```kotlin
object FitElevation {
    val none = 0.dp
    val menuButton = 2.dp
    val dialog = 6.dp
    val navBar = 10.dp
}
```

Migrar: AppShell.kt:179 (`10.dp` → `FitElevation.navBar`), AppShell.kt:~444 ShellMenuButton (`2.dp` → `FitElevation.menuButton`), WorkoutScreen.kt:149 y ~929 dialogos custom (`6.dp` → `FitElevation.dialog`).

### D1.4 Deduplicar colores de system bar

**Archivo**: `core/design/SystemBarAppearance.kt:5-6`

`LightSystemBarBackground`/`DarkSystemBarBackground` duplican los valores de `LightColors.background`/`DarkColors.background`. Hacer que referencien esas definiciones (importar de Theme.kt) en vez de repetir el hex. Cero cambio visual.

### D1.5 Colores de confetti desde la paleta

**Archivo**: `core/design/components/ConfettiAnimation.kt:88-93`

Sustituir la lista default de 6 hex por una lista construida desde el tema. Como el default se evalua fuera de composicion, la forma limpia: quitar el default del parametro y pasar los colores desde el call site (WorkoutScreen, overlay de PR ~linea 237-256) usando `MaterialTheme.colorScheme` (primary, primaryMid, accentWarm, tertiary, success, accentSoft). Elegir 5-6 con contraste alegre; es una celebracion. Cambio visual menor permitido (anotar en commit).

---

## Fase D2 — Grupos de botones

Commit sugerido: `refactor: group repeated buttons into shared components`

**Archivo nuevo**: `core/design/Buttons.kt` — todos los componentes de esta fase viven ahi.

Importante: los grupos de abajo ya son visualmente identicos entre si HOY (todos usan el default de Material 3). Agruparlos no cambia pixeles; solo cambia la fuente.

### D2.1 `FitTrackPrimaryButton` — el CTA relleno

```kotlin
@Composable
fun FitTrackPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,   // el call site decide fillMaxWidth
    enabled: Boolean = true,
    icon: ImageVector? = null,       // pintado leading, size 18.dp, spacing FitSpacing.sm
)
```

Implementacion: `Button(onClick, enabled, modifier)` con contenido `icon? + Text(label)`. Sin overrides de color: usa el default del tema (asi el restyle futuro = tocar este componente o el tema).

**Call sites a migrar** (verificar linea exacta al abrir cada archivo; la app puede haber cambiado unas lineas):

| Pantalla | Sitio | Linea aprox. | Nota |
|---|---|---|---|
| Home | CTA del hero (ir a entrenar / crear rutina) | HomeScreen.kt:206-218 | |
| Onboarding | Siguiente / Empezar | OnboardingScreen.kt:110-127 | fullWidth |
| Routines | Crear rutina (empty state) | RoutinesScreen.kt:424 | |
| Routines | Guardar (footer del editor) | RoutinesScreen.kt:865-887 | el Save del par |
| Workout | Iniciar entrenamiento | WorkoutScreen.kt:444-457 | |
| Workout | Finalizar entrenamiento | WorkoutScreen.kt:541-555 | fullWidth; mantener el `enabled` que exista |
| Settings | Instalar Health Connect | SettingsScreen.kt:208-221 | fullWidth |
| Settings | Conectar Health Connect | SettingsScreen.kt:252-259 | fullWidth |
| Settings | Cargar datos demo | SettingsScreen.kt:279-286 | fullWidth |

### D2.2 `FitTrackTonalButton` — accion secundaria tonal

```kotlin
@Composable
fun FitTrackTonalButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
)
```

Wrapper de `FilledTonalButton`, mismo contrato que D2.1.

**Call sites**:

| Pantalla | Sitio | Linea aprox. |
|---|---|---|
| Routines | Activar rutina | RoutinesScreen.kt:585-597 |
| Routines | Restaurar archivada | RoutinesScreen.kt:695-708 |
| Routines | Usar plantilla | RoutinesScreen.kt:514 |
| Workout | Crear alternativa (dialogo) | WorkoutScreen.kt:1019-1024 |
| Workout | Volver (empty state dia no encontrado) | WorkoutScreen.kt:389-400 |

**NO migrar aqui**: los 6 controles del timer (pausa/reset/cancelar/60/90/120, WorkoutScreen.kt:659-710). Ya pasan por el wrapper local `RestTimerActionButton` — eso es correcto segun la filosofia (boton propio del contexto timer). Unica accion: mover `RestTimerActionButton` de composable privado suelto a... nada, dejarlo donde esta. Es el ejemplo de "distinto por pantalla esta bien".

### D2.3 `FitTrackOutlinedButton` — accion terciaria / cancelar / destructiva suave

```kotlin
@Composable
fun FitTrackOutlinedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    destructive: Boolean = false,  // true → contentColor = colorScheme.error (replica el estilo actual de Desconectar)
)
```

**Call sites**:

| Pantalla | Sitio | Linea aprox. | Nota |
|---|---|---|---|
| Routines | Editar (RoutineEditButton) | RoutinesScreen.kt:623 | absorber el wrapper local; borrarlo |
| Routines | Archivar (RoutineArchiveButton) | RoutinesScreen.kt:645 | idem |
| Routines | Cancelar (footer del editor) | RoutinesScreen.kt:865-887 | |
| Settings | Desconectar Health Connect | SettingsScreen.kt:240-249 | `destructive = true` |

### D2.4 `FitTrackAddButton` — el boton de añadir (peticion explicita del dueño)

```kotlin
@Composable
fun FitTrackAddButton(
    label: String,          // "Añadir dia", "Añadir ejercicio"...
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
```

Implementacion: delega en `FitTrackOutlinedButton(icon = Icons.Default.Add)`. Existe como nombre propio porque "añadir" es un rol que crecera (añadir serie, añadir nota...) y el dueño quiere poder restylearlo como grupo.

**Call sites**:

| Pantalla | Sitio | Linea aprox. |
|---|---|---|
| Routines | Añadir dia | RoutinesScreen.kt:834-847 |
| Routines | Añadir ejercicio | RoutinesScreen.kt:1076-1089 |

**Done D2 (grep)**: en `feature/`, `rg "\bButton\(" --type kotlin` no debe devolver llamadas directas en los call sites migrados; `rg "FilledTonalButton|OutlinedButton" app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature` solo debe devolver `RestTimerActionButton` (Workout) y usos internos de `core/design/`. `RoutineEditButton` y `RoutineArchiveButton` eliminados.

---

## Fase D3 — Dialogos

Commit sugerido: `refactor: unify dialog shells into FitTrackDialog and FitTrackConfirmDialog`

**Archivo nuevo**: `core/design/Dialogs.kt`

Hoy hay 9 dialogos en 2 patrones (7 `AlertDialog` + 2 `Dialog(Surface(extraLarge, 6dp))` custom en Workout). Los TextButton de confirmar/cancelar son ~18 de los 97 botones crudos: este es el grupo mas grande de todos.

### D3.1 `FitTrackConfirmDialog` — confirmaciones de dos botones

```kotlin
@Composable
fun FitTrackConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
    destructive: Boolean = false,  // true → confirm en colorScheme.error
)
```

Implementacion: `AlertDialog` estandar con dos `TextButton` (el aspecto EXACTO de hoy). `destructive` solo tiñe el texto del confirm.

**Call sites**:

| Dialogo | Sitio | Linea aprox. | destructive |
|---|---|---|---|
| Archivar rutina | RoutinesScreen.kt:119-143 | si |
| Descartar cambios del editor | RoutinesScreen.kt:192-231 | si |
| Descartar ediciones de historial | HistoryScreen.kt:301-316 | si |
| Recargar datos demo | SettingsScreen.kt:79-105 | si |
| Finalizar entrenamiento | WorkoutScreen.kt:145-188 | no — y añadir `confirmEnabled = !state.isFinishing` (fix UI-H5 de la auditoria tecnica; coordinar con R1 si ya esta hecho alli) |

Nota: el de finalizar hoy es `Dialog(Surface)` custom, no AlertDialog. Migrarlo a `FitTrackConfirmDialog` ES un cambio visual pequeño (de card custom a AlertDialog estandar) — permitido y deseado: era el unico motivo de su shell custom y no lo justifica.

### D3.2 `FitTrackInputDialog` — dialogos con un campo de texto

```kotlin
@Composable
fun FitTrackInputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    label: String? = null,          // label del OutlinedTextField
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
)
```

**Call sites**: reps personalizadas (RoutinesScreen.kt:1132-1166), notas de ejercicio (RoutinesScreen.kt:1168-1209, `singleLine = false`).

### D3.3 `FitTrackDialog` — shell generico para dialogos ricos

Para los dialogos que NO caben en confirm/input (alternativas de Workout:926-1075, alternativas del editor Routines:1387-1501):

```kotlin
@Composable
fun FitTrackDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,   // IconButton(Close) en la cabecera
    content: @Composable ColumnScope.() -> Unit,  // area scrollable
    actions: (@Composable RowScope.() -> Unit)? = null,  // fila fija inferior
)
```

Implementacion: `Dialog { Surface(shape = extraLarge, tonalElevation/shadowElevation = FitElevation.dialog) }` con cabecera fija (titulo + close), contenido en `Column(verticalScroll)` y actions fija abajo. Esto ademas resuelve dos hallazgos de la auditoria tecnica (UI-H4): añadir `Modifier.fillMaxHeight(fraction = 0.85f)` como maximo con cabecera siempre visible, y NO usar `imePadding()` dentro del dialogo (gestionar el teclado con el scroll del contenido). Si R2 ya migro el dialogo de alternativas a otra solucion, respetar la de R2 y solo re-etiquetar.

Migrar los dos dialogos de alternativas a este shell manteniendo su contenido tal cual.

**Done D3 (grep)**: `rg "AlertDialog\(" app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature` → cero; `rg "Dialog\(" .../feature` → cero (todo pasa por core/design/Dialogs.kt).

---

## Fase D4 — Duplicados exactos no-boton

Commit sugerido: `refactor: deduplicate stepper, selectors and hero tag components`

### D4.1 `FitTrackStepper`

**Archivo nuevo**: `core/design/components/Stepper.kt`

Hoy hay TRES steppers implementados por separado:

1. `SetStepperButton` (WorkoutScreen.kt:1345-1371): Box 28dp + combinedClickable con long-press.
2. `ExerciseSetsStepper` (RoutinesScreen.kt:1503-1531): IconButton x2 dentro de Box con fondo surface.
3. `StepGoalStepper` (SettingsScreen.kt:324-354): IconButton x2 a pelo.

```kotlin
@Composable
fun FitTrackStepper(
    value: String,                      // texto central ya formateado
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    onLongIncrement: (() -> Unit)? = null,  // null → sin long-press
    onLongDecrement: (() -> Unit)? = null,
    compact: Boolean = false,           // true → tamaño Workout (para caber en la fila de serie)
    decrementEnabled: Boolean = true,
    incrementEnabled: Boolean = true,
)
```

Decisiones: los botones internos usan **minimo 48dp de area tactil** (resuelve el hallazgo UI-L6 de la auditoria tecnica: los 28dp actuales son demasiado pequeños; en `compact` mantener el tamaño VISUAL de 28dp pero extender el hit area con `Modifier.minimumInteractiveComponentSize()` o `sizeIn`). Este es el segundo cambio visual-funcional permitido de la spec.

Migrar los tres sitios. En Workout conservar EXACTAMENTE el comportamiento de long-press actual (incrementos grandes de peso).

**Done**: `SetStepperButton`, `ExerciseSetsStepper` y `StepGoalStepper` eliminados; `rg "combinedClickable" .../feature/workout` cero (vive en el componente).

### D4.2 `FitTrackSegmentedSelector`

**Archivo nuevo**: `core/design/components/SegmentedSelector.kt`

`UnitSelector`/`UnitSegment` (SettingsScreen.kt:357-415) y `WeightUnitInlineSelector` (AppShell.kt:401-434) son pixel-identicos: `Row(surfaceAlt, medium, padding 3dp)` + celdas `Box(surface/transparente, small)`.

```kotlin
@Composable
fun FitTrackSegmentedSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
)
```

Copiar el aspecto exacto del actual (fondo `surfaceAlt`, padding 3dp, seleccionada `surface` con texto `primary`, no seleccionada texto `onSurfaceVariant`). Migrar ambos sitios y borrar los cuatro composables privados.

### D4.3 `FitTrackThemeModeSelector`

**Archivo nuevo**: `core/design/components/ThemeModeSelector.kt` (o junto al anterior)

`ThemeModeSelector`/`ThemeModeOption` (SettingsScreen.kt:418-509) y `ThemeModeInlineSelector` (AppShell.kt:355-397) son casi identicos (tiles de 3 columnas primarySoft/surfaceAlt). Diferencia real: el de Settings pinta un radio circle custom de 22dp; el del drawer no. Unificar en un componente con `showRadio: Boolean` y migrar ambos.

```kotlin
@Composable
fun FitTrackThemeModeSelector(
    selected: AppThemeMode,
    onSelect: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    showRadio: Boolean = true,
)
```

### D4.4 `FitTrackHeroTag`

**Archivo**: añadir en `core/design/Labels.kt`

`MiniHeroTag` (HomeScreen.kt:472-485) y `HeroTag` (WorkoutScreen.kt:462) son el mismo pill (Box blanco translucido sobre hero oscuro) con dos nombres.

```kotlin
@Composable
fun FitTrackHeroTag(text: String, modifier: Modifier = Modifier)
```

Copiar el estilo actual (`Color.White.copy(alpha = 0.10f)` fondo, texto `White.copy(0.78f)`; comprobar shape real en cada sitio — Home usa CircleShape, Workout shapes.medium: elegir **CircleShape** como canonico, cambio visual minimo en Workout, anotar en commit). Migrar ambos, borrar los dos privados.

**Done D4**: los 7 composables privados duplicados eliminados; `test` + `build` verdes.

---

## Orden, dependencias y cierre

```
D1 (tokens) → D2 (botones) → D3 (dialogos) → D4 (duplicados)
```

- D2-D4 dependen de D1 solo en los puntos que usan `success`/`FitElevation`; el resto es independiente. Ejecutar en orden igualmente: son commits separados en la misma rama.
- Si alguna linea referenciada no coincide (el codigo evoluciona), buscar el elemento por su descripcion — cada tabla incluye pantalla + proposito. NO migrar nada que no este en las tablas.
- Coordinar con las fases R de `docs/planning/roadmap-release.md`: si R1/R2 ya se ejecutaron y tocaron el dialogo de finalizar o el de alternativas, D3 se adapta a lo que exista (re-etiquetar, no revertir fixes).

Al cerrar la rama:

1. Actualizar `docs/progress/project-progress.md` y `docs/progress/phase-log.md` (fases D1-D4, que se hizo, greps de verificacion, cambios visuales anotados).
2. Actualizar `docs/design/ui-component-audit.md`: marcar en la seccion 7 las fases completadas.
3. Push y avisar al usuario para pasada manual en emulador: Home, Rutinas (lista + editor + dialogos), Entrenar (sesion completa + timer + alternativas + finalizar), Historial (filtros + detalle + edicion), Datos, Ajustes (unidad, tema, Health Connect, demo), drawer (tema + unidad).

### Resumen de cambios visuales permitidos (todo lo demas: identico)

1. Verde de exito en dark mode mas claro (D1.1) — mejora de contraste.
2. Colores del confetti desde la paleta (D1.5).
3. Dialogo de finalizar entrenamiento pasa de card custom a AlertDialog estandar (D3.1).
4. Area tactil de steppers a 48dp manteniendo tamaño visual (D4.1) — fix de usabilidad.
5. HeroTag de Workout adopta CircleShape (D4.4).

### Recuento esperado

Antes: ~97 botones crudos + 9 dialogos artesanales + 3 steppers + 4 selectores + 2 hero tags.
Despues de D1-D4: ~50 call sites pasan por 10 componentes compartidos; quedan fuera (a proposito) los IconButtons, FABs, TextButtons unicos y el timer. Reduccion de superficie de restyle: ~50%, que es exactamente el objetivo pedido.
