# Phase Log

Bitacora viva del proyecto. Cada fase debe anadir aqui lo que se hizo, lo que se verifico, problemas encontrados y decisiones tomadas.

## Fase 0 - Mobile foundation

Rama:

- `codex/phase-0-mobile-foundation`

Commit:

- `c1b2f31 Initialize FitTrackPlus v2 mobile foundation`

Objetivo:

- Crear una base Android moderna para FitTrackPlus v2.
- Separar la nueva app Compose de la v1 XML/Fragments.
- Preparar arquitectura, persistencia local y documentacion.

Cambios principales:

- Se habilito Compose.
- Se agrego Hilt.
- Se agrego Room con KSP.
- Se agrego DataStore.
- Se agrego Navigation Compose.
- Se creo estructura por `core`, `data`, `domain` y `feature`.
- Se creo una shell visual con tabs iniciales.
- Se creo modelo de datos v2 con snapshots historicos.
- Se crearon repositorios y casos de uso iniciales.

Problemas encontrados:

- El workspace no tenia `.git`; se inicializo un repo local.
- Git marco la carpeta como `dubious ownership`; se agrego como `safe.directory`.
- Algunas versiones nuevas de AndroidX/Hilt pedian AGP mas nuevo; se ajustaron a versiones compatibles con AGP `8.5.1`.
- `build` fallo inicialmente por lint en XML legacy con `android:paddingHorizontal` y `android:paddingVertical`; se reemplazaron por paddings compatibles con minSdk 23.
- Se detecto `.gitignore` con restos de conflicto y se limpio.

Decisiones:

- Mantener Firebase fuera del MVP.
- No trackear `google-services.json`.
- No trackear `app/src/main/java` legacy.
- No trackear XML legacy de `layout`, `menu` y `navigation`.
- Mantener un solo modulo Gradle por ahora.

Verificacion:

```powershell
.\gradlew.bat clean test
.\gradlew.bat build --no-daemon
```

Resultado:

- Verificacion automatica correcta.

Pendiente:

- Prueba manual en emulador/dispositivo.

## Fase 1 - Rutinas

Estado:

- Completada tecnicamente.

Rama:

- `codex/phase-1-routines`

Commit:

- `Complete phase 1 routines`

Objetivo:

- Crear, listar, editar, archivar y seleccionar rutinas.

Fuera de alcance:

- Registro de entrenamientos.
- Historial.
- Estadisticas.
- Firebase.

Cambios principales:

- Se reemplazo el placeholder de rutinas por una pantalla Compose funcional.
- Se creo `RoutinesViewModel` con `UiState` y `StateFlow`.
- Se conecto la UI con `RoutineRepository` para listar, crear, editar y archivar.
- Se conecto la seleccion de rutina activa con `UserPreferencesRepository` y DataStore.
- Se agrego editor simple para dias y ejercicios.
- Se agrego validacion basica de campos antes de guardar.
- Se agrego `.kotlin/` a `.gitignore`.
- Se creo `docs/work-methodology/` como guia de estudio y metodologia.
- Se actualizo `docs/development-workflow.md` para incluir la guia metodologica en cada cierre de fase.

Problemas encontrados:

- El primer `git switch -c` fallo por permisos del sandbox; se creo la rama tras aprobacion.
- `gradlew test` dentro del sandbox fallo porque no podia crear locks en `.gradle`; se ejecuto fuera del sandbox.
- Kotlin no podia hacer smart cast de `state.editor` al venir de `collectAsStateWithLifecycle`; se uso una variable local explicita.
- Un intento de `build` con daemon quedo sin salida hasta timeout; se paro el daemon y se verifico con `--no-daemon --console=plain`.

Decisiones:

- Mantener el flujo en una sola pantalla por ahora: lista y editor alternan dentro de `RoutinesScreen`.
- No crear navegacion nueva para el editor hasta que el flujo crezca.
- Seleccionar automaticamente la primera rutina creada solo si no habia rutina activa.
- Archivar una rutina activa limpia la preferencia de rutina activa.
- Al cerrar una fase, tambien se actualiza la guia metodologica y se informa al usuario que se anadio.

Verificacion:

