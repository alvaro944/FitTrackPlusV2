# Auditoria ronda 3 — comportamiento, no solo componentes

Fecha: 2026-08-29
Rama de origen: `refactor/design-system-unification`
Informe navegable: https://claude.ai/code/artifact/3ef4c46d-ff28-4e2a-af28-8552b8676ee7

Las rondas 1 y 2 unificaron componentes visuales. Esta ronda audita **comportamiento**:
que pasa al pulsar dos veces, que se guarda sin avisar, que ajuste no hace nada y que
dato desaparece.

Metodo: cinco barridos en paralelo sobre `app/src/main/kotlin` (13.291 lineas de UI).
Uno por pestana (Rutinas, Entrenar, Historial+Datos, Home+Ajustes+navegacion) mas uno
transversal con inventarios completos de campos de texto, dialogos, snackbars, estilos
hardcodeados, accesibilidad y acciones destructivas. Unos 250 hallazgos crudos,
consolidados aqui en 76.

---

## Resumen por severidad

| Tier | Que es | Nº |
|---|---|---|
| P0 | Perdida o corrupcion de datos | 9 |
| P1 | Funciones que existen en la UI y no funcionan | 12 |
| P2 | Calidad de entrada de texto | 8 |
| P3 | Navegacion y controles | 8 |
| P4 | Sistema visual | 11 |
| P5 | Accesibilidad | 5 |
| P6 | Idioma y copy | 4 |

---

## Los dos fallos reportados por el usuario

### Variantes duplicadas — cuatro defectos encadenados

`WorkoutViewModel.kt:173-224` (`saveExerciseAlternative`)

1. **Orden invertido**: `createExerciseAlternative()` persiste la alternativa ANTES de
   que `replaceWorkoutExerciseVariant()` diga si se puede aplicar. Cuando el ejercicio
   ya tiene series registradas, el swap se rechaza pero la alternativa ya esta escrita.
2. **El dialogo no se cierra**: la rama rechazada solo limpia `isSaving`. El mensaje
   "Cambia la variante antes de registrar series" se lee como error, el usuario vuelve
   a pulsar.
3. **Sin deduplicacion**: `newVariantKey()` genera clave nueva en cada llamada
   (`DefaultRoutineRepository.kt:104`). El indice unico cubre `variantKey`, no
   `(routineExerciseId, name)`.
4. **Snackbar detras del dialogo**: `WorkoutScreen.kt:174` (dialogo) vs `:192`
   (SnackbarHost dentro del Scaffold). Estructural: pasa en las 5 pantallas.

Arreglo: comprobar `canSwap` al construir el picker y deshabilitar las opciones no
aplicables; `if (picker.isSaving) return`; deduplicar por `(routineExerciseId, name)`.

### Aspecto de "Dia 1 / Ejercicio 1"

Seis causas tecnicas, no cuestion de gusto:

| Que | Donde | Problema |
|---|---|---|
| Tarjeta dentro de tarjeta | `RoutinesScreen.kt:813` | Radio 16dp dentro de radio 16dp. Rompe radios concentricos |
| Barra de acento | `RoutinesScreen.kt:829` | 6dp de ancho con radio 8dp: lingote deformado, no pildora |
| Tres grises, dos iguales | `Theme.kt:31` | `surfaceAlt` es byte a byte `surfaceVariant` |
| Bloque sin borde | `RoutinesScreen.kt:1074` | Unico contenedor relleno-sin-borde de la app |
| "Ejercicio N" | `RoutinesScreen.kt:1086` | `labelLarge` a `onSurface` completo, compite con la etiqueta del campo |
| "Ejercicio N" (2) | idem | No informa: el nombre esta debajo y el numero se renumera al reordenar |

Ademas el nombre del dia usa `titleLarge`, el mismo peso que el titulo de la rutina padre.

**Propuesta** (no requiere componentes nuevos):
- Una sola `FitTrackCard` por dia, sin relleno anidado
- `FitTrackIconBadge.Number` para el ordinal (ya existe, ya se usa en Home y SetRow)
- Expansion senalada solo con rotacion del chevron
- Nombre del dia a `titleMedium`
- Variante predeterminada con `FitTrackBadge("PREDET.")`, igual que ya hace Entrenar

---

## P0 — Perdida de datos

