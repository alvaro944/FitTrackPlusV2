# Spec: Progression Engine PRIMARY V1

**Fecha:** 2026-09-16
**Fase:** 2 del arco de periodizacion (fusiona 2a + 2b + motor)
**Plan de ejecucion:** `docs/superpowers/plans/2026-09-16-progression-engine-primary-v1.md`
**Prerequisito:** ninguno. Parte de `develop`.

**Supersede a:**
- `docs/superpowers/specs/2026-07-30-estimated-1rm-confidence.md` (nunca ejecutada)
- `docs/superpowers/specs/2026-07-30-effort-quality-rir.md` (nunca ejecutada)
- `docs/superpowers/specs/2026-06-10-weight-progression-hints.md`, **solo para ejercicios PRIMARY**

---

## Filosofia del motor

Se escribe aqui porque gobierna cada decision de abajo:

> FitTrackPlus no intenta predecir el rendimiento humano. Usa historial, tendencia de
> fuerza, repeticiones y esfuerzo percibido para hacer modificaciones pequenas y
> razonables de una exposicion a la siguiente. Una medicion aislada nunca es la verdad:
> el sistema busca tendencias. El objetivo no es maximizar el e1RM, es que el
> rendimiento real progrese con una carga de entrenamiento sostenible.

---

## Por que

La progresion actual (`GetProgressionHintUseCase`) mira la media de repeticiones de las
ultimas 3 sesiones contra un rango fijo y devuelve `UP` / `DOWN` / `NONE`. No sabe nada de
esfuerzo, no distingue un mal dia de una tendencia, no ajusta nada dentro de la sesion y
trata igual un ejercicio que quieres progresar que uno de relleno.

Para los 2-3 ejercicios que el usuario quiere progresar de verdad (PRIMARY) eso es
insuficiente. Esta spec construye el motor completo para PRIMARY: medicion de esfuerzo,
e1RM con confianza, alternancia de estimulos, autorregulacion intra-sesion y recuperacion
reactiva.

SECONDARY y ACCESSORY quedan **fuera**: siguen con `GetProgressionHintUseCase` intacto.

## Estado actual verificado (2026-09-16)

- `WorkoutSetEntity`: `weightKg`, `reps`, `isCompleted`, `notes`. **Nada de esfuerzo.**
- `WorkoutExerciseEntity`: `targetRepsSnapshot`, `targetRepsMinSnapshot/MaxSnapshot`,
  `performedVariantKey`, `notes`, `position`. **Nada de esfuerzo.**
- **DB en version 6.** `MIGRATION_5_6` solo anade `notes` a `workout_exercises`.
  Registradas en `di/DatabaseModule.kt`. Sin `fallbackToDestructiveMigration`.
- `rg -i "rir|e1rm|estimated1rm|effortQuality"` sobre `app/src/main/kotlin`: **cero
  resultados reales.** Las specs 2a y 2b se escribieron pero nunca se ejecutaron.
- `estimatedOneRepMaxKg()` es una funcion privada de extension en
  `ObserveWorkoutStatsUseCase.kt` (~linea 240), Epley crudo, unica condicion `reps > 0`,
  devuelve `0.0` como centinela.
- La app **no modela series de calentamiento**. `WorkoutSetEntity.setNumber` empieza en 1
  y todas las series son de trabajo. Por tanto "primera serie" no es ambiguo.
- `core/design/SegmentedSelector.kt` existe y es el componente a reutilizar.
- Codigo de produccion en `app/src/main/kotlin/`. Tests en `app/src/test/java/`.

## Correcciones a las specs superseded

Se documentan porque Codex debe saber que no son olvidos.

**C1 — La migracion de RIR es v6 -> v7, no v5 -> v6.** La spec 2b se escribio con la DB en
v5. Ya estamos en v6.

**C2 — El RIR se pregunta sobre la PRIMERA serie, no sobre "el ejercicio".** Sigue siendo
un campo por ejercicio y un toque: la friccion no cambia. Cambia la semantica. "El RIR del
ejercicio" no identifica ninguna medicion concreta, y si el motor sube la carga de las
series 2 y 3, un agregado le devuelve su propia correccion: bucle de realimentacion.
La primera serie es la unica medicion sin fatiga acumulada y a carga prescrita.
Efecto lateral positivo: la metrica de calidad de esfuerzo pasa a ser comparable entre
ejercicios, cosa que con un agregado subjetivo no era.

**C3 — La confianza del e1RM se calcula sobre `reps + rir`, no sobre `reps`.** El error de
Epley depende de la proximidad al fallo, no del numero de repeticiones. Un 8 @RIR5 (13
efectivas) es mal predictor; un 8 @RIR0 (8 efectivas) es bueno. La tabla original no podia
distinguirlos porque cuando se escribio el RIR no existia.

**C4 — Se invierte el orden 2a/2b y se fusionan.** Al depender la confianza del RIR, el
e1RM ya no puede ir primero.