```powershell
.\gradlew.bat test
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Verificacion automatica correcta.

Pendiente:

- Crear rutina Push/Pull/Legs.
- Editar dias y ejercicios.
- Seleccionar rutina activa.
- Cerrar y abrir la app para comprobar persistencia.

## Fase 2 - Registro de entrenamiento

Estado:

- Completada tecnicamente.

Rama:

- `codex/phase-2-workout-logging`

Commit:

- `Complete phase 2 workout logging`

Objetivo:

- Iniciar entrenamientos desde la rutina activa.
- Registrar series con peso y repeticiones.
- Finalizar sesiones.
- Mantener historial basado en snapshots.

Fuera de alcance:

- Historial visual completo.
- Estadisticas.
- Firebase.
- Refactors generales fuera del flujo de entrenamiento.

Cambios principales:

- Se reemplazo el placeholder de Entrenar por una pantalla Compose funcional.
- Se creo `WorkoutViewModel` con `UiState` y `StateFlow`.
- Se agrego preview del siguiente dia de rutina activa.
- Se agrego inicio de sesion desde rutina activa usando snapshot historico.
- Se agrego reanudacion de una sesion abierta para evitar duplicados.
- Se agrego edicion de peso en kg y repeticiones por serie.
- Se agrego persistencia de series en Room.
- Se agrego finalizacion de sesion con `finishedAt`.
- Se extendio `WorkoutDao` y `WorkoutRepository` para detalle de sesion, sesion abierta y actualizacion de series.
- Se agregaron `GetNextWorkoutPreviewUseCase` y `UpdateWorkoutSetUseCase`.
- Se agregaron tests unitarios para inicio, reanudacion y normalizacion de series.
- Se actualizaron README, plan, progreso y guia de metodologia.

Problemas encontrados:

- `git switch -c` volvio a requerir aprobacion fuera del sandbox para crear la rama.
- Un primer `.\gradlew.bat test` quedo sin salida hasta timeout.
- La siguiente ejecucion de tests fallo al intentar borrar `app/build/test-results` porque habia archivos abiertos; se paro Gradle con `.\gradlew.bat --stop`.
- Dos assertions fallaron por comparar `Int` con `Long`; se corrigieron usando literales `Long`.
- El primer build de cierre quedo sin salida hasta timeout; se paro Gradle y se repitio con mas margen.
- `adb` no esta disponible en PATH, asi que la prueba manual queda pendiente.

Decisiones:

- Solo puede haber una sesion abierta global.
- Si existe una sesion abierta, Entrenar la reanuda y no crea otra.
- El ciclo de dias se basa solo en sesiones finalizadas.
- Finalizar sesiones parciales es valido para el MVP.
- Inputs invalidos o vacios de peso/reps se normalizan a `0` al persistir.
- Peso se guarda en `weightKg` y la UI muestra kg.
- No se cambia el schema de Room; se reutilizan las tablas historicas existentes.

Verificacion:

```powershell
.\gradlew.bat test
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Verificacion automatica correcta.

Pendiente:

- Prueba manual de Fase 2 en emulador/dispositivo cuando `adb` este disponible:
  - Crear o seleccionar Push/Pull/Legs.
  - Iniciar Push.
  - Editar series y volver a Entrenar para confirmar reanudacion.
  - Finalizar y comprobar que el siguiente dia es Pull.
  - Completar ciclo hasta volver a Push en semana 2.
  - Editar rutina y confirmar que el historico mantiene snapshots.

## Fase 3 - Historial

Estado:

- Completada tecnicamente.

Rama:

- `codex/phase-3-history`

Commit:

- `Complete phase 3 history`

Objetivo:

- Listar sesiones pasadas.
- Mostrar detalle de ejercicios y series.
- Confirmar que el historial usa snapshots y no depende de la rutina editable actual.
- Anadir datos demo para probar el flujo.

Fuera de alcance:

- Estadisticas.
- Firebase.
- Pulido visual o efectos.
- Refactors generales fuera de historial.

Cambios principales:

- Se agregaron consultas Room para observar solo sesiones finalizadas.
- Se agrego carga de detalle historico solo para sesiones finalizadas.
- Se agregaron modelos de dominio para resumen y detalle de historial.
- Se agregaron `ObserveWorkoutHistoryUseCase` y `GetWorkoutHistoryDetailUseCase`.
- Se creo `HistoryViewModel` con lista, seleccion, detalle y estados minimos.
- Se reemplazo el placeholder de Historial por una UI Compose minima.
- Se agrego seed demo automatico solo en builds debug cuando la base esta vacia.
- El seed demo crea una rutina PPL y varias sesiones finalizadas con pesos y reps.
- Se agregaron tests unitarios para listado, detalle, orden y snapshots.

Problemas encontrados:

- `.\gradlew.bat test` volvio a quedarse sin salida hasta timeout.
- Se paro Gradle con `.\gradlew.bat --stop`.
- La verificacion de tests paso al repetir con `--no-daemon --console=plain`.
- El build paso con warnings ya conocidos de AGP/compileSdk 35 y D8/Kotlin metadata.
- `adb` no esta disponible en PATH, asi que la prueba manual queda pendiente.

Decisiones:

- El historial lista solo sesiones con `finishedAt`.
- El detalle se carga desde `WorkoutSessionWithExercises` y se mapea desde snapshots.
- La UI de Historial queda deliberadamente simple para no invertir la fase en frontend.
- El seed demo no se ejecuta en release y no duplica datos si ya hay rutinas o sesiones.
- No se cambia el schema Room; se reutilizan las tablas historicas existentes.

Verificacion:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Verificacion automatica correcta.

Pendiente:

- Prueba manual en emulador/dispositivo cuando `adb` este disponible:
  - Abrir Historial.
  - Ver registros demo.
  - Entrar al detalle.
  - Editar rutina y confirmar que el historial antiguo conserva snapshots.

## Fase 4 - Estadisticas MVP

Estado:

- Completada tecnicamente.

Rama:

- `codex/phase-4-statistics-mvp`

Commit:

- `Complete phase 4 statistics MVP`

Objetivo:

- Calcular estadisticas MVP desde sesiones finalizadas.
- Mostrar volumen por sesion, progreso por ejercicio y mejores marcas.
- Mantener UI minima para verificar datos sin invertir la fase en pulido visual.

Fuera de alcance:

- Firebase.
- Sync.
- Graficos avanzados.
- Refactors generales fuera de estadisticas e historial necesario.

Cambios principales:

- Se agrego observacion Room de sesiones finalizadas con ejercicios y series.
- Se extendio `WorkoutRepository` para exponer detalle historico reactivo.
- Se agregaron modelos de dominio para estadisticas.
- Se creo `ObserveWorkoutStatsUseCase`.
- Se calcula volumen como `peso * reps`.
- Se calcula progreso por ejercicio en orden cronologico.
- Se calculan mejores marcas de peso, reps, volumen de set y 1RM estimado con formula Epley.
- Se agrupan ejercicios por nombre snapshot normalizado, no por ID editable.
- Se creo `StatsViewModel` con `UiState` y `StateFlow`.
- Se reemplazo el placeholder de Datos por una UI Compose minima.
- Se agregaron tests unitarios de estadisticas.

Problemas encontrados:

- El primer `.\gradlew.bat test --no-daemon --console=plain` fallo en KSP por un archivo generado incremental ausente.
- Se ejecuto `.\gradlew.bat --stop` y al repetir el test paso correctamente.
- Un build posterior quedo sin salida hasta timeout; se paro Gradle y al repetir paso correctamente.
- `adb` no esta disponible en PATH, asi que la prueba manual queda pendiente.

Decisiones:

- No se cambia el schema Room; las estadisticas se calculan en memoria.
- Las estadisticas usan solo sesiones finalizadas.
- El progreso se agrupa por nombre snapshot normalizado para soportar reemplazo de IDs al editar rutinas.
- Si un ejercicio se renombra, se considera otro ejercicio en estadisticas.
- Las marcas con peso requieren `weightKg > 0` y `reps > 0`.
- La marca de reps permite peso `0.0` para ejercicios de peso corporal o asistidos registrados asi.

Verificacion:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests pasan.
- Build completo pasa.

Pendiente:

- Prueba manual en emulador/dispositivo cuando `adb` este disponible:
  - Abrir Datos.
  - Ver volumen por sesion.
  - Revisar progreso por ejercicio.
  - Revisar mejores marcas.
  - Confirmar que una sesion abierta no aparece en estadisticas.

