# Mejoras Propuestas Por Claude

> Estado: documento historico. El roadmap vigente post-v1 vive en `docs/planning/roadmap-2.1.md`, que sustituye a `docs/planning/roadmap-2.1.md`.

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
- **Esfuerzo**: medio. Solo justificado si Phase 6 rediseña el editor de rutinas o la UI de logging. Si Phase 6 no toca esas pantallas, mover al roadmap vigente si se valida.
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

## BAJA - Historico de ideas no priorizadas

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

### 18. Dataset externo de ejercicios (hasaneyldrm/exercises-dataset)

- Fuente: https://github.com/hasaneyldrm/exercises-dataset
- Contenido: 1.324 ejercicios en `data/exercises.json` + `exercises.schema.json`. Campos: id, nombre, categoria, parte del cuerpo, equipamiento, musculo objetivo, musculos primarios/secundarios, instrucciones en 10 idiomas (español incluido), y rutas a thumbnail y GIF de animacion (180x180).
- Encaje: alimentaria la entrada 14 (ExerciseCatalog global) como seed de Room. El schema formal permite validar en build.
- **Bloqueo legal a resolver antes de usar los medios**: la licencia MIT cubre el codigo y la estructura del dataset. Los thumbnails y GIFs son propiedad de GymVisual, redistribuidos con permiso concedido *a ese repo*. Ese permiso no se hereda. Usarlos en FitTrackPlus — sobre todo si se publica — es redistribuir material con copyright ajeno sin acuerdo propio.
- Opciones a evaluar cuando toque:
  1. Contactar/pagar licencia a GymVisual para los medios.
  2. Usar solo la parte textual (nombres, categorias, musculos, equipamiento, instrucciones), que si cae bajo MIT con atribucion, y sustituir los medios por otros propios o de fuente libre.
  3. Descartar el dataset entero.
- Decision del usuario (2026-07-16): anotarlo aqui para no depender de memoria personal. Sin fecha de ejecucion.

### 19. Periodizacion: mesociclos, microciclos y progresiones

- Idea: ir mas alla de la heuristica actual ("si superas X repeticiones, sube peso") hacia programacion real. Que la app dirija el bloque: p.ej. dos semanas en rango de 15, luego cambio de ejercicios, luego bloque de fuerza, etc.
- Motivacion del usuario: tiene material de su antiguo entrenador y quiere apoyarse en metodos con base cientifica, no en invenciones.
- Trabajo previo obligatorio (CONCEPTOS ANTES QUE CODIGO): definir el modelo de dominio de periodizacion antes de tocar entidades. Sin eso, cualquier esquema de Room nace mal.
- Preguntas abiertas a cerrar en fase de diseño:
  - Que modelos se soportan (lineal, ondulante diario/semanal, por bloques) y cual es el minimo viable.
  - Como convive un plan con la invariante de snapshot: el plan es editable, pero el historial debe seguir leyendo snapshots.
  - Que pasa cuando el usuario se salta sesiones o cambia de rutina a mitad de mesociclo.
  - Donde vive la logica: `domain/usecase` puro y testeable, nunca en Compose.
- **Cautela de fuentes**: no se puede volcar contenido de libros al codigo ni a la UI. Lo que se modela son los principios (que son hechos, no expresion protegida), reescritos con lenguaje propio y citando la fuente en docs. Lo mismo aplica al material del antiguo entrenador: sirve como referencia para entender el metodo, no como texto a copiar.
- Tamaño estimado: no es una mejora, es una feature grande. Merece su propio ciclo de diseño (spec + plan) cuando se promueva.

#### Modelo de capas (refinado 2026-07-16 con el usuario)

La periodizacion se separa en TRES capas independientes. Mezclarlas es el error a evitar:

1. **Estructura** — que ejercicios y en que orden. Es la rutina actual. Ya existe.
2. **Prescripcion** — rangos, series, intensidad, RIR. Vive en el BLOQUE, no en la rutina. Aqui caben las semanas de descarga ("mismo peso, mitad de series" = misma estructura, otra prescripcion) y los cambios de rango sin duplicar rutinas.
3. **Objetivo** — motor de proyeccion. Dado el estado actual y una meta (ej: "100 kg en banca, hoy 80x5"), GENERA las prescripciones de los proximos bloques y adapta la rutina. Es la capa que mas ilusiona al usuario.

Mesociclo = lista ordenada de bloques. Bloque = terna (rutina, prescripcion, duracion). Cambiar un ejercicio entre bloques = apuntar a otra rutina (ahi si cambia la estructura). El snapshot se mantiene intacto: al empezar sesion se resuelve estructura+prescripcion y se snapshotea el RESULTADO; el historial no sabe que existe un mesociclo.

**Dependencia dura**: la capa 3 (objetivos) NO puede existir sin 1RM estimado (e1RM). El motor necesita e1RM para calcular % y proyectar.

#### Estado actual verificado (2026-07-27)

**CORRECCION** — lo anotado el 2026-07-16 en este apartado era falso. Se afirmo que la app no calculaba 1RM; se afirmo a partir de una busqueda mal construida (`rg -r`, que es "replace", no "recursive") que devolvio resultados corruptos. Verificado de nuevo leyendo el codigo:

- **El e1RM YA EXISTE.** `ObserveWorkoutStatsUseCase.kt:240-246` aplica Epley (`weightKg * (1 + reps / 30)`) por set. Se expone en `ExerciseProgressEntry.estimatedOneRepMaxKg` y `ExerciseRecords.bestEstimatedOneRepMax` (`domain/model/StatsModels.kt`), y se muestra como chip `ProgressMetric.EstimatedOneRepMax("1RM","kg")` (`feature/stats/StatsViewModel.kt:283-288`) pintado en `LineChart`. El cimiento de la capa 3 ya esta puesto.
- La progresion actual es un enum de 3 estados `ProgressionHint { UP, DOWN, NONE }` (`domain/model/ProgressionHint.kt`) mas `GetProgressionHintUseCase`: media de reps de las ultimas 3 sesiones, si 2 o mas superan el techo del rango → UP. Es una heuristica minima, no un motor.
- **No hay cursor de dia persistido.** `GetNextRoutineDayUseCase.kt:6-23` lo deriva: `nextDayIndex = sesionesFinalizadas % numDias`, `weekNumber = sesionesFinalizadas / numDias + 1`. El mesociclo debe seguir esta misma filosofia (derivar el bloque activo) en vez de crear una tabla de progreso que se desincronice.
- **Bloqueante real identificado**: `targetRepsText: String` es texto libre (`RoutineExerciseEntity.kt:30` y `RoutineExerciseAlternativeEntity`), parseado en caliente con regex en `GetProgressionHintUseCase.kt:41-65`. Ningun motor de prescripcion puede construirse sobre un String. Cruza con la entrada 9 de este mismo backlog.
- **El snapshot se materializa en un unico punto**: `DefaultWorkoutRepository.createSessionFromRoutineDay` (`DefaultWorkoutRepository.kt:59-104`). Ese es el unico gancho que necesitara la capa de prescripcion.
- **Dos identidades de ejercicio conviven**: stats agrupa por `scopeKey` (rutina|dia|variante normalizados, `ObserveWorkoutStatsUseCase.kt:227-234`); `GetProgressionHintUseCase` agrupa solo por `performedVariantKey`. A resolver al definir contra que se mide un objetivo.

#### Secuencia acordada (2026-07-27) — no empezar por el mesociclo

1. **Fase 0 — investigacion sin codigo**: recopilar metodos con base cientifica — formulas de e1RM (Epley vs Brzycki, y su limite de fiabilidad por encima de ~10-12 reps), zonas %1RM, esquemas de progresion (lineal, doble progresion, RIR/RPE), periodizacion (lineal, ondulante, por bloques), protocolos de descarga, proyeccion a objetivo con margen de seguridad. Modelar como principios con lenguaje propio + cita de fuente. NO copiar texto de libros ni del material del antiguo entrenador.
2. **Fase 1 — reps estructuradas**: migrar `targetRepsText` a `targetRepsMin/Max` de forma aditiva (DB v5). Es el cimiento que desbloquea las fases 2, 3 y 4.
3. **Fase 2 — objetivos por ejercicio**: encima del e1RM que ya existe.
4. **Fase 3 — bloques y mesociclos**, y **Fase 4 — motor de objetivos** (no diseñar hasta tener semanas de datos reales).