**C5 — Se elimina la linea "Fuera de alcance: RIR por serie" de la spec 2b.** Deja de
aplicar: se sigue sin capturar RIR por serie.

---

## Modelo conceptual

**Exposicion** = una sesion en la que se entreno el ejercicio PRIMARY. La unidad del motor
es la exposicion, no la semana: el ejercicio puede entrenarse 1-2 veces por semana.

**Probe set** = la primera serie de trabajo de esa exposicion, ejecutada **siempre** a la
carga prescrita. Es la unica serie sobre la que se mide esfuerzo y la unica que alimenta la
decision de la siguiente exposicion.

**Carriles de carga** = el motor mantiene **dos cargas independientes** por ejercicio, una
para exposiciones VOLUME y otra para STRENGTH. No se derivan la una de la otra.

### El escalar central: `surplus`

Todo el motor gira alrededor de un unico numero.

```
effectiveReps = reps + rir
repMid        = floor((repMin + repMax) / 2)
baseline      = repMid + targetRir
surplus       = effectiveReps - baseline
```

Clasificacion:

| surplus | outcomeClass |
|---|---|
| >= +3 | `EASY` |
| == +2 | `STRONG` |
| +1 o 0 | `ON_TARGET` |
| == -1 | `HARD` |
| <= -2 | `FAILED` |

La doble progresion sale gratis de esto: mantener carga hasta que `surplus` llegue a +2 ES
doble progresion. No se implementa logica aparte.

---

## Requisitos

### R1 — RIR de la primera serie, persistido por ejercicio de sesion

Nueva columna `firstSetRir: Int?` en `WorkoutExerciseEntity`. Migracion **aditiva v6 -> v7**
(`ALTER TABLE ADD COLUMN`), sin recrear tablas, sin backfill. Todo el historico queda `NULL`
y eso es correcto: `NULL` significa "no reportado" y es un estado de primera clase.

Rango valido `0..10`. Fuera de rango se rechaza.

El nombre de la columna lleva `firstSet` deliberadamente: obliga a que cualquier lector
futuro sepa de que serie habla.

### R2 — Es opcional y no bloquea nada

Terminar un ejercicio o una sesion **nunca** requiere RIR. Sin rellenar, todo funciona como
hoy. Ninguna validacion nueva puede impedir cerrar un entrenamiento.

No negociable: registrar peso y repeticiones es la funcion central de la app y no puede
degradarse por una metrica secundaria.

### R3 — Entrada de un solo toque, con la pregunta correcta

Selector en la pantalla de entrenamiento, reutilizando `core/design/SegmentedSelector.kt`.
Opciones: `0`, `1`, `2`, `3`, `4+`. `4+` se persiste como `4`.

El texto **debe** dejar claro que se pregunta por la primera serie. No vale "RIR". Algo del
estilo "Repeticiones que te sobraron en la primera serie". La app informa, no juzga.

La zona 1-3 se senala como util. El `0` se etiqueta como fallo y **no** recibe tratamiento
de logro: ni medalla, ni color de exito, ni felicitacion.

Editable y borrable (volver a "no reportado") mientras la sesion este abierta. Si ya existe
via de edicion en historial se reutiliza; si no existe, **no se abre una nueva**.

### R4 — e1RM en dominio, con confianza sobre reps efectivas

Extraer la formula a `domain/model/progression/StrengthEstimate.kt` con tests propios.
Hoy es una extension privada dentro de un use case, sin cobertura directa.

```
e1rm = load * (1 + (reps + rir) / 30)
```

Se mantiene **Epley**. No se cambia de formula: cambiarla reescribiria retroactivamente la
curva historica del usuario, y eso es peor que el error conocido de la formula. Anotar el
motivo en el propio codigo.

| effectiveReps (`reps + rir`) | Confianza | Peso |
|---|---|---|
| <= 6 | `HIGH` | 1.0 |
| 7-10 | `MEDIUM` | 0.6 |
| > 10 | **no se estima** | — |

Sin `rir` reportado no hay `effectiveReps`, por tanto **no hay e1RM**. Esto es un cambio
respecto al comportamiento actual y es intencionado.

### R5 — Ausencia de estimacion es `null`, nunca `0.0`

`0.0` es un valor legitimo en un grafico y se pinta como punto en el eje. "No estimable" es
ausencia de dato.

`ExerciseProgressEntry.estimatedOneRepMaxKg` y `ExerciseSetRecord.estimatedOneRepMaxKg`
pasan a `Double?`. Eliminar el centinela `0.0`.

### R6 — La serie de 1RM omite los puntos no estimables, sin tocar las demas metricas

- `MaxWeight`, `Volume` y `Reps` siguen pintando **todos** los puntos, igual que hoy.
- `EstimatedOneRepMax` pinta **solo** los estimables.
- La condicion "hay grafico" (`>= 2 puntos`) se evalua sobre la serie **ya filtrada**.
- `bestEstimatedOneRepMax` solo considera series estimables; puede quedar `null`.

Si el usuario entrena siempre a 15 repeticiones, el chip de 1RM muestra un vacio con
sentido: ni grafico plano en cero, ni crash.

