# Auditoria tecnica — Julio 2026

Fecha: 2026-07-04
Metodo: tres revisiones independientes (capa de datos/dominio, capa de features/UI, calidad/release) sobre el codigo real, con evidencia por archivo y linea.
Estado del repo auditado: main en `5479de3` (workout UX fixes mergeados), docs reorganizados en `858f7cd`.

Este documento es el inventario de hallazgos. El plan de ejecucion derivado esta en `docs/planning/roadmap-release.md`.

---

## Resumen ejecutivo

La app esta funcionalmente madura (el flujo core completo existe y se usa a diario), pero hay:

- **2 fallos de integridad de datos** en el ciclo de vida de la sesion de entrenamiento (doble sesion por race condition, finishSession no atomico). Son los unicos fallos que pueden corromper o ensuciar datos reales.
- **2 crashes latentes** en UI (`options.first()` en alternativas, `requireNotNull(activity)` en rutinas).
- **2 bloqueantes de Google Play** (backup de la DB sin reglas — riesgo de restauracion corrupta — y politica de privacidad obligatoria por Health Connect).
- **1 permiso roto en la practica**: `POST_NOTIFICATIONS` nunca se pide en runtime, asi que la notificacion de sesion activa no aparece en Android 13+.
- Deuda estructural manejable: `WorkoutScreen.kt` de ~1500 lineas, cero `strings.xml`, sin crash reporting, sin tests del flujo finish/discard.

Nada de esto invalida el objetivo de prueba cerrada. Todo es corregible en fases cortas.

---

## 1. Integridad de datos (invariante de snapshots y ciclo de sesion)

### DAT-C1 — Doble sesion abierta por race condition [CRITICO]

`domain/usecase/StartWorkoutSessionUseCase.kt:14-35`

El check de sesion activa y la creacion de la nueva sesion son dos operaciones separadas sin transaccion ni constraint. Dos invocaciones concurrentes (doble tap en Iniciar) pasan ambas el check nulo y crean dos sesiones abiertas. La segunda pasa a ser la "activa" (`ORDER BY startedAt DESC LIMIT 1`) y la primera queda como basura invisible: sin `finishedAt`, nunca aparece en Historial ni Stats, pero existe.

Confirmado de forma independiente por las dos auditorias (datos y UI).

Fix: indice unico parcial `WHERE finishedAt IS NULL` sobre `workout_sessions` + envolver check+create en `database.withTransaction`. Reforzar en UI respetando `isStarting` hasta que el coroutine complete.

### DAT-C2 — finishSession no atomico ni idempotente [CRITICO]

`data/repository/DefaultWorkoutRepository.kt:153-161`

Read-modify-write en dos llamadas sin transaccion. Si el proceso muere entre medias, la sesion queda abierta para siempre. Ademas una segunda llamada re-escribe `finishedAt` con timestamp nuevo, mutando historial en silencio.

Fix: un solo UPDATE atomico con guarda:
`UPDATE workout_sessions SET finishedAt = :ts, notes = :notes WHERE id = :id AND finishedAt IS NULL`

Relacionado en UI: el boton Finalizar del dialogo de confirmacion no tiene `enabled = !state.isFinishing` (`feature/workout/WorkoutScreen.kt:176-184`), permitiendo llamadas multiples.

### DAT-H1 — Agrupacion de ejercicios en Stats inconsistente para datos migrados [ALTO]

`domain/usecase/ObserveWorkoutStatsUseCase.kt:112-114` + `MIGRATION_1_2` en `core/database/FitTrackPlusDatabase.kt`

Tras la migracion 1→2 conviven tres formatos de `performedVariantKey`: `exercise-$id`, `legacy:$nombre` y UUID. El fallback de Stats (`exerciseNameSnapshot.lowercase()`) no casa con el formato `legacy:`. Resultado: el mismo ejercicio puede partirse en dos series de progreso distintas para usuarios que vienen de v1.

Fix: normalizar la clave de agrupacion en un unico sitio (funcion canonica) que resuelva los tres formatos, y cubrirla con tests.

