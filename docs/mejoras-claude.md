# Mejoras Propuestas Por Claude

Este documento es un backlog de mejoras que Claude detecto tras revisar el codigo y la documentacion del proyecto. **No son decisiones tomadas**. Cada entrada esta pensada para que el usuario la valide, descarte o mueva a otra fase.

Las entradas estan priorizadas segun su utilidad ante la Fase 6 (UI visual con herramienta externa, que llega ya y se esta trabajando en paralelo) y el objetivo del proyecto (aprender + portfolio, con uso real secundario).

Restricciones respetadas en la priorizacion:

- No sobreingenieria, MVP minimo funcional.
- No tocar el invariante de snapshots del historial.
- No adelantar Firebase / sync.
- No hacer trabajo que el rediseno visual vaya a tirar.

Formato por entrada: **Problema**, **Propuesta**, **Esfuerzo**, **Cuando**, **Archivos afectados**.

---

## Revision post-Fase 6 (2026-04-23)

La Fase 6 ha aterrizado en la rama `codex/phase-6-ui-visual-front`. Hay un nuevo sistema visual en `core/design/FitTrackPlusDesignSystem.kt` con componentes reutilizables (`FitTrackCard`, `SectionLabel`, `Metric`, `Badge`, `ScreenHeader`, `EmptyState`, `LoadingCard`, `ProgressBar`), tema grafito + esmeralda con dark mode, y rediseno de las 5 pantallas principales. Esta revision actualiza el estado de las mejoras previas y anade una nueva seccion con problemas UX/UI detectados tras el rediseno.

**Mejoras previas — estado actualizado**:

- **Item 1 (design tokens)**: *parcialmente hecho*. Colores, typography y shapes ya estan separados (Theme.kt + extensiones). **Pendiente**: tokens de espaciado (`FitSpacing`) — los paddings siguen hardcodeados (12.dp, 18.dp, 22.dp) en componentes y pantallas.
- **Item 2 (core/ui/components)**: *resuelto funcionalmente* via `FitTrackPlusDesignSystem.kt`, aunque como archivo unico de 8 composables en vez de carpeta. Ver nuevo item 24 sobre reorganizacion.
- **Item 10 (accesibilidad)**: sigue pendiente, se audita ahora — ver items nuevos 18 y 22.
- **Item 11 (strings.xml)**: sigue para post-Fase 6, el copy ya se considera estable.

---

## Nuevo - UX/UI post-Fase 6

Bloque anadido el 2026-04-23 tras auditar las 5 pantallas rediseñadas. Ordenado por impacto en el usuario real.

## Fase 6 / ALTA - Fricciones visibles para usuario nuevo y en el gimnasio

### 18. Home muestra metricas hardcodeadas que no reflejan estado real

- **Problema**: la Home muestra "5 areas activas" y "100% flujo local" como metricas en cards. Son texto estatico, no datos del usuario. Para usuario nuevo parece que la app "ya tiene contenido" que en realidad no existe.
- **Propuesta**: sustituirlas por metricas reales (rutinas creadas, sesiones de la semana, racha) o eliminarlas si aun no hay que mostrar datos. Considerar dos estados: "primer uso" vs "con datos".
- **Esfuerzo**: bajo-medio.
- **Cuando**: pronto — es visible desde el primer segundo de uso.
- **Archivos afectados**: `feature/home/HomeScreen.kt` (lineas de las metricas), potencial use case `GetHomeDashboardUseCase` en `domain/usecase/`.

### 19. CTA principal de Home lleva a Entrenar sin rutina activa

- **Problema**: el boton primario del hero de Home es "Ir a Entrenar". Si el usuario no ha creado ni activado una rutina, llega a una pantalla con empty state en vez de a una accion util.
- **Propuesta**: CTA dinamica — si no hay rutina activa, el boton dice "Preparar rutina" y lleva a Rutinas; si hay, "Ir a Entrenar".
- **Esfuerzo**: bajo.
- **Cuando**: junto con item 18, ambos afectan el mismo hero.
- **Archivos afectados**: `feature/home/HomeScreen.kt`, posible `HomeViewModel` nuevo para leer `activeRoutineId` desde `UserPreferencesRepository`.