### R7 — La confianza es visible

Etiqueta de confianza en `ProgressPointDetails` y en la tarjeta del record de 1RM.
"alta" / "media". No inventar escala numerica.

### R8 — Metrica de calidad de esfuerzo

Nueva metrica en stats: **porcentaje de ejercicios con RIR reportado que caen en zona 1-3**.

- Denominador: **solo** los ejercicios con RIR reportado. Los `NULL` no cuentan ni a favor
  ni en contra.
- Si no hay ningun RIR reportado en el periodo, la metrica **no se muestra**. No mostrar
  "0%", que se leeria como "lo haces todo mal" cuando lo que pasa es que no hay datos.
- Indicar sobre cuantos ejercicios se calcula: un 100% sobre dos no es un 100% sobre
  cincuenta.
- Respetar los filtros `WorkoutStatsPeriod` existentes.

### R9 — Reparto por zonas

Ademas del porcentaje, el reparto entre **al fallo (0)**, **zona util (1-3)** y **lejos del
fallo (4+)**. Un 40% de zona util puede venir de entrenar blando o de machacarse: problemas
opuestos con soluciones opuestas. Ninguna zona se presenta como buena o mala en si misma.

### R10 — Rol de progresion por ejercicio

Nueva columna `progressionRole: String` en `RoutineExerciseEntity`, valores
`PRIMARY` / `SECONDARY` / `ACCESSORY`, **default `ACCESSORY`**. Migracion v7 -> v8.

En V1 solo `PRIMARY` cambia de comportamiento. `SECONDARY` y `ACCESSORY` son etiquetas que
por ahora se comportan identico (siguen con `GetProgressionHintUseCase`).

**Enmendado el 2026-09-18 por decision del dueño: el numero de PRIMARY es una recomendacion,
no un limite.** Marcar un cuarto no se rechaza. El motivo del consejo sigue en pie —PRIMARY
implica gestion de fatiga y en V1 no hay fatiga cruzada, asi que con muchos primarios el
modelo es menos fiable— pero eso se dice en el texto de guia en vez de bloquear.
`MAX_PRIMARY_EXERCISES` pasa de tope a numero recomendado. Bloquear al dueño de su propia
rutina por una heuristica de producto (`P-013`, documentada como arbitraria) era paternalismo.

Un duplicado de ejercicio o de dia **nunca hereda PRIMARY**: aterriza en SECONDARY y se le
limpian objetivo e incremento. Copiar la estructura no es elegir un primario.

**Alternativas (variantes del mismo movimiento).** Cada variante mantiene **su propio
perfil**: dos maquinas del mismo ejercicio son el mismo patron con distinta resistencia, y
mezclarlas en una sola serie meteria un diente de sierra que el detector de tendencia leeria
como cambio real. Ademas, tener el e1RM por maquina es util en si mismo: permite ver si vas
parecido en las dos.

Lo que un perfil separado NO debe hacer es empezar de cero cada vez que la maquina habitual
esta ocupada. Una variante sin perfil se siembra en este orden:

1. Su propio historial. Nada gana a datos reales en esa maquina.
2. La carga de trabajo de una variante hermana. Mismo movimiento, barrio correcto.
3. El historial de una hermana.
4. Si no se sabe nada, no hay prescripcion. Es un estado normal, no un fallo.

**La semilla es una carga de partida, no un trasplante**: la variante nueva entra en
CALIBRATING y calibra sola. No hereda EWMA ni contador de puntos de su hermana.

### R11 — Perfil de progresion persistido

Nueva tabla `exercise_progression_profiles`, clave `variantKey` (coherente con como stats
agrupa hoy).

```
variantKey: String            PK
goalWeightKg: Double?
loadIncrementKg: Double       default 2.5
state: String                 CALIBRATING | PROGRESSING | RECOVERING | EVALUATING
loadVolumeKg: Double?
loadStrengthKg: Double?
pendingConfirmVolume: Boolean
pendingConfirmStrength: Boolean
nextExposureType: String      VOLUME | STRENGTH
hardExposureStreak: Int
exposuresSinceRecovery: Int
consecutiveFailVolume: Int
consecutiveFailStrength: Int
previousLoadVolumeKg: Double?
previousLoadStrengthKg: Double?
stallCount: Int
ewmaStrengthE1rm: Double?
ewmaVolumeE1rm: Double?
strengthPointCount: Int
volumePointCount: Int
recoveryExposuresRemaining: Int
evaluationFailStreak: Int
calibrationRetries: Int
```

`CONSOLIDATING` **no es un estado persistido**. Es un estado derivado para la UI:
`pendingConfirmVolume || pendingConfirmStrength`. Modelarlo como estado real duplicaria la
logica de prescripcion sin ganar nada.

### R12 — Exposicion persistida, prescrito contra realizado

Nueva tabla `progression_exposures`. Es un **snapshot inmutable**, coherente con el
invariante de historial de la app: una vez escrita no se recalcula nunca.

