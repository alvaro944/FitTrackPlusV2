# Spec: Bugs de edicion de campos en variantes, tema nativo heredado y proporcion peso/reps

Fecha: 2026-07-05
Ejecutor: Codex
Rama: `bug/workout-input-ux-fixes` (nueva rama, cuatro commits, uno por bug)
Origen: dogfooding del dueño sobre `main` tras el merge de `refactor/ui-component-grouping`.

## Contexto

Cuatro bugs encontrados usando la app real. Los cuatro estan verificados contra el codigo actual (evidencia con archivo y linea en cada seccion). No son especulacion: ya se confirmo la causa raiz de los 4 antes de escribir esta spec.

Verificacion minima por commit: `./gradlew test` y `./gradlew build` en verde. Los bugs B y C dependen del renderizado nativo de Android (selection handle / floating toolbar) y **no son verificables por test automatico** — requieren pasada manual del usuario en dispositivo/emulador. Decirlo explicitamente en el commit y en el aviso final.

---

## Bug A — Los campos de variante de ejercicio no seleccionan el texto al enfocar (causa "8" + "10" = "810")

### Sintoma

Al crear o editar una variante/alternativa de un ejercicio (desde el editor de rutina o desde el dialogo de alternativas durante el entrenamiento), tocar un campo como "Reps" que ya tiene un valor (p. ej. "8") y escribir "10" no reemplaza el "8": lo concatena, resultando en "810" en vez de "10".

### Causa raiz confirmada

Este comportamiento YA esta arreglado correctamente en las filas de serie del entrenamiento (`WorkoutSetRow`, dentro de `WorkoutScreen.kt`): los campos de peso y reps usan un `TextFieldValue` local que se selecciona entero al enfocar o al soltar el tap:

- `internal fun selectAllWorkoutFieldValue(current: TextFieldValue)` — WorkoutScreen.kt:1243-1245 (pone `selection = TextRange(0, text.length)`).
- `internal fun syncWorkoutFieldValue(current, externalText)` — WorkoutScreen.kt:1247-1259.
- Uso correcto en el campo de peso, `WeightFieldColumn` — WorkoutScreen.kt:994-1004+ (`remember(setId) { mutableStateOf(TextFieldValue(weightText)) }` + `onFocusChanged`/`PressInteraction.Release` que llama a `selectAllWorkoutFieldValue`).
- Uso correcto en el campo de reps de la fila de serie — WorkoutScreen.kt:1080-1178 (mismo patron: `repsFieldValue`, `LaunchedEffect(set.repsText)` con `syncWorkoutFieldValue`, `onFocusChanged` que llama a `selectAllWorkoutFieldValue`).

Pero **los dos dialogos de creacion/edicion de variante NO usan este patron**. Sus campos son `OutlinedTextField` atados directamente a un `String` plano, sin `TextFieldValue` ni gestion de seleccion:

1. **`ExerciseAlternativesEditorDialog`** en `feature/routines/RoutinesScreen.kt` (editor de rutina), campos dentro del bloque `if (isEditing)`:
   - Nombre: linea 1329-1335 (`value = alternative.name`)
   - Series: linea 1337-1343 (`value = alternative.targetSets`)
   - Reps: linea 1344-1350 (`value = alternative.targetRepsText`) ← el campo del sintoma reportado
   - Notas: linea 1352-1358 (`value = alternative.notes`)

2. **`ExerciseAlternativesDialog`** en `feature/workout/WorkoutScreen.kt` (dialogo de alternativas durante el entrenamiento), formulario de creacion:
   - Nombre: linea 944-950 (`value = picker.draft.name`)
   - Series: linea 952-959 (`value = picker.draft.targetSets`)
   - Reps: linea 960-966 (`value = picker.draft.targetRepsText`)
   - Notas: linea 968-974 (`value = picker.draft.notes`)