### 20. Sin feedback al registrar una serie en Workout

- **Problema**: al escribir peso o reps, el state se actualiza silenciosamente. En el gimnasio, sin feedback visual ni haptico, el usuario duda si se guardo. La ProgressBar del resumen se actualiza, pero esta arriba y no se ve si estas scrolleado.
- **Propuesta**: feedback inmediato por serie — tick animado en el badge del setNumber al primer cambio, y/o vibracion corta (`HapticFeedback`). Barra de progreso sticky en la parte superior si la lista es larga.
- **Esfuerzo**: bajo.
- **Cuando**: alta prioridad — es la pantalla mas critica.
- **Archivos afectados**: `feature/workout/WorkoutScreen.kt` (`WorkoutSetRow`).

### 21. Inputs de peso/reps en Workout sin tamano tactil garantizado

- **Problema**: los `OutlinedTextField` de peso y reps usan `weight(1f)` sin `Modifier.heightIn(min = 56.dp)` o `defaultMinSize`. En un movil con dedo sudoroso puede ser dificil tocarlos con precision.
- **Propuesta**: altura minima 56dp en los inputs, badge de set clickable tambien para enfocar el input. Revisar contentDescription en el badge.
- **Esfuerzo**: bajo.
- **Cuando**: con item 20, son la misma pantalla.
- **Archivos afectados**: `feature/workout/WorkoutScreen.kt`.

### 22. Editor de rutinas sin proteccion ante perdida de trabajo

- **Problema**: dos acciones destructivas sin confirmacion:
  1. Cerrar el editor (X) descarta los cambios sin avisar.
  2. Eliminar un dia o un ejercicio con el IconButton de papelera es instantaneo.
- **Propuesta**: ConfirmDialog al cerrar si hay cambios no guardados ("Descartar cambios?"). Confirmacion inline (snackbar con "Deshacer" 5s) al eliminar dia/ejercicio.
- **Esfuerzo**: bajo-medio.
- **Cuando**: pronto — una confusion = trabajo perdido.
- **Archivos afectados**: `feature/routines/RoutinesScreen.kt` (editor), `RoutinesViewModel.kt` para detectar `isDirty`.

### 23. Rutinas archivadas son invisibles

- **Problema**: al archivar una rutina desaparece del listado. No hay tab "Archivadas" ni filtro. Si archivas por error, la unica recuperacion es abrir la DB.
- **Propuesta**: tab o chip de filtro "Activas / Archivadas" en el header del listado. Accion "Restaurar" en cada item archivado.
- **Esfuerzo**: bajo-medio.
- **Cuando**: junto con item 22 porque toca la misma pantalla.
- **Archivos afectados**: `feature/routines/RoutinesScreen.kt`, `RoutinesViewModel.kt`, posible ampliacion de `RoutineRepository.observeRoutines()` para incluir archivadas.

## Fase 6 / MEDIA - Calidad de vida del usuario

### 24. Peso anterior no visible como referencia en Workout

- **Problema**: al registrar una serie, el usuario suele querer igualar o superar el ultimo peso. Hoy no se muestra. Tiene que ir a Historial, buscar la sesion anterior y volver.
- **Propuesta**: placeholder o hint gris debajo del campo peso con el "ultimo peso registrado" para ese ejercicio (lookup a la ultima sesion finalizada con el mismo `exerciseNameSnapshot`). Sin autocompletar — solo referencia.
- **Esfuerzo**: medio (use case nuevo + query en DAO).
- **Cuando**: post-confirmaciones de items 20-23.
- **Archivos afectados**: nuevo use case `GetLastSetForExerciseUseCase`, query en `WorkoutDao`, `WorkoutScreen.kt`.

### 25. `targetRepsText` libre sin validacion ni ejemplos

- **Problema**: campo de reps objetivo acepta cualquier string ("abc", "999-888", "3x10"). Sin placeholder ni mascara. El usuario puede escribir algo que luego sorprende al revisar historial.
- **Propuesta** (ligado al item 9 condicional del plan previo): placeholder "8-12" como ejemplo + validacion inline que acepte solo formatos `N` o `N-M`. O migrar a dos campos `min/max` como propone item 9.
- **Esfuerzo**: bajo (solo placeholder + regex) o medio (migracion Room a min/max).
- **Cuando**: oportunidad para cerrar el item 9 si se decide migrar.
- **Archivos afectados**: `feature/routines/RoutinesScreen.kt` (editor), si migracion: `data/local/entity/RoutineExerciseEntity.kt` y migraciones.

