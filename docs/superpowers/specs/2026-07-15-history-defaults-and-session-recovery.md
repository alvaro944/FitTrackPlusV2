# Spec: Historial por rutina activa, select-all en edicion de historial, y recuperacion de sesiones incompletas

Fecha: 2026-07-15
Ejecutor: Codex
Rama: `feature/history-defaults-and-session-recovery` (nueva rama **desde `develop`**, no desde `main` ni desde `new-desing`)
Origen: dogfooding del dueño. Auditoria de Claude sobre `new-desing` (2026-07-15).

## Contexto y decision de rama

`new-desing` = `develop` + 1 commit: el diseño "grit" (`bae2874`), que el dueño va a **aparcar**. Ese commit se queda donde esta (no tocarlo, no revertirlo). Todo el trabajo real del proyecto ya vive en `develop`, asi que **esta rama parte de `develop`** para no arrastrar el diseño aparcado.

Tres mejoras relacionadas (historial + entrada de entrenamiento), un solo grupo, una sola rama, commits separados por mejora.

Verificacion minima por commit: `./gradlew test` y `./gradlew build` en verde (en este entorno: `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`). Las mejoras 2 y 3 tienen partes que dependen de render nativo o de flujo real y **requieren pasada manual** del dueño en dispositivo/emulador — decirlo en el commit y en el aviso final.

**Invariante que NO se puede romper** (ver `CLAUDE.md` → "Snapshot invariant"): el historial lee de snapshots, nunca de rutinas vivas; las sesiones abiertas (`finishedAt IS NULL`) no aparecen en historial ni en stats. La mejora 3 se apoya en este invariante, no lo viola.

---

## Mejora 1 — El historial arranca filtrado por la rutina seleccionada, no por "Todo"

### Sintoma / pedido

Hoy, al abrir Historial, el filtro de rutina arranca en "Todo" (todas las rutinas). El dueño quiere que **por defecto** muestre la rutina actualmente seleccionada (la rutina activa, p. ej. "Rutina Álvaro"). El usuario sigue pudiendo cambiar el filtro a "Todo" u a otra rutina manualmente.

### Estado confirmado

- El filtro por rutina ya existe y funciona por **nombre**: `HistoryUiState.selectedRoutineName: String? = null` → `null` significa "Todo" (`HistoryViewModel.kt:323`, `filterByRoutine` en `HistoryViewModel.kt:483-488`).
- La rutina activa se guarda por **ID** en DataStore, no por nombre: `userPreferencesRepository.activeRoutineId` (se ve consumido en `WorkoutViewModel.kt:70`).
- `HistoryViewModel` **no** inyecta hoy ninguna fuente de la rutina activa ni de rutinas — hay que añadirla.

### Trampa a cuidar (importante)

El filtro compara contra `session.routineNameSnapshot` (el nombre que la rutina tenia **el dia** de esa sesion). Si el dueño renombro la rutina despues, las sesiones viejas guardan el nombre viejo. El default debe apuntar al **nombre actual** de la rutina activa (el que el usuario ve hoy), asumiendo que las sesiones recientes usan ese mismo nombre. No intentar "seguir" renombrados historicos: simplemente resolver id→nombre actual de la rutina activa y usar ese string como `selectedRoutineName` inicial.

### Fix

1. Inyectar en `HistoryViewModel` lo necesario para resolver el **nombre actual de la rutina activa**:
   - `UserPreferencesRepository` (o el mismo tipo que ya expone `activeRoutineId`) para leer el id activo.
   - `RoutineRepository` (o el use case/consulta ya existente) para resolver id→nombre actual de esa rutina. Reutilizar lo que ya use `WorkoutViewModel`/Home para lo mismo; no inventar una consulta nueva si ya hay uno equivalente (revisar `RoutineRepository`).
2. En el `init`, ademas del stream de historial, observar el id de rutina activa y resolver su nombre actual. Cuando llegue, si `selectedRoutineName` sigue en su valor **inicial no tocado por el usuario**, fijarlo al nombre de la rutina activa y re-aplicar filtros.
   - Cuidado con el orden de emision: el nombre de la rutina activa puede llegar antes o despues del primer batch de sesiones. La solucion debe funcionar en ambos ordenes (combinar flows, o guardar el nombre resuelto y aplicarlo en cuanto ambos esten disponibles).