## Fase 5 - Pulido UX funcional

Estado:

- Completada tecnicamente.

Rama:

- `codex/phase-5-ux-polish`

Commit:

- `Complete phase 5 UX polish`

Objetivo:

- Mejorar claridad, estados, confirmaciones y accesibilidad basica sin redisenar la app.

Fuera de alcance:

- Firebase.
- Sync.
- Redisenio visual avanzado.
- Animaciones, graficos o cambios de reglas de negocio.

Cambios principales:

- Se agregaron estados de carga con texto contextual en Rutinas, Entrenar, Historial y Datos.
- Se mejoraron estados vacios para orientar a un usuario nuevo.
- Se agrego confirmacion antes de archivar una rutina.
- Se agrego confirmacion antes de finalizar un entrenamiento.
- Se ajusto Inicio para explicar el recorrido minimo de uso.
- Se mejoraron `contentDescription` en acciones de Rutinas, Entrenar e Historial.
- Se marco cada sesion de Historial como accion clicable con etiqueta semantica.
- Se actualizo el roadmap para insertar Fase 6 visual antes de sync.

Problemas encontrados:

- `adb` sigue sin estar disponible en PATH, asi que la prueba manual queda pendiente.

Decisiones:

- Las confirmaciones viven en estado local de Compose porque no cambian reglas de negocio.
- No se agregaron dependencias nuevas.
- No se modifico Room, DataStore, repositorios ni casos de uso.
- La fase visual queda separada como Fase 6 para no mezclar pulido funcional con redisenio.
- Firebase/sync pasa a Fase 7.

Verificacion:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests pasan.
- Build completo pasa.

Pendiente:

- Prueba manual en emulador/dispositivo cuando `adb` este disponible:
  - Revisar estados de usuario nuevo.
  - Confirmar archivado de rutina.
  - Confirmar finalizacion de entrenamiento.
  - Revisar Historial y Datos con sesiones finalizadas.

## Fase 6 - UI visual / Front con herramienta

Estado:

- Completada tecnicamente.

Rama:

- `codex/phase-6-ui-visual-front`

Objetivo:

- Aplicar el diseno visual de referencia a la app Compose real.
- Mejorar tema, navegacion, jerarquia visual, superficies y componentes compartidos.
- Mantener intactas las reglas de negocio y los snapshots historicos.

Fuera de alcance:

- Firebase.
- Sync.
- Cambios en Room, DataStore, repositorios o casos de uso por razones visuales.

Cambios principales:

- Se creo un sistema visual reutilizable en `core/design` con tokens extra, tipografia, shapes, cards, badges, metricas, headers, estados vacios, loading y progreso.
- Se actualizo `FitTrackPlusTheme` para alinearlo con la paleta grafito + esmeralda del diseno de referencia.
- Se redisenaron las tabs principales:
  - Inicio
  - Rutinas
  - Entrenar
  - Historial
  - Datos
- Inicio dejo de ser placeholder y paso a ser dashboard de entrada con accesos rapidos.
- Rutinas gano banner de rutina activa, biblioteca mas clara y editor alineado con el nuevo sistema.
- Entrenar gano una hero card para el siguiente entrenamiento, sesion activa mas enfocada y estados vacios con CTA.
- Historial y detalle historico mejoraron legibilidad y jerarquia sin tocar snapshots.
- Datos gano overview visual con resumen, progreso y records.
- La bottom navigation se ajusto para tener seleccion, contraste y presencia visual coherentes con la fase.

Problemas encontrados:

- La primera verificacion fallo por errores de Compose menores:
  - faltaba importar `dp` en la navegacion
  - una lista de acciones en Inicio se construyo dentro del DSL de `LazyColumn` usando llamadas `@Composable`
- Se corrigieron esos errores y la verificacion posterior paso.
- `adb` sigue sin estar disponible en PATH, asi que la prueba manual queda pendiente.

Decisiones:

- Usar la referencia visual guardada en `docs/` como direccion de arte, no como replica 1:1 del HTML/JSX.
- Traducir el diseno a componentes Compose compartidos antes de tocar pantallas para evitar duplicacion.
- Mantener la arquitectura intacta:
  - Compose pinta estado
  - ViewModel conserva eventos
  - no se mueve logica de negocio a UI
- Mejorar navegacion util desde Inicio y estados vacios, pero sin abrir nuevas rutas ni cambiar el flujo de datos.

Verificacion:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests pasan.
- Build completo pasa.

Pendiente:

- Prueba manual en emulador/dispositivo cuando `adb` este disponible:
  - revisar shell y tabs con el nuevo tema
  - recorrer Inicio, Rutinas, Entrenar, Historial y Datos
  - confirmar archivado de rutina y finalizacion de entrenamiento
  - validar que Historial y Datos siguen usando solo sesiones finalizadas
  - validar que editar rutina no altera snapshots historicos previos

### Iteracion de mejora post-redisenio

Motivo:

- Se revisaron los cambios de documentacion, `CLAUDE.md` y `docs/mejoras-claude.md` para separar backlog util de propuestas fuera de alcance.

Cambios principales:

- Inicio elimina metricas fijas que podian sonar a datos reales y mueve el CTA principal a `Rutinas` para evitar un callejon sin salida al primer uso.
- Rutinas simplifica el copy repetido por tarjeta y deja la explicacion de snapshots en un unico bloque de contexto.
- Entrenar muestra mejor feedback visual en cada serie registrada y eleva el tamano minimo tactil de los inputs.
- Se creo `core/design/Spacing.kt` como primera base de tokens de espaciado para seguir normalizando la capa visual.
- `docs/work-methodology/` ahora deja mas claro el trabajo multiagente, la prioridad de `CLAUDE.md` como regla operativa y la diferencia entre propuesta y decision.

Decisiones:

- Solo se implementan mejoras de bajo riesgo y puramente visuales/UX en esta pasada.
- Se evita abrir cambios de dominio, nuevos casos de uso o lectura de datos adicionales solo para decorar Home.
- El backlog de Claude sigue siendo backlog: no todo entra automaticamente en Fase 6.

Verificacion:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests pasan.
- Build completo pasa.

Pendiente:

- Prueba manual en emulador/dispositivo cuando `adb` este disponible.

### Cierre tecnico y apertura de branding

Estado:

- Fase 6 se considera cerrada tecnicamente.

Cambios principales:

- Se crea `docs/branding/` como linea de trabajo separada del roadmap funcional.
- La carpeta de branding arranca con estructura inicial para:
  - base de marca
  - cuestionario
  - color
  - tipografia
  - direccion de logo
  - direccion de icono
  - prompt para siguiente chat
- Se deja documentado que branding no sustituye a Fase 7 ni la adelanta.

Pendiente:

- Validacion manual de Fase 6 en movil.
- Definir personalidad de marca antes de explorar logo final o imagen generada.

### Bloque 6 - Polish visual y accesibilidad

Estado:

- completado tecnicamente

Rama:

- `codex/v2-mejoras`

Objetivo:

- cerrar el WIP real de `core/design` sin rehacer el trabajo ya empezado
- terminar la normalizacion de espaciado en pantallas principales
- anadir transiciones suaves y rematar la accesibilidad basica del sistema visual

Cambios principales:

- `FitSpacing` queda consolidado en `core/design/Spacing.kt` con tokens intermedios reutilizables (`tiny`, `smMd`, `mdLg`, `cardPadding`).
- `FitTrackPlusDesignSystem.kt` deja de duplicar composables y pasa a contener solo enums compartidos.
- Se crea `Indicators.kt` y `core/design` queda dividido por responsabilidad:
  - `Cards.kt`
  - `Labels.kt`
  - `States.kt`
  - `Indicators.kt`
- Home, Rutinas, Entrenar, Historial y Datos sustituyen los `dp` hardcodeados pendientes por tokens de `FitSpacing`.
- `FitTrackPlusNavHost` gana `fadeIn/fadeOut` de 200 ms para navegacion entre tabs.
- `HistoryScreen` separa listado y detalle en composables distintos y los intercambia con `AnimatedContent`.
- `FitTrackProgressBar` gana `semantics`, `ProgressBarRangeInfo` y `contentDescription`; Entrenar y Datos pasan textos accesibles.
- Rutinas conserva `minimumInteractiveComponentSize()` en acciones iconicas como cierre de accesibilidad tactil.