Plan detallado de las fases 0 y 1 en `docs/superpowers/plans/2026-07-27-structured-target-reps.md`.

#### REENFOQUE (2026-07-27): retos por ejercicio, no periodizar la rutina entera

El dueño trajo una propuesta trabajada con ChatGPT. Revisada contra el codigo real, **aporta una idea mejor que el diseño de bloques** y contiene dos diagnosticos falsos.

**Se adopta**: periodizar **1-3 ejercicios clave** mediante un "reto" asociado a un ejercicio concreto ("llegar a 50 kg en press de maquina"), en vez de montar mesociclos y bloques sobre toda la rutina. El resto de ejercicios funcionan exactamente como hoy. Da la mayor parte del valor con una fraccion del trabajo, y es abandonable a medias sin romper nada.

Puntos concretos adoptados:
- `goalId` **opcional** en el ejercicio de sesion: terminar o cancelar un reto no destruye ningun registro.
- El reto no reinterpreta el historial; solo genera indicaciones para sesiones futuras.
- Fases dentro del reto (base → carga → fuerza → aproximacion → prueba) como secuencia ligera.
- Motor de progresion determinista y simple (subir / mantener / repetir-bajar), con explicacion textual. Nada de IA.
- Gamificacion que premia constancia y cumplimiento semanal, **nunca el fallo** (coincide con la evidencia [A] de la investigacion).
- Cada maquina como ejercicio independiente: **ya se cumple** via `performedVariantKey`.

**Se descartan dos diagnosticos falsos de esa propuesta:**
1. *"Cambiar el rango de la rutina reinterpreta los registros antiguos"* — **falso en esta app**. `DefaultWorkoutRepository.kt:69-99` ya congela `routineNameSnapshot`, `dayNameSnapshot`, `exerciseNameSnapshot`, `targetRepsSnapshot` y materializa las series como filas. La instantanea de prescripcion dentro de la sesion, que es justo la solucion propuesta, existe desde el dia uno. Su "primera etapa: arreglar la base historica" ya esta hecha.
2. *"Crear un StrengthCalculator propio"* — ya existe (Epley, `ObserveWorkoutStatsUseCase.kt:240-246`). No hay que crearlo, hay que **extraerlo** a `domain/` y acotarlo por repeticiones.

**Consecuencia**: los mesociclos y bloques sobre la rutina completa quedan **sustituidos** por los retos por ejercicio. El modelo de bloques diseñado sigue siendo valido si algun dia hace falta, pero no se implementa.

#### Arco revisado

| Fase | Contenido | Estado |
|---|---|---|
| 0 | Investigacion de metodos | **Hecha** (`docs/research/training-methods.md`) |
| 1 | Reps estructuradas (DB v5) | Spec y plan listos, pendiente Codex |
| 2 | e1RM acotado + RIR por ejercicio | Pendiente de spec |
| 3 | Retos por ejercicio (objetivo + progreso) | Pendiente |
| 4 | Motor de progresion determinista | Pendiente |
| 5 | Fases del reto, misiones, prueba final | Pendiente |
| 6 | Analisis por grupo muscular | Depende de la entrada 18 |

**Nota**: `wger` (proyecto abierto con API de ejercicios, musculos y equipamiento) queda anotado como referencia alternativa a la entrada 18 para clasificacion de ejercicios. **Revisar su licencia antes de usar sus datos**, igual que con GymVisual.

#### Decisiones de producto abiertas

