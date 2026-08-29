# Spec: Corregir funciones que existen en la UI y no funcionan (P1)

**Fecha:** 2026-08-29
**Origen:** `docs/design/auditoria-ronda-3.md`, seccion P1.
**Plan de ejecucion:** `docs/superpowers/plans/2026-08-29-fix-broken-features.md`
**Prerequisito:** `chore/dependency-upgrade` (ya incluye `fix/data-loss` + Gradle 9.7.1/AGP 9.3.2/Kotlin 2.1.20/Compose BOM 2026.08.00) — parte desde ahi, no desde `develop`, para construir esta UI nueva ya sobre el stack actualizado.

---

## Por que

Estos 12 hallazgos no pierden datos, pero son **promesas rotas de la interfaz**: un control que existe, se puede tocar, parece que hace algo, y no hace nada (o hace lo contrario de lo que dice). Es el tipo de bug que mas erosiona la confianza en una app, porque el usuario no tiene forma de saber que esta roto sin leer el codigo. Verificado de forma independiente: **P1-1 y P1-3 confirmados** con coincidencia exacta.

## Estado actual verificado

### P1-1 — El selector kg/lb es decorativo
- `UserPreferencesRepository.kt:30`, `SettingsScreen.kt:116-135`: la preferencia existe, se guarda, tiene UI para cambiarla.
- **Confirmado**: cero referencias a `weightUnit` en `feature/workout`, `feature/history`, `feature/stats`. Todo pantalla escribe `"kg"` literal.

### P1-2 — Las notas no se pueden escribir o no se leen (tres casos distintos)
- **De sesion**: `finishWorkoutSession` se llama sin notas y el dialogo de finalizar no tiene campo para escribirlas, aunque Historial las pinta si existieran.
- **De serie**: no hay forma de escribirlas en la UI de Entrenar, aunque `FitTrackSetRow` ya sabe renderizarlas (recibe `notes` como parametro).
- **De ejercicio**: se escriben (hay campo y se guardan en el editor de rutina) pero no se leen en ningun sitio — `WorkoutExerciseEntity` no tiene columna para persistirlas en la sesion.

### P1-3 — El filtro de periodo de Datos solo afecta a 3 numeros
- `StatsViewModel.kt:62`: `observeWorkoutStats(period = WorkoutStatsPeriod.All)` — **hardcodeado**, ignora el `period` real del usuario.
- **Confirmado**: linea exacta.
- `StatsViewModel.kt:236-280`, `StatsScreen.kt:347`: graficas y marcas siguen mostrando todo el historico pase lo que pase en el filtro.

### P1-4 — Tarjetas de Datos mal etiquetadas
- `exerciseCount` suma entradas ejercicio-sesion, no ejercicios distintos (5 ejercicios × 4 sesiones = "20 EJERCICIOS").
- "PRs" cuenta ejercicios con marca historica, no records reales, e ignora el filtro de periodo.

### P1-5 — Temporizador de descanso con varios problemas
- Cuenta con contador (no reloj de pared) → deriva si la app pierde frames.
- Muere si se mata el proceso (no sobrevive a rotacion/backgrounding agresivo).
- Sin notificacion ni sonido — solo vibracion, y esa vibracion vive en un `LaunchedEffect` dentro de la tarjeta visible: si el usuario hace scroll y la tarjeta sale de pantalla, no vibra.
- El auto-start (activar temporizador solo al completar serie) no se persiste entre sesiones.

### P1-6 — Confeti encallado
- `ConfettiAnimation.kt:41-47`: `onFinished()` se cancela si el usuario cambia de pestaña mientras anima.
- `celebration` nunca se limpia en ese caso → la animacion de "nuevo PR" se repite indefinidamente en visitas futuras a la pantalla.

### P1-7 — Home deja de actualizarse tras un error
- `HomeViewModel.kt:71-73`: un `.catch` colocado sobre un `combine` termina el flujo upstream completo. Un error puntual (p.ej. fallo transitorio de lectura) deja Home congelada hasta reiniciar la app.

### P1-8 — Widget y atajos desconectados
- Cero llamadas a `updateAll` fuera del `updatePeriodMillis` de 30 minutos — el widget no se actualiza al cambiar datos, solo por temporizador.
- Sin `launchMode`/`onNewIntent` en el manifest: el extra `open_tab` de los atajos nunca se lee.

### P1-9 — Nada protege al salir de un entrenamiento activo
- Entrenar no registra `setNavigationBlocker` — se puede navegar fuera de una sesion activa sin aviso.
- El bloqueador (donde exista) tiene fuga: nada lo limpia al salir de composicion.

### P1-10 — "Exportar datos" es un snackbar de "proximamente"
- `NavigationShellConfig.kt:42-46`. Una app local-first sin forma de exportar/importar/borrarlo todo es un riesgo de producto, no solo una feature que falta.

### P1-11 — Desconectar Health Connect no revoca el permiso
- `SettingsViewModel.kt:100-105,43-44`: solo cambia un booleano local. El permiso real de Health Connect sigue concedido. Ademas los flags de disponibilidad son `val` evaluados una sola vez (no reaccionan a que el usuario instale/desinstale Health Connect despues de abrir la app).

### P1-12 — Codigo muerto
- `HeatmapCalendar.kt` (cero referencias — confirmado en la ronda 1, decision correcta de no fusionar, pero quedo huerfano), `FitTrackLoadingCard` (deprecado, sin llamadas), `selectExercise`/`withSelectedExercise`, dos politicas de auto-start solo usadas en tests, ~15 imports sin usar.

## Requisitos

### R1 — El selector de unidad de peso funciona de verdad

