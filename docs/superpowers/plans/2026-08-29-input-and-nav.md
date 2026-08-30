# Plan: Calidad de entrada de texto y navegacion/controles (P2 + P3)

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `fix/broken-features`** (verifica primero que este tecnicamente completa — compila, tests pasan; si no, avisa y decide con el dueño si esperar). Commits separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica local (`test` + `build`) antes de cada commit; NO lances el emulador. No commits de WIP.

**Rama:** `refactor/input-and-nav` (nueva, desde `fix/broken-features`)
**Spec:** `docs/superpowers/specs/2026-08-29-input-and-nav.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

Esta rama toca muchos sitios pequeños en vez de pocos grandes — 14 requisitos. Si alguno resulta mas grande de lo que parece (p.ej. R13 unificar los dos enums de periodo puede tocar mas ViewModels de los previstos), para y avisa antes de expandir el commit.

---

## Tarea 0: Preparar rama

- [ ] Verificar que `fix/broken-features` esta tecnicamente completa
- [ ] `git checkout fix/broken-features && git pull` si aplica
- [ ] `git checkout -b refactor/input-and-nav`

---

## Tarea 1 (R1): Capitalizacion e IME nativos

**Archivos:** `feature/routines/RoutinesViewModel.kt:643`, `feature/workout/WorkoutViewModel.kt:913`, y los `FitTrackSelectAllTextField`/`OutlinedTextField` de nombre en `RoutinesScreen.kt`/`WorkoutScreen.kt`

- [ ] Sustituir la normalizacion manual por `KeyboardOptions(capitalization = KeyboardCapitalization.Words)` en los campos de nombre
- [ ] Eliminar `normalizeEditorNameInput`/`normalizeWorkoutAlternativeNameInput` si el `KeyboardCapitalization` nativo cubre el caso (confirmar con el dueño en el PR si hay diferencia de comportamiento notable)
- [ ] Encadenar foco con `ImeAction.Next`/`KeyboardActions` en: editor de rutina, editor de alternativa (x2 sitios), fila de serie — terminar en `ImeAction.Done`
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: use native keyboard capitalization and IME focus chaining`

---

## Tarea 2 (R2): Select-all consistente en los bordes

**Archivos:** `RoutinesScreen.kt:897`, `core/design/Dialogs.kt:98`

- [ ] Nombre de dia precargado: revisar/aplicar `selectAllOnFocus = false` para que no reseleccione en cada toque
- [ ] `FitTrackInputDialog`: migrar internamente de `String` a `TextFieldValue` para poder ofrecer select-all donde aplique
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: make select-all-on-focus consistent at the edges`

---

## Tarea 3 (R3+R4): Saneado de reps/peso y locale consistente

**Archivos:** `WorkoutViewModel.kt:332-343,850-856,954-968`, `HistoryViewModel.kt:652-654`

- [ ] Rechazar/sanear reps negativas o con sufijos invalidos antes de persistir
- [ ] Corregir el manejo de notacion cientifica en peso
- [ ] Unificar la politica de separador decimal entre lectura y edicion en Historial
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: sanitize reps/weight input and unify decimal locale handling`

---

## Tarea 4 (R5+R6): Errores tras interaccion, foco en elemento nuevo, maxLength

**Archivos:** `RoutinesViewModel.kt:487`, `RoutinesScreen.kt:1116`, campos de texto libre en `RoutinesScreen.kt`/`WorkoutScreen.kt`

- [ ] Error solo tras que el usuario haya interactuado con el campo (no antes de escribir nada)
- [ ] Foco/scroll automatico al campo de nombre de un ejercicio recien añadido
- [ ] `maxLength` razonable en nombre de rutina/dia/ejercicio/alternativa y notas
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: delay validation errors until interaction and add field length limits`

---

## Tarea 5 (R7): Pulsacion larga en steppers, tope en objetivo de pasos

**Archivos:** `core/design/TargetPrescriptionFields.kt:38`, `feature/settings/SettingsScreen.kt:212-224`

- [ ] Pulsacion larga en el stepper de series (step x5 o similar)
- [ ] Tope superior sensato en el objetivo de pasos diario
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add long-press stepping and cap daily step goal`

---

## Tarea 6 (R8): Boton atras y drawer en el lado correcto

