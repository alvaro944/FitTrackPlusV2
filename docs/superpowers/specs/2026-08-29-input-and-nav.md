# Spec: Calidad de entrada de texto y navegacion/controles (P2 + P3)

**Fecha:** 2026-08-29
**Origen:** `docs/design/auditoria-ronda-3.md`, secciones P2 y P3.
**Plan de ejecucion:** `docs/superpowers/plans/2026-08-29-input-and-nav.md`
**Prerequisito:** `fix/broken-features` mergeada, o al menos con P1 tecnicamente completo — esta rama parte de ahi para heredar el stack ya actualizado (Compose BOM 2026.08.00 trae predictive back con soporte de framework, relevante para P3).

---

## Por que

P0 y P1 arreglaron perdida de datos y funciones rotas. Esta ronda ataca dos cosas que no rompen nada pero friccionan cada interaccion: como se escribe texto (sin capitalizacion, sin saltos de teclado, seleccion inconsistente) y como se navega (botones en el lado equivocado, sin confirmar acciones, estado que no sobrevive a una rotacion).

Verificado de forma independiente antes de escribir esta spec:
- **P2, hecho global**: confirmado por grep — cero apariciones de `KeyboardCapitalization`, `ImeAction` o `KeyboardActions` en los 107 ficheros de `app/src/main/kotlin`.
- **P2-1**: `normalizeEditorNameInput` (`RoutinesViewModel.kt:643`) y `normalizeWorkoutAlternativeNameInput` (`WorkoutViewModel.kt:913`) son practicamente identicas — la misma logica de "poner mayuscula la primera letra" duplicada en dos features.
- **P3-6**: `discardSession` (que borra fisicamente, `DefaultWorkoutRepository.kt:169-171`) existe en el repositorio, pero se usa **solo** desde `WorkoutViewModel.finishWorkout()` para el caso de sesion vacia (P0-1) — cero usos en `feature/history/`. Matiz sobre el hallazgo original de la auditoria: la funcion si existe en el repositorio, lo que no existe es una via desde Historial para llamarla.
- **P3-8**: confirmado — cero `SavedStateHandle` en `HistoryViewModel.kt` y `StatsViewModel.kt`.

## Estado actual verificado (resto de hallazgos, heredados del audit)

### P2 — Entrada de texto
- P2-2: ningun campo encadena foco con el siguiente (editor de rutina, editor de alternativa x2, fila de serie).
- P2-3: select-all bien en los 4 numericos de series (ronda 1/2 ya lo dejaron correcto); mal en los bordes — reselecciona en cada toque en el nombre de dia precargado con "Dia 1" (`RoutinesScreen.kt:897`), e imposible en `FitTrackInputDialog` porque usa `String` en vez de `TextFieldValue` (`Dialogs.kt:98`).
- P2-4: reps sin sanear (`WorkoutViewModel.kt:332-343,850-856`) — admite `-5`, `12x`; peso con notacion cientifica mal formateada.
- P2-5: separador decimal fijado a coma sin consultar locale, con politicas distintas entre lectura y edicion en Historial.
- P2-6: errores en rojo antes de escribir nada (`RoutinesViewModel.kt:487`); sin foco/scroll al añadir ejercicio nuevo.
- P2-7: sin `maxLength` en ningun campo del proyecto.
- P2-8: series objetivo solo por stepper sin pulsacion larga (`TargetPrescriptionFields.kt:38` — el componente que la ronda 2 acaba de crear); objetivo de pasos igual, sin tope superior.

