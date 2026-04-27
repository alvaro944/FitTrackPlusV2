# FitTrackPlus v2 Progress

Este documento resume donde estamos, que se ha hecho y cual es el siguiente paso.

## Estado Actual

- Rama actual: `codex/phase-2.1b-comparison-theme`.
- Commit inicial local: `c1b2f31 Initialize FitTrackPlus v2 mobile foundation`.
- Commit de cierre de Fase 1: `9df5a44 Complete phase 1 routines`.
- Commit de cierre de Fase 2: `7cf2c02 Complete phase 2 workout logging`.
- Commit de cierre de Fase 3: `5e84fef Complete phase 3 history`.
- Commit de cierre de Fase 4: `Complete phase 4 statistics MVP`.
- Commit de cierre de Fase 5: `Complete phase 5 UX polish`.
- Remoto configurado: `https://github.com/alvaro944/FitTrackPlusV2.git`.
- Rama local alineada con `origin/codex/v2-mejoras` al iniciar Bloque 7; este pass anade solo CI + docs y queda pendiente de commit/push.
- Primera version funcional completa cerrada tecnicamente.
- Roadmap post-v1 canonico creado en `docs/roadmap-2.1.md`; sustituye a `docs/future-improvements.md`.
- Fase 6 cerrada tecnicamente.
- Bloque 6 de polish visual y accesibilidad cerrado tecnicamente y verificado con `test` + `build`.
- Bloque 7 CI implementado en `.github/workflows/ci.yml` para `push` en `main` y `codex/**`, y para `pull_request`.
- Gate 0 del Roadmap 2.1 ejecutado y validado en telefono fisico para flujo principal, dark mode, navegacion, Historial, Datos y snapshots.
- Gate 0 detecto ajustes acotados:
  - back fisico desde detalle de Historial volvia a Inicio en vez de al listado.
  - acciones de rutinas inactivas partian texto en botones (`Activar`, `Editar`, `Archivar`) en el ancho real del telefono.
  - dialogo de finalizar entrenamiento mostraba artefactos claros en dark mode.
- Ajustes pequenos aplicados en codigo para esos hallazgos; tras parar daemons Gradle/Kotlin colgados, `test` y `build` pasaron.
- Revalidacion manual del usuario:
  - intro/arranque OK
  - Rutinas OK
  - Historial OK
  - Datos OK
  - snapshots historicos OK
  - Entrenar queda con issues menores: inputs peso/reps no reemplazan `0`, bordes blancos en campos enfocados y dialogo de finalizar, preview no refresca nombre de rutina activa hasta cambiar seleccion activa
- Issues menores de Gate 0 delegados a Claude en `docs/coordination/claude-gate0-minor-fixes.md`.
- Branding cerrado: docs de marca, logo decidido, app icon generado con fondo crema.
- Bloque 3 UX implementado en local: Ajustes, Inicio dinamico, feedback haptico, editor protegido y archivadas.
- Trabajo visual actual: validar intro de arranque clara en Compose basada en `docs/branding/Pantalla incio fondo claro/`.
- Starter pack metodologico reusable consolidado y afinado en `docs/project-methodology/`.
- Tercera pasada metodologica aplicada: ownership multiagente, handoff Codex/Claude, modos ligero/fase y derivacion de `AGENTS.md` quedan mas operativos.
- Kickoff metodologico documentado: la metodologia se usa para configurar el proyecto y `AGENTS.md` queda como referencia operativa diaria.
- Colaboracion entre plataformas formalizada: Codex, Claude, herramientas visuales y revisores tienen roles, ownership y handoff definidos.
- Fichero compartido de coordinacion definido como pieza reusable para que varias plataformas se comuniquen dentro del repo.
- Fase 2.1A implementada en workspace:
  - Home muestra mensajes de error mediante snackbar.
  - Entrenar conserva los fixes menores de Claude y mantiene normalizacion de peso/reps en dominio.
  - Rutinas valida inline campos requeridos y reps objetivo razonables (`8`, `8-12`, `AMRAP`, `RPE 8`).
  - Historial muestra notas, duracion, volumen total y mejor set si existen.
  - Settings confirma cambios de unidad `kg/lb` mediante snackbar.
- Fase 2.1A verificada con `test` y `build`; antes hizo falta regenerar `:app:compileDebugKotlin --rerun-tasks` por un `R.jar` local inconsistente.
- Fase 2.1B.1 implementada y verificada en rama `codex/phase-2.1b-routines`:
  - plantillas locales de rutina PPL, Upper/Lower y Full Body
  - duplicado de dias y ejercicios en el editor
  - reordenado con botones subir/bajar en dias y ejercicios
  - sin cambios de schema, Firebase/sync ni snapshots historicos
