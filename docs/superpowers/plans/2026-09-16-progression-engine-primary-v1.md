# Plan: Progression Engine PRIMARY V1

> **Para Codex:** lee la spec **entera** antes de empezar, y lee tambien las dos specs que
> supersede para entender que se corrige y por que. **Crea la rama desde `develop`.** Commits
> separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica `test` + `build`
> antes de cada commit; NO lances el emulador, eso lo hace el dueño. No commits de WIP.

**Rama:** `feature/progression-engine-primary` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-09-16-progression-engine-primary-v1.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

**Codigo de produccion en `app/src/main/kotlin/`. Tests en `app/src/test/java/`.**
No tocar `app/src/main/java/` (residuo legacy, fuera del build).

---

## Regla que gobierna todo el plan

El motor es una **funcion pura** (R20). Si te ves inyectando un DAO, un `Context`, un reloj
o una corrutina dentro de la logica de decision, **para**: la has puesto en el sitio
equivocado. La fontaneria va en el use case, la decision va en `domain/model/progression/`.

Si en cualquier tarea te ves tocando ficheros que no estan en su lista, **para y avisa**.

---

## Tarea 0: Preparar rama

La rama **ya existe y ya parte de la punta de `develop`**. Verificado el 2026-09-16:
`develop...HEAD` = 0/0 y `develop` == `origin/develop`. **No hagas
`git checkout develop && git pull`**: no aporta nada aqui y te obliga a mover cambios sin
confirmar sin motivo.

- [ ] Confirmar que estas en `feature/progression-engine-primary`
- [ ] Commit inicial **solo de la documentacion de esta fase**: la spec y el plan
      `2026-09-16-progression-engine-primary-v1.md` y los tres banners `SUPERSEDED`.
      Mensaje: `docs: spec y plan del progression engine primary v1`
- [ ] **`design/` no se toca.** Es un prototipo de UI (React/Vite) ajeno a esta fase y ya
      estaba sin seguimiento antes de empezar. Ni commit, ni borrado, ni movimiento, ni
      entrada en `.gitignore`. No es tuyo.
- [ ] `test` + `build` en verde antes de tocar codigo, para partir de base sana

---

## Tarea 1: RIR de la primera serie — persistencia (R1)

Ficheros: `data/local/entity/WorkoutExerciseEntity.kt`,
`core/database/FitTrackPlusDatabase.kt`, `di/DatabaseModule.kt`, mappers del repositorio.

- [ ] Columna `firstSetRir: Int?` en `WorkoutExerciseEntity`, default `null`.
- [ ] `MIGRATION_6_7` aditiva: `ALTER TABLE workout_exercises ADD COLUMN firstSetRir INTEGER`.
      Sin recrear tablas, sin backfill, sin `fallbackToDestructiveMigration`.
- [ ] Subir `version = 7` en `FitTrackPlusDatabase` y registrar la migracion en `DatabaseModule`.
- [ ] Propagar el campo por el modelo de dominio y los mappers. Validar rango `0..10`.
- [ ] Test de mapeo: `null` sobrevive el viaje de ida y vuelta, y un valor fuera de rango se
      rechaza.
- [ ] `app/schemas/.../7.json` generado y **commiteado**.
- [ ] `test` + `build`, commit.

## Tarea 2: RIR — entrada en Workout (R2, R3)

Ficheros: `feature/workout/WorkoutScreen.kt`, `feature/workout/WorkoutViewModel.kt`,
`res/values/strings.xml`.

- [ ] Selector con `core/design/SegmentedSelector.kt`. **No crear componente nuevo.**
- [ ] Opciones `0 / 1 / 2 / 3 / 4+`. El `4+` se persiste como `4`.
- [ ] El texto **menciona explicitamente la primera serie**. No vale poner "RIR" a secas.
      Algo del estilo "Repeticiones que te sobraron en la primera serie".
- [ ] Zona 1-3 señalada como util, sin regañar al salirse. El `0` etiquetado como fallo y
      **sin ningun tratamiento de logro**: ni color de exito, ni medalla, ni felicitacion.
