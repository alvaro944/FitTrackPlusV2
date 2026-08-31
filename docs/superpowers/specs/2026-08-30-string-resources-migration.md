# Spec: Migrar literales de texto a stringResource (P6-1)

**Fecha:** 2026-08-30
**Origen:** `docs/design/auditoria-ronda-3.md`, seccion P6, item P6-1.
**Plan de ejecucion:** `docs/superpowers/plans/2026-08-30-string-resources-migration.md`
**Prerequisito — CUMPLIDO (2026-08-31):** `refactor/home-stats-visual-locale` y `fix/shared-accessibility-and-copy` estan ambas fusionadas en `develop` y sus ramas borradas. Esta spec queda desbloqueada. Se mantiene la nota original porque explica por que era secuencial: Es un cambio global (toca practicamente cada pantalla), y ejecutarlo en paralelo con esas dos ramas activas garantiza conflictos de merge en casi todos los ficheros. Es deliberadamente secuencial, no paralelo.

---

## Por que

Toda la app tiene el texto visible como literales Kotlin — `strings.xml` solo tiene 6 entradas. La app declara `supportsRtl="true"` en el manifest sin que eso sirva de nada, porque no hay ninguna capa de recursos que un sistema de localizacion o herramientas de accesibilidad/QA puedan enganchar. No es una tarea urgente (la app funciona igual para el usuario actual, todo en español), pero es la puerta de salida para cualquier cosa futura: i18n real, QA de copy centralizado, o simplemente encontrar y cambiar un texto sin grep-ear 20 ficheros.

Verificado de forma independiente antes de escribir esta spec: confirmado por grep, cero apariciones de `stringResource` o `pluralStringResource` en `app/src/main/kotlin`, y `strings.xml` con 6 entradas (`app_name` y poco mas).

## Alcance y decision de diseno

- **No es una migracion a i18n multi-idioma.** El objetivo es sacar los literales a `strings.xml` mantenienendo español como unico idioma — no se crean `values-en/`, `values-fr/`, etc. Eso seria una fase completamente distinta y no la pide esta spec.
- **Los plurales usan `<plurals>` + `pluralStringResource`**, aprovechando que P6-3 (rama `refactor/home-stats-visual-locale`) ya limpio la logica de plural manual en Home — esta migracion la traslada a recursos en vez de reintroducir logica manual.
- **Los textos con placeholders** (ej. `"${sessionsThisWeek} sesiones esta semana"`) usan `stringResource(R.string.x, arg1, arg2)` con `%1$d`, `%1$s` etc. en el XML.
- **No tocar** contenido que no es UI-facing: nombres de rutas internas, logs, mensajes de excepcion no mostrados al usuario, nombres de tablas/columnas Room.

## Estrategia de ejecucion (fases por paquete, no un solo commit)

Dado el tamaño ("global"), se ejecuta en fases por paquete de feature, cada una su propio commit verificado con `test` + `build`, en el orden siguiente (de menor a mayor riesgo de regresion):

1. `core/design` (componentes compartidos: dialogos, botones, labels genericos) — el que mas se reutiliza, conviene dejarlo primero y estable.
2. `feature/onboarding`, `feature/launch` — pantallas pequeñas y aisladas.
3. `feature/settings` — pantalla de tamaño medio, ya tocada por P4/P5, buen punto intermedio.
4. `feature/home`
5. `feature/routines`
6. `feature/workout`
7. `feature/history`
8. `feature/stats`
9. `FitTrackPlusWidget.kt` y cualquier resto (notificaciones, deep links)

Cada fase: extraer literales de ese paquete a `strings.xml` con nombres agrupados por prefijo (ej. `home_greeting`, `home_week_activity_title`, `routines_error_name_blank`), sustituir en el Composable, `test` + `build` en verde, commit.

## Requisitos

### R1 — Inventario de literales

- Antes de tocar codigo, generar un listado (puede ser un fichero de trabajo temporal, no necesita commitearse) de todos los `Text(` con literal directo, `contentDescription = "literal"`, y strings usados en `Toast`/`Snackbar`, agrupados por fichero.

### R2 — Extraer a `strings.xml` por fases

- Seguir el orden de la seccion "Estrategia de ejecucion".
- Nombres de recursos en `snake_case`, prefijados por pantalla/componente (evitar colisiones y hacer grep-able el origen).
- Placeholders con `%1$d`/`%1$s` documentados con un comentario XML si el orden de argumentos no es obvio.

### R3 — Plurales via recursos

- Los casos ya identificados en P6-3 (sesiones, dias, y cualquier otro que aparezca durante el inventario) usan `<plurals>` + `pluralStringResource`, no logica manual `if (n == 1)`.

### R4 — Verificacion de que no se rompe nada visible

- Tras cada fase, revisar (aunque sea leyendo el diff con atencion) que ningun texto cambio de contenido al moverse a XML — esto es un refactor puro, no una reescritura de copy.

## Criterios de aceptacion

1. `test` y `build` en verde en cada fase.
2. Cero literales de texto visible al usuario quedan como `String` Kotlin directo en los paquetes migrados (verificable por grep de `Text\(\s*"` tras cada fase).
3. Los plurales usan `<plurals>`, no logica manual.
4. `strings.xml` organizado con nombres agrupados por pantalla, sin colisiones.
5. Ningun texto cambia de contenido visible respecto a antes de la migracion (es un refactor, no un rediseño de copy).
6. Se ejecuta en fases con un commit por paquete, no un commit monolitico.
7. Arranca solo despues de que `refactor/home-stats-visual-locale` y `fix/shared-accessibility-and-copy` esten fusionadas en `develop`.