```
id: Long                      PK autogenerado
variantKey: String            indexado
workoutExerciseId: Long?      FK, ON DELETE SET NULL
exposureIndex: Int
performedAt: Long
type: String                  VOLUME | STRENGTH | RECOVERY | EVALUATION
prescribedLoadKg: Double
prescribedRepMin: Int
prescribedRepMax: Int
prescribedTargetRir: Int
prescribedSets: Int
probeReps: Int?
probeRir: Int?
probeSurplus: Int?
outcomeClass: String?
setCompletionRatio: Double?
exposureE1rm: Double?
e1rmConfidence: String?
excludeFromTrend: Boolean
userFlaggedBadDay: Boolean
intraSessionAdjustmentSteps: Int
decisionReason: String
```

Sin esto no hay motor: saber que hizo 5 repeticiones no informa. Saber que **le pedimos
5 @RIR2 y consiguio 5 @RIR1** si.

### R13 — Dos series de e1RM, nunca mezcladas

El motor mantiene **dos EWMA separadas**, una por tipo de exposicion.

```
ewma_n = ewma_{n-1} + EWMA_ALPHA * w_n * (e1rm_n - ewma_{n-1})
```

Motivo, y es un bug real si se ignora: Epley sobrestima progresivamente con mas
repeticiones, asi que las exposiciones VOLUME leen sistematicamente mas alto que las
STRENGTH. Mezcladas en una sola serie generan un diente de sierra artificial que dispara
recuperaciones fantasma.

**El e1RM que se muestra al usuario es `ewmaStrengthE1rm`.** Es el honesto.

Tendencia (minimo `TREND_MIN_POINTS` puntos en el carril):

```
delta% = (ewma_n - ewma_{n-TREND_LOOKBACK}) / ewma_{n-TREND_LOOKBACK} * 100
delta% >= TREND_RISING_PCT   -> RISING
delta% <= TREND_FALLING_PCT  -> FALLING
else                         -> FLAT
```

`trend` = tendencia del carril STRENGTH si tiene puntos suficientes; si no, la del carril
VOLUME; si no, `UNKNOWN`.

Las bandas son **asimetricas a proposito**: declarar un descenso exige mas evidencia que
declarar una subida.

### R14 — Clasificacion de la exposicion, con guardas

Sobre el probe set, aplicando en orden:

1. Calcular `surplus` y mapear a `outcomeClass` por la tabla del modelo conceptual.
2. Si `probeReps < prescribedRepMin` -> la clase no puede ser mejor que `HARD`.
3. Si `probeReps < prescribedRepMin - 1` -> `FAILED` forzado.
4. `setCompletionRatio` = series con `reps >= prescribedRepMin` / `prescribedSets`.
   Si `< SET_COMPLETION_RATIO_MIN` -> bajar una clase.
5. Si `probeRir == null` -> bajar una clase y marcar `excludeFromTrend = true`.

El paso 4 usa solo repeticiones, no RIR. Es la red de seguridad que detecta un derrumbe
intra-ejercicio sin necesitar RIR por serie.

### R15 — Decision de carga para la siguiente exposicion

Se lee la ultima exposicion **del mismo tipo** y se opera sobre **ese carril**.
`step = loadIncrementKg`.

| outcomeClass previo | pendingConfirm | Accion sobre la carga | Efectos |
|---|---|---|---|
| `EASY` | — | `+ STEP_EASY * step`, cap `MAX_SINGLE_JUMP_PCT` | guarda carga previa, `pendingConfirm = true` |
| `EASY` (2a seguida) | — | `+ STEP_STRONG * step` | `pendingConfirm = true` |
| `STRONG` | — | `+ STEP_STRONG * step` | guarda carga previa, `pendingConfirm = true` |
| `ON_TARGET` | — | sin cambio | `pendingConfirm = false`, `consecutiveFail = 0` |
| `HARD` | `false` | sin cambio | `consecutiveFail += 1` |
| `HARD` | `true` | sin cambio | mantiene `pendingConfirm` |
| `FAILED` | `false`, `consecutiveFail == 0` | **sin cambio** | `consecutiveFail = 1` |
| `FAILED` | `false`, `consecutiveFail >= 1` | `+ STEP_REDUCE * step` | `consecutiveFail = 0`, `stallCount += 1` |
| `FAILED` | **`true`** | **revierte a `previousLoad`** | `stallCount += 1`, `pendingConfirm = false` |

Las dos ultimas filas son la regla mas importante del motor y hay que entender la
asimetria:

> En una carga **ya confirmada** (produjo `ON_TARGET` o mejor alguna vez), un `FAILED` no
> baja nada: hacen falta dos. Un mal dia no significa nada.
> En una carga **recien subida y sin confirmar**, un solo `FAILED` revierte, porque sabemos
> que la anterior funcionaba y no tenemos ninguna evidencia de que la nueva sirva.

`nextExposureType` alterna `VOLUME <-> STRENGTH` salvo que el estado lo sobreescriba.

### R16 — Nivel de especificidad hacia el objetivo

