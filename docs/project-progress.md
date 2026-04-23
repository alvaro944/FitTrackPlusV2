# FitTrackPlus v2 Progress

Este documento resume donde estamos, que se ha hecho y cual es el siguiente paso.

## Estado Actual

- Rama actual: `codex/phase-6-ui-visual-front`.
- Commit inicial local: `c1b2f31 Initialize FitTrackPlus v2 mobile foundation`.
- Commit de cierre de Fase 1: `9df5a44 Complete phase 1 routines`.
- Commit de cierre de Fase 2: `7cf2c02 Complete phase 2 workout logging`.
- Commit de cierre de Fase 3: `5e84fef Complete phase 3 history`.
- Commit de cierre de Fase 4: `Complete phase 4 statistics MVP`.
- Commit de cierre de Fase 5: `Complete phase 5 UX polish`.
- Remoto configurado: `https://github.com/alvaro944/FitTrackPlusV2.git`.
- Fase 6 cerrada tecnicamente.
- Validacion manual de Fase 6 pendiente en movil.
- Branding cerrado: docs de marca, logo decidido, app icon generado con fondo crema.
- Siguiente trabajo: mejoras UX alta prioridad (items 18-23 de `docs/mejoras-claude.md`).
- Siguiente fase funcional: `phase-7-sync-futura`.

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

## Verificacion Realizada

Comandos ejecutados:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests pasan.
- Build completo pasa.

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

## Siguiente Paso

Mejoras UX de alta prioridad (Bloque 3 del roadmap):

1. Validacion manual en dispositivo/emulador via Android Studio (antes de iterar mas).
2. Settings funcional: unidad de peso + placeholder de exportar historial + version.
3. HomeViewModel + metricas reales + CTA dinamica (items 18-19).
4. Feedback en Workout y touch targets (items 20-21).
5. Proteccion editor de rutinas + archivadas visibles (items 22-23-35).