- Todas las pantallas que muestran o registran peso (Entrenar, Historial, Datos) deben leer `weightUnit` y convertir/etiquetar en consecuencia.
- Decidir y documentar: la conversion ¿se hace solo para mostrar (el dato se guarda siempre en kg) o se guarda en la unidad elegida? Recomendado: guardar siempre en kg internamente, convertir solo en la capa de presentacion — evita corromper datos historicos si el usuario cambia de unidad a mitad de uso.

### R2 — Notas: sesion, serie y ejercicio

- Sesion: añadir campo de notas al dialogo de finalizar entrenamiento, y pasarlo a `finishWorkoutSession`.
- Serie: añadir la entrada de notas en la UI de Entrenar (el campo ya existe a nivel de dato/render en `FitTrackSetRow`, falta el control para escribirlo).
- Ejercicio: añadir columna de notas a `WorkoutExerciseEntity` (nueva migracion de Room, aditiva) y leerla donde corresponda mostrarla.

### R3 — El filtro de periodo de Datos filtra de verdad

- `StatsViewModel.kt:62`: pasar el `period` real del usuario a `observeWorkoutStats`, no `WorkoutStatsPeriod.All`.
- Confirmar que graficas y marcas de PR respetan el mismo filtro.

### R4 — Etiquetas de Datos dicen lo que cuentan

- `exerciseCount`: contar ejercicios distintos, no entradas ejercicio-sesion (o renombrar la etiqueta si el numero actual es el que se quiere mantener por otro motivo — decidir cual).
- "PRs": contar records reales del periodo filtrado, no ejercicios-con-marca-historica ignorando el filtro.

### R5 — Temporizador de descanso fiable

- Cambiar el conteo a basarse en marca de tiempo real (reloj de pared), no en un contador de ticks.
- Persistir el estado del temporizador (tiempo restante, si esta corriendo) de forma que sobreviva a que el proceso muera y se restaure.
- Mover la vibracion fuera del `LaunchedEffect` atado a la visibilidad de la tarjeta, para que suene aunque el usuario haya hecho scroll.
- Persistir la preferencia de auto-start entre sesiones.
- Notificacion/sonido: alcance minimo aceptable para esta ronda si el trabajo completo es grande — documentar que queda para una rama posterior si se recorta.

### R6 — Confeti no se queda encallado

- Asegurar que `celebration` se limpia aunque el usuario cambie de pestaña antes de que `onFinished()` se dispare (usar un mecanismo que no dependa de que la composicion siga viva, o limpiar el estado al entrar de nuevo a la pantalla).

### R7 — Home se recupera de un error

- Mover el `.catch` para que no termine el `combine` upstream — que un fallo puntual en una fuente no deje Home congelada permanentemente.

### R8 — Widget y atajos funcionan

- Llamar a `updateAll` cuando cambien los datos relevantes (no solo depender del periodo de 30 min).
- Añadir `launchMode`/manejo de `onNewIntent` para que el extra `open_tab` de los atajos funcione.

### R9 — Proteccion al salir de un entrenamiento activo

- Registrar `setNavigationBlocker` en Entrenar mientras haya una sesion activa.
- Limpiar el bloqueador al salir de composicion (evitar la fuga descrita).

### R10 — Exportar datos deja de ser "proximamente"

- Alcance minimo para esta ronda: exportar el historial (rutinas + sesiones) a un formato simple (JSON o CSV) que el usuario pueda guardar/compartir. Importar y "borrar todo" pueden quedar para otra ronda si el alcance crece demasiado — decidir y documentar.

### R11 — Desconectar Health Connect revoca el permiso real

- Al desconectar, revocar el permiso de Health Connect, no solo cambiar el booleano local.
- Los flags de disponibilidad deben reevaluarse (no fijarse una sola vez con `val`) por si el estado de Health Connect cambia mientras la app esta abierta.

### R12 — Limpieza de codigo muerto

- Eliminar: `FitTrackLoadingCard` (deprecado sin llamadas), `selectExercise`/`withSelectedExercise`, las 2 politicas de auto-start solo usadas en tests (o los tests, si la politica no tiene sentido de producto), ~15 imports sin usar.
- `HeatmapCalendar.kt`: dado que la ronda 1 decidio explicitamente no fusionarlo con el calendario de Datos, decidir entre (a) eliminarlo si no hay plan de usarlo, o (b) dejar una nota explicita en el fichero de por que existe sin consumidores. No dejarlo huerfano en silencio.

## Fuera de alcance

- P0 (rama separada, ya en curso).
- P2 en adelante (texto, navegacion, visual, accesibilidad, idioma) — ramas separadas.
- Migracion de dependencias.

## Criterios de aceptacion

1. `test` y `build` en verde.
2. Cambiar la unidad a "lb" en Ajustes cambia lo que se muestra en Entrenar/Historial/Datos.
3. Se pueden escribir y leer notas de sesion, de serie y de ejercicio.
4. Cambiar el filtro de periodo en Datos cambia las graficas y marcas, no solo los 3 numeros superiores.
5. Las etiquetas de Datos reflejan lo que realmente cuentan.
6. El temporizador de descanso no se desincroniza tras hacer scroll ni se pierde si Android mata el proceso.
7. El confeti de PR no se repite en visitas posteriores a la pantalla.
8. Provocar un error transitorio en la fuente de datos de Home no la deja congelada.
9. El widget se actualiza al cambiar datos relevantes; los atajos abren la pestaña correcta.
10. Salir de una sesion activa sin confirmar pide confirmacion.
11. Existe una via para exportar los datos del usuario.
12. Desconectar Health Connect revoca el permiso real, verificable en los ajustes del sistema.
13. Pasada manual del dueño en emulador/dispositivo antes de mergear.