### P3 — Navegacion y controles
- P3-1: boton atras de Historial a la derecha en vez de a la izquierda, probablemente tapado por la hamburguesa flotante del shell (mismo rango horizontal); usa `Icons.Filled.ArrowBack` deprecado sin espejado RTL.
- P3-2: el drawer abre por la izquierda pero su boton de apertura esta a la derecha; el sheet no tiene `verticalScroll` (se corta con fuente grande).
- P3-3: doble toque en pestaña activa no hace nada; volver a Historial devuelve dentro del detalle en vez de a la lista.
- P3-4: Ajustes es un callejon de una entrada; 2 de las 3 filas del drawer dicen "proximamente" pese a que el widget ya existe.
- P3-5: onboarding sin atras (el back del sistema saca de la app) y sin forma de repetirlo — `setHasSeenOnboarding(false)` existe y nadie lo llama.
- P3-6: sin confirmar eliminar alternativa (la ronda 2 ya lo arreglo), eliminar nota, recuperar entrenamiento (muta el historial), cambiar de variante. Y no hay via de UI para borrar una sesion (ver arriba).
- P3-7: Historial arranca filtrado por la rutina activa sin pedirlo; Historial y Datos tienen dos enums paralelos para el mismo filtro de periodo, con orden y valor por defecto distintos.
- P3-8: cero `SavedStateHandle` en Historial/Datos — mes visible y dia de pasos se pierden en cada rotacion; `LaunchedEffect(initialTab)` re-ejecuta en cada recreacion y devuelve a la pestaña del atajo aunque el usuario ya haya navegado.

## Requisitos

### R1 — Capitalizacion e IME nativos donde el teclado ya lo resuelve

- Sustituir `normalizeEditorNameInput`/`normalizeWorkoutAlternativeNameInput` (logica manual, pelea con el IME) por `KeyboardOptions(capitalization = KeyboardCapitalization.Words)` en los campos de nombre correspondientes. Eliminar las dos funciones duplicadas si el `KeyboardCapitalization` nativo cubre el caso.
- Encadenar foco con `ImeAction.Next` + `KeyboardActions` en los 3 formularios señalados (editor de rutina, editor de alternativa, fila de serie), terminando en `ImeAction.Done`.

### R2 — Select-all consistente en los bordes

- Nombre de dia precargado ("Dia 1"): no debe reseleccionar en cada toque una vez que el usuario ya ha escrito — revisar si aplica `selectAllOnFocus = false` como los demas campos de nombre libre.
- `FitTrackInputDialog` (`core/design/Dialogs.kt:98`): migrar de `String` a `TextFieldValue` internamente para que pueda ofrecer select-all donde tenga sentido (p.ej. reps personalizadas).

### R3 — Saneado de reps y peso

- Reps: rechazar o sanear negativos y sufijos invalidos (`-5`, `12x`) en el punto de entrada, no dejar que lleguen a BD como `0`.
- Peso: manejar notacion cientifica correctamente o impedir que se escriba (`1.0E7` no debe convertirse en `"1,07"`).

### R4 — Locale consistente

- Unificar la politica de separador decimal entre lectura y edicion en Historial (`WorkoutViewModel.kt:954-968` vs `HistoryViewModel.kt:652-654`).
- No fijar `Locale("es","ES")` a fuego si convive con `Locale.getDefault()`/`Locale.US` en la misma pantalla — decidir una politica de locale unica para todo formato de numeros/fechas visible al usuario (ligado a P6, pero el fix tecnico de raiz es de esta rama).

### R5 — Errores solo tras interaccion, foco en elemento nuevo

- No mostrar error en rojo antes de que el usuario haya tocado el campo (`RoutinesViewModel.kt:487`).
- Al añadir un ejercicio nuevo, llevar el foco/scroll al campo de nombre del ejercicio recien creado.

### R6 — Limites de longitud

- Añadir `maxLength` razonable a los campos de texto libre (nombre de rutina/dia/ejercicio/alternativa, notas).

### R7 — Pulsacion larga en steppers sin tope superior sensato

- `FitTrackTargetPrescriptionFields` (series): añadir pulsacion larga (step x5 o similar) para no depender de 17 toques para llegar de 3 a 20.
- Objetivo de pasos en Ajustes: añadir un tope superior sensato (p.ej. 50000) para evitar valores absurdos por error de toque.

### R8 — Boton atras y drawer en el lado correcto

