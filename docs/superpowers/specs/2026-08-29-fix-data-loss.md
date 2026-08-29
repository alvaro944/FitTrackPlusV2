# Spec: Corregir perdida y corrupcion de datos (P0)

**Fecha:** 2026-08-29
**Origen:** `docs/design/auditoria-ronda-3.md` (auditoria de comportamiento con Opus, 5 barridos en paralelo sobre 13.291 lineas de UI), seccion P0.
**Plan de ejecucion:** `docs/superpowers/plans/2026-08-29-fix-data-loss.md`
**Prerequisito:** `develop` ya tiene mergeadas las rondas 1+2 de unificacion de design system y `structured-target-reps` (Room v5) — verificado. Parte desde `develop`.

---

## Por que

La auditoria encontro 9 formas en las que la app pierde o corrompe datos reales del usuario hoy. Esto va antes que cualquier otra cosa — migracion de dependencias, rediseno visual, todo — porque son datos de entrenamientos reales, no deuda tecnica. Verificado de forma independiente (no solo copiado del informe): **P0-1, P0-2, P0-4 y P0-5 confirmados leyendo el codigo actual**, con coincidencia exacta de linea en los cuatro casos.

## Estado actual verificado

### P0-1 — Una sesion de peso corporal se borra entera al finalizar
- `WorkoutViewModel.kt:982-986` (`isWorkoutSetCompleted`): exige `weightKg > 0.0 && reps > 0`. Una serie de dominadas o abdominales (sin peso) **nunca** se marca completada.
- `WorkoutViewModel.kt:409-422` (`finishWorkout`): `shouldDiscardSession = session.completedSetCount == 0`. Si ninguna serie completo, se llama a `workoutRepository.discardSession(sessionId)` en vez de `finishWorkoutSession(sessionId)`.
- **Confirmado**: una sesion entera de ejercicios de peso corporal se borra al pulsar "Finalizar", sin aviso.

### P0-2 — Tocar una nota existente la selecciona entera y la primera tecla la borra
- `RoutinesScreen.kt:1278` y `WorkoutScreen.kt:962`: los campos "Notas" (multilinea) usan `FitTrackSelectAllTextField` sin pasar `selectAllOnFocus = false`. El default del componente es `true`.
- **Confirmado**: linea exacta coincide en ambos ficheros pese a los cambios de la ronda 2.

### P0-3 — Historial escribe en cada tecla
- `HistoryViewModel.kt:272-298,215`, `HistoryScreen.kt:351-361`. Borrar el campo de peso para reescribirlo persiste un `0` intermedio. El boton "Guardar" es no-op (ya se guardo solo), "Descartar" hace una segunda ronda de escrituras para deshacer, y los botones estan en las posiciones invertidas de lo esperado (peligro de pulsar el que no tocaba).

### P0-4 — Cerrar el dialogo de alternativas (no el boton Cancelar) confirma el cambio a medio escribir
- `RoutinesScreen.kt:1013-1022` (`onDismiss` del `FitTrackDialog` completo): llama a `finishInlineAlternativeEdit`, que solo limpia el snapshot sin restaurarlo — a diferencia de `cancelInlineAlternativeEdit`, que si revierte.
- La ronda 2 (tarea 5) ya añadio un boton "Cancelar" **dentro** de la fila en edicion que si funciona bien. El hueco es la via **X / tocar fuera / atras del sistema** sobre el dialogo completo, que sigue usando el camino que no revierte.
- **Confirmado**: linea exacta.

### P0-5 — Editar una serie completada la des-completa y despues infla el contador de PRs
- `WorkoutViewModel.kt:996-1001` (`updateWorkoutSetWeightInput`/`updateWorkoutSetRepsInput`): al cambiar peso o reps, ponen `isCompleted = false` pero **no tocan `prType`**.
- `WorkoutViewModel.kt:504-513`: al volver a completar la serie, se llama de nuevo a `detectPersonalRecord` y, si aplica, se incrementa `prCount` otra vez — sin que el `prType` previo se haya limpiado nunca en ningun punto del fichero (verificado: cero asignaciones de `prType = null`).
- **Confirmado**.