3. **Distinguir "el usuario eligio Todo" de "aun no se aplico el default"**: si el usuario toca el filtro y elige "Todo" (`null`) a proposito, NO volver a forzar la rutina activa encima. Modelar esto con una bandera interna del estilo `routineFilterInitialized: Boolean` (o `hasUserOverriddenRoutineFilter`). El default solo se aplica una vez, mientras el usuario no haya tocado el selector.
4. Si la rutina activa no tiene ninguna sesion en el historial todavia, el resultado filtrado quedara vacio — eso es correcto y esperado. El selector debe permitir volver a "Todo" facilmente (ya existe). Verificar que el estado vacio del historial ya se renderiza con sentido (ver `HistoryScreen.kt`); si no, mostrar el empty state que ya exista, no crear uno nuevo.
5. Caso sin rutina activa (`activeRoutineId == null`): dejar el default en "Todo" (`null`), comportamiento actual.

### Done

Abrir Historial con "Rutina Álvaro" seleccionada como rutina activa → el historial muestra solo sesiones de esa rutina, con el chip/selector de rutina marcando esa rutina (no "Todo"). Cambiar el selector a "Todo" → se ven todas y NO se vuelve a forzar la rutina activa al recibir nuevas emisiones. Test unitario del filtro/estado inicial si encaja con el patron de tests ya existente para `applyHistoryFilters`.

---

## Mejora 2 — Select-all al enfocar en los campos de peso/reps de la edicion del historial

### Sintoma / pedido

Al ver una sesion anterior y entrar en modo edicion, tocar un campo (p. ej. reps "8") pone el cursor detras del "8" en vez de seleccionar todo. Escribir "10" concatena → "810". El mismo problema que ya se arreglo en Entrenar y en las variantes (spec `2026-07-05-workout-input-ux-bugs.md`), pero **el historial quedo fuera de aquella migracion**.

### Estado confirmado

- El componente compartido **ya existe**: `FitTrackSelectAllTextField` en `core/design/components/SelectAllTextField.kt` (hace exactamente esto: `selectAllOnFocus` por defecto `true`, seleccion entera al enfocar y al soltar el tap).
- Los campos de edicion del historial usan `OutlinedTextField` **pelado**, sin gestion de seleccion: `HistoryScreen.kt:744` y `HistoryScreen.kt:752` (los campos de peso y reps de cada serie en modo edicion). El otro `OutlinedTextField` del archivo (`HistoryScreen.kt:322`) es el **selector de rutina** (dropdown) — ese NO se toca.

### Fix

1. Sustituir los dos `OutlinedTextField` de peso/reps (`HistoryScreen.kt:744` y `:752`) por `FitTrackSelectAllTextField`, manteniendo label/placeholder/singleLine/keyboardOptions/colors tal cual estan hoy. Solo cambia la seleccion, no el comportamiento de teclado (los `keyboardOptions` numericos se conservan).
2. Los callbacks ya son `(String) -> Unit` (`updateSetWeight`/`updateSetReps` en `HistoryViewModel.kt:210` y `:222`), asi que el cambio es puramente de UI, sin tocar el ViewModel.
3. No tocar el dropdown de rutina (`HistoryScreen.kt:322`).

### Done

En una sesion del historial, entrar en editar, tocar el campo de reps con "8" y escribir "10" → queda "10", no "810". Mismo comportamiento en el campo de peso. Verificar en pasada manual (depende de render nativo de seleccion).

---

## Mejora 3 — Guardar y recuperar entrenamientos sin terminar

### Pedido (resumen de lo que dijo el dueño)

- Si al finalizar un entrenamiento **todas las series estan completas** → el flujo es "Terminar", normal.
- Si al finalizar **quedan series sin completar** → poder finalizarlo igual, pero que se guarde como **incompleto**.
- En el historial, las sesiones incompletas muestran una **señal pequeña (LED/punto)**; al tocarla, se puede **recuperar** esa sesion para seguir entrenandola.

### Estado confirmado y decision de diseño