- Avance del bloque: ¿por fechas o por sesiones completadas? Hoy el usuario NO quiere poder saltar semanas ni entrenamientos. A futuro interesa un "saltar entrenamiento sin contarlo" y un "reiniciar rutina desde el dia 1 sin registrar". Ninguno existe aun a nivel de rutina normal, asi que no bloquea; se disena cuando toque.

---

## Nuevo - Design system unification (2026-08-29)

Ideas anotadas durante la revision de la rama `refactor/design-system-unification` (spec: `docs/superpowers/specs/2026-08-28-design-system-unification.md`). El dueño acepto el comportamiento actual de ambas para esta rama; quedan aqui como mejora futura, no como bloqueantes.

### 36. `FitTrackIconBadge` sin color personalizable por seccion

Al migrar las `QuickActionCard` de Home, las 4 acciones (rutinas, entrenar, historial, datos) pasaron de tener un color de acento distinto cada una a compartir el mismo `tone = Soft`. Se acepta por ahora. Si en el futuro se quiere recuperar la diferenciacion por color, valorar añadir un `tone` adicional (o un parametro de color explicito) a `FitTrackIconBadge` en vez de volver a resolverlo a mano por feature.

### 37. `FitTrackIconBadge` sin estado intermedio para "listo para completar"

`WorkoutSetCompletionButton` tenia 3 estados visuales (pendiente sin borde, listo-para-completar con fondo suave + borde, completado relleno). Tras migrar a `FitTrackIconBadge` (solo `Soft`/`Outlined`/`Filled`) los dos primeros se fusionan en `Outlined`, perdiendo la distincion. Se acepta por ahora. Idea para el futuro: permitir un estilo personalizado por contexto de uso (p.ej. distinguir "listo"/"completado" en Entrenar de los usos en Historial/registros), en vez de forzar todos los consumidores al mismo set de tonos genericos.

---

## Nuevo - Deshacer real en acciones destructivas (2026-08-29)

Encontrado durante la implementacion de `fix/data-loss` (P0-9 de `docs/design/auditoria-ronda-3.md`).

### 38. No existe infraestructura para "Deshacer" de verdad

`RoutinesViewModel.removeDay`/`removeExercise`/`removeExerciseAlternative` mutan el editor sin guardar ningun snapshot de lo eliminado, y el unico canal hacia el `SnackbarHost` es un mensaje de texto de una via (`viewModel.clearMessage()`), sin `actionLabel` ni logica de restauracion. `fix/data-loss` cerro el riesgo inmediato con confirmacion antes de borrar (dia, ejercicio, alternativa), pero un "Deshacer" real requiere: snapshot del elemento eliminado en el ViewModel, un canal de evento (no solo un `String`) hacia la UI, y logica de restauracion que reinserte el elemento en su posicion original si el usuario pulsa "Deshacer" antes de que expire el snackbar. Aplica igual a las demas instancias de `SnackbarHostState` del proyecto (7 en total, 0 con `actionLabel` a fecha de la auditoria).

**Esfuerzo**: medio — no es un fix puntual, es una pieza de infraestructura compartida (probablemente un helper en `core/` reutilizable desde varias features).

---

### 39. Avisos de finalizacion del temporizador fuera de la pantalla

El temporizador de descanso conserva su estado mediante `DataStore` y calcula el tiempo restante contra un instante absoluto, por lo que sobrevive a un cierre del proceso. Esta iteracion solo da respuesta haptica mientras `WorkoutScreen` sigue compuesta. Las notificaciones y el sonido al terminar con la app en segundo plano se posponen: requieren definir un canal de notificacion, su permiso en Android 13+, la politica de alertas y el ciclo de vida de un trabajo/alarma fiable. Deben disenar e implementarse juntos como una feature de alertas de fondo, no como un añadido aislado al temporizador.

**Esfuerzo**: medio — afecta a permisos, notificaciones y comportamiento en segundo plano.

---

## Siguiente paso sugerido

El usuario revisa entrada por entrada y marca cuales entran en el backlog real. Las descartadas se dejan aqui como registro. Las aceptadas se repriorizan en `docs/planning/roadmap-2.1.md` cuando pasan a ser direccion vigente.
