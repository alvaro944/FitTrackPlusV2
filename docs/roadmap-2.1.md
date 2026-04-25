# FitTrackPlus Roadmap 2.1

Este documento sustituye a `docs/future-improvements.md` como roadmap oficial de mejoras post-v1.

La primera version funcional queda **cerrada tecnicamente**: el nucleo local, las pantallas principales, el sistema visual, la intro, CI y la documentacion base existen y han pasado verificacion automatica cuando aplicaba. La validacion manual en movil/emulador sigue pendiente y no debe documentarse como completada hasta ejecutarla.

Fuente de entrada:

- Auditoria externa: `C:\Users\Alvaro\.claude\plans\y-en-otro-lado-cozy-gosling.md`
- Backlog historico: `docs/mejoras-claude.md`
- Estado vivo: `docs/project-progress.md`

## Principios De Ejecucion

- Trabajar en fases pequenas y verificables.
- No mezclar fixes criticos con features grandes.
- Mantener Firebase/sync fuera del camino hasta cerrar V2.1 local.
- No romper el invariante de historial por snapshots.
- Cada fase de codigo debe cerrar con `.\gradlew.bat test` y `.\gradlew.bat build`.
- Cada cambio visible debe tener validacion manual o quedar anotado como pendiente.
- Al cerrar una fase, actualizar `docs/project-progress.md` y `docs/phase-log.md`.

## Gate 0 - Cierre Tecnico Y Validacion Manual

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
- Registrar resultado en `docs/project-progress.md` y `docs/phase-log.md`.

## Fase 2.1A - Estabilidad Y Fricciones Criticas

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

Objetivo:

- Subir la utilidad real de la app sin adelantar sync ni rehacer la arquitectura.

Mejoras prioritarias:

| Mejora | Objetivo | Alcance |
|--------|----------|---------|
| Plantillas de rutina | Reducir friccion del primer uso | PPL, Upper/Lower y Full Body desde estado vacio o selector simple |
| Duplicar dias/ejercicios | Ahorrar entrada repetitiva | Acciones de duplicado en editor sin migracion DB |
| Reordenar dias/ejercicios | Permitir corregir orden sin borrar | Botones subir/bajar primero; drag and drop solo si merece la pena |
| Timer de descanso | Cubrir feature core de gimnasio | Countdown local, haptic y ajuste simple de duracion |
| Historial con filtros | Hacer util el historial largo | Rango de fechas y orden reciente/antiguo/volumen |
| Stats con tooltip y periodos | Convertir grafica en consultable | Tooltip por punto y chips 4w/12w/all |
| Comparativa de sesion | Dar contexto al progreso | Delta vs sesion anterior del mismo dia o rutina |
| Selector de tema | Control visual basico | System/light/dark persistido en DataStore |

Criterio de salida:

- Cada feature se implementa como fase independiente o subfase clara.
- No se introducen cambios de schema salvo decision explicita.
- Tests cubren reglas nuevas.
- Verificacion manual cubre la pantalla afectada.

## Fase 2.1C - Portfolio WOW

Objetivo:

- Anadir piezas visuales y de producto que demuestren calidad sin comprometer mantenibilidad.

Mejoras candidatas:

| Mejora | Objetivo | Nota |
|--------|----------|------|
| Heatmap calendario | Mostrar constancia y volumen de entreno | Alto valor visual; requiere Canvas/data viz |
| Achievements | Dar sensacion de progreso | Mantener reglas simples y locales |
| PR en vivo | Celebrar records durante entrenamiento | Comparar contra mejor previo por ejercicio |
| Celebracion al finalizar | Reforzar hitos | Confetti/haptic solo si hay PR o hito |
| Skeleton loaders | Mejorar performance percibida | Sustituir loading generico pantalla a pantalla |
| Onboarding/demo data | Facilitar review de portfolio | Demo opcional, nunca en release real sin confirmacion |
| App shortcuts/widget | Profundizar integracion Android | Solo despues de estabilizar flujo principal |

Criterio de salida:

- Seleccionar maximo 2-3 piezas por ciclo para evitar feature creep.
- Cada pieza debe poder demostrarse en portfolio con datos reales o demo controlada.
- No debe romper la sencillez local-first del proyecto.

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

- [ ] Leer `README.md`, `docs/project-plan.md`, `docs/project-progress.md`, `docs/phase-log.md` y este roadmap.
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
- [ ] Actualizar `docs/project-progress.md`.
- [ ] Actualizar `docs/phase-log.md`.
- [ ] Actualizar `docs/work-methodology/` solo si aparece un aprendizaje especifico del repo.
- [ ] Actualizar `docs/project-methodology/` solo si aparece una regla general reusable.