### 26. Reordenar dias y ejercicios en rutinas no es posible

- **Problema**: el orden en el editor es el de creacion. Si el usuario quiere meter un ejercicio en el medio, tiene que borrar y recrear.
- **Propuesta**: drag handle (`Reorderable` para Compose) en cada fila. Reordenar actualiza `position`.
- **Esfuerzo**: medio.
- **Cuando**: post-MVP visual. No es bloqueante pero es molesto.
- **Archivos afectados**: `feature/routines/RoutinesScreen.kt`, `RoutinesViewModel.kt`.

### 27. Historial sin agrupacion temporal

- **Problema**: lista plana con timestamps absolutos (dd/MM/yyyy HH:mm). El usuario piensa en "esta semana / la semana pasada" no en fechas.
- **Propuesta**: agrupar por semana con cabeceras sticky (`Semana 16 — del 13 al 19 de abril`). Mantener la fecha en el item como detalle secundario.
- **Esfuerzo**: medio.
- **Cuando**: post-MVP visual.
- **Archivos afectados**: `feature/history/HistoryScreen.kt`, `ObserveWorkoutHistoryUseCase` puede agrupar o dejarlo a la pantalla.

### 28. Stats sin graficos ni filtros

- **Problema**: todas las metricas son texto y tablas. Los humanos leen tendencias mucho mejor en graficos. Ademas no se puede filtrar por ejercicio o por rango de fechas.
- **Propuesta**: un grafico de linea simple por ejercicio (volumen o 1RM estimado en el tiempo) + un filtro de ejercicio (dropdown). Rango de fechas opcional. Libreria ligera tipo `Vico` o Canvas manual para no inflar el APK.
- **Esfuerzo**: medio-alto.
- **Cuando**: despues de lo basico anterior. Alto valor portfolio.
- **Archivos afectados**: `feature/stats/StatsScreen.kt`, `StatsViewModel.kt`, posible nuevo `ExerciseHistoryChart` en `core/design/` o en `feature/stats/components/`.

### 29. Formula de 1RM sin explicar

- **Problema**: badge "1RM 80 kg" sin contexto. Un usuario casual no sabe que significa ni como se calcula.
- **Propuesta**: tooltip/bottom sheet con un parrafo corto ("Estimacion de 1 repeticion maxima con formula Epley: peso × (1 + reps/30). Es una aproximacion.").
- **Esfuerzo**: bajo.
- **Cuando**: con item 28, mismo espacio.
- **Archivos afectados**: `feature/stats/StatsScreen.kt`.

## Fase 6 / BAJA - Deuda tecnica del sistema visual

### 30. Tokens de espaciado siguen hardcodeados

- **Problema**: padding 12.dp, 18.dp, 22.dp aparecen literalmente en multiples composables. No hay `FitSpacing.xs/sm/md/lg` centralizado. Si el diseño evoluciona, toca repasar archivos.
- **Propuesta**: un `Spacing.kt` con valores nombrados, exponerlo por `CompositionLocal` o como `object FitSpacing { val md = 16.dp }`. Migrar progresivamente.
- **Esfuerzo**: bajo.
- **Cuando**: en una sesion dedicada de "normalizacion".
- **Archivos afectados**: nuevo `core/design/Spacing.kt`, refactor en pantallas y en `FitTrackPlusDesignSystem.kt`.

### 31. `FitTrackPlusDesignSystem.kt` es un archivo grande unico

- **Problema**: 8 composables reutilizables conviven en un solo archivo. Ya esta al borde de ser incomodo de navegar.
- **Propuesta**: trocear en `core/design/components/` con un archivo por componente (o agrupados por tipo: `Cards.kt`, `Labels.kt`, `States.kt`).
- **Esfuerzo**: bajo.
- **Cuando**: cuando se añada el noveno componente.
- **Archivos afectados**: `core/design/FitTrackPlusDesignSystem.kt`, imports en pantallas (probablemente se mantienen por paquete).

