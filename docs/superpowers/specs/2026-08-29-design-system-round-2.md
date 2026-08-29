# Spec: Unificacion de design system, ronda 2

**Fecha:** 2026-08-29
**Origen:** dos auditorias adicionales tras cerrar `refactor/design-system-unification` (ronda 1): verificacion de que la migracion quedo completa, y catalogo de dialogos/formularios y campos de series-reps en toda la app.
**Plan de ejecucion:** `docs/superpowers/plans/2026-08-29-design-system-round-2.md`
**Prerequisito:** `refactor/design-system-unification` (ronda 1) mergeada a `develop`.

---

## Por que

La ronda 1 unifico tarjetas, badges, dropdowns y filas de series. Al revisar el resultado con el dueño aparecieron dos capas mas de duplicacion que la ronda 1 no cubria:

1. **Un olvido real de la ronda 1**: un sitio que deberia haberse migrado y no se migro.
2. **Formularios y dialogos crecidos de forma organica**: cada uno con su propia fila de botones (algunos sin forma de cancelar) y su propia manera de pedir "series" y "reps" (texto libre, stepper, chips), sin que nadie se haya planteado que son el mismo dato.

La idea que gobierna esta ronda, en palabras del dueño: *"si hay cinco botones distintos, nos quedamos con los que hagan falta — pueden ser dos, no tiene que ser uno — pero que sea el mismo componente en todos los sitios que hacen lo mismo, para poder cambiar una cosa una vez y que se propague."*

## Estado actual verificado

### Olvido de la ronda 1
- `feature/routines/RoutinesScreen.kt`, banner "rutina activa" (~lineas 312-348): sigue con `Box.background(primary, CircleShape) { Icon(...) }` a mano en vez de `FitTrackIconBadge`.

### Ficha de nombre de rutina (aprobado por el dueño en la revision visual)
- `RoutinesScreen.kt:673-685`: el campo de nombre de la rutina es el unico input de la seccion "Identidad" que no esta dentro de una `FitTrackCard` — flota suelto, mientras que el nombre del dia (`RoutinesScreen.kt:798`) y los ejercicios si tienen tarjeta.

### Campos de texto libre sin envolver
- `RoutinesScreen.kt:674` (nombre rutina), `:916` (nombre dia), `:1158` (nombre ejercicio): los tres son `OutlinedTextField` crudo. El resto de campos de texto libre de la app (p.ej. "Nombre" en los dialogos de alternativas) ya usan `FitTrackSelectAllTextField(selectAllOnFocus = false)`.

### Toolbar de reordenar/duplicar/eliminar — duplicado 2 veces
- `RoutineDayEditor` (`RoutinesScreen.kt:869-914`): 4 iconos (subir, bajar, duplicar, eliminar).
- `RoutineExerciseEditor` (`RoutinesScreen.kt:1106-1155`): los mismos 4 iconos + uno extra ("alternativas").
- Logica de habilitado/deshabilitado (`canMoveUp`/`canMoveDown`/`canRemove`) identica en los dos sitios.
- Confirmado que Entrenar no tiene nada parecido — es una duplicacion de 2, no de 3.

### Filas de accion de dialogo — sin criterio comun
Precedente ya bueno en `core/design/Dialogs.kt`: `FitTrackConfirmDialog` (confirmar+cancelar, con variante destructiva) y `FitTrackInputDialog` (campo + confirmar+cancelar, con slot opcional para una 3a accion) ya son consistentes y **se mantienen tal cual**. El problema esta en los usos de `FitTrackDialog` con `actions` libre:

- `ExerciseAlternativesEditorDialog` (Rutinas, `RoutinesScreen.kt:1264-1393`): el modo de edicion en linea de una alternativa solo tiene un boton **"Listo"** (`:1347-1349`) — no hay forma de cancelar y descartar los cambios que se esten escribiendo. El resto del dialogo (`:1387-1391`) solo tiene "Cerrar".
- `ExerciseAlternativesDialog` (Entrenar, `WorkoutScreen.kt:869-994`): en modo lista no hay fila de acciones (aplicar es tocar la tarjeta, correcto y se mantiene); en modo creacion si tiene "Cancelar"/"Guardar y usar" (`:983-991`) — este es el patron bueno.