- Fase 2.1B.1 verificada con `test` y `build`; antes hizo falta parar procesos Java/Kotlin que bloqueaban salidas generadas por KSP en Windows.
- Handoff de revision acotada para Claude creado en `docs/coordination/claude-phase-2.1b-routines-review.md`; por decision posterior del usuario, Codex hizo esa revision interna y no se envian mas tareas a Claude por ahora.
- Fase 2.1B.2 implementada y verificada en rama `codex/phase-2.1b-timer`:
  - timer local de descanso en Entrenar
  - controles 60s/90s/120s
  - pausa, reanudar, reiniciar y cancelar
  - auto-arranque opcional al completar una serie
  - sin Room, DataStore, Firebase/sync ni snapshots historicos
- Fase 2.1B.2 verificada con `test` y `build`.
- Fase 2.1B.3 implementada y verificada en rama `codex/phase-2.1b-history-filters`:
  - filtros de periodo en Historial: Todo, 4 semanas, 12 semanas
  - orden del listado: Reciente, Antiguo, Mayor volumen
  - resumen enriquecido con volumen, duracion y numero de series desde snapshots
  - sin cambios de schema, Firebase/sync ni snapshots historicos
- Fase 2.1B.3 verificada con `test` y `build`.
- Fase 2.1B.4 implementada y verificada en rama `codex/phase-2.1b-stats-periods`:
  - periodos en Datos: Todo, 4 semanas, 12 semanas
  - tooltip de punto en grafica de progreso
  - resumen, volumen, progreso y records recalculados por periodo
  - sin cambios de schema, Firebase/sync ni snapshots historicos
- Fase 2.1B.4 verificada con `test` y `build`.
- Fase 2.1B.5 implementada en rama `codex/phase-2.1b-comparison-theme`:
  - detalle de Historial muestra comparativa contra la sesion anterior comparable por snapshot de rutina y dia
  - Ajustes gana selector de tema `Sistema`, `Claro`, `Oscuro`
  - el tema se persiste en DataStore y se aplica desde el root de la app
  - sin cambios de schema Room, Firebase/sync ni snapshots historicos
- Fase 2.1B.5 verificada con `test` y `build`.
- Backlog visual separado creado en `docs/visual-improvements.md`.
- Siguiente fase funcional de producto: elegir la siguiente subfase de 2.1B.
- Migracion GPT-5.5 aplicada a agentes/docs: no hay integracion runtime OpenAI en la app, asi que no habia modelo de API que cambiar.

## Hecho Hasta Ahora

### Planificacion

- Se decidio rehacer FitTrackPlus v2 desde cero como Android nativo.
- Se eligio Kotlin + Jetpack Compose.
- Se decidio enfoque local-first.
- Firebase queda fuera del MVP inicial.
- Se adapto una metodologia de trabajo por fases.

### Fase 0 - Mobile foundation

Implementado:

- Proyecto Android con Compose habilitado.
- `MainActivity` nueva con `ComponentActivity`.
- `FitTrackPlusApp` con Hilt.
- `Navigation Compose` con tabs iniciales:
  - Inicio
  - Rutinas
  - Entrenar
  - Historial
  - Datos
- Tema Material 3 inicial.
- Room configurado con schema exportado.
- DataStore preparado para preferencias.
- Hilt configurado para base de datos y repositorios.
- KSP configurado para Room/Hilt.
- Repositorios base:
  - `RoutineRepository`
  - `WorkoutRepository`
  - `UserPreferencesRepository`
- Casos de uso iniciales:
  - `GetNextRoutineDayUseCase`
  - `StartWorkoutSessionUseCase`
  - `FinishWorkoutSessionUseCase`
- Test unitario del ciclo de dias.

### Fase 1 - Rutinas

Implementado:

- Pantalla real de rutinas en Compose.
- `RoutinesViewModel` con `UiState` expuesto por `StateFlow`.
- Listado reactivo de rutinas desde Room mediante `RoutineRepository`.
- Creacion de rutinas con dias y ejercicios.
- Edicion de rutinas reemplazando dias y ejercicios.
- Archivado de rutinas.
- Seleccion de rutina activa con DataStore.
- Limpieza de rutina activa cuando se archiva.
- Validacion basica antes de guardar.
- `.kotlin/` agregado a `.gitignore` como salida local de Gradle/Kotlin.
- Guia de metodologia creada en `docs/work-methodology/`.