### P0-6 — Rutinas duplicadas por doble toque
- `RoutinesScreen.kt:148-154`: el FAB de guardar (`ExtendedFloatingActionButton`) llama a `viewModel::saveEditor` sin `enabled = false` mientras se guarda.
- `RoutinesViewModel.kt:359-361` (`saveEditor`): comprueba `editor.canSave` pero no si ya hay un guardado en curso (`isSaving`).
- Contraste: el boton inline equivalente (linea ~765 segun la auditoria) si respeta `isSaving`.

### P0-7 — El estado de los dialogos de rutina esta atado a la posicion, no al elemento
- `RoutinesScreen.kt:694,910,618`: `itemsIndexed`/`forEachIndexed` sin `key`. Un `remember` de estado de dialogo (edicion, confirmacion de borrado) se reasigna al reordenar dias/ejercicios, aplicandose al elemento equivocado.

### P0-8 — Eliminar un dia entero no pide confirmacion
- `RoutinesViewModel.kt:161`, `RoutineDao.kt:65`: `removeDay` borra el dia y todos sus ejercicios sin dialogo de confirmacion, mientras que eliminar un solo ejercicio si lo pide.

### P0-9 — No existe deshacer en ningun sitio
- 7 `SnackbarHostState` en el proyecto, 0 usan `actionLabel`. Ninguna accion destructiva (eliminar dia, ejercicio, alternativa, nota) ofrece un "Deshacer".

## Requisitos

### R1 — Las sesiones de peso corporal se guardan

- Redefinir "serie completada" para que no dependa de `weightKg > 0.0`. Una serie de ejercicio de peso corporal se considera completada con `reps > 0` (sin exigir peso), manteniendo `weightKg > 0.0 && reps > 0` para ejercicios con peso.
- Revisar como distinguir un ejercicio "de peso corporal" — buscar si ya existe alguna marca en el dominio (categoria de ejercicio, o inferencia por "peso siempre vacio/0 en el historial de ese ejercicio"). Si no existe ninguna señal de dominio, la opcion minima es: **una serie con `reps > 0` y peso vacio/0 se considera completada igual**, sin distinguir "peso real de 0kg" de "sin peso" — documentar la decision tomada.
- `finishWorkout` no debe poder borrar una sesion que tiene series con reps registradas, aunque el peso sea 0 en todas.
- **Ampliacion (encontrada durante la implementacion, 2026-08-29):** la misma regla de completado estaba triplicada — `WorkoutViewModel.isWorkoutSetCompleted`, `HistoryViewModel.kt:304`, y `UpdateWorkoutSetUseCase.kt:24` — cada una con su propio `weightKg > 0.0 && reps > 0` copiado a mano. Las tres deben quedar en **una sola funcion en `domain/`** que las tres llamen, no tres copias sincronizadas por separado. Si se arreglan las tres por separado sin consolidar, se reabre este mismo bug la proxima vez que alguien toque una sola copia.

### R2 — Las notas no se seleccionan enteras al tocar

- `RoutinesScreen.kt:1278` y `WorkoutScreen.kt:962`: pasar `selectAllOnFocus = false` a los dos campos de notas.

### R3 — Historial deja de escribir en cada tecla

- Los cambios de peso/reps en el modo edicion de Historial se acumulan localmente y solo se persisten al pulsar "Guardar" (o al confirmar "Descartar cambios", que no debe escribir nada).
- Corregir el orden de los botones para que coincida con la convencion del resto de la app (confirmar a la derecha, descartar/cancelar a la izquierda o como corresponda segun `FitTrackConfirmDialog`).

### R4 — Cerrar el dialogo de alternativas sin pulsar Cancelar tambien revierte

- `RoutinesScreen.kt:1013-1022`: cuando `editingAlternativeIndex != null`, el `onDismiss` del dialogo completo debe llamar a `cancelInlineAlternativeEdit` (no a `finishInlineAlternativeEdit`) antes de cerrar, reutilizando el mecanismo de snapshot que ya existe en `RoutinesViewModel` desde la ronda 2.

### R5 — Editar una serie completada limpia su marca de PR

- `updateWorkoutSetWeightInput`/`updateWorkoutSetRepsInput` deben poner `prType = null` ademas de `isCompleted = false`.
- Al re-completar la serie, `prCount` solo debe subir si `detectPersonalRecord` la detecta como PR **de nuevo**, sin arrastrar un conteo previo ya contado.