| # | Hallazgo | Referencias |
|---|---|---|
| P0-1 | **Un entrenamiento de peso corporal se borra al finalizar.** `isWorkoutSetCompleted` exige `weightKg > 0.0`, asi que dominadas/abdominales nunca se completan. Y finalizar con 0 completadas ejecuta `deleteSession` | `WorkoutViewModel.kt:982`, `DefaultWorkoutRepository.kt:169` |
| P0-2 | **Tocar una nota existente la borra.** Campos multilinea sin pasar `selectAllOnFocus`; el default del componente es `true` | `RoutinesScreen.kt:1278`, `WorkoutScreen.kt:962` |
| P0-3 | **Historial escribe en cada tecla.** Borrar el peso para reescribirlo guarda un `0`. "Guardar" es no-op, "Descartar" hace una segunda ronda de escrituras, y estan en posiciones invertidas | `HistoryViewModel.kt:272-298,215`, `HistoryScreen.kt:351-361` |
| P0-4 | **Cerrar el dialogo de alternativas confirma la edicion.** `onDismiss` llama a `finishInlineAlternativeEdit` | `RoutinesScreen.kt:1013-1022` |
| P0-5 | **Editar una serie completada la des-completa e infla los PRs.** `prType` no se limpia; al re-completar, `detectPrIfEligible` vuelve a contar | `WorkoutViewModel.kt:328,500-516` |
| P0-6 | **Rutinas duplicadas por doble toque.** FAB sin `enabled`; `saveEditor()` comprueba `canSave` pero no `isSaving`. El boton inline de `:765` si lo hace bien | `RoutinesScreen.kt:146-148`, `RoutinesViewModel.kt:359` |
| P0-7 | **Estado de dialogos atado a la posicion.** `itemsIndexed` sin `key` + `forEachIndexed`. Reordenar reasigna el estado `remember` a otro ejercicio. Igual con `PendingExerciseRemoval` | `RoutinesScreen.kt:694,910,618` |
| P0-8 | **`removeDay` sin confirmacion** aunque destruye todos sus ejercicios. Eliminar un solo ejercicio si la tiene | `RoutinesViewModel.kt:161`, `RoutineDao.kt:65` |
| P0-9 | **No existe deshacer.** Ni un `showSnackbar` en el proyecto pasa `actionLabel` | 7 SnackbarHostState, 0 actionLabel |

---

## P1 — Funciones que no funcionan

| # | Hallazgo | Referencias |
|---|---|---|
| P1-1 | **El selector kg/lb es decorativo.** La preferencia existe y se guarda; ninguna pantalla la lee. Todo escribe `"kg"` a fuego | `UserPreferencesRepository.kt:30`, `SettingsScreen.kt:116-135` |
| P1-2 | **Las notas no se pueden escribir o no se leen.** De sesion: imposible escribir (`finishWorkoutSession` se llama sin notas y el dialogo no tiene campo), aunque Historial la pinta. De serie: imposible escribir, aunque `FitTrackSetRow` la renderiza. De ejercicio: se escribe pero no se lee en ningun sitio; `WorkoutExerciseEntity` no tiene columna | `WorkoutViewModel.kt:421`, `WorkoutSetEntity.kt:30`, `WorkoutExerciseEntity.kt:24-32` |
| P1-3 | **El filtro de periodo de Datos solo afecta a 3 numeros.** `observeWorkoutStats` se llama siempre con `All`; graficas y marcas siguen mostrando todo el historico | `StatsViewModel.kt:62,256-280` |
| P1-4 | **Tarjetas de Datos mal etiquetadas.** `exerciseCount` suma entradas ejercicio-sesion (5 ejercicios en 4 sesiones = "20 EJERCICIOS"). "PRs" cuenta ejercicios con marca, no records, e ignora el filtro | `StatsViewModel.kt:236-238`, `StatsScreen.kt:347` |
| P1-5 | **Temporizador de descanso:** cuenta con contador y no reloj de pared (deriva), muere con el proceso, sin notificacion ni sonido, y la vibracion esta en un `LaunchedEffect` dentro de la tarjeta (si haces scroll, no suena). El auto-start no se persiste | `WorkoutViewModel.kt:609-617`, `WorkoutScreen.kt:328,526-530` |
| P1-6 | **Confeti encallado.** `onFinished()` se cancela al cambiar de pestana; `celebration` nunca se limpia y se repite para siempre | `ConfettiAnimation.kt:41-47`, `WorkoutScreen.kt:222-233` |
| P1-7 | **Home deja de actualizarse tras un error.** `.catch` sobre un `combine` termina el upstream | `HomeViewModel.kt:71-73` |
| P1-8 | **Widget y atajos desconectados.** Cero llamadas a `updateAll` (solo el `updatePeriodMillis` de 30 min). Sin `launchMode` ni `onNewIntent`, el extra `open_tab` no se lee nunca | `FitTrackPlusWidget.kt`, `AndroidManifest.xml:26-38` |
| P1-9 | **Nada protege al salir de un entrenamiento activo.** Entrenar no registra `setNavigationBlocker`. Y el bloqueador tiene fuga: nada lo limpia al salir de composicion | `WorkoutScreen.kt:113-140`, `AppShellViewModel.kt:59-68` |
| P1-10 | **App local-first sin exportar/importar/borrar todo.** "Exportar datos" es un snackbar de "proximamente" | `NavigationShellConfig.kt:42-46` |
| P1-11 | **Desconectar Health Connect no revoca el permiso**, solo cambia un booleano. Y los flags de disponibilidad son `val` evaluados una vez | `SettingsViewModel.kt:100-105,43-44` |
| P1-12 | **Codigo muerto:** `HeatmapCalendar.kt` (cero referencias), `FitTrackLoadingCard` (deprecado, sin llamadas), `selectExercise`/`withSelectedExercise`, dos politicas de auto-start que solo usan los tests, ~15 imports sin usar | varios |

Nota sobre P1-12: en la ronda 1 se decidio no fusionar el calendario de Datos con
`HeatmapCalendar` porque son UX distintas a proposito. La decision sigue siendo
correcta; lo que no se cerro es la consecuencia: el componente quedo sin uso.

---

## P2 — Entrada de texto

Dos hechos globales verificados con ripgrep sobre los 107 ficheros:
**`KeyboardCapitalization` aparece 0 veces. `ImeAction`/`KeyboardActions` aparecen 0 veces.**