### Fase 2 - Registro de entrenamiento

Implementado:

- Pantalla real de Entrenar en Compose.
- `WorkoutViewModel` con `UiState` y `StateFlow`.
- Deteccion de rutina activa desde DataStore.
- Preview del siguiente dia de la rutina activa.
- Inicio de entrenamiento desde la rutina activa.
- Reanudacion de una sesion abierta sin crear duplicados.
- Creacion de sesion historica desde snapshot de rutina, dia, ejercicios y reps objetivo.
- Registro editable de peso en kg y repeticiones por serie.
- Persistencia de cada serie en Room.
- Finalizacion de sesion aunque queden series parciales.
- Ciclo de dias basado solo en sesiones finalizadas.
- Casos de uso nuevos:
  - `GetNextWorkoutPreviewUseCase`
  - `UpdateWorkoutSetUseCase`
- Tests unitarios para ciclo de inicio, reanudacion de sesion abierta y normalizacion de series.

### Fase 3 - Historial

Implementado:

- Rama local `codex/phase-3-history`.
- Consultas Room para observar solo sesiones finalizadas.
- Carga de detalle historico solo para sesiones finalizadas.
- Modelos de dominio para resumen y detalle de historial.
- Casos de uso:
  - `ObserveWorkoutHistoryUseCase`
  - `GetWorkoutHistoryDetailUseCase`
- `HistoryViewModel` con `UiState`, lista de sesiones, seleccion y detalle.
- Pantalla Compose minima de Historial:
  - listado de sesiones finalizadas
  - detalle de ejercicios y series
  - vuelta simple al listado
- Sembrado automatico de datos demo solo en builds debug cuando la base esta vacia.
- Datos demo Push/Pull/Legs con varias sesiones finalizadas y snapshots historicos.
- Tests unitarios para listado, detalle, orden y lectura desde snapshots.

### Fase 4 - Estadisticas MVP

Implementado:

- Rama local `codex/phase-4-statistics-mvp`.
- Lectura reactiva de sesiones finalizadas con ejercicios y series historicas.
- Modelos de dominio para estadisticas de entrenamiento.
- `ObserveWorkoutStatsUseCase` calculando:
  - volumen por sesion
  - progreso por ejercicio
  - mejores marcas de peso, reps, volumen de set y 1RM estimado
- Agrupacion de ejercicios por nombre snapshot normalizado para resistir reemplazos de IDs al editar rutinas.
- `StatsViewModel` con `UiState` y `StateFlow`.
- Pantalla Compose minima de Datos para verificar volumen, progreso y marcas.
- Tests unitarios de estadisticas con sesiones finalizadas, sesion abierta, snapshots, orden cronologico y series con peso cero.

### Fase 5 - Pulido UX funcional

Implementado:

- Rama local `codex/phase-5-ux-polish`.
- Estados de carga con texto contextual en Rutinas, Entrenar, Historial y Datos.
- Estados vacios mas claros para usuario nuevo.
- Confirmacion antes de archivar una rutina.
- Confirmacion antes de finalizar un entrenamiento.
- Textos de Inicio orientados al recorrido basico:
  - crear rutina
  - marcar rutina activa
  - entrenar
  - finalizar para alimentar Historial y Datos
- Content descriptions mas especificos en acciones de Rutinas, Entrenar e Historial.
- Fila de historial marcada como accion clicable con etiqueta semantica.
- Roadmap actualizado para insertar Fase 6 visual antes de Firebase/sync.

### Fase 6 - UI visual / Front con herramienta

Implementado:

- Rama local `codex/phase-6-ui-visual-front`.
- Diseno de referencia cargado desde la carpeta visual de `docs/`.
- Tema visual renovado en Compose con nueva paleta, tipografia, shapes y tokens extra para superficies y acentos.
- Sistema visual compartido en `core/design` para cards, metricas, badges, headers, empty states, loading y barras de progreso.
- Bottom navigation redisenada con seleccion mas clara y mejor jerarquia visual.
- Inicio convertido de placeholder a dashboard de entrada con accesos rapidos y recorrido guiado.
- Rutinas redisenada con banner de rutina activa, cards mas limpias, FAB y editor visualmente alineado.
- Entrenar redisenada para preview, sesion activa, estados vacios y bloques de series con mejor foco visual.
- Historial redisenado para listado y detalle con mejor legibilidad y estructura.
- Datos redisenada con overview, bloques resumen, progreso y records usando el mismo lenguaje visual.
- Navegacion rapida anadida desde Inicio a las tabs principales y desde Entrenar vacio a Rutinas.
- Iteracion posterior de Fase 6 aplicada tras revisar `docs/mejoras-claude.md`:
  - Inicio deja de mostrar metricas engañosas y reorienta el CTA principal a preparar rutina.
  - Rutinas reduce ruido repetido sobre snapshots y centraliza ese contexto en un solo bloque.
  - Entrenar mejora el feedback visual por serie y garantiza mejor tamano tactil en inputs.
  - Se anaden tokens iniciales de espaciado para empezar a sacar `dp` hardcodeados del sistema visual.
  - `docs/work-methodology/` refleja mejor la coordinacion multiagente con `Codex` como ejecutor principal.