### DAT-M1 — Heatmap usa dias UTC, no fecha local [MEDIO]

`domain/usecase/GetWorkoutHeatmapUseCase.kt:19-29`

`finishedAt / DAY_MS` da el dia UTC. Un entrenamiento a las 23:30 en UTC+2 se pinta en el dia anterior. El streak (`GetWorkoutStreakUseCase`) ya lo hace bien con `ZoneId.systemDefault()`; el heatmap no.

Fix: mismo patron que el streak — `Instant → ZonedDateTime → LocalDate.toEpochDay()`.

### DAT-M2 — Cargas O(N) completas para operaciones puntuales [MEDIO]

- `GetWorkoutHistoryDetailUseCase.kt:22-28`: carga TODAS las sesiones con ejercicios y sets para encontrar la sesion anterior comparable.
- `GetWorkoutStreakUseCase.kt:14-16`: carga todas las sesiones para calcular el streak.

Con historial pequeño no se nota; con 6-12 meses de uso diario es presion de memoria y lentitud en cada apertura de detalle. Fix: queries dirigidas en DAO (previous comparable session con LIMIT 1; streak como SELECT DISTINCT de fechas).

### DAT-M3 — Otros hallazgos de datos

- `replaceRoutine` es delete+reinsert destructivo (`DefaultRoutineRepository.kt:74-88`); deja `exerciseTemplateId` colgando en `workout_exercises` (FK nullable sin accion). Los snapshots protegen el historial, pero es deuda para sync.
- Indice unico GLOBAL sobre `routine_exercise_alternatives.variantKey`: un caller que repita clave entre ejercicios provoca `UNIQUE constraint failed` en runtime.
- `DebugDemoDataSeeder`: check de vacio en dos queries sin transaccion; instancia inyectada tambien en release (solo la ejecucion esta guardada por flag debuggable).
- Defaults `error("Not implemented")` en la interfaz `WorkoutRepository` (`replaceWorkoutExerciseVariant`, `discardSession`): crash en runtime si un doble de test no los sobreescribe. Mejor metodos abstractos.
- `weekNumber` es un contador derivado, no semana de calendario; nombre confuso.

---

## 2. Crashes y UX en features

### UI-C1 — Crash en dialogo de alternativas con lista vacia [CRITICO]

`feature/workout/WorkoutViewModel.kt:768`

```kotlin
get() = options.firstOrNull { it.variantKey == currentVariantKey } ?: options.first()
```

`options.first()` lanza `NoSuchElementException` si `options` esta vacia. Fix: `firstOrNull()` + no abrir el dialogo sin opciones. Nota estructural: propiedad computada que lanza dentro de una `data class` — mover a funcion con manejo explicito.

### UI-C2 — requireNotNull(activity) en RoutinesScreen [CRITICO]

`feature/routines/RoutinesScreen.kt:92-93`

Crash duro si `LocalActivity.current` es null (previews, tests, reclamo de Activity). Ademas acopla el feature de rutinas al ViewModel del shell via Activity. Fix: pasar el navigation blocker como lambda desde el nivel del NavHost/Shell.

### UI-H1 — La notificacion de sesion activa no navega con la app viva [ALTO]

`core/notification/ActiveSessionNotificationManager.kt:47` + `MainActivity.kt:39-43`

El extra `open_tab` solo se lee en `onCreate`. Con `FLAG_ACTIVITY_SINGLE_TOP` y la app viva, llega por `onNewIntent`, que no esta sobreescrito: tocar la notificacion no hace nada. Fix: sobreescribir `onNewIntent` y señalizar al NavHost.

### UI-H2 — POST_NOTIFICATIONS nunca se pide en runtime [ALTO]

Manifest lo declara y el codigo hace `checkSelfPermission`, pero no existe ninguna `requestPermissions` en todo el codigo. En Android 13+ la notificacion de sesion activa simplemente no aparece nunca. Fix: pedir el permiso al iniciar el primer entrenamiento con rationale.

### UI-H3 — Edicion de historial puede descartarse en silencio [ALTO]

