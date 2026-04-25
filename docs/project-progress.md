# FitTrackPlus v2 Progress

Este documento resume donde estamos, que se ha hecho y cual es el siguiente paso.

## Estado Actual

- Rama actual: `codex/v2-mejoras`.
- Commit inicial local: `c1b2f31 Initialize FitTrackPlus v2 mobile foundation`.
- Commit de cierre de Fase 1: `9df5a44 Complete phase 1 routines`.
- Commit de cierre de Fase 2: `7cf2c02 Complete phase 2 workout logging`.
- Commit de cierre de Fase 3: `5e84fef Complete phase 3 history`.
- Commit de cierre de Fase 4: `Complete phase 4 statistics MVP`.
- Commit de cierre de Fase 5: `Complete phase 5 UX polish`.
- Remoto configurado: `https://github.com/alvaro944/FitTrackPlusV2.git`.
- Rama local alineada con `origin/codex/v2-mejoras` al iniciar Bloque 7; este pass anade solo CI + docs y queda pendiente de commit/push.
- Fase 6 cerrada tecnicamente.
- Bloque 6 de polish visual y accesibilidad cerrado tecnicamente y verificado con `test` + `build`.
- Bloque 7 CI implementado en `.github/workflows/ci.yml` para `push` en `main` y `codex/**`, y para `pull_request`.
- Validacion manual de Fase 6 pendiente en movil.
- Branding cerrado: docs de marca, logo decidido, app icon generado con fondo crema.
- Bloque 3 UX implementado en local: Ajustes, Inicio dinamico, feedback haptico, editor protegido y archivadas.
- Trabajo visual actual: validar intro de arranque clara en Compose basada en `docs/branding/Pantalla incio fondo claro/`.
- Starter pack metodologico reusable consolidado y afinado en `docs/project-methodology/`.
- Tercera pasada metodologica aplicada: ownership multiagente, handoff Codex/Claude, modos ligero/fase y derivacion de `AGENTS.md` quedan mas operativos.
- Kickoff metodologico documentado: la metodologia se usa para configurar el proyecto y `AGENTS.md` queda como referencia operativa diaria.
- Colaboracion entre plataformas formalizada: Codex, Claude, herramientas visuales y revisores tienen roles, ownership y handoff definidos.
- Fichero compartido de coordinacion definido como pieza reusable para que varias plataformas se comuniquen dentro del repo.
- Siguiente trabajo: validacion manual de intro, transiciones y accesibilidad; si no aparecen ajustes reales, reabrir Bloque 4 funcional.
- Siguiente fase funcional de producto: Bloque 4 (export, referencia de peso anterior y mejora de stats).

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

1. Validacion manual en dispositivo/emulador via Android Studio, incluyendo launcher, transiciones de tabs, Historial animado, TalkBack y dark mode.
2. Ajustar Bloque 8 solo si la intro de arranque clara muestra un problema real de timing, legibilidad o convivencia con dark mode.
3. Mantener `6.2 strings.xml` diferido hasta estabilizar copy.
4. Reabrir Bloque 4 funcional cuando la validacion manual no detecte ajustes visuales extra.