- El modelo hoy es **binario**: `finishedAt IS NULL` = sesion activa (y **ya se auto-recupera**: `StartWorkoutSessionUseCase.kt` devuelve la sesion activa existente si la hay, antes de crear una nueva); `finishedAt` con fecha = finalizada (aparece en historial, y hoy no se puede reabrir).
- Cada serie **ya** tiene `isCompleted` (`WorkoutSetEntity.kt`), y el entrenamiento ya calcula `completedSetCount` / `totalSetCount` (`WorkoutScreen.kt:489,495,744`).

**Decision de arquitectura (sin migracion de DB):** "sesion incompleta" **se deriva** de las series — una sesion finalizada es incompleta si tiene al menos una serie con `isCompleted == false`. NO añadir columna nueva a `WorkoutSessionEntity`. Esto respeta el invariante del snapshot (la verdad ya esta en las series) y evita churn de schema (la DB ya va por v3). El LED se calcula, no se persiste.

**Decision de diseño de "recuperar":** recuperar = **reabrir** la sesion (`finishedAt = null`). Asi vuelve a ser la sesion activa y el flujo de Entrenar ya la carga sola. Consecuencia coherente con el invariante: mientras esta reabierta, **desaparece** del historial y de stats (porque ya no esta finalizada); cuando el usuario la vuelve a finalizar, reaparece. Esto es lo correcto, no un bug.

**Guarda de sesion unica:** solo puede haber UNA sesion activa a la vez (`observeActiveSession` es `LIMIT 1`). Si el usuario intenta recuperar una sesion incompleta **mientras ya hay otra sesion activa abierta**, NO reabrir: mostrar un mensaje del tipo "Termina primero tu entrenamiento en curso" y no hacer nada. Confirmar la existencia de sesion activa con `getActiveSessionWithExercises()` antes de reabrir.

### Fix — parte A: finalizar permitiendo incompletos

Hoy `finishWorkout()` (`WorkoutViewModel.kt:398`) ya finaliza aunque falten series; solo **descarta** si `completedSetCount == 0` (`shouldDiscardSession`). Y el dialogo de confirmar ya distingue el caso 0 (`WorkoutScreen.kt:149-161`). Cambios:

1. Mantener el descarte cuando `completedSetCount == 0` (no hay nada que recuperar; comportamiento actual).
2. Cuando `completedSetCount > 0` pero `completedSetCount < totalSetCount` (quedan series sin completar), el dialogo de confirmacion debe avisar que se guardara como **incompleto** y se podra recuperar. Texto sugerido del dialogo en ese caso: titulo "Finalizar entrenamiento", cuerpo "Quedan series sin completar. Se guardara como incompleto y podras recuperarlo desde el historial.", confirm "Finalizar". Cuando `completedSetCount == totalSetCount`, texto normal de terminar. Ajustar el `finishDialogText` existente en `WorkoutScreen.kt:149`.
3. No hace falta un segundo boton "guardar": un solo boton, el texto/dialogo cambia segun el estado. (El dueño evaluo la opcion de dos botones y prefirio uno solo.)

### Fix — parte B: LED de incompleto en el historial

1. Plumbing de completitud hasta el historial (derivado, sin persistir):
   - `WorkoutHistorySummary` (`domain/model/HistoryModels.kt:3`): añadir campo derivado, p. ej. `val isComplete: Boolean` (o `incompleteSetCount: Int`). Calcularlo en `ObserveWorkoutHistoryUseCase.kt` (`toHistorySummary`) a partir de `exercises.flatMap { it.sets }` → `all { it.isCompleted }`. Los sets de la relacion (`WorkoutSetEntity`) ya traen `isCompleted`.
   - `HistorySessionUiState` (`HistoryViewModel.kt:350`): añadir el mismo `isComplete` y mapearlo en `WorkoutHistorySummary.toUiState()` (`HistoryViewModel.kt:438`).
   - Para el detalle: `WorkoutHistorySet` (`HistoryModels.kt:34`) y `HistorySetUiState` (`HistoryViewModel.kt:428`) hoy **no** arrastran `isCompleted`. Añadirlo y mapearlo en `GetWorkoutHistoryDetailUseCase.kt:48-51` y en `WorkoutHistorySet.toUiState()` (`HistoryViewModel.kt:556`). Con eso el detalle tambien puede saber que series quedaron sin completar.