### R6 — Guardar rutina no se puede disparar dos veces

- FAB de guardar: `enabled = !isSaving` (o equivalente), igual que ya hace el boton inline.
- `saveEditor()`: añadir guarda temprana `if (_uiState.value.isSaving) return`, ademas de `canSave`.

### R7 — El estado de los dialogos de rutina sigue al elemento, no a la posicion

- Añadir `key` estable (id de dia/ejercicio, no el indice) a los `itemsIndexed`/bucles señalados en `RoutinesScreen.kt:694,910,618`.
- **Ampliacion (encontrada durante la implementacion, 2026-08-29):** ni `RoutineDayEditorUiState` ni `RoutineExerciseEditorUiState` tienen hoy un identificador estable para un elemento aun no guardado — `routineExerciseId` es `null` en cualquier ejercicio nuevo del borrador. Hace falta introducir un **id de borrador** (generado localmente, p.ej. UUID o contador monotono) en ambos estados:
  - Se genera una vez al crear el elemento (nuevo dia, nuevo ejercicio) y se mantiene mientras exista en el borrador.
  - **Duplicar genera un id nuevo para la copia** — nunca comparte id con el original.
  - **Reordenar mueve el id junto con los datos del elemento**, nunca se queda fijo a la posicion de la lista.
  - Este id de borrador es independiente del `routineExerciseId` persistido (que sigue siendo `null` hasta guardar) — es solo para que Compose sepa distinguir elementos entre recomposiciones.
- Verificar tras el cambio que reordenar un dia/ejercicio no traslada ningun dialogo de confirmacion ni estado de edicion abierto a otro elemento.

### R8 — Eliminar un dia pide confirmacion

- Añadir `FitTrackConfirmDialog` (destructivo) antes de `removeDay`, con el mismo patron que ya existe para eliminar un ejercicio.

### R9 — Confirmar antes de eliminar una alternativa (recortado durante la implementacion)

- **Decision (2026-08-29):** construir "Deshacer" de verdad (snapshot + canal de eventos entre ViewModel y `SnackbarHost` + restauracion real) es tamaño de feature, no de bugfix de emergencia — no se improvisa en esta rama. `RoutinesViewModel` no tiene hoy ningun snapshot de lo eliminado ni transporte hacia el snackbar mas alla de un mensaje de texto de una via.
- Con R8 ya cubriendo dia y ejercicio con confirmacion, el hueco real que queda **sin ninguna proteccion** es eliminar una alternativa (`RoutinesScreen.kt:1058-1060`, boton "Eliminar" sin dialogo). Alcance de esta tarea: añadir `FitTrackConfirmDialog` (destructivo) antes de `onRemoveAlternative`, mismo patron que dia/ejercicio.
- El "Deshacer" real para las 3 acciones (y las demas instancias de `SnackbarHostState` del proyecto) queda anotado como pendiente para `fix/broken-features` o una rama propia — no se cierra aqui.

## Fuera de alcance

- Todo lo de P1 en adelante (funciones rotas, calidad de texto, navegacion, visual, accesibilidad, idioma) — ramas separadas.
- La migracion de dependencias — rama separada, sin relacion con estos bugs.

## Criterios de aceptacion

1. `test` y `build` en verde.
2. Una sesion de solo ejercicios de peso corporal (reps sin peso) se guarda al finalizar, no se borra.
3. Tocar una nota existente y escribir no borra el contenido previo.
4. En Historial, editar y pulsar "Descartar" no deja ningun cambio a medias escrito en BD.
5. Editar el nombre de una alternativa y cerrar el dialogo con la X (no con Cancelar) no deja el cambio a medio escribir aplicado.
6. Completar una serie, editarla, y volver a completarla no incrementa el contador de PRs si ya se habia contado antes.
7. Pulsar el FAB de guardar rutina dos veces seguidas rapido no crea dos rutinas.
8. Reordenar dias/ejercicios con un dialogo de confirmacion o edicion abierto no lo traslada a otro elemento.
9. Eliminar un dia pide confirmacion.
10. Eliminar una alternativa pide confirmacion, igual que dia y ejercicio.
11. Pasada manual del dueño en emulador/dispositivo antes de mergear, con foco especial en R1 (sesion de peso corporal) por ser el bug mas grave.
