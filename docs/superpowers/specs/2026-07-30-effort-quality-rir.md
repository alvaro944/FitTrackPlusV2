# Spec: RIR por ejercicio y metrica de calidad de esfuerzo

**Fecha:** 2026-07-30
**Fase:** 2b del arco de periodizacion (`docs/design/mejoras-claude.md`, entrada 19)
**Plan de ejecucion:** `docs/superpowers/plans/2026-07-30-effort-quality-rir.md`
**Prerequisito:** `feature/estimated-1rm-confidence` mergeada a `develop`.

---

## Por que

El dueño quiere control sobre **cuanto exprime cada ejercicio**. Hoy la app no tiene ni un dato sobre esfuerzo: sabe el peso, las repeticiones y si la serie se completo, pero no sabe si esas 10 repeticiones fueron comodas o al limite. Sin ese dato no hay forma de distinguir un entrenamiento productivo de uno blando ni de uno que quema fatiga sin comprar adaptacion.

La pieza que falta es el **RIR** (repeticiones en reserva): cuantas repeticiones mas se podrian haber hecho.

### El hallazgo que define el diseño

La investigacion de Fase 0 (`docs/research/training-methods.md`, apartado de proximidad al fallo) es contraintuitiva y **[A] solida**:

- Los meta-analisis **no encuentran diferencia** entre entrenar al fallo y no llegar al fallo, ni en fuerza ni en hipertrofia.
- Cuando el volumen no se iguala, el **no-fallo sale favorecido** en fuerza: llegar al fallo cuesta volumen, y el volumen es lo que construye.
- Entrenar a **1-3 RIR produce la misma hipertrofia** que llegar al fallo completo.

Consecuencia de producto, y es la regla que gobierna toda esta spec: **la app no premia el fallo. Premia la consistencia en la zona 1-3 RIR.** Una app que celebra el RIR 0 estaria empujando al usuario a gastar fatiga sin beneficio, y con respaldo cientifico en contra.

## Decision de UX ya tomada

**Un solo RIR por ejercicio, no por serie.** Decidido por el dueño.

El motivo es de friccion, no de precision: un campo por serie es mas informativo (permite ver la caida dentro del ejercicio, que es la señal real de fatiga), pero obliga a rellenar un dato mas en cada serie, con el movil en la mano entre series. Un campo que se abandona a la segunda semana no vale nada.

Se pierde la caida intra-ejercicio. Se acepta.

## Estado actual verificado

- `WorkoutSetEntity` (`data/local/entity/WorkoutSetEntity.kt`) tiene `weightKg`, `reps`, `isCompleted`, `notes`. **Nada de esfuerzo.**
- `WorkoutExerciseEntity` tiene `targetRepsSnapshot`, `targetRepsMinSnapshot/MaxSnapshot` (de la Fase 1), `performedVariantKey`, `position`. **Nada de esfuerzo.**
- DB en version 5 tras la Fase 1. Migraciones en `core/database/FitTrackPlusDatabase.kt`, registradas en `di/DatabaseModule.kt`. Sin `fallbackToDestructiveMigration`.
- El pipeline de stats agrupa por `scopeKey` (rutina|dia|variante) en `ObserveWorkoutStatsUseCase.kt:227-234`.
- La pantalla de entrenamiento es `feature/workout/WorkoutScreen.kt` + `WorkoutViewModel.kt`.

## Requisitos

### R1 — El RIR se persiste en el ejercicio de la sesion

Nueva columna `rir: Int?` en `WorkoutExerciseEntity`. Migracion aditiva a **DB v6**: `ALTER TABLE ADD COLUMN`, sin recrear tablas, sin backfill (no hay dato historico que recuperar — todo queda `NULL`).

`NULL` significa "no reportado", y es un estado de primera clase, no un dato pendiente. La mayoria del historial existente sera `NULL` para siempre y eso es correcto.

Rango valido: `0..10`. Valores fuera de rango se rechazan.

### R2 — Es opcional y no bloquea nada

Terminar un ejercicio o una sesion **nunca** debe requerir RIR. Si no se rellena, todo funciona como hoy. Ninguna validacion nueva puede impedir cerrar un entrenamiento.

Esto es no negociable: el registro de peso y repeticiones es la funcion central de la app y no puede degradarse por una metrica secundaria.

### R3 — Entrada de un solo toque

El RIR se introduce **por ejercicio**, en la pantalla de entrenamiento. Debe poder ponerse en un toque desde una fila de opciones (0, 1, 2, 3, 4+), no escribiendo en un campo de texto ni con un stepper.

Debe poder cambiarse y borrarse (volver a "no reportado") mientras la sesion este abierta. Una vez finalizada, se edita por la misma via que el resto de datos del historial si esa via ya existe; si no existe, **no se abre una nueva** en esta fase.

Reutilizar los componentes de seleccion que ya existan en `core/design/`. No crear un componente nuevo si hay uno equivalente.

### R4 — Semantica visible, no solo un numero

Un "2" suelto no significa nada para quien no conoce el concepto. Junto al selector debe quedar claro que se pregunta: cuantas repeticiones mas se podrian haber hecho.

Y la zona 1-3 debe estar señalada como la util, sin regañar al usuario por salirse de ella. La app informa, no juzga.

Sobre el `0`: se etiqueta como fallo, y **no se destaca como logro**. Ni medalla, ni color de exito, ni felicitacion. Es un dato mas.

### R5 — Metrica de calidad de esfuerzo

Nueva metrica en stats: **porcentaje de ejercicios con RIR reportado que caen en la zona 1-3**.

Reglas de calculo:
- El denominador son **solo** los ejercicios con RIR reportado. Los `NULL` no cuentan ni a favor ni en contra.
- Si no hay ningun RIR reportado en el periodo, la metrica **no se muestra**. No mostrar "0%", que se leeria como "lo haces todo mal" cuando en realidad no hay datos.
- Debe indicarse sobre cuantos ejercicios se calcula, para que un 100% sobre dos ejercicios no se confunda con un 100% sobre cincuenta.
- Se respetan los filtros de periodo ya existentes (`WorkoutStatsPeriod`).

### R6 — Reparto por zonas, no solo un porcentaje

Ademas del porcentaje, mostrar el reparto entre las tres zonas: **al fallo (0)**, **zona util (1-3)** y **lejos del fallo (4+)**. Es lo que convierte la metrica en accionable: un 40% de zona util puede venir de entrenar blando o de machacarse, y son problemas opuestos con soluciones opuestas.

Ninguna de las tres zonas se presenta como buena o mala en si misma. Se describe el reparto.

## Fuera de alcance

- RIR por serie.
- Usar el RIR para recomendar cargas: eso es el motor de progresion (fase posterior).
- Tocar `GetProgressionHintUseCase`. Sigue como esta.
- RPE, velocidad de barra, dolor o molestias articulares.
- Descargas. Aparcadas por decision del dueño.

## Criterios de aceptacion

1. `test` y `build` en verde.
2. `app/schemas/.../6.json` generado y commiteado.
3. Actualizar desde una instalacion con datos reales no pierde nada; todos los ejercicios historicos quedan con `rir == NULL`.
4. Se puede terminar un entrenamiento completo sin tocar el RIR ni una vez.
5. Un ejercicio con RIR 2 cuenta en la zona util; con 0 cuenta como fallo; con 5 cuenta en 4+.
6. Con cero RIR reportados en el periodo, la metrica de calidad de esfuerzo no aparece.
7. En ningun sitio de la UI el RIR 0 recibe tratamiento de logro.
