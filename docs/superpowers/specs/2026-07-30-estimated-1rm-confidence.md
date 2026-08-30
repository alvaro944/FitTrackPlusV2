# Spec: e1RM acotado por repeticiones y nivel de confianza

**Fecha:** 2026-07-30
**Fase:** 2a del arco de periodizacion (`docs/design/mejoras-claude.md`, entrada 19)
**Plan de ejecucion:** `docs/superpowers/plans/2026-07-30-estimated-1rm-confidence.md`
**Prerequisito:** `feature/structured-target-reps` mergeada a `develop`.

---

## Por que

La app estima el 1RM con Epley y lo muestra como metrica de progreso. El problema es que **lo calcula sobre cualquier serie, sin mirar cuantas repeticiones tiene**.

`ObserveWorkoutStatsUseCase.kt:240-246`:

```kotlin
private fun WorkoutSetEntity.estimatedOneRepMaxKg(): Double {
    return if (weightKg > 0.0 && reps > 0) {
        weightKg * (1.0 + reps / 30.0)
    } else {
        0.0
    }
}
```

La unica condicion es `reps > 0`. Una serie de 20 repeticiones genera un punto en la grafica de 1RM con el mismo peso visual que una de 3.

La investigacion de Fase 0 (`docs/research/training-methods.md`, apartado 1) es concluyente: las ecuaciones de prediccion solo son fiables a repeticiones bajas, **Epley sobrestima de forma significativa a partir de 5 repeticiones**, y por encima de ~10-12 la estimacion es ruido. Un grafico que mezcla ambas cosas no informa: engaña.

Ademas esto es cimiento. El motor de objetivos de fases posteriores decidira cargas a partir del e1RM. Si el numero de partida esta contaminado, todo lo que se construya encima hereda el error.

## Estado actual verificado

- `estimatedOneRepMaxKg()` devuelve `0.0` como centinela cuando no puede calcular. No usa `null`.
- `ExerciseProgressEntry.estimatedOneRepMaxKg: Double` (no nullable), se rellena con `maxOfOrNull { ... } ?: 0.0` en `ObserveWorkoutStatsUseCase.kt:161-163`.
- `ExerciseRecords.bestEstimatedOneRepMax` filtra `weightKg > 0.0 && reps > 0` y coge el maximo (`ObserveWorkoutStatsUseCase.kt:186-188`).
- `ProgressMetric.EstimatedOneRepMax` ya existe como chip (`StatsViewModel.kt:283-288`).
- `StatsUiState.progressChartValues` (`StatsViewModel.kt:273-280`) mapea **todos** los puntos con un `when` sobre la metrica. No filtra nada.
- El `LineChart` de progreso solo se pinta si `progressPoints.size >= 2` (`StatsScreen.kt:844-858`).

## Requisitos

### R1 — El calculo de e1RM vive en dominio y es probado

Extraer la formula a un modelo propio en `domain/model/`, con tests. Hoy es una funcion privada de extension dentro de un use case, imposible de reutilizar y sin cobertura directa.

Debe exponer, ademas del valor, el **nivel de confianza**:

| Repeticiones | Confianza | Comportamiento |
|---|---|---|
| 1-5 | Alta | Estimar |
| 6-10 | Media | Estimar |
| > 10 | — | **No estimar** |

Se mantiene **Epley**, no se cambia de formula. Cambiarla reescribiria retroactivamente la curva historica del usuario, y eso es peor que el error conocido de la formula. Queda anotado en el propio codigo por que.

### R2 — Ausencia de estimacion se representa con `null`, no con `0.0`

`0.0` es un valor legitimo en un grafico y se pinta como un punto en el eje. "No se puede estimar" no es cero: es ausencia de dato.

`ExerciseProgressEntry.estimatedOneRepMaxKg` y `ExerciseSetRecord.estimatedOneRepMaxKg` pasan a `Double?`. Una entrada de sesion solo tiene e1RM si **al menos una** de sus series es estimable.

### R3 — La serie de 1RM omite los puntos sin estimacion, sin afectar a las demas metricas

Este es el punto delicado. Una sesion de 3x20 sigue teniendo volumen, peso maximo y repeticiones validos. Solo le falta el 1RM.

Por tanto:
- Las metricas `MaxWeight`, `Volume` y `Reps` siguen pintando **todos** los puntos, exactamente como hoy.
- La metrica `EstimatedOneRepMax` pinta **solo** los puntos estimables.
- La condicion de "hay grafico" (`>= 2 puntos`) debe evaluarse sobre la **serie ya filtrada**, no sobre el total de puntos. Si el usuario siempre entrena a 15 repeticiones, el chip de 1RM debe mostrar un vacio con sentido, no un grafico de una linea plana en cero ni un crash.

### R4 — `bestEstimatedOneRepMax` solo considera series estimables

El record de 1RM deja de poder salir de una serie de 20 repeticiones. Si el usuario no tiene ninguna serie de <= 10 reps, ese record es `null` y la tarjeta no lo muestra.

### R5 — La confianza es visible

El usuario debe poder distinguir un e1RM fiable de uno inferido. Minimo: etiqueta de confianza en el detalle del punto seleccionado (`ProgressPointDetails`) y en la tarjeta del record de 1RM.

Texto en español, sin tildes cuando se pueda, siguiendo el estilo de la app. No inventar una escala numerica: "alta" / "media" basta.

### R6 — Cambio de comportamiento visible, y es intencionado

**A diferencia de la Fase 1, esta fase SI cambia lo que el usuario ve.** Puntos que hoy aparecen en la grafica de 1RM van a desaparecer, y el record de 1RM puede bajar o vaciarse.

Eso no es una regresion: es la correccion de un dato que estaba mal. Debe anunciarse al dueño en el aviso de fin de tarea para que no lo confunda con un bug durante la pasada manual.

## Fuera de alcance

- Cambiar de formula o promediar varias formulas.
- RIR y calidad de esfuerzo (van en la spec `2026-07-30-effort-quality-rir.md`).
- Objetivos, retos y proyeccion.
- Migracion de base de datos: **no hace falta ninguna**. El e1RM se calcula al leer, no esta persistido.

## Criterios de aceptacion

1. `test` y `build` en verde.
2. Una serie de 100 kg x 3 produce e1RM con confianza alta; 100 x 8 confianza media; 100 x 15 **no produce e1RM**.
3. Un ejercicio con solo series de mas de 10 repeticiones no aparece en la grafica de 1RM, pero **si** en las de peso, volumen y repeticiones.
4. Seleccionar el chip de 1RM en un ejercicio sin datos estimables no rompe la pantalla ni pinta ceros.
5. `bestEstimatedOneRepMax` es `null` cuando no hay ninguna serie de <= 10 reps.
6. No hay cambios en `app/schemas/` — esta fase no toca la base de datos.
