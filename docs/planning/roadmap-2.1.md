# FitTrackPlus Roadmap 2.1

Este documento sustituye a `docs/planning/roadmap-2.1.md` como roadmap oficial de mejoras post-v1.

La primera version funcional queda **cerrada tecnicamente**: el nucleo local, las pantallas principales, el sistema visual, la intro, CI y la documentacion base existen y han pasado verificacion automatica cuando aplicaba. La validacion manual en movil/emulador sigue pendiente y no debe documentarse como completada hasta ejecutarla.

Fuente de entrada:

- Auditoria externa: `C:\Users\Alvaro\.claude\plans\y-en-otro-lado-cozy-gosling.md`
- Backlog historico: `docs/design/mejoras-claude.md`
- Estado vivo: `docs/progress/project-progress.md`

## Snapshot De Avance (2026-04-26)

| Fase | Estado | Commits / Rama |
|------|--------|----------------|
| Gate 0 | Cerrado (fixes menores integrados por Claude) | `docs/coordination/claude-gate0-minor-fixes.md` |
| Fase 2.1A | Cerrada | `1595b2a Complete roadmap 2.1A stability` |
| Fase 2.1B.1 Rutinas (plantillas, duplicar, reordenar) | Cerrada | `309dcd4 Complete phase 2.1B.1 routines` |
| Fase 2.1B.2 Timer de descanso | Cerrada | `be0d564 Complete phase 2.1B.2 rest timer` |
| Fase 2.1B.3 Historial con filtros y orden | Cerrada | `5f1b51e Add history filters and sorting` |
| Fase 2.1B.4 Datos con periodos y tooltip | Cerrada | `d98cf02 Add stats periods and chart tooltip` |
| Fase 2.1B.5 Comparativa de sesion + selector de tema | Implementada y verificada en automatico, **commit pendiente** | rama `codex/phase-2.1b-comparison-theme` (WIP no commiteado) |
| Validacion manual conjunta 2.1B.1 a 2.1B.5 | Pendiente | en dispositivo/emulador |
| Fase 2.1C Portfolio WOW | No iniciada | a planificar tras cerrar 2.1B |

Decisiones inmediatas tras este pase:

1. Commitear el WIP de 2.1B.5 para dejar la rama publicable.
2. Ejecutar la validacion manual conjunta de todas las subfases 2.1B en dispositivo.
3. Solo entonces, abrir Fase 2.1C eligiendo 2-3 piezas concretas (ver seccion 2.1C).

## Principios De Ejecucion

- Trabajar en fases pequenas y verificables.
- No mezclar fixes criticos con features grandes.
- Mantener Firebase/sync fuera del camino hasta cerrar V2.1 local.
- No romper el invariante de historial por snapshots.
- Cada fase de codigo debe cerrar con `.\gradlew.bat test` y `.\gradlew.bat build`.
- Cada cambio visible debe tener validacion manual o quedar anotado como pendiente.
- Al cerrar una fase, actualizar `docs/progress/project-progress.md` y `docs/progress/phase-log.md`.

## Gate 0 - Cierre Tecnico Y Validacion Manual

Estado: **CERRADO**. Validado en telefono fisico para flujo principal, dark mode, navegacion, Historial, Datos y snapshots. Los 4 issues menores detectados quedaron resueltos por Claude (`docs/coordination/claude-gate0-minor-fixes.md`).

Objetivo:

- Confirmar en dispositivo/emulador que la v1 tecnica funciona como experiencia real.

Alcance:

- Revisar intro de arranque clara.
- Revisar navegacion inferior, transiciones entre tabs y animacion de Historial.
- Revisar dark mode y legibilidad general.
- Revisar flujo principal:
  - crear rutina
  - activar rutina
  - iniciar entrenamiento
  - registrar series
  - finalizar entrenamiento
  - abrir Historial
  - revisar Datos
- Revisar accesibilidad basica con TalkBack cuando sea posible.

Criterio de salida:

- Si aparecen problemas reales, abrir fase corta de ajuste visual/UX.
- Si no aparecen problemas, pasar a Fase 2.1A.
- No marcar validacion manual como hecha hasta ejecutarla.

Verificacion:

- Manual en Android Studio, emulador o dispositivo fisico.
- Registrar resultado en `docs/progress/project-progress.md` y `docs/progress/phase-log.md`.

## Fase 2.1A - Estabilidad Y Fricciones Criticas

Estado: **CERRADA** en commit `1595b2a`. Las 7 mejoras de la tabla quedan implementadas. Validacion manual conjunta sigue pendiente y se hace en bloque con 2.1B.

Objetivo:

- Corregir fricciones que un usuario nuevo o un revisor de portfolio notaria en la experiencia basica.

Mejoras:

| Mejora | Objetivo | Alcance | Verificacion |
|--------|----------|---------|--------------|
| Home muestra errores | No silenciar `state.message` | Renderizar snackbar o card de error en Home | Forzar error controlado o revisar estado con fake/test si aplica |
| Workout bloquea inputs invalidos | Evitar peso negativo y reps no validas | Filtrar peso a decimal positivo y reps a entero positivo | Unit test de normalizacion + prueba manual en sesion |
| Rutinas valida inline | Explicar por que no se puede guardar | `isError`, mensajes inline y touched state en campos requeridos | Prueba manual de editor vacio/parcial |
| Reps objetivo con regex | Evitar strings absurdos | Aceptar formatos tipo `8`, `8-12`, `AMRAP`, `RPE 8` | Unit test o prueba manual del dialog |
| Historial muestra detalle completo | Aprovechar datos ya existentes | Pintar notas, volumen total, duracion y mejor set si existe | Prueba manual con sesion finalizada |
| Back en detalle de Historial | Evitar que back cierre la pantalla | `BackHandler` para volver al listado | Prueba manual con boton back |
| Settings confirma cambios | Dar feedback al cambiar unidad | Snackbar/toast al guardar kg/lb | Prueba manual de selector |

Criterio de salida:

- Todas las mejoras criticas estan implementadas o justificadamente descartadas.
- `test` y `build` pasan.
- Las pantallas tocadas se revisan manualmente.
- Docs de progreso y bitacora quedan actualizadas.

## Fase 2.1B - Features De Valor

Estado: **TECNICAMENTE COMPLETA**. Las 8 mejoras quedan implementadas y verificadas en automatico, repartidas en 5 subfases. La unica accion pendiente es commitear 2.1B.5 y correr la validacion manual conjunta en dispositivo.

Objetivo:

- Subir la utilidad real de la app sin adelantar sync ni rehacer la arquitectura.

Mejoras prioritarias:

| Mejora | Objetivo | Alcance | Estado |
|--------|----------|---------|--------|
| Plantillas de rutina | Reducir friccion del primer uso | PPL, Upper/Lower y Full Body desde estado vacio o selector simple | OK 2.1B.1 (`309dcd4`) |
| Duplicar dias/ejercicios | Ahorrar entrada repetitiva | Acciones de duplicado en editor sin migracion DB | OK 2.1B.1 (`309dcd4`) |
| Reordenar dias/ejercicios | Permitir corregir orden sin borrar | Botones subir/bajar primero; drag and drop solo si merece la pena | OK 2.1B.1 (`309dcd4`) |
| Timer de descanso | Cubrir feature core de gimnasio | Countdown local, haptic y ajuste simple de duracion | OK 2.1B.2 (`be0d564`) |
| Historial con filtros | Hacer util el historial largo | Rango de fechas y orden reciente/antiguo/volumen | OK 2.1B.3 (`5f1b51e`) |
| Stats con tooltip y periodos | Convertir grafica en consultable | Tooltip por punto y chips 4w/12w/all | OK 2.1B.4 (`d98cf02`) |
| Comparativa de sesion | Dar contexto al progreso | Delta vs sesion anterior del mismo dia o rutina | OK 2.1B.5 (WIP rama actual) |
| Selector de tema | Control visual basico | System/light/dark persistido en DataStore | OK 2.1B.5 (WIP rama actual) |

Criterio de salida:

- Cada feature se implementa como fase independiente o subfase clara.
- No se introducen cambios de schema salvo decision explicita.
- Tests cubren reglas nuevas.
- Verificacion manual cubre la pantalla afectada.

Pendiente para cerrar 2.1B oficialmente:

- Commit del WIP de 2.1B.5 (comparativa + selector de tema).
- Validacion manual conjunta 2.1B.1 a 2.1B.5 en dispositivo: plantillas, duplicar, reordenar, timer, filtros historial, periodos stats, comparativa de sesion, cambio de tema en caliente.
- Anotar resultado en `docs/progress/project-progress.md` y `docs/progress/phase-log.md`.

## Fase 2.1C - Portfolio WOW

Estado: **TECNICAMENTE COMPLETA** (2026-04-28). Los tres sprints estan implementados y verificados con `test` + `build`. Validacion manual en dispositivo pendiente para cierre oficial.

| Sprint | Rama | Commit | Estado |
|--------|------|--------|--------|
| 2.1C.A — Heatmap + PR en vivo + Celebracion | `codex/phase-2.1c-a-heatmap-pr-celebration` | implementado | test+build OK, validacion manual pendiente |
| 2.1C.B — Skeletons + Demo data + Onboarding | `codex/phase-2.1c-b-skeletons-demo-onboarding` | implementado | test+build OK, validacion manual pendiente |
| 2.1C.C — Shortcuts + Widget + Notificacion | `codex/phase-2.1c-c-shortcuts-widget-notification` | `ee7649b` | test+build OK, validacion manual pendiente |

Objetivo:

- Anadir piezas visuales y de producto que demuestren calidad sin comprometer mantenibilidad.

Piezas implementadas:

| Mejora | Sprint | Estado |
|--------|--------|--------|
| Heatmap calendario (Canvas custom 52x7, escala por volumen) | A | OK |
| PR en vivo en WorkoutScreen (badge + haptic doble) | A | OK |
| Celebracion al finalizar sesion con PR (confetti Canvas + overlay) | A | OK |
| Skeleton loaders con shimmer en las 5 pantallas principales | B | OK |
| Demo data on demand en Ajustes (solo debug) | B | OK |
| Onboarding minimo de 3 paginas con HorizontalPager | B | OK |
| App shortcuts (long-press launcher: "Entrenar" y "Stats") | C | OK |
| Widget homescreen 2x1 (racha + sesiones de la semana) | C | OK |
| Notificacion silenciosa persistente mientras hay sesion activa | C | OK |

Criterio de salida pendiente:

- Validacion manual en dispositivo de los 3 sprints (prevista 2026-04-29).
- Merge de las 3 ramas al trunk o PR final.
- Actualizar este documento marcando cierre oficial.

## Diferido

Estas ideas quedan fuera del roadmap inmediato:

- Firebase/Auth/Firestore o sync cloud.
- ExerciseCatalog global con FTS.
- Supersets, dropsets, cardio avanzado, RPE/RIR.
- Import/export avanzado si no entra como fase propia validada.
- i18n completa con `strings.xml` y `values-en`.
- Foto de progreso.
- GPS, HIIT/Tabata y actividades fuera de gimnasio.

## Checklist Por Fase

Antes de empezar:

- [ ] Leer `README.md`, `docs/planning/project-plan.md`, `docs/progress/project-progress.md`, `docs/progress/phase-log.md` y este roadmap.
- [ ] Confirmar alcance exacto de la fase.
- [ ] Crear o usar rama `codex/...` adecuada.
- [ ] Revisar el area concreta que se va a tocar.

Durante la fase:

- [ ] Mantener Compose sin logica de negocio.
- [ ] Mantener ViewModels como coordinadores de estado/eventos.
- [ ] Mantener repositorios ocultando Room/DataStore.
- [ ] No tocar Firebase/sync salvo fase dedicada.
- [ ] No mezclar refactors transversales con features si no son necesarios.

Antes de cerrar:

- [ ] Ejecutar `.\gradlew.bat test` si hay codigo.
- [ ] Ejecutar `.\gradlew.bat build` si hay codigo.
- [ ] Hacer validacion manual si hay UI o flujo visible.
- [ ] Actualizar `docs/progress/project-progress.md`.
- [ ] Actualizar `docs/progress/phase-log.md`.
- [ ] Actualizar `docs/methodology/work-methodology/` solo si aparece un aprendizaje especifico del repo.
- [ ] Actualizar `docs/methodology/project-methodology/` solo si aparece una regla general reusable.