### Campo "series + reps objetivo" — 3 formas distintas para el mismo dato
- **Ejercicio principal** (`RoutineExerciseEditor`, `RoutinesScreen.kt:1187` y `:1217`): `FitTrackStepper` para series + `FlowRow` de `FilterChip` (mas un chip "+" que abre `FitTrackInputDialog` para un valor custom) para reps.
- **Alternativa en Rutinas** (`RoutinesScreen.kt:1323`, `:1330`): dos `FitTrackSelectAllTextField` (series, reps), **sin teclado numerico en ninguno de los dos**.
- **Alternativa en Entrenar** (`WorkoutScreen.kt:955`, `:963`): los mismos dos `FitTrackSelectAllTextField`, pero aqui Series **si** tiene teclado numerico y Reps no — inconsistente incluso dentro del mismo par de campos.

Es el mismo dato conceptual (series objetivo + reps objetivo de un ejercicio) resuelto de 3 maneras distintas dentro del mismo fichero.

### Resaltado visual del item actual/predeterminado (feedback del dueño sobre el canvas)
- Entrenar ya marca la opcion "en uso ahora" con un borde verde en la tarjeta — el dueño prefiere ese tratamiento, es "mas visual".
- Rutinas marca la opcion predeterminada solo con el texto del boton ("Predeterminada" vs "Usar por defecto"), sin ningun resalte — "se ve menos".

## Decisiones de diseño ya tomadas (aprobadas por el dueño)

- La ficha de nombre de rutina se envuelve en `FitTrackCard` (sin cambios de contenido, solo el envoltorio).
- Entrenar y Rutinas **siguen teniendo comportamientos distintos** en sus dialogos de alternativas (aplicar-ahora vs fijar-a-futuro) — **no se fusionan** en un unico flujo. Lo que se unifica es: el estilo visual del resaltado del item actual/predeterminado, el teclado numerico de los campos, y la fila de botones de los formularios de creacion.
- El campo de series+reps de las alternativas pasa a usar el mismo componente (stepper + chips) que ya usa el ejercicio principal, en vez de texto libre. Esto es un cambio de interaccion, no solo visual, y el dueño lo ha aceptado explicitamente sabiendo que implica tocar chips en vez de escribir numeros.

## Requisitos

### R1 — Arreglar el olvido: badge de "rutina activa"

Migrar el circulo con icono del banner "rutina activa" (`RoutinesScreen.kt`, ~312-348) a `FitTrackIconBadge(variant = Icon, tone = Filled)` (o el tone que visualmente coincida con el uso actual de `primary` como fondo).

### R2 — Envolver la ficha de nombre de rutina en `FitTrackCard`

`RoutinesScreen.kt:673-685`: envolver el `item {}` del nombre de rutina en `FitTrackCard`, igual que el nombre del dia (`:798`). Sin cambios de contenido ni de logica de validacion.

### R3 — Envolver los 3 campos de texto libre sueltos

`RoutinesScreen.kt:674` (nombre rutina), `:916` (nombre dia), `:1158` (nombre ejercicio): sustituir el `OutlinedTextField` crudo por `FitTrackSelectAllTextField(selectAllOnFocus = false)`, igual que el campo "Nombre" de los dialogos de alternativas. Mismo comportamiento (texto libre, sin seleccionar todo), solo se unifica el componente.

### R4 — `FitTrackReorderActions`

Nuevo componente en `core/design/` (fichero nuevo, p.ej. `ReorderActions.kt`).

- Props: `canMoveUp: Boolean`, `canMoveDown: Boolean`, `canRemove: Boolean`, `onMoveUp`, `onMoveDown`, `onDuplicate`, `onRemove`, y un slot opcional `extraAction: (@Composable RowScope.() -> Unit)?` para el boton extra de alternativas que solo tiene `RoutineExerciseEditor`.
- Migrar `RoutineDayEditor` (`:869-914`) sin `extraAction`.
- Migrar `RoutineExerciseEditor` (`:1106-1155`) con `extraAction` = boton de alternativas.

### R5 — Fila de acciones de formulario, unificada en los dialogos de alternativas