`feature/history/HistoryViewModel.kt` (init/onEach): si la sesion seleccionada desaparece del flow mientras `isEditMode == true`, la pantalla vuelve a la lista sin pasar por la confirmacion de cambios sin guardar.

### UI-H4 — Dialogo de alternativas: altura sin limite + imePadding dentro de Dialog [ALTO]

`feature/workout/WorkoutScreen.kt:926-961`

Sin `heightIn(max)`: en pantallas pequeñas o con teclado abierto el titulo y el boton de cerrar pueden salirse de pantalla. `imePadding()` dentro de `Dialog` no recibe insets correctos en todas las versiones (ventana separada). Fix: limitar altura (p.ej. `fillMaxHeight(0.85f)`) con cabecera fija, y valorar `ModalBottomSheet` que gestiona IME de forma fiable.

### UI-H5 — imeBottom leido por frame en composicion [ALTO]

`feature/workout/WorkoutScreen.kt:284-297`

`WindowInsets.ime.getBottom()` leido directo en composicion re-layouta el LazyColumn en cada frame de la animacion del teclado (flicker). Fix: `derivedStateOf` o gestionar via scroll.

### UI-M — Deuda estructural de features

- `WorkoutScreen.kt` ~1492 lineas, god composable con `@file:Suppress("TooManyFunctions")`. `HistoryViewModel` importa funciones `internal` del feature workout (`parseWorkoutWeightInput` etc.): violacion de frontera entre features. Fix: extraer utilidades de parseo a `core/util/`.
- `persistSet` escribe en Room por cada tecla sin debounce (`WorkoutViewModel.kt:477-507`). Fix: debounce ~300ms por setId.
- `editSnapshot` de HistoryViewModel en `var` plano, no en `SavedStateHandle`.
- Funcion muerta `shouldAutoStartRestTimer` en `RestTimerState.kt:74-87` (la autoritativa vive en el VM).
- Sesion recuperada tras process-death que ya esta finalizada: se limpia la key sin mensaje; el usuario ve la pestaña Entrenar en blanco sin explicacion.

### UI-L — Pulido

- Cero cobertura de `strings.xml`: todos los textos son literales Kotlin en composables. Bloqueo total para i18n (y para el mercado internacional post-lanzamiento).
- Verde hardcodeado `Color(0xFF2E7D32)` x3 en WorkoutScreen; deberia ser token semantico del design system (no responde a dark mode).
- `SimpleDateFormat` instanciado por item por frame en listas (WorkoutScreen:1476, HistoryScreen:711).
- `HistoryScreen` sin `contentWindowInsets = WindowInsets(0,0,0,0)` (inconsistente con Workout y Routines; posible doble inset).
- Steppers de reps con area tactil de 28dp (minimo Material 3: 48dp) — usabilidad real en mitad de una serie.
- Fila expandible de ejercicio sin semantica de accesibilidad (rol/descripcion).

---

## 3. Release readiness (Google Play)

### REL-B1 — Backup sin reglas con allowBackup=true [BLOQUEANTE]

`AndroidManifest.xml:16` + `backup_rules.xml` / `data_extraction_rules.xml` (plantillas sin tocar)

La Room DB se respalda a Google Drive sin control. Una restauracion de una DB v1 sobre una app que espera v2 (o al reves) rompe la apertura de la base de datos. Para una app offline-first donde la DB es LA UNICA copia de los datos, un backup sin control es peor que no tener backup.

Fix para prueba cerrada: excluir `fittrackplus_v2.db` del backup en ambos ficheros. Revisitar con estrategia de backup consciente de migraciones antes de produccion.

### REL-B2 — Politica de privacidad obligatoria por Health Connect [BLOQUEANTE]

La app pide `android.permission.health.READ_STEPS` y declara el rationale de Health Connect. Play Console exige URL de politica de privacidad publica + declaracion de Health apps para publicar en CUALQUIER track, incluida la prueba cerrada.

Fix: pagina simple (GitHub Pages sirve) + formulario Data Safety + declaracion Health Connect en Play Console.

### REL-H — Antes de la prueba cerrada