Solo si hay `goalWeightKg`. `r = ewmaStrengthE1rm / goalWeightKg`. Sin objetivo -> `BASE`.

| Nivel | Condicion | VOLUME | STRENGTH |
|---|---|---|---|
| `BASE` | `r < 0.85` | 7-10 @RIR2 | 4-6 @RIR2 |
| `SPECIFIC` | `0.85 <= r < 0.95` | 6-8 @RIR2 | 3-5 @RIR1 |
| `PEAKING` | `r >= 0.95` | 6-8 @RIR2 | 2-4 @RIR1 |

Saber hacer 10 repeticiones no es lo mismo que saber ejecutar una carga muy alta. Si el
objetivo es mover 100 kg de verdad, el ultimo tramo tiene que acercarse al objetivo real.

### R17 — Ajuste intra-sesion por la probe

La primera serie se ejecuta **siempre a la carga prescrita**. Tras completarla se calcula
`probeSurplus` y se ajustan las series restantes **una sola vez**:

| probeSurplus | Series restantes |
|---|---|
| `>= +3` | `L + 2 * step` |
| `== +2` | `L + 1 * step` |
| `+1` o `0` | `L` |
| `== -1` | `L` |
| `<= -2` | `L - 1 * step` |

Cap absoluto `INTRA_MAX_STEPS` respecto a lo prescrito. Registrar en
`intraSessionAdjustmentSteps`.

**Regla anti-doble-conteo, critica:** la decision de la *siguiente* exposicion usa
**exclusivamente** `probeSurplus`, nunca las series ajustadas. Contabilizar ambos hace que
el motor se autoamplifique y oscile.

Desactivado en `CALIBRATING`, `RECOVERING` y `EVALUATING`.

La sugerencia se **propone**, no se impone: el usuario puede ignorarla y registrar el peso
que quiera. Lo que se registre es lo que se guarda.

### R18 — Maquina de estados

Cuatro estados persistidos.

```
CALIBRATING --3 exposiciones validas--> PROGRESSING
PROGRESSING --trigger--> RECOVERING --agotadas--> EVALUATING
EVALUATING --1 exposicion--> PROGRESSING | CALIBRATING
```

#### CALIBRATING

Entrada: se marca PRIMARY sin historial valido, o desde `EVALUATING` fallido dos veces.

Semilla `L0`: serie mas pesada de los ultimos 60 dias del `variantKey`. Si no hay, se pide
al usuario una carga que pueda mover unas 8 repeticiones.

```
loadFor(e1rm, reps, rir) = floorToIncrement(e1rm / (1 + (reps + rir) / 30), step)
```

Secuencia fija de `CALIBRATION_EXPOSURES`:

| # | Tipo | Prescripcion |
|---|---|---|
| 1 | VOLUME | 3 x 7 @RIR2 a `L0` |
| 2 | STRENGTH | 3 x 5 @RIR2 a `loadFor(e1rm_1, 5, 2)` |
| 3 | VOLUME | 3 x 7 @RIR2 a `loadFor(ewmaVolume, 7, 2)` |

Se prescribe **por el suelo del rango** (7, no 8) a proposito: `7 + 2 = 9` queda con margen
bajo el corte de 10 reps efectivas, para que la calibracion no se quede sin estimacion.

Sin ajuste intra-sesion. Sin subidas de carga.

Si una exposicion sale `FAILED`: se descarta, se repite ese tipo a
`load * CALIBRATION_FAIL_FACTOR`, `calibrationRetries += 1`. Al superar
`CALIBRATION_MAX_RETRIES` se continua con lo que haya.

Salida: 3 exposiciones validas, ambos tipos muestreados, y al menos un e1RM estimable.
Se fijan `loadVolumeKg` y `loadStrengthKg` desde las EWMA. -> `PROGRESSING`.

#### PROGRESSING

Alternancia normal, R15, R16 y R17. `hardExposureStreak += 1` y
`exposuresSinceRecovery += 1` en cada exposicion.

Al cerrar cada exposicion se evaluan los triggers **en este orden**:

```
R0 (cooldown) exposuresSinceRecovery < RECOVERY_COOLDOWN_EXPOSURES
              -> ningun trigger puede disparar. Se sale.
T1 hardExposureStreak >= R1_HARD_STREAK_CAP              -> RECOVERING, 2 exposiciones
T2 trend == FALLING
   AND (>= R2_BAD_REQUIRED de las ultimas R2_BAD_IN_LAST_N en {HARD, FAILED})
                                                          -> RECOVERING, 2 exposiciones
T3 media(probeSurplus ultimas 3) <= R3_MEAN_SURPLUS_MAX
   AND trend != RISING                                    -> RECOVERING, 1 exposicion
T4 stallCount >= R4_STALL_COUNT en las ultimas R4_STALL_WINDOW
                                                          -> RECOVERING, 1 exposicion
T5 hardExposureStreak >= R5_HARD_STREAK                   -> RECOVERING, 1 exposicion
```

