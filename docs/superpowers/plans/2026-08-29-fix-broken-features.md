# Plan: Corregir funciones que existen en la UI y no funcionan (P1)

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `chore/dependency-upgrade`** (ya tiene `fix/data-loss` incluido, mas Gradle 9.7.1/AGP 9.3.2/Kotlin 2.1.20/Compose BOM 2026.08.00 — verificado, `test`+`build` en verde, pusheada a origin). Commits separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica local (`test` + `build`) antes de cada commit; NO lances el emulador. No commits de WIP.

**Rama:** `fix/broken-features` (nueva, desde `chore/dependency-upgrade`)
**Spec:** `docs/superpowers/specs/2026-08-29-fix-broken-features.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

**Este plan es mas grande que el de P0.** Algunas tareas (R2 notas de ejercicio, R5 temporizador, R10 exportar) tienen alcance real de feature, no solo bugfix. Si al empezar una tarea ves que se sale mucho del tamaño de las demas, **para, avisa, y propon partirla en su propia rama** en vez de inflar esta.

---

## Tarea 0: Preparar rama

- [ ] `git checkout chore/dependency-upgrade && git pull` si aplica
- [ ] `git checkout -b fix/broken-features`

---

## Tarea 1 (R1): Unidad de peso kg/lb funcional

**Archivos:** `feature/workout/WorkoutScreen.kt`, `feature/history/HistoryScreen.kt`, `feature/stats/StatsScreen.kt`, ViewModels correspondientes

- [ ] Decidir y documentar: se guarda siempre en kg y se convierte solo en presentacion (recomendado)
- [ ] Leer `weightUnit` desde las 3 pantallas y aplicar conversion/etiqueta donde se muestra peso
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: apply weight unit preference across workout, history, and stats`

---

## Tarea 2 (R2): Notas de sesion, serie y ejercicio

**Archivos:** dialogo de finalizar entrenamiento, `WorkoutScreen.kt` (fila de serie), `WorkoutExerciseEntity.kt`, migracion de Room, `WorkoutViewModel.kt`

- [ ] Añadir campo de notas al dialogo de finalizar sesion, pasarlo a `finishWorkoutSession`
- [ ] Añadir control de notas por serie en la UI de Entrenar (el dato y el render ya existen en `FitTrackSetRow`)
- [ ] Añadir columna de notas a `WorkoutExerciseEntity`, migracion aditiva de Room (nueva version de DB, generar y commitear el schema JSON)
- [ ] Leer y mostrar la nota de ejercicio donde corresponda
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: support session, set, and exercise notes end to end`

---

## Tarea 3 (R3): Filtro de periodo real en Datos

**Archivos:** `feature/stats/StatsViewModel.kt:62`

- [ ] Sustituir `observeWorkoutStats(period = WorkoutStatsPeriod.All)` por el `period` real
- [ ] Confirmar que graficas y marcas de PR cambian al cambiar el filtro
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: apply selected period filter to stats charts and records`

---

## Tarea 4 (R4): Etiquetas de Datos correctas

**Archivos:** `feature/stats/StatsViewModel.kt:236-238`, `StatsScreen.kt:347`

- [ ] `exerciseCount`: contar ejercicios distintos, no entradas ejercicio-sesion
- [ ] "PRs": contar records reales del periodo filtrado
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: correct exercise count and PR count labels in stats`

---

## Tarea 5 (R5): Temporizador de descanso fiable

**Archivos:** `feature/workout/WorkoutViewModel.kt:609-617`, `WorkoutScreen.kt:328,526-530`

- [ ] Cambiar el conteo a reloj de pared en vez de contador de ticks
- [ ] Persistir el estado del temporizador para que sobreviva a la muerte del proceso
- [ ] Mover la vibracion fuera del `LaunchedEffect` atado a la visibilidad de la tarjeta
- [ ] Persistir la preferencia de auto-start
- [ ] Si notificacion/sonido resulta demasiado grande para esta rama, documentarlo como pendiente explicito, no lo dejes sin decir
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: make rest timer wall-clock based and survive process death`

