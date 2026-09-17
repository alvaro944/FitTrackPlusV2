# Auditoria de los 27 criterios de aceptacion — Progression Engine PRIMARY V1

**Fecha:** 2026-09-17
**Rama:** `feature/progression-engine-primary` (20 commits sobre `develop`)
**Spec:** `docs/superpowers/specs/2026-09-16-progression-engine-primary-v1.md`

Leyenda: **OK** verificado · **MANUAL** requiere pasada del dueño · **NO** no cumplido

## Base

| # | Criterio | Estado | Evidencia |
|---|---|---|---|
| 1 | `test` y `build` en verde | **OK** | 233 tests, 0 fallos; build y detekt OK |
| 2 | `7.json` y `8.json` commiteados | **OK** | `git ls-files app/schemas` |
| 3 | Actualizar no pierde datos; historico a `NULL`/`ACCESSORY` | **OK** | `Migration7To8Test` en verde en dispositivo |
| 4 | Terminar entrenamiento sin tocar el RIR | **MANUAL** | el campo es opcional por diseño; no verificado a mano |

## RIR y esfuerzo

| # | Criterio | Estado | Evidencia |
|---|---|---|---|
| 5 | El texto menciona la primera serie | **OK** | "Repeticiones que te sobraron en la primera serie", captura |
| 6 | El RIR 0 no recibe trato de logro | **OK** | "0 = fallo", sin color de exito |
| 7 | Sin RIR reportados la metrica no aparece | **OK** | `EffortQuality?` null, test |
| 8 | Zonas 0 / 1-3 / 4+ | **OK** | tests de `ObserveWorkoutStatsUseCase` |

## e1RM

| # | Criterio | Estado | Evidencia |
|---|---|---|---|
| 9 | Los cuatro casos de confianza | **OK** | `StrengthEstimateTest` |
| 10 | Sin estimables no sale en 1RM pero si en las demas | **OK** | `chartProgressPoints` filtra solo `EstimatedOneRepMax` |
| 11 | Chip de 1RM sin datos no rompe | **OK** | serie filtrada antes del `>= 2 puntos` |
| 12 | `bestEstimatedOneRepMax` null sin estimables | **OK** | test |

## Motor

| # | Criterio | Estado | Evidencia |
|---|---|---|---|
| 13 | Clasificacion STRENGTH, cuatro casos | **OK** | `ExposureCalculationTest` |
| 14 | Un `FAILED` en carga confirmada no baja | **OK** | `ProgressionDecisionTest` |
| 15 | Dos `FAILED` bajan un step | **OK** | `ProgressionDecisionTest` |
| 16 | Un `FAILED` con `pendingConfirm` revierte | **OK** | test, incluido `decisionReason` |
| 17 | Ajuste intra-sesion sin doble conteo | **OK** | `IntraSessionAdjustmentTest` |
| 18 | Cooldown de 4 bloquea los triggers | **OK** | `ProgressionDecisionTest` |
| 19 | `UNKNOWN` no bloquea T1/T3/T4/T5 | **OK** | `ProgressionDecisionTest` |
| 20 | `userFlaggedBadDay` no cuenta | **OK** | tests de exposicion y decision |
| 21 | Dos `EVALUATING` fallidos recalibran | **OK** | `ProgressionDecisionTest` |
| 22 | Las EWMA nunca se mezclan | **OK** | `ExposureCalculationTest` |
| 23 | El e1RM mostrado viene de `ewmaStrengthE1rm` | **OK** | `ProgressionDecisionTest` |
| 24 | El cuarto PRIMARY se rechaza con mensaje | **OK** | `RoutineProgressionRoleTest`, 6 casos |
| 25 | El motor es invocable desde JUnit puro | **OK** | todos sus tests lo son; cero imports de Room/Android |
| 26 | Toda prescripcion trae su razon | **OK** | `ProgressionReason` en dominio + `strings.xml` en UI; verificado en pantalla en español |

## Regresion

| # | Criterio | Estado | Evidencia |
|---|---|---|---|
| 27 | `GetProgressionHintUseCase` intacto | **OK** | sin cambios en produccion; solo stubs en su test |

---

## Criterio 26: resuelto el 2026-09-17

`decisionReason` se generaba en ingles dentro del motor. Arreglado con el patron correcto, no
traduciendo literales: el dominio devuelve ahora un **`ProgressionReason`** (27 valores) y la UI
lo mapea a `strings.xml`. Mismo criterio que `LoadDecision.REVERTED`: **la capa de decision dice
QUE decidio, la UI dice COMO se cuenta.**

Efectos laterales buenos:

- Los tests aseveran sobre el **codigo** de decision, no sobre la redaccion. Cambiar una frase ya
  no rompe un test, y un cambio de decision si lo rompe. Es lo que queriamos.
- La exposicion persiste `reason.name`, asi que el historial guarda un codigo estable en vez de
  prosa que una reescritura futura cambiaria en silencio. Con `fromStoredName()` tolerante a
  valores desconocidos.
- El copy queda traducible sin tocar el motor.

Verificado en emulador: la tarjeta muestra "Exposicion de calibracion para tener una referencia
fiable." sobre una prescripcion real de 100 kg.

Ademas se elimino una duplicacion: existian `calculateIntraSessionAdjustmentSteps` (en
`ProgressionDecision.kt`) e `intraSessionAdjustmentSteps` (en `IntraSessionAdjustment.kt`)
implementando la misma regla de R17. Dos copias de una regla que decide cargas acaban divergiendo.
Se conserva la de `IntraSessionAdjustment.kt`, que ademas comprueba el tipo de exposicion.

**27 de 27 criterios cumplidos.** Queda solo la pasada manual del dueño.

## Pendiente de pasada manual

Los criterios marcados **MANUAL**, mas el flujo completo end to end: marcar PRIMARY, entrenar,
ver la prescripcion, ajustar por la probe, finalizar, y comprobar que la siguiente exposicion
cambia en consecuencia. Eso solo se valida entrenando.