- Historial: mover el boton atras a la izquierda (convencion Material), verificar que no queda tapado por la hamburguesa flotante del shell, y sustituir `Icons.Filled.ArrowBack` por `Icons.AutoMirrored.Filled.ArrowBack`.
- Drawer: verificar coherencia entre el lado de apertura y el lado del boton que lo abre; añadir `verticalScroll` al sheet.

### R9 — Doble toque en pestaña activa, y volver desde detalle

- Doble toque en la pestaña activa navega al tope de esa pestaña (patron estandar).
- Volver desde el detalle de Historial regresa a la lista, no sale de la app/pestaña.

### R10 — Ajustes deja de ser un callejon, y las filas del drawer dicen la verdad

- La fila "Widget & atajos" del drawer no puede decir "Proximamente" si el widget ya existe (contradice P1-8, ya resuelto en la rama anterior) — actualizar el copy o la navegacion segun corresponda.
- Evaluar si Ajustes necesita mas de una entrada, o si el callejon es aceptable por ahora — decision de producto, no solo tecnica.

### R11 — Onboarding con atras y forma de repetirlo

- Onboarding debe permitir retroceder entre sus pasos sin salir de la app.
- Exponer una via (en Ajustes) para volver a verlo, usando `setHasSeenOnboarding(false)` que ya existe pero no se llama desde ningun sitio.

### R12 — Confirmaciones que faltan, y borrar una sesion

- Confirmar antes de: eliminar nota, recuperar un entrenamiento (que muta el historial), cambiar de variante durante una sesion activa.
- Añadir una via de UI en Historial para borrar una sesion finalizada, reutilizando `discardSession` (ya existe en el repositorio) o el metodo que corresponda tras revisar si `discardSession` es semanticamente el correcto para un borrado explicito del usuario (hoy solo se usa para el caso "sesion vacia" de P0-1 — confirmar si hace falta un metodo distinto o el mismo sirve).

### R13 — Un solo enum de periodo, y filtro de Historial sin sorpresas

- Unificar los dos enums paralelos de filtro de periodo entre Historial y Datos en uno solo, con el mismo orden y el mismo valor por defecto.
- Historial no debe arrancar pre-filtrado por la rutina activa sin que el usuario lo haya pedido — o, si se mantiene por decision de producto, mostrarlo visiblemente en el propio filtro (no como un estado invisible).

### R14 — Estado que sobrevive a rotacion

- Añadir `SavedStateHandle` a `HistoryViewModel` y `StatsViewModel` para el mes visible y el dia de pasos seleccionado.
- `LaunchedEffect(initialTab)` en `FitTrackPlusNavHost.kt:34-38`/`WorkoutScreen.kt:119`: no debe re-ejecutar en cada recreacion de proceso si el usuario ya navego lejos del atajo que lo origino.

## Fuera de alcance

- P4, P5, P6 (visual, accesibilidad, idioma) — rama separada `refactor/design-system-round-3`.
- Cualquier cambio de dependencias — ya cerrado en `chore/dependency-upgrade`.

## Criterios de aceptacion

1. `test` y `build` en verde.
2. Los campos de nombre usan `KeyboardCapitalization` nativo, no logica manual.
3. Los 3 formularios señalados encadenan foco con Next/Done.
4. Reps y peso rechazan/sanean entradas invalidas antes de persistir.
5. Un solo enum de filtro de periodo compartido entre Historial y Datos.
6. `HistoryViewModel`/`StatsViewModel` sobreviven a una rotacion sin perder mes/dia seleccionado.
7. Boton atras de Historial a la izquierda, visible, no tapado por la hamburguesa.
8. Doble toque en pestaña activa vuelve al tope; volver desde detalle de Historial va a la lista.
9. Onboarding tiene atras y es repetible desde Ajustes.
10. Eliminar nota, recuperar entrenamiento y cambiar de variante piden confirmacion.
11. Existe una via de UI para borrar una sesion de Historial.
12. Pasada manual del dueño en emulador/dispositivo antes de mergear a `main`.