- [ ] Editable y borrable (volver a no-reportado) con la sesion abierta.
- [ ] **Terminar una sesion sin tocar el RIR ni una vez tiene que funcionar.** Ninguna
      validacion nueva puede bloquear el cierre. Esto es no negociable.
- [ ] Strings a `strings.xml`, no hardcodeados.
- [ ] Test de UI state: sin RIR el ejercicio se cierra igual.
- [ ] `test` + `build`, commit.

## Tarea 3: e1RM a dominio con confianza sobre reps efectivas (R4, R5)

Ficheros: nuevo `domain/model/progression/StrengthEstimate.kt`,
`domain/usecase/ObserveWorkoutStatsUseCase.kt`.

- [ ] Sacar la formula de la extension privada de `ObserveWorkoutStatsUseCase` a un modelo de
      dominio propio. Se mantiene **Epley**: anota en el codigo por que no se cambia (cambiarla
      reescribiria retroactivamente la curva historica del usuario).
- [ ] `effectiveReps = reps + rir`. Confianza: `<= 6` alta, `<= 10` media, `> 10` **no estima**.
- [ ] **Sin RIR no hay e1RM.** Es un cambio de comportamiento y es intencionado.
- [ ] Eliminar el centinela `0.0`: `ExerciseProgressEntry.estimatedOneRepMaxKg` y
      `ExerciseSetRecord.estimatedOneRepMaxKg` pasan a `Double?`.
- [ ] Tests del criterio 9 de la spec, los cuatro casos.
- [ ] `test` + `build`, commit.

## Tarea 4: Graficas y records honestos (R6, R7)

Ficheros: `feature/stats/StatsViewModel.kt`, `feature/stats/StatsScreen.kt`.

- [ ] `MaxWeight`, `Volume` y `Reps` siguen pintando **todos** los puntos. No los toques.
- [ ] `EstimatedOneRepMax` pinta solo los estimables.
- [ ] La condicion `>= 2 puntos` se evalua sobre la serie **ya filtrada**. Un ejercicio sin
      datos estimables muestra vacio con sentido: ni linea plana en cero, ni crash.
- [ ] `bestEstimatedOneRepMax` solo considera estimables; puede quedar `null` y entonces la
      tarjeta no se muestra.
- [ ] Etiqueta de confianza ("alta" / "media") en `ProgressPointDetails` y en el record.
- [ ] `test` + `build`, commit.

## Tarea 5: Calidad de esfuerzo en Datos (R8, R9)

Ficheros: `domain/usecase/ObserveWorkoutStatsUseCase.kt`, `feature/stats/`.

- [ ] Porcentaje de ejercicios con RIR reportado en zona 1-3. Denominador **solo** los
      reportados: los `NULL` no cuentan ni a favor ni en contra.
- [ ] Con cero RIR reportados en el periodo la metrica **no se muestra**. No pintar "0%".
- [ ] Indicar sobre cuantos ejercicios se calcula.
- [ ] Reparto por zonas: fallo (0), util (1-3), lejos (4+). Ninguna presentada como buena o
      mala en si misma.
- [ ] Respetar los filtros `WorkoutStatsPeriod` existentes.
- [ ] `test` + `build`, commit.

---

> A partir de aqui empieza el motor. Las tareas 1-5 son la capa de medicion y tienen valor
> por si solas. Si algo se tuerce, ese es el punto de corte natural.

---

## Tarea 6: Modelo de dominio del motor (R11, R12, R21)

Ficheros: todo nuevo bajo `domain/model/progression/`.

- [ ] Enums: `ExerciseRole`, `ProgressionState`, `ExposureType`, `OutcomeClass`,
      `E1rmConfidence`, `StrengthTrend`, `SpecificityTier`.
- [ ] `ProgressionTuning.kt` con **todas** las constantes de R21, copiadas tal cual de la spec.
      Ninguna constante magica fuera de aqui.
- [ ] `ExerciseProgressionProfile`, `ProgressionExposure`, `ProgressionPrescription`
      (con `decisionReason: String`).
- [ ] `CONSOLIDATING` **no existe** como valor de `ProgressionState`. Es derivado:
      `pendingConfirmVolume || pendingConfirmStrength`. Exponlo como propiedad calculada.
