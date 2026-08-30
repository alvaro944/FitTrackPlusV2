# Plan: e1RM acotado por repeticiones y nivel de confianza

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `develop`** (no desde `main`). Commits separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica local (`test` + `build`) antes de cada commit; NO lances el emulador (eso lo hace el dueño). No commits de WIP.

**Rama:** `feature/estimated-1rm-confidence` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-07-30-estimated-1rm-confidence.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

**PREREQUISITO:** `feature/structured-target-reps` tiene que estar mergeada a `develop`. Si `git log develop --oneline | rg "structured target reps"` no devuelve nada, **para y avisa** en vez de partir de una base incompleta.

**Aviso:** esta fase **si cambia comportamiento visible** (puntos que desaparecen de la grafica de 1RM, record de 1RM que puede bajar o vaciarse). Es intencionado. Dilo explicitamente al avisar al dueño.

**Sin migracion de base de datos.** Si acabas tocando `app/schemas/`, algo va mal.

---

## Tarea 0: Preparar rama

- [ ] Verificar el prerequisito de arriba
- [ ] `git checkout develop && git pull` si aplica
- [ ] `git checkout -b feature/estimated-1rm-confidence`

---

## Tarea 1: Calculo de e1RM en dominio, con confianza y tests

**Archivos:**
- Nuevo: `domain/model/OneRepMaxEstimate.kt`
- Test nuevo: `app/src/test/.../domain/model/OneRepMaxEstimateTest.kt`

- [ ] Crear un modelo con el valor y la confianza, p.ej.:
  - `enum class OneRepMaxConfidence { HIGH, MEDIUM }`
  - `data class OneRepMaxEstimate(val valueKg: Double, val confidence: OneRepMaxConfidence)`
  - `companion object { fun from(weightKg: Double, reps: Int): OneRepMaxEstimate? }`
- [ ] Reglas: `weightKg <= 0.0` o `reps <= 0` → `null`. `reps > MAX_ESTIMABLE_REPS (10)` → `null`. `reps in 1..5` → `HIGH`. `reps in 6..10` → `MEDIUM`
- [ ] Formula: Epley, `weightKg * (1.0 + reps / 30.0)`. Caso `reps == 1` devuelve `weightKg` exacto
- [ ] **Comentario en el codigo** (en ingles) explicando por que se mantiene Epley y no se cambia a Brzycki: cambiar de formula reescribiria retroactivamente el historial del usuario
- [ ] Tests: 100x1 → 100.0 HIGH; 100x3 HIGH; 100x5 HIGH; 100x6 MEDIUM; 100x10 MEDIUM; 100x11 → null; 100x20 → null; 0x5 → null; 100x0 → null; peso negativo → null
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add domain one rep max estimate with confidence levels`

---

## Tarea 2: Propagar nullabilidad en el pipeline de stats

**Archivos:**
- `domain/model/StatsModels.kt`
- `domain/usecase/ObserveWorkoutStatsUseCase.kt`
- Tests en `app/src/test/.../domain/usecase/`

- [ ] `ExerciseProgressEntry.estimatedOneRepMaxKg` y `ExerciseSetRecord.estimatedOneRepMaxKg` pasan a `Double?`
- [ ] Añadir la confianza donde aporte: al menos en `ExerciseSetRecord` (para poder etiquetar el record de 1RM en la UI)
- [ ] Sustituir la extension privada `WorkoutSetEntity.estimatedOneRepMaxKg()` (`ObserveWorkoutStatsUseCase.kt:240-246`) por `OneRepMaxEstimate.from(...)`. **Eliminar el centinela `0.0`**
- [ ] En `toProgressEntry` (`:154-165`), `estimatedOneRepMaxKg` = maximo de las estimaciones **no nulas** de las series, o `null` si ninguna es estimable. No usar `?: 0.0`
- [ ] En `toExerciseRecords` (`:186-188`), `bestEstimatedOneRepMax` filtra las series con estimacion no nula antes de coger el maximo
- [ ] **No tocar** `volumeKg`, `maxWeightKg` ni `totalReps`: siguen calculandose sobre todas las series
- [ ] Tests: sesion con series 100x3 y 90x15 → e1RM de la entrada sale de la de 3 reps; sesion con solo 3x20 → entrada con `estimatedOneRepMaxKg == null` pero volumen y peso correctos; ejercicio sin series estimables → `bestEstimatedOneRepMax == null`
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: cap estimated one rep max to reliable rep ranges`

---

## Tarea 3: UI de stats — serie filtrada y confianza visible

**Archivos:**
- `feature/stats/StatsViewModel.kt`
- `feature/stats/StatsScreen.kt`

- [ ] `ExerciseProgressEntryUiState.estimatedOneRepMaxKg` y `ProgressChartPointUiState.estimatedOneRepMaxKg` pasan a `Double?`. Añadir la confianza en el UI state del record de 1RM
- [ ] **Punto clave** — `StatsUiState.progressChartValues` (`StatsViewModel.kt:273-280`): hoy es un `map` sobre todos los puntos. Convertirlo en `mapNotNull` de forma que:
  - `MaxWeight`, `Volume`, `Reps` → siguen incluyendo **todos** los puntos (comportamiento actual intacto)
  - `EstimatedOneRepMax` → **omite** los puntos con e1RM nulo
- [ ] Revisar la condicion de pintado del `LineChart` de progreso (`StatsScreen.kt:844-858`, hoy `progressPoints.size >= 2`): debe evaluarse sobre la **serie ya filtrada** (`progressChartValues.size >= 2`), no sobre el total de puntos. Si no, el chip de 1RM puede intentar pintar con menos de dos valores
- [ ] Estado vacio con sentido cuando la metrica de 1RM no tiene datos suficientes: mensaje corto explicando que hacen falta series de 10 repeticiones o menos. **Reutilizar el componente de estado vacio que ya exista** en la pantalla; no crear uno nuevo
- [ ] `ProgressPointDetails` (`StatsScreen.kt:870+`): si el e1RM del punto es nulo, no mostrar la fila; si no, mostrarla con la etiqueta de confianza ("confianza alta" / "confianza media")
- [ ] Tarjeta de records: etiquetar el record de 1RM con su confianza; si es `null`, no mostrar la fila
- [ ] Strings en español sin tildes, coherentes con el resto de la app
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: show one rep max confidence and skip unestimable points`

---

## Verificacion final antes de avisar al dueño

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] `git diff develop --stat` no muestra cambios en `app/schemas/` ni en `core/database/` ni en `data/local/entity/`. Si los muestra, te saliste del alcance
- [ ] Repasar que las metricas de peso, volumen y reps pintan exactamente los mismos puntos que antes del cambio
- [ ] Push y avisar, **diciendo explicitamente** que la grafica de 1RM va a mostrar menos puntos y que eso es el objetivo de la tarea

**Pendiente de pasada manual (la hace el dueño):** abrir stats, elegir un ejercicio con series de mas de 10 repeticiones, y comprobar que el chip de 1RM se comporta con sentido mientras peso/volumen/reps siguen igual.

**Merge:** a `develop` cuando compile y pasen los tests. A `main` solo tras la pasada manual del dueño.