---

## Tarea 6 (R6): Confeti no se queda encallado

**Archivos:** `core/design/components/ConfettiAnimation.kt:41-47`, `WorkoutScreen.kt:222-233`

- [ ] Limpiar `celebration` de forma que no dependa de que `onFinished()` llegue a dispararse si el usuario cambia de pestaña
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: stop stuck PR celebration confetti on tab switch`

---

## Tarea 7 (R7): Home se recupera de errores

**Archivos:** `feature/home/HomeViewModel.kt:71-73`

- [ ] Mover el `.catch` para que no termine el `combine` upstream
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: recover home dashboard flow after a transient error`

---

## Tarea 8 (R8): Widget y atajos funcionales

**Archivos:** `FitTrackPlusWidget.kt`, `AndroidManifest.xml:26-38`

- [ ] Llamar a `updateAll` cuando cambien datos relevantes
- [ ] Añadir `launchMode`/`onNewIntent` para que el extra `open_tab` funcione
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: update widget on data change and handle shortcut deep links`

---

## Tarea 9 (R9): Proteccion al salir de entrenamiento activo

**Archivos:** `WorkoutScreen.kt:113-140`, `AppShellViewModel.kt:59-68`

- [ ] Registrar `setNavigationBlocker` mientras haya sesion activa en Entrenar
- [ ] Limpiar el bloqueador al salir de composicion
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: block accidental navigation away from an active workout session`

---

## Tarea 10 (R10): Exportar datos

**Archivos:** `NavigationShellConfig.kt:42-46`, nuevo use case/repositorio segun corresponda

- [ ] Exportar historial (rutinas + sesiones) a JSON o CSV, compartible/guardable por el usuario
- [ ] Si importar/borrar-todo se descartan de esta rama, documentarlo explicitamente
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add local data export for routines and workout history`

---

## Tarea 11 (R11): Desconectar Health Connect revoca el permiso

**Archivos:** `feature/settings/SettingsViewModel.kt:100-105,43-44`

- [ ] Revocar el permiso real de Health Connect al desconectar, no solo el booleano local
- [ ] Los flags de disponibilidad dejan de ser `val` fijos; se reevaluan
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: revoke health connect permission on disconnect`

---

## Tarea 12 (R12): Limpieza de codigo muerto

**Archivos:** varios, ver spec

- [ ] Eliminar `FitTrackLoadingCard`, `selectExercise`/`withSelectedExercise`, politicas de auto-start solo-test, imports sin usar
- [ ] Decidir sobre `HeatmapCalendar.kt`: eliminar o documentar por que se mantiene sin consumidores
- [ ] `test` + `build` en verde
- [ ] Commit: `chore: remove dead code found in behavior audit`

---

## Verificacion final antes de avisar al dueño

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] Repaso de los 13 criterios de aceptacion de la spec
- [ ] Push y avisar, marcando claramente cualquier tarea que se recortara de alcance (R5 notificacion/sonido, R10 importar/borrar-todo)

**Pendiente de pasada manual (la hace el dueño):**
1. Cambiar unidad a lb y comprobar Entrenar/Historial/Datos.
2. Escribir notas de sesion, serie y ejercicio y comprobar que se leen despues.
3. Cambiar el filtro de periodo en Datos y comprobar que las graficas cambian.
4. Usar el temporizador de descanso haciendo scroll durante la cuenta atras.
5. Conseguir un PR, cambiar de pestaña durante el confeti, volver.
6. Salir de una sesion activa por el boton atras del sistema.
7. Exportar datos y comprobar el fichero resultante.
8. Desconectar Health Connect y comprobar en ajustes del sistema que el permiso se revoco.

**Merge:** a `develop` cuando compile y pasen los tests (arrastra `fix/data-loss` y las rondas 1+2). A `main` solo tras la pasada manual del dueño.