| # | Hallazgo | Referencias |
|---|---|---|
| P2-1 | Ningun campo capitaliza. Y hay un intento fallido: `normalizeEditorNameInput` fuerza mayuscula en cada tecla, pelea contra el IME e impide escribir en minuscula. Hay copia gemela en Entrenar | `RoutinesViewModel.kt:639`, `WorkoutViewModel.kt:890-895` |
| P2-2 | Ningun campo encadena con el siguiente. Afecta a 3 formularios: editor de rutina, editor de alternativa (x2) y fila de serie | global |
| P2-3 | Select-all: **bien** en los 4 numericos de series. Mal en los bordes: reselecciona en cada toque (imposible corregir un decimal), falta en el nombre de dia precargado con "Dia 1", e imposible en `FitTrackInputDialog` porque usa `String` y no `TextFieldValue` | `SetRow.kt:312-318`, `RoutinesScreen.kt:897`, `Dialogs.kt:98` |
| P2-4 | Reps sin sanear (admite `-5`, `12x`; la UI muestra una cosa y la BD guarda `0`). Peso con notacion cientifica: `1.0E7` acaba en `"1,07"`. Sin maximos | `WorkoutViewModel.kt:332-343,850-856` |
| P2-5 | Separador decimal fijado a coma sin consultar locale. En Historial, lectura y edicion usan politicas distintas | `WorkoutViewModel.kt:954-968`, `HistoryViewModel.kt:652-654` |
| P2-6 | Errores en rojo antes de escribir nada. Y al anadir ejercicio no hay foco ni scroll al campo nuevo | `RoutinesViewModel.kt:487`, `RoutinesScreen.kt:1116` |
| P2-7 | Sin `maxLength` en ningun campo del proyecto | global |
| P2-8 | Series objetivo solo por stepper, sin pulsacion larga (3→20 son 17 toques). Igual el objetivo de pasos, ademas sin tope superior | `TargetPrescriptionFields.kt:38`, `SettingsScreen.kt:212-224` |

---

## P3 — Navegacion y controles

| # | Hallazgo | Referencias |
|---|---|---|
| P3-1 | Boton atras de Historial arriba a la **derecha** (Material lo pone a la izquierda) y probablemente **tapado** por la hamburguesa flotante del shell: mismos rangos horizontales. Usa el `ArrowBack` deprecado sin espejado | `HistoryScreen.kt:377-403`, `AppShell.kt:157-172` |
| P3-2 | El drawer abre por la izquierda y su boton esta a la derecha. El sheet no tiene `verticalScroll`: con fuente grande se corta | `AppShell.kt:103,158-166,223-236` |
| P3-3 | Doble toque en pestana activa no hace nada. Volver a Historial te devuelve dentro del detalle | `AppShell.kt:185-208`, `AppShellViewModel.kt:75` |
| P3-4 | Ajustes es un callejon de una entrada. Y 2 de las 3 filas del drawer son "proximamente" — la de widget es falsa, el widget ya existe | `AppShell.kt:136`, `NavigationShellConfig.kt:20-28` |
| P3-5 | Onboarding sin atras (el back del sistema sale de la app) y sin forma de repetirlo. `setHasSeenOnboarding(false)` existe y nadie lo llama | `OnboardingScreen.kt:63-129` |
| P3-6 | Sin confirmar: eliminar alternativa, eliminar nota, recuperar entrenamiento (muta el historial), cambiar de variante. Y **no se puede borrar una sesion**: `deleteSession` existe en el DAO, no en el repositorio | `RoutinesScreen.kt:1312,997`, `HistoryScreen.kt:513` |
| P3-7 | Historial arranca filtrado por la rutina activa sin pedirlo. Y Historial/Datos tienen dos enums paralelos para el mismo filtro, con orden y defecto distintos | `HistoryViewModel.kt:58-86,400`, `StatsViewModel.kt:46` |
| P3-8 | Cero `SavedStateHandle` en History y Stats. Mes visible y dia de pasos se pierden en cada rotacion. El `LaunchedEffect(initialTab)` re-ejecuta en cada recreacion y te arranca de vuelta a la pestana del atajo | `FitTrackPlusNavHost.kt:34-38`, `WorkoutScreen.kt:119` |

---

## P4 — Sistema visual