### 32. Navegacion sin transiciones

- **Problema**: al cambiar de tab el contenido cambia en seco. Se percibe como app basica.
- **Propuesta**: `NavHost` con `enterTransition` / `exitTransition` sutiles (fade + slide horizontal corto). Nada intrusivo.
- **Esfuerzo**: bajo.
- **Cuando**: polish visual final.
- **Archivos afectados**: `core/navigation/FitTrackPlusNavHost.kt`.

### 33. History → detalle sin transicion

- **Problema**: el detalle aparece sin animacion. En lista grande cuesta orientarse.
- **Propuesta**: `AnimatedContent` entre listado y detalle. En el futuro, shared element.
- **Esfuerzo**: bajo.
- **Cuando**: junto con item 32.
- **Archivos afectados**: `feature/history/HistoryScreen.kt`.

### 34. Accesibilidad — contentDescription y tamaño minimo

- **Problema**: varios `IconButton` sin `contentDescription` o con descripciones genericas. Badges y metricas sin `semantics`. Botones Delete pequeños en editor de rutinas.
- **Propuesta**: pasada sistematica con checklist:
  - Todos los iconos accionables con contentDescription descriptivo.
  - Tarjetas clickables con role Button.
  - IconButton con size minimo 48dp (usar `Modifier.minimumInteractiveComponentSize()`).
- **Esfuerzo**: bajo-medio.
- **Cuando**: puede ir con cualquier item de UX. Alto valor portfolio.
- **Archivos afectados**: transversal en todas las pantallas.

### 35. Subtitulos demasiado largos en listado de rutinas

- **Problema**: cada card de rutina muestra "El historial antiguo sigue protegido..." como subtexto. Es ruido para el usuario habitual; solo ayuda la primera vez.
- **Propuesta**: mover ese mensaje a un banner/tip dismissible arriba del listado (solo primera visita) o a una pantalla de ayuda.
- **Esfuerzo**: bajo.
- **Cuando**: cuando se toque el item 23 (filtros de archivadas).
- **Archivos afectados**: `feature/routines/RoutinesScreen.kt`.

---

## (Backlog original - estado pre-Fase 6)

Las mejoras de abajo son las que propuse antes de ver la Fase 6. Mantenidas por trazabilidad. Los items 1, 2, 10 y 11 estan actualizados al principio del documento.

---

## ALTA - Preparar integracion visual (Phase 6)

### 1. Centralizar design tokens