Problemas encontrados:

- El arbol real no estaba limpio: habia un split parcial de `core/design` con archivos nuevos, pero el archivo anterior seguia exportando los mismos simbolos.
- Un primer `build` se quedo sin salida hasta timeout; se paro Gradle, se verifico primero `:app:compileDebugKotlin` y despues `test` y `build` completos.

Decisiones:

- No devolver `FitSpacing` a `FitTrackPlusDesignSystem.kt`; `Spacing.kt` pasa a ser la fuente canonica de tokens.
- Aprovechar el WIP existente en vez de rehacer el split desde cero.
- No abrir cambios de branding ni de intro durante este pass; la intro queda pendiente solo de validacion manual.
- El siguiente paso tecnico no es Bloque 4 funcional sino validacion manual del polish y despues CI.

Verificacion:

```powershell
.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- `compileDebugKotlin` pasa.
- Tests pasan.
- Build completo pasa.

Pendiente:

- Validacion manual en emulador/dispositivo:
  - fade entre tabs
  - fade lista/detalle en Historial
  - TalkBack en barras de progreso
  - targets tactiles en Rutinas
  - timing y legibilidad de la intro

## Branding base + Bloque 3 UX + intro clara

Estado:

- Branding base cerrado.
- Bloque 3 UX completado en local.
- Intro de arranque clara implementada en Compose.

Rama:

- `codex/phase-6-ui-visual-front`

Commits de referencia:

- Branding base e icono: commit local previo de cierre de branding
- Bloque 3 UX: `ef954ac`

Objetivo:

- Cerrar docs de marca y dejar icono de app coherente con el logo elegido.
- Completar el bloque UX funcional posterior al redisenio.
- Sustituir la exploracion de arranque previa por una intro clara real integrada en la app.

Cambios principales:

- Se cerraron los docs de branding:
  - `brand-foundation.md`
  - `brand-questionnaire.md`
  - `color-system.md`
  - `typography.md`
  - `logo-direction.md`
  - `app-icon.md`
- Se definio el logo final como simbolo abstracto y se genero el adaptive icon con fondo crema `#F4F4F1`.
- Se implemento el Bloque 3 UX:
  - Ajustes reales con selector `kg/lb`
  - Inicio con metricas reales y CTA dinamica
  - feedback haptico en entrenamiento
  - proteccion `isDirty` en editor de rutinas
  - tabs `Activas / Archivadas` con `Restaurar`
  - banner dismissible de snapshots persistido en DataStore
- Se toma `docs/branding/Pantalla incio fondo claro/` como nueva referencia visual oficial para el arranque.
- Esa referencia se aterriza a Compose nativo:
  - sin WebView
  - sin HTML embebido
  - sin video
- Se crea `FitTrackPlusAppRoot` para mostrar una intro breve antes del `NavHost`.
- Se copia el asset usado por la intro al modulo de app para no depender de `docs/` en runtime.

Problemas encontrados:

- La referencia visual nueva venia en HTML/CSS con efectos mas complejos de los que conviene mantener en movil real.
- Se simplificaron los efectos a una solucion mantenible en Compose: fondo mineral, glow muy leve, reveal del logo, wordmark y loader corto.
- Una primera verificacion en paralelo (`test` + `build`) provoco un conflicto de caches de KSP.
- La verificacion estable se hizo en secuencia: corregir fallo de compilacion pequeno, parar Gradle si hacia falta y ejecutar `test` y `build` por separado.
- La validacion manual sigue pendiente porque no se ha pasado aun por dispositivo/emulador en esta iteracion.

Decisiones:

- El simbolo sigue siendo el logo principal.
- El wordmark en la intro es solo acompanamiento de producto en arranque.
- Los assets de `docs/branding/` no se usan directamente en runtime; se copian al modulo `app` si forman parte de la experiencia real.
- La intro clara sustituye la direccion previa mas oscura como referencia de arranque.
- La siguiente fase funcional sigue siendo Bloque 4, no Firebase/sync.