| # | Hallazgo | Referencias |
|---|---|---|
| P4-1 | **Dos tokens muertos.** `surfaceAlt` == `surfaceVariant` y `surfaceCard` == `surface` en ambos esquemas. Por tanto `FitTrackCard(highlighted = true)` no produce ninguna diferencia visual | `Theme.kt:31,73,74,77` |
| P4-2 | Heroes con `Color.White` literal. En oscuro `primaryDark` es verde claro: blanco al 55% queda bajo 3:1. El boton del hero es verde oscuro sobre verde oscuro (~1.3:1) | `HomeScreen.kt:159,190,198`, `Labels.kt:124,130` |
| P4-3 | **Cuatro pantallas aplican el inset de status bar dos veces** (Scaffold anidado sin `contentWindowInsets`). Home y Ajustes ademas sin `contentPadding` en su LazyColumn | `HomeScreen.kt:109`, `SettingsScreen.kt:90`, `HistoryScreen.kt:102`, `StatsScreen.kt:91` |
| P4-4 | Tres idiomas de cabecera: `FitTrackScreenHeader` (4 pantallas), pila propia a `displayLarge` (Home), `TopAppBar` (Ajustes) | `HomeScreen.kt:118-138`, `SettingsScreen.kt:93-107` |
| P4-5 | Ningun esqueleto se parece a lo que carga. Home ni siquiera tiene, y su badge dice "SIN SESION" antes de que llegue ningun dato. Falta un esqueleto de lista compartido | `WorkoutScreen.kt:1069`, `StatsScreen.kt:1078`, `HomeScreen.kt:165` |
| P4-6 | **No existe estado de error en el design system.** Cada pantalla improvisa. Si falla el detalle de Historial, los esqueletos brillan para siempre y la unica salida es atras. Datos pinta el mismo mensaje como tarjeta y como snackbar compartiendo estado | `States.kt`, `HistoryScreen.kt:440-444`, `StatsScreen.kt:157-172` |
| P4-7 | Color semantico contradictorio: subir volumen es verde en Historial y naranja en Datos. Y en Historial se aplica el mismo mapeo a la **duracion**, asi que un entrenamiento mas largo se premia en verde. Los roles `success`/`successSoft` del tema estan sin usar | `HistoryScreen.kt:661-669,610-615`, `StatsScreen.kt:571,955` |
| P4-8 | `LineChart` dibuja texto con `textSize = 10.dp.toPx()` — **dp para tamano de texto**, ignora el ajuste de fuente del usuario. Sin eje Y ni linea base: 100→101 kg se ve como 50→150 | `LineChart.kt:83-87,136-146` |
| P4-9 | Las sesiones sin peso no cuentan como dia entrenado en el calendario (`totalVolumeKg > 0.0`). Misma ceguera al peso corporal que P0-1 | `StatsScreen.kt:362-366` |
| P4-10 | `LaunchIntroScreen` es una paleta duplicada del tema, **solo clara** (fogonazo blanco en cada arranque en oscuro), con 1.240 ms bloqueantes fijos y sin usar la API `SplashScreen` de Android 12+ | `LaunchIntroScreen.kt:56-61,85-93`, `MainActivity.kt:37` |
| P4-11 | El widget usa `GlanceTheme { }` sin argumentos: colores dinamicos del sistema, no la paleta de la app. El XML no declara `initialLayout` (obligatorio) | `FitTrackPlusWidget.kt:55` |

---

## P5 — Accesibilidad

En toda la app hay **dos** elementos pulsables que declaran su rol correctamente:
`HomeScreen.kt:441` y `HistoryScreen.kt:457`. Esos dos son la plantilla.

| # | Hallazgo | Referencias |
|---|---|---|
| P5-1 | Nueve filas/cajas con `.clickable` pelado, sin `role` ni `onClickLabel`. El selector de tema deberia ser `Role.RadioButton`, el segmentado `Role.Tab` | `AppShell.kt:325`, `ThemeModeSelector.kt:89`, `SegmentedSelector.kt:48`, `WorkoutScreen.kt:903`, `StatsScreen.kt:964` |
| P5-2 | Los controles mas pulsados estan bajo el minimo tactil: stepper compacto **28dp**, badge de completar 30dp, hamburguesa 40dp. Solucion: mantener la caja visual y expandir solo el area tactil | `Stepper.kt:90-100`, `SetRow.kt:258` |
| P5-3 | TalkBack lee el id de Room: "Bajar peso de la serie 4718". El stepper de reps, 3 lineas mas abajo, usa `setNumber` correctamente | `SetRow.kt:329-330` |
| P5-4 | Estado codificado solo con color (WCAG 1.4.1). En oscuro, `primarySoft` contra `surface` da ~1.1:1: la celda de "hoy" es indistinguible de una vacia. La tira semanal de Home no tiene `contentDescription` | `HomeScreen.kt:311-344`, `StatsScreen.kt:463-498`, `Theme.kt:89-94` |
| P5-5 | Las graficas no existen para un lector de pantalla: Canvas sin `contentDescription`, sin nodos por punto, interaccion por `pointerInput` crudo | `LineChart.kt:51-126` |

---

## P6 — Idioma y copy

| # | Hallazgo | Referencias |
|---|---|---|
| P6-1 | **Cero `stringResource`.** Todo el texto visible son literales Kotlin; `strings.xml` tiene 6 entradas. La app declara `supportsRtl="true"` | global |
| P6-2 | **"Anadir" no es "Anadir"**: se sustituye n por n, que es otra letra. No es una tilde omitida, es una falta. Cuatro apariciones. Y la convencion de quitar tildes se aplica desigual (el shell escribe "MENU", "Proximamente", "Version") | `RoutinesScreen.kt:736,939,980,1368` |
| P6-3 | Sin plurales: "Racha: 1 dias", "1 sesiones". Home tiene un apano manual y una version correcta duplicada 250 lineas mas abajo | `FitTrackPlusWidget.kt:64-78`, `HomeScreen.kt:181-182` |
| P6-4 | `Locale("es","ES")` a fuego en 5 sitios, conviviendo con `Locale.getDefault()` y `Locale.US` en la misma tarjeta (una produce "3.5k" y la otra "3,5"). `SimpleDateFormat` duplicado en dos ficheros y reasignado en cada composicion | `StatsScreen.kt:413,1003,1059`, `HomeScreen.kt:512` |

---

## Actualizacion de dependencias

### Estado actual vs disponible