**Archivos:** `feature/history/HistoryScreen.kt:377-403`, `core/design/AppShell.kt:103,157-172,223-236`

- [ ] Mover el boton atras de Historial a la izquierda, verificar que no queda tapado por la hamburguesa
- [ ] `Icons.Filled.ArrowBack` -> `Icons.AutoMirrored.Filled.ArrowBack`
- [ ] `verticalScroll` en el sheet del drawer
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: reposition history back button and fix drawer scroll`

---

## Tarea 7 (R9): Doble toque en pestaña activa, volver desde detalle

**Archivos:** `core/design/AppShell.kt:185-208`, `core/navigation/AppShellViewModel.kt:75`

- [ ] Doble toque en la pestaña activa navega al tope de esa pestaña
- [ ] Volver desde el detalle de Historial va a la lista, no fuera de la pestaña
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: pop to top on active-tab re-tap and fix history detail back nav`

---

## Tarea 8 (R10): Copy del drawer y Ajustes

**Archivos:** `core/navigation/NavigationShellConfig.kt:20-28`

- [ ] Corregir la fila "Widget & atajos" del drawer, que dice "Proximamente" pese a que el widget ya existe
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: correct drawer copy claiming widget is not yet available`

---

## Tarea 9 (R11): Onboarding con atras y repetible

**Archivos:** `feature/onboarding/OnboardingScreen.kt:63-129`, Ajustes

- [ ] Permitir retroceder entre pasos del onboarding sin salir de la app
- [ ] Exponer una via en Ajustes que llame a `setHasSeenOnboarding(false)` para repetirlo
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add onboarding back navigation and replay entry point`

---

## Tarea 10 (R12): Confirmaciones que faltan y borrar sesion

**Archivos:** `RoutinesScreen.kt:1312,997`, `HistoryScreen.kt:513`, `WorkoutRepository`/`DefaultWorkoutRepository.kt:169-171`

- [ ] Confirmar antes de: eliminar nota, recuperar entrenamiento, cambiar de variante en sesion activa
- [ ] Revisar si `discardSession` (hoy solo usado para el caso "sesion vacia" de P0-1) sirve tambien para un borrado explicito desde Historial, o si hace falta un metodo separado — decidir y documentar
- [ ] Añadir la via de UI en Historial para borrar una sesion finalizada, con confirmacion
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add missing confirmations and session deletion from history`

---

## Tarea 11 (R13): Un solo enum de periodo

**Archivos:** `feature/history/HistoryViewModel.kt:58-86,400`, `feature/stats/StatsViewModel.kt:46`

- [ ] Unificar los dos enums paralelos de filtro de periodo en uno solo, mismo orden y valor por defecto
- [ ] Decidir si Historial sigue arrancando pre-filtrado por la rutina activa; si se mantiene, hacerlo visible en el filtro
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: unify period filter enum between history and stats`

---

## Tarea 12 (R14): Estado que sobrevive a rotacion

**Archivos:** `feature/history/HistoryViewModel.kt`, `feature/stats/StatsViewModel.kt`, `core/navigation/FitTrackPlusNavHost.kt:34-38`, `feature/workout/WorkoutScreen.kt:119`

- [ ] `SavedStateHandle` en `HistoryViewModel`/`StatsViewModel` para mes visible y dia de pasos seleccionado
- [ ] `LaunchedEffect(initialTab)` no debe re-ejecutar en cada recreacion si el usuario ya navego lejos del atajo original
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: persist history/stats selection state across rotation`

---

## Verificacion final antes de avisar al dueño

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] Repaso de los 12 criterios de aceptacion de la spec
- [ ] Push y avisar, marcando cualquier tarea recortada de alcance

**Pendiente de pasada manual (la hace el dueño):**
1. Escribir nombres en minuscula en los formularios de rutina/alternativa — deben capitalizarse solos sin pelear con el teclado.
2. Navegar entre campos con la tecla "siguiente" del teclado en los 3 formularios señalados.
3. Rotar la pantalla en Historial/Datos con un mes o dia seleccionado — no debe perderse.
4. Doble toque en la pestaña activa, y volver desde el detalle de una sesion.
5. Repetir el onboarding desde Ajustes.
6. Borrar una sesion desde Historial.

**Merge:** a `develop` cuando compile y pasen los tests. A `main` solo tras la pasada manual del dueño.