- No tocar `FitTrackConfirmDialog`/`FitTrackInputDialog` — ya son el patron correcto.
- Nuevo composable pequeño (p.ej. `FitTrackFormDialogActions(cancelLabel, confirmLabel, onCancel, onConfirm, confirmEnabled)`) para el patron "Cancelar + accion primaria" que ya usa bien `ExerciseAlternativesDialog` de Entrenar en modo creacion (`WorkoutScreen.kt:983-991`).
- Migrar el modo creacion de `ExerciseAlternativesDialog` (Entrenar) a este componente.
- Dar al modo edicion en linea de `ExerciseAlternativesEditorDialog` (Rutinas, `:1313-1350`) un cancelar real: usar el mismo `FitTrackFormDialogActions` con "Cancelar" (descarta los cambios del campo en edicion, vuelve a la vista de solo lectura) + "Guardar" (aplica y cierra el modo edicion), sustituyendo el boton unico "Listo".
- El modo lista de Entrenar (aplicar tocando la tarjeta) y el boton "Cerrar" del dialogo completo de Rutinas **no cambian** — son comportamientos intencionadamente distintos, no una fila de formulario.

### R6 — Resaltado visual del item actual/predeterminado

- En `ExerciseAlternativesEditorDialog` (Rutinas): la tarjeta del ejercicio/alternativa que sea el `defaultVariantKey` actual recibe el mismo tratamiento visual que Entrenar usa para "en uso ahora" (borde verde en la `FitTrackCard`, p.ej. `border(1.5.dp, primary, shapes)`), ademas del texto que ya tiene.
- No cambia la logica: sigue siendo un boton de accion en Rutinas (fijar a futuro) y solo informativo en Entrenar (badge de sesion actual). Solo se iguala el lenguaje visual del resaltado.

### R7 — Unificar el campo "series + reps objetivo"

- Extraer el par `FitTrackStepper` (series) + selector de reps (chips + opcion personalizada) de `RoutineExerciseEditor` a un componente compartido en `core/design/` (p.ej. `FitTrackTargetPrescriptionFields.kt`), parametrizado por los valores actuales y los callbacks de cambio.
- Migrar `RoutineExerciseEditor` a usar el componente extraido (sin cambio de comportamiento, es la misma UI que ya existe, solo movida).
- Migrar el par series+reps de la alternativa en Rutinas (`:1323`, `:1330`) al mismo componente extraido.
- Migrar el par series+reps de la alternativa en Entrenar (`:955`, `:963`) al mismo componente extraido.
- **Efecto esperado**: desaparecen los `FitTrackSelectAllTextField` de series/reps en ambos dialogos de alternativas; el usuario elige series con +/- y reps con chips en los 3 sitios por igual.

## Fuera de alcance

- El bug de "seleccionar todo al tocar" — queda pendiente de que el dueño reproduzca en dispositivo real e indique pantalla y teclado exactos. No se toca en esta ronda sin evidencia.
- `WeeklyStepsCard` (grafico de barras de Stats) y el calendario de Stats — decisiones ya cerradas en la ronda 1, no se reabren.
- Cualquier cambio de dominio, persistencia o Room.

## Criterios de aceptacion

1. `test` y `build` en verde.
2. El banner "rutina activa" usa `FitTrackIconBadge`.
3. La ficha de nombre de rutina esta dentro de una `FitTrackCard`.
4. Los 3 campos de texto libre sueltos usan `FitTrackSelectAllTextField(selectAllOnFocus = false)`.
5. `RoutineDayEditor` y `RoutineExerciseEditor` comparten `FitTrackReorderActions`; cero logica de reordenar duplicada.
6. El modo edicion en linea de alternativas en Rutinas tiene un boton de cancelar real que descarta los cambios sin guardarlos.
7. El item actual/predeterminado se resalta con el mismo lenguaje visual (borde) en Entrenar y en Rutinas.
8. Ningun `FitTrackSelectAllTextField` queda usado para series/reps objetivo en ninguno de los 2 dialogos de alternativas — los 3 sitios (ejercicio principal, alternativa Rutinas, alternativa Entrenar) usan el mismo componente de prescripcion.
9. Pasada manual del dueño en emulador/dispositivo antes de mergear a `main`, prestando atencion especial a R7 (cambio de interaccion, no solo visual).