| Pieza | Ahora | Ultimo estable | Salto |
|---|---|---|---|
| Compose BOM | 2024.04.01 (material3 **1.2.1**) | 2026.08.00 (material3 **1.4.0**, compose 1.12.0) | ~2 anos |
| AGP | 8.5.1 | 9.3.0 (jul 2026) | major |
| Gradle | 8.7 | 9.1+ (requisito duro de AGP 9) | major |
| compileSdk / targetSdk | 35 / 35 | 36 | +1 |
| JDK | 17 | 17 (AGP 9 exige minimo 17) | ninguno |
| SDK Build Tools | — | 36.0.0 (requisito de AGP 9) | — |
| minSdk | 23 | sin cambio necesario | ninguno |

### Fecha relevante

Google Play exige **targetSdk 36 (Android 16) a partir del 31 de agosto de 2026** para
apps nuevas y actualizaciones. Existe prorroga solicitable hasta el 1 de noviembre de
2026. Solo aplica si la app se publica en Play; para uso propio por sideload, no.

### Que desbloquea realmente material3 1.4.0

Matiz importante: la suite completa de **Material 3 Expressive** (ButtonGroup,
SplitButton, WavyProgressIndicator, TopAppBars flexibles, ToggleButton, FAB Menu,
SearchBar por slots, expressive list items) se esta promocionando a estable en
**material3 1.5.0**, que a fecha de hoy sigue en **alpha27**. En 1.4.0 esos APIs
existen pero marcados `@ExperimentalMaterial3ExpressiveApi`.

El proyecto ya usa opt-in de APIs experimentales (`ExperimentalFoundationApi` en
`app/build.gradle.kts:60`), asi que adoptarlos con opt-in es viable — pero es una
decision consciente, no un "ya es estable".

Lo que si llega estable con el salto, y resuelve hallazgos de esta auditoria:

- **Predictive back** con soporte de framework (P3, `AndroidManifest` sin
  `enableOnBackInvokedCallback`)
- **Shape morphing** y `MaterialShapes` — util para la jerarquia dia/ejercicio (P4)
- Mejoras de `LazyColumn` y de rendimiento de recomposicion
- `LoadingIndicator` / `ContainedLoadingIndicator` — cubre P4-5 y el hueco de
  indicadores de progreso en botones (`isSaving` que solo cambia la etiqueta)
- Autofill, accesibilidad y semantica mejoradas (ayuda a P5)

### Riesgo

El salto AGP 8.5 → 9.x cruza un major con cambios de ruptura, y arrastra Gradle
8.7 → 9.1 (otro major). Puntos concretos a vigilar en este repo:

- `sourceSets { getByName("main") { java.setSrcDirs(...) } }` (`app/build.gradle.kts:69`)
- `providers.exec` para leer git en tiempo de build (`app/build.gradle.kts:16`)
- detekt 1.23.6 — version antigua, comprobar compatibilidad con la version de Kotlin destino
- KSP `2.1.0-1.0.28` va atado a la version de Kotlin
- Hilt 2.57.2, Glance 1.1.0, Health Connect 1.1.0-alpha11

Recomendacion: hacerlo **por etapas verificables**, no en un solo commit.
Etapa A Gradle+AGP+SDK. Etapa B Kotlin+KSP+plugins. Etapa C Compose BOM.
`test` + `build` en verde al final de cada etapa.

---

## Orden de trabajo propuesto

**Actualizado 2026-08-29** tras decidir subir la actualizacion de dependencias antes
de `fix/broken-features`: P1 va a construir UI nueva (notas, exportar datos, widget) y
no tiene sentido construirla dos veces, una sobre el stack viejo y otra tras migrar.

| # | Rama | Contenido | Por que aqui |
|---|---|---|---|
| 1 | `fix/data-loss` | P0 completo | Destruye datos reales hoy. No depende de nada |
| 2 | `chore/dependency-upgrade` | Gradle 9.1, AGP 9.3, SDK 36, minSdk 26, Kotlin, Compose BOM 2026.08.00 | Antes de construir mas UI nueva. Por etapas verificables |
| 3 | `fix/broken-features` | P1 completo | Promesas rotas de la UI, ya sobre el stack actualizado |
| 4 | `refactor/input-and-nav` | P2 + P3 | Se beneficia de los APIs nuevos (predictive back) |
| 5 | `refactor/design-system-round-3` | P4 + P5 + P6 | Cierre natural de las rondas 1 y 2, ya sobre material3 1.4.0 |

`fix/data-loss` no depende de la configuracion de build y puede ir en paralelo con
cualquier otra cosa; el resto del orden si importa por lo dicho arriba.

---

## Verificacion de P0 y P1 (2026-08-30)

Revision de `develop` contra esta auditoria despues de mergear `fix/data-loss`,
`fix/broken-features` y `chore/dependency-upgrade`. `test` + `build` en verde.

### P0 — 8 de 9 resueltos, 1 diferido conscientemente

| # | Estado | Nota |
|---|---|---|
| P0-1 | OK | Regla centralizada en `domain/model/WorkoutSetCompletion.kt`: `reps > 0`, sin exigir peso |
| P0-2 | OK | Todos los campos de texto libre pasan `selectAllOnFocus = false` |
| P0-3 | OK | Ediciones bufferadas; `confirmSaveChanges` persiste solo lo cambiado; botones del dialogo en su posicion correcta |
| P0-4 | OK | `onDismiss` llama a `cancelInlineAlternativeEdit` |
| P0-5 | OK | `prType = null` al des-completar |
| P0-6 | OK | `if (_uiState.value.isSaving) return` en `saveEditor` |
| P0-7 | OK | `draftId` estables como `key` en dias y ejercicios |
| P0-8 | OK | Confirmacion antes de eliminar dia y alternativa |
| P0-9 | Diferido | Documentado como entrada 38 en `mejoras-claude.md`. Decision correcta: el riesgo inmediato se cerro con confirmaciones, y el deshacer real es infraestructura compartida |