Los callbacks de ambos dialogos son `(String) -> Unit` (p. ej. `onAlternativeRepsChange: (Int, String) -> Unit` en RoutinesScreen.kt:1290, `onDraftRepsChange` en WorkoutScreen.kt), asi que el estado real sigue siendo `String` en el ViewModel — el fix es puramente de UI, replicando el mismo patron ya usado en `WorkoutSetRow`.

### Por que hacerlo como componente compartido (pedido explicito del usuario)

El usuario ya dijo: "cuando se cree un elemento de este tipo, tiene que ser igual a un elemento de la rutina que ya existia, con sus mismas cosas, sus mismas funciones". Copiar el patron 8 veces mas (4 campos x 2 dialogos) séria exactamente el tipo de duplicacion que la rama `refactor/ui-component-grouping` ya elimino para botones/dialogos/steppers. Ademas, esto ya estaba identificado como brecha en la auditoria (`docs/design/ui-component-audit.md`, gap #2 "FitTrackTextField") y como deuda tecnica (`docs/planning/roadmap-release.md`, R5: "Extraer utilidades de parseo peso/reps a core/util/").

**Hacerlo asi**:

1. Archivo nuevo: `core/design/components/SelectAllTextField.kt`.
2. Mover (no duplicar) `selectAllWorkoutFieldValue` y `syncWorkoutFieldValue` desde `WorkoutScreen.kt:1243-1259` a este archivo nuevo, como funciones publicas (quitar `internal`, o mantener visibilidad de modulo si Kotlin lo permite sin friccion — el objetivo es que dejen de vivir dentro de `WorkoutScreen.kt`). Actualizar los usos existentes en `WorkoutScreen.kt` para importar desde el nuevo archivo.
3. Crear un composable wrapper:

```kotlin
@Composable
fun FitTrackSelectAllTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
)
```

Implementacion: exactamente el patron de `WeightFieldColumn`/reps field — `var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }`, `LaunchedEffect(value) { fieldValue = syncWorkoutFieldValue(fieldValue, value) }`, `interactionSource` con `PressInteraction.Release` → `selectAllWorkoutFieldValue`, y `Modifier.onFocusChanged { if (it.isFocused) fieldValue = selectAllWorkoutFieldValue(fieldValue) }`. El `OutlinedTextField` interno recibe `value = fieldValue` y en `onValueChange` actualiza `fieldValue` y llama a `onValueChange(it.text)`.

4. Migrar los 8 campos listados arriba (4 en RoutinesScreen.kt, 4 en WorkoutScreen.kt) para usar `FitTrackSelectAllTextField` en vez de `OutlinedTextField` crudo. Mantener label/placeholder/singleLine/minLines/keyboardOptions tal cual estan hoy en cada sitio (no cambiar comportamiento de teclado, solo la seleccion).
5. El campo de peso (`WeightFieldColumn`) y el de reps de `WorkoutSetRow` pueden quedarse como estan (ya correctos) o migrarse tambien al nuevo componente si el ejecutor ve que encaja sin friccion — no es obligatorio, es opcional para mayor consistencia. Prioridad: que los 8 campos rotos queden arreglados.

**Done**: escribir "8" en el campo de reps de una variante nueva, tocarlo, escribir "10" → el campo queda en "10", no en "810". Verificar los 4 campos (nombre, series, reps, notas) en AMBOS dialogos.

---

## Bug B — Panel flotante de copiar/cortar/pegar con contraste pobre

## Bug C — Cuadrado blanco detras de la flecha verde del cursor de seleccion

Se investigan y arreglan juntos porque comparten la misma causa raiz.

### Causa raiz confirmada

El panel flotante de seleccion de texto (copiar/cortar/pegar) y el "handle" de seleccion (la gota/flecha que aparece al tocar un campo de texto o seleccionar una palabra) son UI nativa de Android, **no de Compose**: Compose delega en el `TextToolbar` del sistema, que se pinta con el tema nativo de la Activity (`android:theme`), no con `FitTrackPlusTheme` (que es puramente Compose y solo pinta lo que Compose dibuja).

El tema nativo aplicado es `Theme.FitTrackPlus`, declarado en `app/src/main/res/values/themes.xml:2-21` y aplicado a nivel `<application>` en `AndroidManifest.xml:23` (`android:theme="@style/Theme.FitTrackPlus"` — afecta a toda la app, no solo al splash).

Este tema es **residuo de la v1** (antes de la migracion a Compose):

- `colorPrimary = @color/primario` → `#1976D2` (azul) — **no tiene nada que ver** con el verde esmeralda real de la v2 (`#1F6B57`, definido en `core/design/Theme.kt`).
- `colorSecondary = @color/acento` → `#FB8C00` (naranja).
- El tema **nunca define** `colorAccent`, `colorControlActivated`, `colorControlNormal`, `colorControlHighlight`, ni `android:textColorPrimary`/`colorBackgroundFloating`. Estos son los atributos que Android usa especificamente para teñir el handle de seleccion y el fondo/texto del panel flotante de copiar/pegar.
- Como el padre es `Theme.MaterialComponents.Light.NoActionBar` y no se sobreescribe `colorAccent`/`colorControlActivated`, Android cae al valor por defecto de la libreria (un verde/teal generico) para el handle — esto explica la "flecha verde" que el usuario ve y que no coincide con ningun color de la paleta real de la app.
- El "cuadrado blanco" detras de la flecha es la ventana emergente (popup) del handle sin fondo transparente — tipico cuando el tema no define correctamente los atributos de control, dejando el popup con el fondo por defecto del framework.

### Fix

**Archivo**: `app/src/main/res/values/themes.xml`

Actualizar `Theme.FitTrackPlus` para:

1. Sustituir los colores por los reales de la v2 (mismo valor que `core/design/Theme.kt` usa para `LightColors.primary` etc. — usar los mismos hex, no inventar nuevos):
   - `colorPrimary` → `#1F6B57` (emerald, mismo que `LightColors.primary`)
   - `colorSecondary`/`colorAccent` → `#C47A49` (copper, mismo que `LightColors.secondary`)
2. Añadir explicitamente los atributos que hoy faltan:
   - `colorAccent` → `#1F6B57` (o `#C47A49` si tras la prueba visual se ve mejor con el acento; probar con el primary primero porque es el color de marca dominante)
   - `colorControlActivated` → mismo valor que `colorAccent`
   - `colorControlNormal` → un gris neutro acorde (p. ej. el `outline` de `LightColors`, `#D5D0C5`, o `android:textColorSecondary` por defecto si no se quiere añadir otro color nuevo)
   - `colorControlHighlight` → puede omitirse si no aporta, o usar una version con alpha del accent
   - `android:textColorPrimary` → `#181A18` o el `onSurface` real de la v2 si se quiere maxima consistencia (opcional, verificar que no rompe nada de lo poco que aun depende de este tema)

Nota: **no crear nuevos colores en `colors.xml` si ya existen equivalentes**; si hace falta un hex que no esta en `colors.xml`, añadirlo con nombre descriptivo (`emerald_primary`, no `verde1`) y comentario indicando que replica el valor de `core/design/Theme.kt`.

3. Revisar si `Theme.FitTrackPlus` sigue siendo necesario mas alla de este fix. Si un grep confirma que esta unicamente para satisfacer el requisito de Android de tener un tema de Activity de base (y todo el contenido visual real lo pinta Compose), dejarlo como esta tras el fix de colores — no es objetivo de este bug eliminar el tema, solo corregir que sus colores de control coincidan con la marca real.

**Done**: no hay criterio de grep automatico para esto — es visual y depende del dispositivo/OEM. Documentar en el commit que requiere verificacion manual: seleccionar una palabra en cualquier campo de texto de la app y comprobar (a) que el panel de copiar/cortar/pegar tiene buen contraste, (b) que el handle de seleccion no muestra un cuadrado/borde blanco visible alrededor de la flecha. Si tras este cambio el problema persiste, anotarlo como pendiente con una nota tecnica de lo probado (para no repetir el mismo intento en el futuro) en vez de insistir a ciegas.

---

## Bug D — Proporcion de ancho entre el campo de peso y el de reps

### Causa raiz confirmada

En la fila de serie del entrenamiento (`WorkoutSetRow`, `WorkoutScreen.kt`), las columnas de peso y reps usan pesos casi iguales, con el de reps ligeramente MAYOR que el de peso — al reves de lo que hace falta:

```kotlin
// WorkoutScreen.kt:1283-1284
private const val WORKOUT_WEIGHT_COLUMN_WEIGHT = 1.0f
private const val WORKOUT_REPS_COLUMN_WEIGHT = 1.1f
```

Uso: linea 1144 (`WeightFieldColumn(..., modifier = Modifier.weight(WORKOUT_WEIGHT_COLUMN_WEIGHT))`) y linea 1147 (`Column(modifier = Modifier.weight(WORKOUT_REPS_COLUMN_WEIGHT), ...)`, la columna de reps).

El usuario escribe pesos como "22,5" (4-5 caracteres) que se cortan por espacio insuficiente, mientras reps ("8", "12") necesita mucho menos espacio pero hoy tiene ligeramente MAS.

### Fix

Cambiar las dos constantes para invertir la proporcion, dandole aproximadamente el doble de espacio al peso que a las reps (proporcion 2:1, tal como pidio el usuario — "el primero ocupe seis y el segundo tres"):

```kotlin
private const val WORKOUT_WEIGHT_COLUMN_WEIGHT = 1.6f
private const val WORKOUT_REPS_COLUMN_WEIGHT = 0.8f
```

No tocar nada mas de la fila (el circulo de numero de serie mantiene su tamaño fijo `WORKOUT_SET_INDEX_SIZE`).

**Atencion**: la columna de reps contiene tambien los botones del `FitTrackStepper` (modo `compact`) ademas del campo de texto — al reducir su peso a 0.8f, verificar en pasada manual que los botones +/- del stepper compacto siguen siendo tocables (no quedan aplastados). Si al probar en dispositivo se ve demasiado estrecho, ajustar a `0.9f`/`1.5f` (mantener la idea de que el peso ocupa notablemente mas que las reps, sin llegar exactamente a 2:1 si eso rompe el stepper) y anotar el valor final elegido en el commit con el motivo.

**Done**: escribir "22,5" en el campo de peso de una serie → el texto se ve completo sin cortarse. El stepper de reps sigue siendo usable con los dedos.

---

## Cierre de la rama

1. Cuatro commits, conventional commits, sin Co-Authored-By:
   - `fix: select all text on focus in exercise variant fields`
   - `fix: rebrand native Android theme to match v2 palette` (bugs B+C, un solo commit porque comparten causa)
   - `fix: rebalance weight/reps column widths in workout set row`
   - (si aplica) commit adicional si el ajuste de D necesita retoque tras la pasada manual
2. Actualizar `docs/progress/project-progress.md` y `docs/progress/phase-log.md`: los 4 bugs, que se hizo, que quedo pendiente de verificacion visual (B y C).
3. Push y aviso al usuario para pasada manual obligatoria en dispositivo — estos 4 bugs se reportaron dogfoodeando, se cierran solo con dogfooding:
   - Crear una variante de ejercicio nueva (desde el editor de rutina Y desde el dialogo de alternativas en Entrenar) y comprobar que reps/series/nombre/notas seleccionan el texto entero al tocar el campo.
   - Seleccionar una palabra en cualquier campo de texto de la app y revisar contraste del panel copiar/pegar y el aspecto del handle de seleccion.
   - Registrar un peso como "22,5" y unas reps como "12" en una serie real y comprobar que ambos campos se ven completos y el stepper de reps sigue siendo comodo de tocar.