Las exposiciones con `userFlaggedBadDay == true` **no cuentan** para T2 ni T3, ni para la
tendencia, ni para `consecutiveFail`. Se guardan, no se juzgan.

Si `trend == UNKNOWN`, T2 no puede disparar pero T1/T3/T4/T5 si. No hay bloqueo posible.

#### RECOVERING

```
load       = roundToIncrement(cargaDelCarril * RECOVERY_LOAD_FACTOR)
sets       = max(RECOVERY_MIN_SETS, ceil(setsNormales * RECOVERY_SETS_FACTOR))
reps       = prescribedRepMin
targetRir  = RECOVERY_TARGET_RIR
```

Prohibido el ajuste intra-sesion. Prohibido subir carga. No se actualizan carriles ni
`consecutiveFail`. Se registra e1RM pero con `excludeFromTrend = true`.

No es un calendario de descargas: no existe "cada X semanas". Se entra por senal.
Al agotar `recoveryExposuresRemaining` -> `EVALUATING`.

#### EVALUATING

Exactamente 1 exposicion. `type = EVALUATION`, prescripcion de STRENGTH,
`load = loadStrengthKg` (la de antes del recovery), `EVALUATION_SETS` series,
`targetRir = EVALUATION_TARGET_RIR`. Sin ajuste intra-sesion.

| probeSurplus | Accion |
|---|---|
| `>= EVALUATION_PASS_SURPLUS` | -> `PROGRESSING`, carriles intactos, `hardExposureStreak = 0`, `stallCount = 0`, `evaluationFailStreak = 0`, `exposuresSinceRecovery = 0` |
| `< 0` y `evaluationFailStreak == 0` | -> `PROGRESSING`, ambos carriles `EVALUATION_DEMOTE_PCT`, streaks a 0, `evaluationFailStreak = 1` |
| `< 0` y `evaluationFailStreak >= 1` | -> `CALIBRATING` con `L0 = loadStrengthKg * EVALUATION_RECALIBRATE_FACTOR` |

La ultima fila es la salida de emergencia: si tras dos ciclos completos de recuperacion el
modelo sigue equivocandose con el usuario, el modelo esta mal. No se insiste, se recalibra.

### R19 — Toda recomendacion es explicable

`ProgressionPrescription` incluye `decisionReason: String`, y la UI **debe** mostrarlo.
Nunca un peso a secas.

Ejemplos del tono:

- "72,5 kg x 5 — la ultima sesion completaste 70 kg x 5 con margen y tu tendencia de fuerza
  sigue subiendo."
- "Manten 70 kg — la ultima sesion fue mas exigente de lo esperado. Una sesion aislada no
  indica perdida de fuerza, asi que mantenemos la carga."
- "Sesion de recuperacion — tus ultimas exposiciones muestran mas esfuerzo para un
  rendimiento similar. Hoy bajamos el volumen antes de volver a progresar."

Cuando la carga no sube pero el esfuerzo baja al mismo peso, el motor lo reconoce como
progreso: "Carga consolidandose. Mismo peso con menos esfuerzo." Mostrar constantemente
"no has subido peso" es falso y desmotiva.

### R20 — El motor es una funcion pura

```kotlin
fun calculateNextPrescription(
    profile: ExerciseProgressionProfile,
    recentExposures: List<ProgressionExposure>
): ProgressionPrescription
```

Sin acceso a Room, sin corrutinas, sin `Context`, sin reloj del sistema (la fecha se pasa
como parametro). Toda la logica de R13-R18 vive aqui y se prueba con JUnit puro. El use
case que la envuelve solo hace de fontaneria.

Esto no es preferencia estetica: sin esto los umbrales de R21 no son testeables y el motor
se vuelve imposible de recalibrar.

### R21 — Umbrales en un unico objeto

Todos los numeros del motor viven en
`domain/model/progression/ProgressionTuning.kt`. **Ninguna constante magica dispersa por el
codigo.** En tres meses se recalibran aqui, no en quince ficheros.