Verificacion:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- `.\gradlew.bat test --no-daemon --console=plain` pasa.
- `.\gradlew.bat build --no-daemon --console=plain` pasa.

Pendiente:

- `git push` de los commits locales pendientes.
- Validacion manual completa en dispositivo/emulador.
- Revisar timing real y legibilidad de la intro en movil.

## Consolidacion del starter pack metodologico

Estado:

- completado a nivel documental

Objetivo:

- separar metodologia reusable entre proyectos de los aprendizajes especificos del repo
- dejar una carpeta unica y canonica para reglas generales de trabajo
- reducir dependencia de docs dispersas o demasiado atadas al stack actual

Cambios principales:

- se consolida la base reusable en `docs/project-methodology/`
- se anaden documentos generales sobre:
  - principios operativos
  - jerarquia de decision
  - modelo multiagente
  - ciclo de iteraciones
  - sistema de documentacion
  - verificacion
  - adaptacion a proyectos nuevos
  - blueprints de ficheros
- `docs/development-workflow.md` pasa a actuar como puente corto hacia la carpeta nueva
- `docs/work-methodology/` queda como espacio de aprendizajes historicos y especificos del repo
- `AGENTS.md` y `README.md` se actualizan para apuntar primero a `docs/project-methodology/`

Decisiones:

- la metodologia general no debe vivir mezclada con detalles de Android, Java o Compose
- el starter pack debe servir para crear despues `AGENTS.md`, roadmap, progreso y bitacora de proyectos nuevos con solo anadir stack, restricciones y plan
- la carpeta canonica de metodologia pasa a ser `docs/project-methodology/`

Verificacion:

- revision manual de consistencia documental

Pendiente:

- seguir iterando el starter pack conforme aparezcan patrones nuevos en proyectos futuros

### Segunda pasada de afinado

Objetivo:

- hacer el starter pack mas practico para colaboracion multiagente
- evitar que la metodologia general se actualice por rutina
- eliminar restos especificos de stack dentro de `docs/project-methodology/`

Cambios principales:

- Se anaden documentos operativos:
  - `agent-operating-model.md`
  - `handoff-protocol.md`
  - `collaboration-modes.md`
  - `anti-patterns.md`
  - `methodology-maintenance.md`
- Se ajustan `README.md`, `multi-agent-model.md`, `iteration-model.md`, `documentation-system.md` y `file-blueprints.md`.
- Se convierten archivos antiguos de `docs/project-methodology/` en puentes neutrales hacia la nueva estructura.
- `AGENTS.md` queda alineado con la regla: la metodologia general solo cambia si aparece una regla reusable.

Decisiones:

- Codex queda como ejecutor principal por defecto.
- Claude u otros agentes pueden asistir o ejecutar tareas acotadas con ownership claro.
- Todo relevo entre herramientas debe dejar handoff.
- `docs/project-methodology/` cambia poco y no guarda detalles especificos de stack.
- `docs/work-methodology/` queda como memoria de aprendizajes concretos del repo.

Verificacion:

- Revision documental completada.
- Busqueda de referencias especificas de stack en `docs/project-methodology/` sin resultados.
- No se ejecutan `test` ni `build` porque solo se modifico documentacion.

### Tercera pasada de calidad metodologica

Objetivo:

- revisar claridad, duplicaciones, conflictos y practicidad del starter pack reusable
- reforzar handoff entre Codex y Claude
- aclarar cuando usar modo ligero y cuando modo fase
- hacer mas directa la derivacion de `AGENTS.md` para proyectos nuevos

Cambios principales:

- Se ajusta la regla de ejecucion: no se prohibe colaborar en paralelo, se prohibe ownership ambiguo y solape sobre la misma zona.
- `multi-agent-model.md` explicita integrador principal, ejecutores acotados y relevo Codex/Claude.
- `collaboration-modes.md` gana criterios para elegir modo y reglas para cambiar de modo.
- `handoff-protocol.md` exige origen, destino, rol, ownership y datos especificos para relevos Codex/Claude.
- `iteration-model.md` distingue mejor modo ligero, modo fase y cuando escalar de uno a otro.
- `methodology-maintenance.md` anade una prueba rapida para decidir si una regla merece entrar en metodologia general.
- `project-adaptation.md` y `file-blueprints.md` explican mejor como derivar un `AGENTS.md` concreto.
- `quality-and-verification.md` anade comprobacion de neutralidad del starter pack para cambios de proceso.