- **Sin signing config** (`app/build.gradle.kts`): no se puede producir un AAB firmado para Play. El workflow de release actual sube un APK de debug. Fix: `signingConfigs.release` leyendo keystore de secrets + `bundleRelease` en CI.
- **R8 desactivado** (`isMinifyEnabled = false`, sin `shrinkResources`): binario grande y sin obfuscar; ademas Play Console no podra simbolicar crashes. Fix: activar con keep rules para Hilt/Room.
- **`versionName = "2.0.0-dev"`**: visible para testers. Definir estrategia de versionado (`2.0.0-alpha1` + incremento de versionCode en CI).
- **Sin crash reporting**: si crashea en el movil de un tester, no te enteras. Recomendacion: Sentry (tier gratuito, sin dependencia de Google, plugin Gradle sube los mapas de R8). Para la metrica de activacion ("segundo entrenamiento en 7 dias"): contador local en DataStore (timestamp de primera sesion + conteo), sin backend de analitica para 12 testers.
- **Flujo finish/discard sin tests** (`FinishWorkoutSessionUseCase`, discard en `WorkoutViewModel`): es la mutacion de datos mas critica de la app y no tiene ni un test. Los tests existentes de use cases usan fakes y prueban comportamiento (buena calidad) — extender ese patron.

### REL-M — Antes de produccion

- Sin test de migracion Room pese a que `MIGRATION_1_2` es compleja (usar `MigrationTestHelper` contra los schemas commiteados — los schemas SI estan commiteados y `exportSchema = true`, esa parte esta bien).
- Compose BOM `2024.04.01` (15 meses desactualizado) y AGP 8.5.1. Health Connect en `1.1.0-alpha11`.
- Icono launcher: placeholder de Android Studio. Necesario icono real de marca para la prueba cerrada.
- `default_web_client_id` (OAuth client ID) commiteado en `strings.xml:2` sin uso aparente (no hay Firebase ni GoogleSignIn en el build). Retirarlo.
- Cero tests de ViewModels.

### REL-L — Higiene

- Zips binarios en `docs/branding/`.
- `ExampleUnitTest` / `ExampleInstrumentedTest` de plantilla aun presentes.
- CI no ejecuta `connectedAndroidTest`: los tests de DAO (instrumentados) nunca corren en CI. Fix: job con `android-emulator-runner`.
- Tests unitarios bajo `src/test/java/` en un proyecto kotlin-only.

---

## 4. Preparacion para sync (pre-Coach)

Decisiones que conviene tomar ANTES de acumular mas datos de usuarios reales, porque cada dia de uso las encarece:

1. **Sin identidad de usuario/dispositivo en ninguna tabla**: añadir sync luego implica migrar 7 tablas con `remoteId` nullable + logica de merge.
2. **`variantKey` es UUID local**: la misma rutina creada en dos dispositivos genera claves distintas → el merge partiria el historial de un ejercicio en dos series. Hace falta identidad canonica antes de sync.
3. **Sin `updatedAt` en workout_sessions/exercises/sets** (rutinas si lo tienen): last-write-wins sin timestamps = perdida de datos silenciosa en merge.
4. **`replaceRoutine` destructivo**: un servidor necesita el diff (añadido/borrado/reordenado), no un reemplazo total.
5. **`activeRoutineId` en DataStore es concepto por-dispositivo**: correcto, pero debe quedar explicitamente scoped como local-only.

No hay que implementar sync ahora. Hay que evitar decisiones nuevas que lo empeoren y, cuando toque tocar esas zonas por otro motivo, dejar el terreno preparado.

---

## Solapamientos entre auditorias (confianza alta)

| Hallazgo | Confirmado por |
|---|---|
| Doble sesion abierta (race en start) | Datos C-1 + UI C-4 |
| Finalizacion sin guarda / no idempotente | Datos C-2 + UI H-5 |
| Defaults `error()` en interfaz de repositorio | Datos M-5 + Release L-5 |
| Seeder debug con checks no atomicos / inyectado en release | Datos M-3 + Datos L-3 |