```kotlin
object ProgressionTuning {
    // e1RM y confianza (R4)
    const val CONF_HIGH_MAX_EFFECTIVE_REPS   = 6      // <= 6  -> HIGH,   w = 1.0
    const val CONF_MEDIUM_MAX_EFFECTIVE_REPS = 10     // <= 10 -> MEDIUM, w = 0.6
                                                      // >  10 -> no estimable
    const val WEIGHT_HIGH   = 1.0
    const val WEIGHT_MEDIUM = 0.6

    // Tendencia (R13)
    const val EWMA_ALPHA        = 0.3
    const val TREND_MIN_POINTS  = 4
    const val TREND_LOOKBACK    = 3
    const val TREND_RISING_PCT  =  1.5
    const val TREND_FALLING_PCT = -2.5

    // Clasificacion (R14)
    const val SURPLUS_EASY_MIN         =  3
    const val SURPLUS_STRONG           =  2
    const val SURPLUS_ON_TARGET_MIN    =  0
    const val SURPLUS_HARD             = -1
    const val SET_COMPLETION_RATIO_MIN = 0.67

    // Carga (R15)
    const val STEP_EASY                  =  2
    const val STEP_STRONG                =  1
    const val STEP_REDUCE                = -1
    const val MAX_SINGLE_JUMP_PCT        = 7.5
    const val FAILS_TO_REDUCE            = 2
    const val FAILS_TO_REVERT            = 1
    const val DEFAULT_INCREMENT_UPPER_KG = 2.5
    const val DEFAULT_INCREMENT_LOWER_KG = 5.0

    // Ajuste intra-sesion (R17)
    const val INTRA_MAX_STEPS = 2

    // Disparadores de RECOVERING (R18)
    const val RECOVERY_COOLDOWN_EXPOSURES = 4
    const val R1_HARD_STREAK_CAP          = 10
    const val R2_BAD_IN_LAST_N            = 3
    const val R2_BAD_REQUIRED             = 2
    const val R3_MEAN_SURPLUS_MAX         = -1.0
    const val R4_STALL_COUNT              = 2
    const val R4_STALL_WINDOW             = 6
    const val R5_HARD_STREAK              = 8

    // Prescripcion de RECOVERING (R18)
    const val RECOVERY_LOAD_FACTOR = 0.90
    const val RECOVERY_SETS_FACTOR = 0.6
    const val RECOVERY_MIN_SETS    = 2
    const val RECOVERY_TARGET_RIR  = 4

    // EVALUATING (R18)
    const val EVALUATION_SETS               = 2
    const val EVALUATION_TARGET_RIR         = 2
    const val EVALUATION_PASS_SURPLUS       = 0
    const val EVALUATION_DEMOTE_PCT         = -5.0
    const val EVALUATION_RECALIBRATE_FACTOR = 0.90

    // CALIBRATING (R18)
    const val CALIBRATION_EXPOSURES   = 3
    const val CALIBRATION_MAX_RETRIES = 2
    const val CALIBRATION_FAIL_FACTOR = 0.90

    // Especificidad (R16)
    const val SPECIFIC_TIER_RATIO = 0.85
    const val PEAKING_TIER_RATIO  = 0.95

    // Limite de producto (R10)
    const val MAX_PRIMARY_EXERCISES = 3
}
```

### R22 — El invariante de snapshot no se rompe

- `progression_exposures` es historial: se escribe una vez y no se recalcula jamas.
- El perfil se recalcula; las exposiciones no.
- Las sesiones abiertas **no** generan exposicion. Solo al finalizar la sesion.
- Si se borra una sesion, su exposicion queda huerfana con `workoutExerciseId = NULL` y
  **sigue contando** para la tendencia: ocurrio.

---

## Fuera de alcance de V1

Explicitamente aparcado. No implementar aunque parezca facil:

- Motores SECONDARY y ACCESSORY. Siguen con `GetProgressionHintUseCase` **intacto**.
- Fatiga cruzada entre ejercicios, `movementPattern`, `fatigueRelationToBench`.
- Propagacion de la recuperacion a ejercicios relacionados.
- RIR por serie.
- Predicciones temporales tipo "alcanzaras 100 kg en 7 semanas". Demasiadas variables.
- Importacion de rutinas por JSON de IA. **Primero el motor determinista, despues la IA.**
- RPE, velocidad de barra, dolor, molestias articulares.
- Cambiar de formula de e1RM o promediar varias.

---

## Evidencia cientifica contra heuristica de producto

Se documenta porque protege al proyecto de defender como ciencia lo que es una decision.

### Respaldado por evidencia razonable

| Regla | Base |
|---|---|
| Autorregulacion por RIR frente a %1RM fijo | escalas RIR/RPE con validez aceptable (Zourdos, Helms) |
| e1RM se degrada con reps altas; fiable por debajo de ~10 | limitacion documentada de Epley/Brzycki |
| Mejor precision del RIR cerca del fallo | la estimacion mejora con proximidad al fallo |
| Entrenar cerca del fallo ~ al fallo, con menos fatiga | meta-analisis (Grgic, Refalo); 1-3 RIR iguala al fallo en hipertrofia |
| La app no premia el fallo | consecuencia directa de lo anterior |
| Especificidad para mover cargas altas | principio establecido en literatura de fuerza |
| Incrementos de 2.5-5% | practica estandar, no ley |

### Heuristica de producto — ajustable, no ciencia