- [ ] Sin dependencias de Room ni de Android en este paquete.
- [ ] `test` + `build`, commit.

## Tarea 7: Persistencia del motor (R10, R11, R12, R22)

Ficheros: `data/local/entity/`, `data/local/dao/`, `core/database/`, `di/DatabaseModule.kt`,
`data/local/entity/RoutineExerciseEntity.kt`.

- [ ] `progressionRole: String` en `RoutineExerciseEntity`, default `"ACCESSORY"`.
- [ ] Tablas `exercise_progression_profiles` (PK `variantKey`) y `progression_exposures`
      (PK autogenerado, `variantKey` indexado, `workoutExerciseId` FK con
      **`ON DELETE SET NULL`**).
- [ ] `MIGRATION_7_8` aditiva con las tres cosas. `version = 8`. Registrar en `DatabaseModule`.
- [ ] DAOs y repositorio.
- [ ] `app/schemas/.../8.json` generado y **commiteado**.
- [ ] **Invariante (R22):** `progression_exposures` se escribe una vez y no se recalcula jamas.
      Las sesiones abiertas **no** generan exposicion; solo al finalizar. Una exposicion huerfana
      (`workoutExerciseId = NULL`) **sigue contando** para la tendencia: ocurrio.
- [ ] Test de migracion con Room in-memory: datos previos intactos, defaults correctos.
- [ ] `test` + `build`, commit.

## Tarea 8: Calculo de exposicion — surplus, clasificacion, e1RM (R13, R14)

Ficheros: `domain/model/progression/`, tests.

- [ ] `surplus = (reps + rir) - (floor((repMin+repMax)/2) + targetRir)` y mapeo a `OutcomeClass`.
- [ ] Las cuatro guardas de R14 **en orden**: reps bajo minimo, reps muy bajo minimo,
      `setCompletionRatio`, RIR ausente.
- [ ] Dos EWMA separadas, una por `ExposureType`. **Nunca mezclar.** Deja un comentario
      explicando el motivo: Epley sobrestima con mas reps, mezclarlas genera un diente de sierra
      artificial que dispara recuperaciones fantasma.
- [ ] Tendencia con `TREND_MIN_POINTS`, `TREND_LOOKBACK` y las bandas **asimetricas**.
      STRENGTH primero, VOLUME de respaldo, `UNKNOWN` si no hay datos.
- [ ] Tests: criterios 13 y 22 de la spec. Incluye el caso `rir == null`.
- [ ] `test` + `build`, commit.

## Tarea 9: Decision de carga y maquina de estados (R15, R16, R18, R20)

Ficheros: `domain/model/progression/`, `domain/usecase/CalculateNextPrescriptionUseCase.kt`.

- [ ] `calculateNextPrescription(profile, recentExposures): ProgressionPrescription`.
      **Funcion pura.** Sin Room, sin corrutinas, sin `Context`, sin reloj del sistema
      (la fecha entra por parametro).
- [ ] Tabla de carga de R15 completa, con los **dos carriles independientes**.
- [ ] La asimetria de R15, que es la regla mas importante: carga confirmada necesita **dos**
      `FAILED` para bajar; carga con `pendingConfirm` revierte al **primer** `FAILED`.
- [ ] Niveles de especificidad de R16.
- [ ] Los cuatro estados de R18 con sus transiciones exactas.
- [ ] `CALIBRATING` prescribe **por el suelo del rango** (7, no 8). El motivo esta en la spec:
      `7 + 2 = 9` deja margen bajo el corte de 10 reps efectivas, si no la calibracion se queda
      sin estimacion.
- [ ] Triggers de recuperacion **en orden**, con `R0` (cooldown) cortando antes que ninguno.
- [ ] `userFlaggedBadDay` excluye de tendencia, T2, T3 y `consecutiveFail`.
- [ ] Tests: criterios 14 a 23. **Esta es la tarea con mas cobertura del plan, no la recortes.**
- [ ] `test` + `build`, commit.

## Tarea 10: Ajuste intra-sesion por la probe (R17)

Ficheros: `feature/workout/WorkoutViewModel.kt`, `domain/model/progression/`.