2. UI del LED:
   - En la **lista** del historial (`HistoryScreen.kt`, el item de cada sesion), mostrar un punto/indicador pequeño y discreto cuando `!isComplete`. Reutilizar colores de la paleta v2 (`core/design/Theme.kt`), no inventar. Que sea accesible (no solo color: añadir `contentDescription`/semantics tipo "Entrenamiento incompleto").
   - En el **detalle** de una sesion incompleta, mostrar el mismo estado + el boton de recuperar (parte C).

**Caveat de datos viejos (verificar en pasada manual):** las sesiones finalizadas **antes** de esta feature podrian tener series con `isCompleted == false` que en su momento el usuario si hizo pero no marco. Esas apareceran con LED de "incompleto". En el modelo de la app, marcar = hecho, asi que es defendible. Si en la pasada manual el dueño ve demasiado ruido en sesiones antiguas, anotarlo como pendiente y evaluar una opcion de guarda (p. ej. solo considerar incompletas las sesiones cuya fecha sea posterior al primer arranque con la feature) — NO implementar esa guarda por defecto; primero ver si molesta de verdad.

### Fix — parte C: accion de recuperar

1. Repo: añadir `suspend fun reopenSession(sessionId: Long)` a `WorkoutRepository` + implementacion en `DefaultWorkoutRepository` + `@Query("UPDATE workout_sessions SET finishedAt = NULL WHERE id = :sessionId")` en `WorkoutDao`. (Simetrico a `finishSession`.)
2. Use case: `ReopenWorkoutSessionUseCase` que:
   - comprueba con `getActiveSessionWithExercises()` si ya hay una sesion activa;
   - si la hay, devuelve un resultado tipo "bloqueado" (sellado/enum) para que la UI muestre el mensaje "Termina primero tu entrenamiento en curso";
   - si no la hay, llama a `reopenSession(sessionId)` y devuelve ok.
3. UI/flujo: en el detalle de una sesion incompleta, boton "Recuperar entrenamiento". Al tocarlo → invocar el use case; si ok, navegar a la pestaña **Entrenar** (que ya autocarga la sesion activa via `StartWorkoutSessionUseCase`); si bloqueado, mostrar el mensaje y quedarse. Revisar como se navega hoy entre pestañas (`core/navigation`) y reutilizar ese mecanismo; no cablear navegacion nueva a mano si hay un patron.
4. Al reabrir, la sesion sale de historial/stats hasta que se vuelva a finalizar — verificar que efectivamente desaparece (consecuencia del invariante, no bug).

### Done (mejora 3)

- Entrenar dejando 1+ series sin marcar y finalizar → confirma con aviso de "incompleto", la sesion aparece en el historial con el LED.
- Entrenar con todas las series marcadas y finalizar → sin LED, texto normal.
- En el detalle de una sesion con LED, "Recuperar" → vuelve a Entrenar con esa sesion cargada, y esa sesion desaparece del historial mientras esta abierta.
- Intentar recuperar teniendo ya una sesion activa → mensaje de bloqueo, no se reabre nada.
- Finalizar con 0 series completas → se descarta (comportamiento actual, sin cambios).

---

## Cierre de la rama

1. Commits separados, conventional commits, **sin Co-Authored-By**:
   - `feat: default history filter to active routine`
   - `fix: select all text on focus in history set edit fields`
   - `feat: allow finishing and recovering incomplete workout sessions`
   (la mejora 3 puede ir en 1 o 2 commits si el ejecutor separa data/domain de UI; a criterio del ejecutor, sin commits de WIP.)
2. Actualizar `docs/progress/project-progress.md` y `docs/progress/phase-log.md`: las tres mejoras, que se verifico con test/build y que quedo pendiente de pasada manual.
3. Corregir el dato desactualizado en `CLAUDE.md` ("DB v2" → la DB va por v3) **solo si el ejecutor lo ve limpio**; si no, dejar nota en progress.
4. Merge objetivo: a `develop` cuando compile y tests pasen. `main` solo tras pasada manual del dueño.
5. Push y aviso al dueño con la checklist de pasada manual (mejoras 2 y 3, mas el caveat de datos viejos del LED).