### P1 — 10 de 12 completos, 2 parciales

| # | Estado | Nota |
|---|---|---|
| P1-1 | OK | `weightUnit` consumido en Workout, History, Stats y `SetRow` |
| P1-2 | OK | Notas de sesion, serie y ejercicio de punta a punta |
| P1-3 | OK | `observeWorkoutStats(period = period)` |
| P1-4 | OK | `exerciseCount` con `distinct()`, `personalRecordCount` cuenta records reales |
| P1-5 | Parcial | Reloj de pared, persistencia, haptico a nivel de pantalla y auto-start persistido: todo OK. Notificacion/sonido pospuesta a backlog (entrada 39 de `mejoras-claude.md`) |
| P1-6 | OK | `scheduleCelebrationDismissal()` en el ViewModel |
| P1-7 | OK | `retryWhen` en vez de `catch` |
| P1-8 | OK | `WidgetUpdateObserver` + deep links de atajos |
| P1-9 | OK | Bloqueador + limpieza con `DisposableEffect` |
| P1-10 | Parcial | Exportacion OK. Importacion y borrado total pospuestos a backlog (entrada 40 de `mejoras-claude.md`) |
| P1-11 | OK | `revokeAllPermissions()` |
| P1-12 | OK | Codigo muerto eliminado |

### Correccion a la auditoria original

El "leak" del bloqueador de navegacion que se describia en P1-9 **no era real**.
`AppShellViewModel.requestNavigation` ya se protegia con
`if (_blockedRoute.value != currentRoute) return false`, asi que un `_blockedRoute`
obsoleto nunca puede lanzar un dialogo en otra pestana. El valor residual existe pero
no es observable.

### Hueco encontrado: el bug de las variantes duplicadas seguia sin arreglar

`saveExerciseAlternative()` estaba intacto: mismo orden de operaciones, sin guard de
`isSaving`, sin deduplicacion, y con el dialogo abierto y el mensaje enganoso en la rama
rechazada.

Causa: en este documento el bug vivia en la seccion destacada "Los dos fallos reportados
por el usuario", **no en la tabla numerada de P0**. La spec se escribio desde las tablas
P0/P1 y el hallazgo se quedo fuera. Es un fallo de estructura de la auditoria, no de la
implementacion.

Arreglado el 2026-08-30 (`test` + `build` en verde):

- `WorkoutRepository.canReplaceWorkoutExerciseVariant(workoutExerciseId)` expone la regla
  de "se puede cambiar" (`hasRecordedData` compartido con `replaceWorkoutExerciseVariant`,
  una sola fuente de verdad)
- `ExerciseAlternativesUiState.canSwapVariant` se calcula al construir el picker
- `saveExerciseAlternative` comprueba `isSaving`, luego `canSwapVariant`, luego duplicado
  por nombre — **todo antes de escribir nada**