| ID | Regla | Estado |
|---|---|---|
| `P-001` | Alternar VOLUME/STRENGTH (inspirado en DUP) | **DUP no esta demostrado superior a la periodizacion lineal.** Los meta-analisis salen equivocos. Se elige por calidad de senal, no por evidencia |
| `P-002` | El escalar `surplus` y sus bandas | invencion propia |
| `P-003` | Dos carriles de carga independientes | decision de modelado |
| `P-004` | `EWMA_ALPHA = 0.3` | arbitrario |
| `P-005` | Bandas de tendencia +1.5 / -2.5 | arbitrario, asimetrico a proposito |
| `P-006` | Recovery a 8 exposiciones, tope 10 | arbitrario |
| `P-007` | `RECOVERY_LOAD_FACTOR = 0.90` | arbitrario |
| `P-008` | Cooldown de 4 exposiciones | arbitrario |
| `P-009` | Tiers 0.85 / 0.95 hacia el objetivo | arbitrario |
| `P-010` | Cortes de confianza 6 / 10 | aproximacion razonada, no calibrada |
| `P-011` | Descargas autorreguladas en vez de calendario fijo | evidencia **debil en ambos sentidos** |
| `P-012` | Probe = primera serie | decision de diseno para evitar realimentacion |
| `P-013` | Maximo 3 PRIMARY | arbitrario, por ausencia de fatiga cruzada en V1 |
| `P-020` | No reaccionar a una sola exposicion mala | gestion de ruido; heuristica robusta |

**Ninguno de los numeros de `ProgressionTuning` esta demostrado.** Son un punto de partida
que se recalibra con los datos reales del usuario. Por eso viven juntos en un solo objeto.

Aviso honesto: esto se valida con n=1. Eso esta perfectamente bien para este proyecto, pero
significa que los umbrales se **ajustan**, no se "demuestran".

---

## Criterios de aceptacion

**Base**
1. `test` y `build` en verde.
2. `app/schemas/.../7.json` y `8.json` generados y commiteados.
3. Actualizar desde una instalacion con datos reales no pierde nada. Todo el historico
   queda con `firstSetRir == NULL` y `progressionRole == ACCESSORY`.
4. Se puede terminar un entrenamiento completo sin tocar el RIR ni una vez.

**RIR y esfuerzo**
5. El texto del selector menciona explicitamente la primera serie.
6. En ningun sitio de la UI el RIR 0 recibe tratamiento de logro.
7. Con cero RIR reportados en el periodo, la metrica de calidad de esfuerzo no aparece.
8. Un ejercicio con RIR 2 cuenta en zona util; con 0 como fallo; con 5 en 4+.

**e1RM**
9. `100 kg x 3 @RIR2` (5 efectivas) -> e1RM confianza alta.
   `100 x 8 @RIR2` (10 efectivas) -> confianza media.
   `100 x 8 @RIR5` (13 efectivas) -> **no produce e1RM**.
   `100 x 8` sin RIR -> **no produce e1RM**.
10. Un ejercicio con solo series no estimables no aparece en la grafica de 1RM, pero **si**
    en peso, volumen y repeticiones.
11. Seleccionar el chip de 1RM sin datos estimables no rompe la pantalla ni pinta ceros.
12. `bestEstimatedOneRepMax` es `null` cuando no hay ninguna serie estimable.

**Motor — casos que deben tener test**
13. STRENGTH 4-6 @RIR2 (baseline 7): `70x5@RIR4` -> `STRONG`; `70x5@RIR2` -> `ON_TARGET`;
    `70x5@RIR0` -> `FAILED`; `70x4@RIR0` -> `FAILED`.
14. Un unico `FAILED` en carga confirmada **no baja la carga**.
15. Dos `FAILED` consecutivos del mismo tipo en carga confirmada bajan un `step`.
16. Un unico `FAILED` en carga con `pendingConfirm == true` **revierte a `previousLoad`**.
17. `probeSurplus <= -2` baja las series restantes un `step`, y la decision de la siguiente
    exposicion sigue usando `probeSurplus`, **no** la carga ajustada.
18. Con `exposuresSinceRecovery < 4` ningun trigger de recuperacion dispara.
19. `trend == UNKNOWN` no bloquea T1/T3/T4/T5.
20. Una exposicion con `userFlaggedBadDay` no cuenta para tendencia, T2, T3 ni
    `consecutiveFail`.
21. Dos `EVALUATING` fallidos consecutivos llevan a `CALIBRATING`.
22. Las EWMA de VOLUME y STRENGTH nunca se mezclan en la misma serie.
23. El e1RM mostrado al usuario procede de `ewmaStrengthE1rm`.
24. Marcar un cuarto PRIMARY NO se rechaza; un duplicado de ejercicio o de dia nunca hereda el
    rol; y una variante alternativa sin perfil se siembra desde una hermana sin heredar su EWMA.
25. `calculateNextPrescription` es invocable desde un test JUnit puro, sin Room ni Android.
26. Toda `ProgressionPrescription` trae `decisionReason` no vacio.

**Regresion**
27. `GetProgressionHintUseCase` y `GetProgressionHintUseCaseTest` siguen existiendo y
    pasando, sin cambios de comportamiento para ejercicios no PRIMARY.

---

## Aviso obligatorio al dueño al terminar

Esta fase **cambia lo que el usuario ve** y es intencionado:

- Puntos que hoy aparecen en la grafica de 1RM van a desaparecer (los que no tienen RIR o
  superan 10 reps efectivas).
- El record de 1RM puede bajar o vaciarse.

No es una regresion: es la correccion de un dato que estaba mal. Debe anunciarse para que
no se confunda con un bug durante la pasada manual.