- **Problema**: `core/design/Theme.kt` concentra paleta y esquemas light/dark con colores hardcodeados. No hay `Color.kt`, `Type.kt`, `Spacing.kt`, `Shape.kt` separados. Integrar un diseno externo hoy obliga a tocar el archivo del theme a mano.
- **Propuesta**: extraer tokens a archivos dedicados dentro de `core/design/`, exponer `MaterialTheme` + tokens semanticos (p. ej. `FitSpacing`, `FitShapes`). La meta es que aplicar el nuevo diseno sea cambiar tokens, no cazar literales en pantallas.
- **Esfuerzo**: bajo (1 sesion). Refactor no funcional.
- **Cuando**: antes de empezar a integrar la Fase 6 visual.
- **Archivos afectados**: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/Theme.kt` (existe), nuevos `Color.kt`, `Type.kt`, `Spacing.kt`, `Shape.kt` en la misma carpeta.

### 2. Crear `core/ui/components/` con piezas reutilizables

- **Problema**: cada feature reinventa empty state, loading y confirmaciones. Fase 5 dejo confirmaciones duplicadas en Rutinas y Entrenar. No existe carpeta central de componentes.
- **Propuesta**: crear `core/ui/components/` y extraer `EmptyState`, `LoadingState`, `ConfirmDialog`, `SectionHeader` y un `FitCard` base. Migrar los usos actuales.
- **Esfuerzo**: bajo-medio. Alinea con Phase 6 sin adelantar decisiones visuales; los componentes quedan listos para aceptar el tema nuevo.
- **Cuando**: antes o a la vez que la integracion visual.
- **Archivos afectados**: nueva carpeta `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/ui/components/`, usos en `feature/routines/`, `feature/workout/`, `feature/history/`, `feature/stats/`.

### 3. Export local de historial (CSV o JSON)

- **Problema**: no hay forma de llevarse los datos. Firebase/sync queda lejos y mientras tanto el usuario depende de un unico dispositivo. Si se pierde la instalacion, se pierde el historial.
- **Propuesta**: accion en Settings que exporte `WorkoutSession` + `WorkoutExercise` + `WorkoutSet` a JSON (mas simple) o CSV (mas amigable para Excel) usando `ActivityResultContracts.CreateDocument` + `ContentResolver`. Sin import todavia (YAGNI hasta que haya dos devices).
- **Esfuerzo**: medio. Valor alto para portfolio (APIs de Storage Access Framework) y utilidad real.
- **Cuando**: despues de la integracion visual basica, para aprovechar Settings ya renovada.
- **Archivos afectados**: nuevo use case en `domain/usecase/`, helper en `data/` para serializar, pantalla en `feature/settings/`.

---

## MEDIA - Calidad y deuda antes del rediseno

### 4. Tests Room in-memory para DAOs de historial y stats

- **Problema**: los tests actuales usan fakes de repositorios, no ejercitan las queries reales de Room. Las queries mas delicadas son las de `WorkoutDao` (sesiones finalizadas + ejercicios + series) y no cambian con el rediseno visual.
- **Propuesta**: banco de tests con `Room.inMemoryDatabaseBuilder` + `runTest` para DAOs clave.
- **Esfuerzo**: bajo. ROI alto y estable.
- **Cuando**: cualquier momento antes de Phase 6.
- **Archivos afectados**: `app/src/test/` o `app/src/androidTest/` nuevo archivo `WorkoutDaoTest`, `RoutineDaoTest`.

### 5. Politica de errores + `SavedStateHandle` donde importa

- **Problema**: no hay auditoria explicita de como fluyen los errores a la UI. Ademas, no se ha verificado si `WorkoutViewModel` persiste el `sessionId` activo en `SavedStateHandle` para sobrevivir process death en mitad de un entrenamiento.
- **Propuesta**: confirmar que todos los `UiState` exponen `error: String?` (o equivalente) y montar un `SnackbarHost` global en la shell. Persistir en `SavedStateHandle` el id de sesion en curso en `WorkoutViewModel`.
- **Esfuerzo**: bajo. Conviene hacerlo antes del rediseno porque Phase 6 puede pedir surfaces distintos de error y mejor tener el modelo ya claro.
- **Cuando**: antes de Phase 6.
- **Archivos afectados**: ViewModels de `feature/routines/`, `feature/workout/`, `feature/history/`, `feature/stats/`; shell de navegacion en `core/navigation/`.

### 6. Dos ADR cortos en `docs/adr/`

- **Problema**: las decisiones mas caracteristicas del proyecto (historial por snapshots, clean-lite sin ExerciseCatalog) viven dispersas en `architecture.md` y el plan. Para portfolio valen mucho mas formalizadas.
- **Propuesta**: crear `docs/adr/0001-snapshot-history.md` y `docs/adr/0002-clean-lite-sin-exercise-catalog.md`. Formato ADR estandar: contexto, decision, consecuencias, alternativas consideradas.
- **Esfuerzo**: bajo (1-2 horas). Solo formaliza lo ya decidido.
- **Cuando**: cualquier momento; util como cierre narrativo del MVP local.
- **Archivos afectados**: nueva carpeta `docs/adr/`.

### 7. Detekt con baseline + config minima

- **Problema**: no hay linter estatico. Como aprendizaje y como senal de calidad en portfolio, falta.
- **Propuesta**: anadir plugin de detekt en `build.gradle.kts`, `detekt.yml` ligero (defaults + desactivar lo que moleste), generar baseline para no bloquear lo preexistente. Ejecutable localmente con `./gradlew.bat detekt`.
- **Esfuerzo**: bajo. CI vendra cuando el repo tenga remoto.
- **Cuando**: despues de Phase 6, para no mezclar ruido de estilo con el rediseno.
- **Archivos afectados**: `app/build.gradle.kts`, `gradle/libs.versions.toml`, nuevo `detekt.yml`, nuevo `detekt-baseline.xml`.

### 8. Turbine en los tests de `ObserveWorkoutStatsUseCase`

- **Problema**: ese es el unico use case con combinatoria real de Flows (`combine`). Los asserts con `runBlocking` + `first()` son legibles pero no expresan bien la secuencia de emisiones.
- **Propuesta**: anadir Turbine y reescribir solo ese test. NO anadir MockK; con los fakes actuales no aporta.
- **Esfuerzo**: bajo.
- **Cuando**: cualquier momento.
- **Archivos afectados**: `gradle/libs.versions.toml`, `app/build.gradle.kts` (testImplementation), `ObserveWorkoutStatsUseCaseTest`.

### 9. Migrar `targetRepsText: String` a `targetRepsMin/Max: Int?` (condicional)

- **Problema**: actualmente se guarda "8-12" como String. Funciona para mostrar, pero no se puede validar rango, comparar ni filtrar.
- **Propuesta**: dos enteros opcionales mas migracion Room. Adaptar `RoutineExerciseEntity`, DAOs, mappers, ViewModels y la pantalla de editar rutina. Los snapshots historicos pueden seguir siendo String (son pasado inmutable).
- **Esfuerzo**: medio. Solo justificado si Phase 6 rediseña el editor de rutinas o la UI de logging. Si Phase 6 no toca esas pantallas, mover a `future-improvements.md`.
- **Cuando**: a decidir tras ver el diseno visual.
- **Archivos afectados**: `data/local/entity/RoutineExerciseEntity.kt`, migraciones en `core/database/`, mappers en `data/repository/`, `feature/routines/RoutinesViewModel.kt` y pantalla.

---

## MEDIA - Auditar junto con Phase 6

### 10. Accesibilidad minima

- **Problema**: `contentDescription` mejoro en Fase 5 pero no hay auditoria sistematica. Faltan `semantics { }` en tarjetas de set, verificacion de tamanos tactiles >= 48dp y contraste AA.
- **Propuesta**: checklist de accesibilidad aplicado pantalla por pantalla, junto con el rediseno. El diseno externo debe llegar con contraste AA probado.
- **Esfuerzo**: bajo-medio. Paralelo al rediseno.
- **Cuando**: durante Phase 6, no antes.
- **Archivos afectados**: pantallas en `feature/`.

### 11. Strings hardcodeados a `strings.xml`

- **Problema**: las copias viven en codigo. Si se migran ahora y el diseno trae otro tono de copy, hay que tocarlas dos veces.
- **Propuesta**: migrar despues del rediseno, cuando el copy este estabilizado. Aprovechar para decidir si se abre puerta a i18n (ES/EN).
- **Esfuerzo**: medio (muchas strings).
- **Cuando**: post-Phase 6.
- **Archivos afectados**: `res/values/strings.xml`, todas las pantallas en `feature/`.

---

## BAJA - Mover a `future-improvements.md` cuando el usuario valide

### 12. Type-safe nav con `@Serializable`

- Beneficio bajo con 6 tabs planas. Reconsiderar si se añaden flujos anidados con argumentos (p. ej. detalle de ejercicio, filtros).

### 13. GitHub Actions CI

- Cuando el repo se suba a remoto: workflow de `build + test + detekt` en PR y push a main.

### 14. ExerciseCatalog global + tipos de ejercicio

- Cambio grande (nuevas entidades, mappers, UI de seleccion). Esperar senal real de uso: cardio, plantillas rapidas, o busqueda por grupo muscular.

### 15. minSdk 26

- Hoy es 23 y eso da mas alcance real. Subir solo si aparece una API que lo justifique.

### 16. Compose UI tests

- Hacer ahora seria tirar esfuerzo: Phase 6 puede cambiar jerarquias y testIds. Retomar post-Phase 6, un smoke test por pantalla.

### 17. Performance historial / stats

- Revisar keys estables en `LazyColumn`, `remember` de calculos de stats, paginacion si el historial crece. No hay problema hoy; documentar y medir si se degrada.

---

## Siguiente paso sugerido

El usuario revisa entrada por entrada y marca cuales entran en el backlog real. Las descartadas se dejan aqui como registro. Las aceptadas se repriorizan y las BAJA validas se migran a `docs/future-improvements.md` con el mismo nivel de detalle.