### Bloque 3 - UX alta prioridad

Implementado en local:

- `SettingsScreen` y `SettingsViewModel` reales con selector `kg/lb`, version y sexta tab operativa.
- `HomeViewModel` + metricas reales de sesiones de la semana y total + CTA dinamica segun rutina activa.
- Feedback haptico en `WorkoutSetRow` al registrar el primer dato de una serie.
- Editor de rutinas protegido con `isDirty` y dialogo de descarte al cerrar sin guardar.
- Filtro `Activas / Archivadas` con accion `Restaurar` para rutinas archivadas.
- Banner dismissible de snapshots persistido en DataStore (`hasSeenSnapshotInfo`).
- `buildConfig = true` para exponer `BuildConfig.VERSION_NAME`.

Pendiente:

- `git push` de los commits locales pendientes al remoto.
- Validacion manual del flujo completo en dispositivo/emulador.

### Bloque 6 - Polish visual y accesibilidad

Implementado en workspace:

- `FitSpacing` se consolida en `core/design/Spacing.kt` y absorbe tokens intermedios (`tiny`, `smMd`, `mdLg`, `cardPadding`).
- `core/design` deja de tener un split roto:
  - `FitTrackPlusDesignSystem.kt` queda solo con enums compartidos.
  - `Cards.kt`, `Labels.kt`, `States.kt` y `Indicators.kt` pasan a ser la unica fuente de verdad de los composables del design system.
- Home, Rutinas, Entrenar, Historial y Datos remapean el espaciado hardcodeado pendiente a tokens del sistema.
- `FitTrackPlusNavHost` gana transiciones `fadeIn/fadeOut` de 200 ms entre tabs.
- `HistoryScreen` separa lista y detalle en dos composables y usa `AnimatedContent` para la transicion entre ambos estados.
- `FitTrackProgressBar` gana semantics y `contentDescription`, y sus usos en Entrenar y Datos pasan descripciones accesibles.
- Rutinas mantiene los `minimumInteractiveComponentSize()` en acciones iconicas como parte del cierre de accesibilidad.

Problemas encontrados:

- El repo no estaba realmente limpio: habia un WIP parcial con `Spacing.kt` y varios archivos nuevos de `core/design`, pero `FitTrackPlusDesignSystem.kt` seguia duplicando simbolos.
- Un primer `build` se quedo sin salida hasta timeout; tras parar Gradle y verificar primero `:app:compileDebugKotlin`, `test` y `build` pasaron correctamente.

### Intro de arranque clara

Implementado en workspace:

- Nueva referencia visual oficial en `docs/branding/Pantalla incio fondo claro/`.
- Intro de arranque aterrizada de HTML/CSS a Compose nativo, sin WebView ni video.
- Asset del logo limpio copiado a `app/src/main/res/drawable-nodpi/launch_logo.png`.
- `MainActivity` ahora arranca a traves de un `FitTrackPlusAppRoot` con una intro breve antes del `NavHost`.
- La intro usa:
  - fondo mineral claro
  - glow esmeralda y cobre muy sutil
  - reveal del simbolo
  - wordmark de producto
  - loader breve

Pendiente:

- Validacion manual de timing, legibilidad y convivencia con dark mode.

## Verificacion Realizada

Comandos ejecutados:

```powershell
.\gradlew.bat :app:compileDebugKotlin --rerun-tasks --no-daemon --console=plain
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
.\gradlew.bat detekt --no-daemon --console=plain
```

Resultado:

- `compileDebugKotlin` recompone correctamente las salidas Kotlin cuando el `build/` local queda en estado inconsistente.
- Tests pasan.
- Build completo pasa.
- Detekt pasa.

Pendiente:

- Prueba manual en emulador/dispositivo: `adb` no esta disponible en PATH.
- Flujo manual pendiente:
  - abrir app y revisar shell, tema, navegacion inferior y jerarquia visual de Inicio, Rutinas, Entrenar, Historial y Datos
  - crear/seleccionar rutina activa
  - confirmar que archivar rutina pide confirmacion
  - iniciar entrenamiento y confirmar que finalizar pide confirmacion
  - revisar que Historial y Datos siguen usando sesiones finalizadas
  - confirmar que editar rutina no altera snapshots historicos previos

## Decisiones Importantes

- La v2 vive en `app/src/main/kotlin`.
- `app/src/main/java` se trata como legacy local y queda fuera del nuevo repo.
- `app/google-services.json` queda ignorado.
- XML legacy de `layout`, `menu` y `navigation` queda fuera del repo nuevo.
- El historial muestra solo sesiones finalizadas.
- El detalle de historial se lee desde snapshots guardados en Room.
- Las estadisticas se calculan desde sesiones finalizadas y snapshots historicos.
- El progreso por ejercicio se agrupa por nombre snapshot normalizado.
- Las mejores marcas incluyen maximo peso, maximo reps, volumen de set y 1RM estimado.
- El seed demo se ejecuta solo si la app es debuggable y la base esta vacia.
- El pulido UX de Fase 5 no cambia reglas de negocio ni schema local.
- La Fase 6 usa una referencia externa de diseno pero aterriza en componentes Compose reutilizables.
- El redisenio visual de Fase 6 no cambia ViewModels, repositorios, Room ni DataStore.
- Inicio gana navegacion util hacia tabs, pero sin crear nuevas reglas de negocio.
- Firebase sigue fuera del MVP.
- Al cerrar cada fase se actualiza tambien `docs/work-methodology/` con aprendizajes reutilizables.

### Branding

Implementado:

- Logo decidido: simbolo abstracto (flechas ascendentes + diamante verde + contorno cobre).
- Logo con fondo: `docs/branding/resources/Logo.png`.
- Logo sin fondo: `docs/branding/resources/Logo sin fondo.png`.
- App icon generado en Android Studio: fondo crema `#F4F4F1`, simbolo centrado.
- Todos los tamaños de mipmap generados (mdpi a xxxhdpi) + ic_launcher-playstore.png.
- `ic_launcher_background.xml` con fondo crema `#F4F4F1`.
- Docs de marca cerrados: brand-foundation, logo-direction, color-system, typography, brand-questionnaire.
- Nueva referencia oficial de arranque: `docs/branding/Pantalla incio fondo claro/`, aterrizada a Compose nativo.

## Siguiente Paso

1. Elegir la siguiente subfase de 2.1B.
2. Validacion manual conjunta pendiente para fases 2.1B.1 a 2.1B.5 cuando haya dispositivo/emulador disponible.
- Sprint 2.1C.A cerrado: heatmap calendario en Stats, PR detection en vivo con haptic + badge, confetti celebration al finalizar sesion con PR. Commit `16d07fc`.
- Sprint 2.1C.B implementado en `codex/phase-2.1c-b-skeletons-demo-onboarding`: skeleton loaders (shimmer) en 5 pantallas, FitTrackLoadingCard deprecated, demo data on demand en Ajustes debug, onboarding 3 paginas con HorizontalPager. Tests pasan. Pendiente build completo + validacion manual.

## Roadmap 2.1

Estado:

- Documento canonico: `docs/roadmap-2.1.md`.
- Sustituye al backlog antiguo `docs/future-improvements.md`.
- `docs/mejoras-claude.md` queda como historico de propuestas, no como roadmap vigente.

Orden recomendado:

1. Gate 0 - Validacion manual de la v1 tecnica.
2. Fase 2.1A - Estabilidad y fricciones criticas:
   - Home muestra errores.
   - Workout bloquea peso negativo y reps invalidas.
   - Rutinas gana validacion inline y regex de reps.
   - Historial muestra notas, volumen/duracion y back correcto.
   - Settings confirma cambio de unidad.
3. Fase 2.1B - Features de valor:
   - plantillas de rutina.
   - duplicar/reordenar dias y ejercicios.
   - timer de descanso.
   - filtros/orden en historial.
   - tooltip/periodos en stats.
   - selector de tema.
4. Fase 2.1C - Portfolio WOW:
   - heatmap calendario.
   - achievements.
   - PR en vivo + celebracion.
   - skeleton loaders.
   - onboarding/demo data.

Diferido:

- Firebase/sync.
- OpenAI API runtime: si se anade en el futuro, disenar primero backend/proxy seguro y partir de `gpt-5.5` + Responses API.
- ExerciseCatalog global.
- supersets/cardio/RPE.
- import/export avanzado salvo fase dedicada.
- i18n completa.