Decisiones:

- La metodologia general sigue neutral al stack y no debe actualizarse en cierres ordinarios.
- `docs/work-methodology/` se mantiene como memoria local del repo, no como fuente canonica.
- Codex sigue como ejecutor principal por defecto, con Claude u otros agentes en roles acotados salvo decision explicita.

Verificacion:

- Revision documental completada.
- Busqueda de referencias especificas de stack en `docs/project-methodology/` sin resultados.
- No se ejecutan `test` ni `build` porque solo se modifico documentacion.

### Aclaracion de kickoff metodologico

Objetivo:

- dejar explicito que la metodologia reusable se usa al arrancar o reajustar un proyecto, no como manual diario de cada tarea
- aclarar que `AGENTS.md` mezcla reglas generales y reglas concretas del repo
- reforzar que tras el kickoff se trabaja desde `AGENTS.md` y docs vivos del proyecto

Cambios principales:

- Se crea `project-kickoff.md` para describir la pasada inicial antes del desarrollo tecnico.
- `README.md` incorpora `project-kickoff.md` al mapa y al orden de lectura.
- `project-adaptation.md` explica la mezcla correcta dentro de `AGENTS.md`.
- `file-blueprints.md` separa reglas obligatorias, contexto del proyecto, modos de trabajo y handoff.

Decisiones:

- `docs/project-methodology/` sirve para configurar el sistema de trabajo.
- `AGENTS.md` es el documento operativo diario dentro de cada repo.
- No se debe copiar toda la metodologia general dentro de `AGENTS.md`; solo las reglas ejecutables.

Verificacion:

- Revision documental completada.
- No se ejecutan `test` ni `build` porque solo se modifico documentacion.

### Fichero compartido de coordinacion

Objetivo:

- asegurar que Codex, Claude y otras plataformas tengan un lugar comun dentro del repo para comunicarse
- evitar que el relevo dependa solo de chats separados
- documentar ownership activo, propuestas pendientes, handoffs y verificaciones de relevo

Cambios principales:

- `cross-platform-collaboration.md` define `docs/coordination-log.md` como nombre recomendado.
- `handoff-protocol.md` exige volcar handoffs relevantes al fichero compartido cuando colaboran varias plataformas.
- `project-kickoff.md` incluye crear o definir el fichero compartido durante el arranque.
- `file-blueprints.md` anade blueprint para `coordination-log.md`.

Decisiones:

- El fichero compartido es operativo y corto; no sustituye a `AGENTS.md`, progreso, bitacora ni arquitectura.
- El nombre recomendado es `docs/coordination-log.md`, pero cada proyecto puede usar equivalente si lo declara.
- El chat ayuda, pero no es suficiente como memoria comun entre plataformas.

Verificacion:

- Revision documental completada.
- No se ejecutan `test` ni `build` porque solo se modifico documentacion.

### Colaboracion entre plataformas

Objetivo:

- convertir la coordinacion entre Codex, Claude y otras herramientas en una pieza central de la metodologia
- evitar que varias plataformas compitan por la verdad del repo
- dejar flujos concretos para planificacion, ejecucion, revision, diseno e integracion

Cambios principales:

- Se crea `cross-platform-collaboration.md`.
- `README.md` incorpora el nuevo documento al mapa y orden de lectura.
- `multi-agent-model.md` enlaza a los flujos concretos entre plataformas.
- `project-kickoff.md` exige definir un mapa de plataformas antes de empezar desarrollo tecnico.
- `file-blueprints.md` anade secciones para plataformas, roles e integrador dentro de `AGENTS.md`.

Decisiones:

- Codex sigue como ejecutor principal por defecto si no se indica otra cosa.
- Claude y otras plataformas pueden planificar, revisar, asistir o ejecutar tareas acotadas con ownership claro.
- El repo, las docs vivas y la verificacion mandan por encima de cualquier conversacion o prototipo.

Verificacion:

- Revision documental completada.
- No se ejecutan `test` ni `build` porque solo se modifico documentacion.