- La rama de carrera perdida cierra el dialogo y dice la verdad ("guardada en la rutina,
  pero no se aplico") en vez de invitar a reintentar
- `applyExerciseVariant` gana guard de `isSaving` y feedback de exito
- La UI deshabilita opciones y el boton de crear cuando no se puede cambiar, y explica por que
- Test de regresion en `ExerciseAlternativesUiStateTest`

### Nota sobre el upgrade

`minSdk` subio de 23 a **26** y `compileSdk` a **37** (exigido por Compose 1.12.0). Ambas
decisiones estan documentadas en `docs/superpowers/specs/2026-08-29-dependency-upgrade.md`
y son deliberadas. Deja fuera dispositivos por debajo de Android 8.0.

---

## Verificacion de P2 y P3 (2026-08-30)

Revision de `refactor/input-and-nav` con `develop` ya mergeado dentro.
`test` + `build` en verde.

### P2 — 8 de 8, con tres huecos corregidos en la revision

| # | Estado | Nota |
|---|---|---|
| P2-1 | OK | `KeyboardCapitalization.Words` en nombres. `normalizeEditorNameInput` y su gemela de Entrenar eliminadas. **Corregido en revision**: las notas no tenian `Sentences`, y `FitTrackInputDialog` seguia con `KeyboardOptions.Default` |
| P2-2 | OK | **Corregido en revision**: el encadenado llego al editor de rutinas y a la fila de serie del historial, pero no a la del entrenamiento en vivo, que es la que se usa al registrar |
| P2-3 | OK | `FitTrackInputDialog` migrado a `TextFieldValue`, asi que ya admite select-all. **Corregido en revision**: seguia reseleccionando en cada toque; ahora `SelectAllArming` lo limita a la primera entrada de foco, para poder corregir un decimal |
| P2-4 | OK | `sanitizeWorkoutRepsInput` y corte de notacion cientifica. **Corregido en revision**: faltaban los topes de digitos |
| P2-5 | Parcial | Saneado unificado en un solo sitio y notacion cientifica resuelta. El separador sigue fijado a coma en vez de derivarse del locale; se deja con P6 (i18n), porque hoy toda la copy es castellano literal |
| P2-6 | OK | Errores diferidos hasta la interaccion |
| P2-7 | OK | `maxLength` en `FitTrackSelectAllTextField` y `FitTrackInputDialog` |
| P2-8 | OK | Pulsacion larga en steppers y tope del objetivo de pasos |

### P3 — 7 de 8, 1 diferido

| # | Estado | Nota |
|---|---|---|
| P3-1 | OK | Slot `leading` en `FitTrackScreenHeader`, atras a la izquierda, `Icons.AutoMirrored` |
| P3-2 | Parcial | El drawer ya hace scroll. El desajuste de lado del boton queda diferido: entrada 41 de `mejoras-claude.md` |
| P3-3 | OK | `activeTabReselected` consumido por las cinco pantallas |
| P3-4 | OK | El menu ya no tiene callejones: widget es informativo y exportar funciona. Ajustes tiene su propio atras |
| P3-5 | OK | `BackHandler` paginado y repeticion del tutorial desde Ajustes |
| P3-6 | OK | Confirmaciones que faltaban, borrado de sesion, y confirmacion al cambiar de variante |
| P3-7 | OK | Un solo enum de periodo con un orden y un defecto. **Anadido en revision**: accion "Limpiar" en la tarjeta de filtros, que faltaba para poder salir del filtro automatico por rutina activa |
| P3-8 | OK | `SavedStateHandle` en Historial y Datos, `rememberSaveable` en lo local, y el tab inicial se consume una sola vez |

### Correccion a la auditoria original

La P3-2 proponia "mover el boton a TopStart, o anclar el drawer a la derecha". Se probo
lo primero y **se revirtio**: `TopStart` es donde `FitTrackScreenHeader` dibuja el titulo
de todas las pantallas, asi que cambiaba un solapamiento por otro peor. El arreglo real
es sacar el boton del overlay y meterlo en la cabecera; queda en la entrada 41.

---

## P4 — sistema visual (2026-08-30)

Rama `refactor/design-system-round-3`, partiendo de `refactor/input-and-nav`.
`test` + `build` en verde en cada commit.

### El editor de rutinas: lo que originó la auditoria

Rehecho sobre el design system, **sin componentes nuevos** salvo un tamaño mas del badge:

- La cabecera del dia ya no pinta un bloque relleno de radio 16dp dentro de una tarjeta
  de radio 16dp con 16dp de margen. La tarjeta es la superficie del dia, asi que vuelve a
  cumplirse la regla de radios concentricos.
- Fuera la barra de acento de 6dp de ancho con radio 8dp. El ordinal es un
  `FitTrackIconBadge`, el mismo marcador que ya usan Home y `SetRow`.
- El bloque de ejercicio era el unico contenedor relleno-sin-borde de la app: ahora es una
  `FitTrackCard`. Y "Ejercicio N" a `labelLarge` en fuerza completa pasa a badge pequeño,
  para dejar de competir con el campo de nombre que tiene justo debajo.
- Nombre del dia a `titleMedium`, un escalon por debajo del nombre de la rutina.
- La expansion se señala solo con el chevron, no cambiando todo el fondo a `primarySoft`.
- La variante predeterminada usa el badge `PREDET.` igual que Entrenar, y "Usar por
  defecto" solo aparece cuando cambiaria algo.
- `FitTrackIconBadgeSize.Small` es lo unico nuevo del design system.

### Estado de P4

| # | Estado | Nota |
|---|---|---|
| P4-1 | OK | `surfaceCard` ya no es identico a `surface`, asi que `FitTrackCard(highlighted = true)` hace algo. Y `surfaceAlt`, que era un alias byte a byte de `surfaceVariant`, se elimina: dos nombres para un mismo color son justo lo que las rondas 1 y 2 vinieron a quitar |
| P4-2 | OK | Roles `onHero` y `onHeroMuted` resueltos por esquema, en lugar de `Color.White` literal sobre un fondo que en oscuro es verde claro |
| P4-3 | OK | Insets zerados en Home, Ajustes, Historial y Datos. Home y Ajustes adoptan la receta compartida de `contentPadding` |
| P4-4 | Pendiente | Siguen tres idiomas de cabecera: `FitTrackScreenHeader`, la pila propia de Home y el `TopAppBar` de Ajustes |
| P4-5 | Pendiente | Los esqueletos siguen sin parecerse a lo que cargan, y falta el esqueleto de lista compartido |
| P4-6 | OK | `FitTrackErrorState` en `core/design`. Arregla ademas la rama de Historial que dejaba los esqueletos brillando para siempre. Corregido tras revision de Codex: el error de Datos compartia campo con el snackbar, asi que la tarjeta desaparecia al cerrarse este; ahora hay un `error` separado de `message`, `retryWhen` en vez de `catch` (que terminaba el flujo) y un reintento real |
| P4-7 | OK | `fitTrackDeltaTone` como fuente unica, con `FitTrackDeltaMeaning.Neutral` para duracion. "Objetivo cumplido" usa el rol `success` que estaba sin usar |
| P4-8 | Parcial | Las etiquetas ya usan sp y siguen el ajuste de fuente. Faltan eje Y, linea base y semantica para lector de pantalla |
| P4-9 | OK | `HeatmapDay.sessionCount`: una sesion de peso corporal cuenta como dia entrenado aunque su volumen sea 0 |
| P4-10 | Pendiente | `LaunchIntroScreen` sigue con paleta duplicada, solo clara, y 1.240 ms fijos |
| P4-11 | OK | El widget usa `LightColors`/`DarkColors` de la app en vez de la paleta dinamica del sistema |

De P5 entra por el camino: `primarySoft` en oscuro daba ~1.1:1 contra `surface`, lo que
hacia indistinguible un dia entrenado de uno vacio. Subido.

---

## Correccion tras pasada manual (2026-08-31)

Probando la pasada 02 de la checklist, el usuario encontro que el arreglo del bug de
variantes era correcto pero **la regla de negocio que protegia era la equivocada**.

**Caso real**: dos maquinas con el mismo patron de movimiento pero de distinta marca
("maquina antigua" y "maquina nueva"), con placas en unidades distintas. Registras 12
repeticiones a 60 kg, y despues ves que la maquina no es esa: en la otra el equivalente
son 57 kg. Quieres cambiar de variante conservando lo que ya has hecho, y la app no te
dejaba. Borrar los valores tampoco desbloqueaba.

Dos causas:

1. `replaceWorkoutExerciseVariant` reconstruia las series desde la prescripcion de la
   variante destino, asi que rechazaba el cambio en cuanto habia datos para no
   destruirlos. Ahora acepta `keepLoggedSets`: en `false` reconstruye (correcto antes de
   entrenar), en `true` solo cambia etiquetas y objetivos y **conserva las series**. El
   picker expone `hasLoggedSets` en vez de `canSwapVariant`: ya no se deshabilita nada,
   se avisa y se confirma.
2. `updateWorkoutExercisesForSet` volvia a aplicar la sugerencia de repeticiones despues
   de **cada** edicion, asi que un campo vaciado se rellenaba solo, y la siguiente
   edicion de esa fila persistia la sugerencia como si la hubieras escrito tu. Por eso
   "desmarcar los valores" no servia de nada. Las sugerencias ahora saltan la fila que se
   esta editando y siguen llegando a las demas.

**Leccion**: al arreglar el bug original hice honesto el pre-chequeo, pero no cuestione
la regla que estaba protegiendo. Un bloqueo correctamente implementado sigue siendo un
bloqueo equivocado si impide el caso en el que mas falta hace.

---

## Verificacion de P5 y P6, y cierre de P4 (2026-08-31)

Trabajo ejecutado en dos ramas hermanas en paralelo (`fix/shared-accessibility-and-copy` y
`refactor/home-stats-visual-locale`, sin ficheros compartidos), ya mergeadas a `develop` y
las ramas borradas. Verificado leyendo el codigo en `develop`, no las specs.

### P4 — 10 de 11

Cerrados desde la revision anterior:

| # | Estado | Nota |
|---|---|---|
| P4-4 | OK | Home y Ajustes ya usan `FitTrackScreenHeader`. Se acaban los tres idiomas de cabecera |
| P4-5 | OK | `SkeletonListItem` como esqueleto de lista compartido, y el badge de Home deja de decir "SIN SESION" antes de que lleguen los datos |
| P4-10 | OK | `LaunchIntroScreen` sin un solo `Color(0xFF...)`: respeta el tema oscuro y su duracion fija queda documentada |
| P4-8 | Parcial | Las etiquetas escalan con la fuente del sistema y la grafica tiene `contentDescription`. **Sigue sin eje Y ni linea base**: normaliza a `minY..maxY`, asi que 100→101 kg se dibuja igual que 50→150 |

### P5 — 4 de 5

| # | Estado | Nota |
|---|---|---|
| P5-1 | OK | De 3 a 9 ficheros con `Role.` declarado en pulsables |
| P5-2 | **Cerrado: no se arregla** | Se subio el area tactil del stepper compacto a 40dp y se revirtio porque estrujaba los campos de peso/reps — la misma trampa del phase-log del 2026-07-05 (Bug D), donde 48dp por boton robaban ~80dp de ancho a la fila. **Decision del dueño el 2026-08-31: se queda como esta.** En uso real el tamaño actual funciona y la alternativa se ve peor. No es deuda pendiente |
| P5-3 | OK | TalkBack ya no lee el id de Room |
| P5-4 | OK | Celdas del calendario y tira semanal de Home con `contentDescription` |
| P5-5 | OK | La grafica de progreso tiene descripcion accesible |

### P6 — 3 de 4

| # | Estado | Nota |
|---|---|---|
| P6-1 | **Pendiente** | Cero `stringResource` en el proyecto. Spec escrita y por fases en `docs/superpowers/specs/2026-08-30-string-resources-migration.md`, sin ejecutar |
| P6-2 | OK | "Anadir" corregido, cero ocurrencias |
| P6-3 | OK | Plurales correctos en widget y Home |
| P6-4 | OK | Politica de locale unificada en constantes con nombre (`STATS_LOCALE`, `HOME_LOCALE`, `spanishCollator()`) en vez de literales repartidos |

### Lo que queda del plan principal

1. **P6-1**: migracion de literales a `stringResource`. Es la pieza grande que queda.
   Spec y plan ya escritos y desbloqueados.
2. **P4-8**: eje Y y linea base en las graficas.
   Spec: `docs/superpowers/specs/2026-08-31-chart-scale-honesty.md`.
3. **Backlog**: entradas 38 a 42 de `mejoras-claude.md`.
4. **Pasada manual** sin terminar, y `main` por detras de `develop`.

P5-2 queda cerrado por decision, no pendiente. Con eso **P5 se da por completo**: los cuatro
puntos accionables estan hechos y el quinto se descarta a proposito.