- [ ] La primera serie se ejecuta **siempre a la carga prescrita**. No se ajusta.
- [ ] Tras completarla, ajustar las series restantes **una sola vez**, tabla de R17,
      cap `INTRA_MAX_STEPS`.
- [ ] **Anti-doble-conteo:** la decision de la siguiente exposicion usa exclusivamente
      `probeSurplus`, **nunca** las series ajustadas. Si contabilizas ambos, el motor se
      autoamplifica y oscila. Deja el comentario en el codigo.
- [ ] Desactivado en `CALIBRATING`, `RECOVERING` y `EVALUATING`.
- [ ] La sugerencia se **propone**, no se impone: el usuario puede registrar el peso que quiera
      y lo que se registre es lo que se guarda.
- [ ] Test del criterio 17.
- [ ] `test` + `build`, commit.

## Tarea 11: Marcar PRIMARY en el editor de rutinas (R10)

Ficheros: `feature/routines/`, `res/values/strings.xml`.

- [ ] Selector de rol en el editor de ejercicio. Default `ACCESSORY`.
- [ ] Limite `MAX_PRIMARY_EXERCISES = 3` por rutina activa. El cuarto se rechaza con mensaje
      explicativo: PRIMARY implica gestion de fatiga y en V1 no hay fatiga cruzada.
- [ ] Al marcar PRIMARY se crea el `ExerciseProgressionProfile` en `CALIBRATING`.
- [ ] Campo opcional de objetivo (`goalWeightKg`) y de `loadIncrementKg`.
- [ ] Test del criterio 24.
- [ ] `test` + `build`, commit.

## Tarea 12: Prescripcion y explicacion en Workout (R19)

Ficheros: `feature/workout/WorkoutScreen.kt`, `feature/workout/WorkoutViewModel.kt`,
`res/values/strings.xml`.

- [ ] Para ejercicios PRIMARY, mostrar la prescripcion: carga, rango de reps, RIR objetivo.
- [ ] **Mostrar siempre `decisionReason`. Nunca un peso a secas.**
- [ ] Cuando la carga no sube pero el esfuerzo baja, el mensaje reconoce el progreso
      ("Carga consolidandose. Mismo peso con menos esfuerzo."). Mostrar constantemente
      "no has subido peso" es falso y desmotiva.
- [ ] Toggle de "mal dia" que marca `userFlaggedBadDay`.
- [ ] **`GetProgressionHintUseCase` sigue intacto para los no-PRIMARY.** No lo borres, no lo
      modifiques, no toques su test. Solo se hace bypass cuando el rol es PRIMARY.
- [ ] Strings a `strings.xml`.
- [ ] Test del criterio 26 y del 27 (regresion).
- [ ] `test` + `build`, commit.

## Tarea 13: Cierre

- [ ] `test` + `build` completos en verde.
- [ ] Verificar los 27 criterios de aceptacion uno por uno. Los que no puedas verificar sin
      emulador, listalos como **pendientes de pasada manual**.
- [ ] Actualizar `docs/progress/project-progress.md` y `docs/progress/phase-log.md`.
- [ ] Marcar como superseded en su cabecera:
      `docs/superpowers/specs/2026-07-30-estimated-1rm-confidence.md`,
      `docs/superpowers/specs/2026-07-30-effort-quality-rir.md`.
      **No las borres:** documentan decisiones y el porque de las correcciones C1-C5.
- [ ] Actualizar el bloque "Current status" de `CLAUDE.md`: la DB pasa a v8 (ahora dice v2,
      esta desactualizado).
- [ ] Push y avisar al dueño.

---

## Aviso obligatorio al dueño al terminar

Decirlo explicitamente, porque **parece un bug y no lo es**:

- Puntos que hoy salen en la grafica de 1RM van a **desaparecer** (los que no tienen RIR o
  superan 10 repeticiones efectivas).
- El record de 1RM puede **bajar o vaciarse**.

Es la correccion de un dato que estaba mal, no una regresion.

Y listar aparte lo que queda **pendiente de pasada manual en emulador**: el flujo completo de
RIR en Workout, el selector de rol, y la prescripcion con su explicacion.
