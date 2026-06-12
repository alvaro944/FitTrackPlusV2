# Phase Log

Bitacora viva del proyecto. Cada fase debe anadir aqui lo que se hizo, lo que se verifico, problemas encontrados y decisiones tomadas.

## Iteracion - Workout entry fixes

Estado:

- Completada tecnicamente.

Rama:

- `codex/ux-improvements`

Objetivo:

- Corregir fricciones puntuales del registro de series en `Entrenar` sin ampliar alcance.
- Mejorar legibilidad del campo de reps.
- Mantener acordeon manual puro.
- Evitar efectos colaterales al editar reps y facilitar reemplazo directo del valor.

Fuera de alcance:

- cambios de schema Room
- cambios en historial/stats fuera del bug de entrada
- nuevas interacciones no descritas en la spec

Cambios principales:

- Entrenar:
  - la columna de reps gana mas espacio horizontal
  - los botones `+/-` de reps se reducen respecto a peso para dejar sitio al input
  - los botones `+/-` de peso se alinean al mismo tamano compacto que los de reps
  - se elimina el auto-colapso al completar un ejercicio; el acordeon queda solo manual
  - peso y reps pasan a `TextFieldValue` local sincronizado con el estado externo
  - al recibir foco, ambos campos seleccionan todo el contenido existente
  - al tocar un campo ya enfocado, la seleccion total tambien se reaplica desde la interaccion real del `TextField`
- Estado y sugerencias:
  - la actualizacion por `setId` queda extraida a helper reusable y testeable
  - las sugerencias de reps ya no pisan valores incompletos que el usuario ya tenia en otros sets
- Tests:
  - se anaden pruebas puras para independencia por `setId`
  - se cubre que las sugerencias preservan reps ya existentes
  - se cubre la logica de `selectAll` y sincronizacion de `TextFieldValue`

Problemas encontrados:

- el bug reportado de reps compartidas no venia del `setId` en si, sino de la recomputacion de sugerencias que sobrescribia sets incompletos con texto ya presente

Decisiones:

- mantener sugerencias iniciales solo para sets vacios; una vez hay texto en un set incompleto, se respeta
- no hacer pasada manual en esta sesion por instruccion explicita del usuario para evitar consumo extra en emulador/capturas

Verificacion:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
bash ./gradlew test --no-daemon --console=plain
bash ./gradlew build --no-daemon --console=plain
```

Resultado:

- Verificacion automatica correcta.
- Sin pasada manual en esta sesion por instruccion explicita del usuario.

Pendiente:

- commit limpio en la rama actual

## Iteracion - Workout screen polish y data integrity

Estado:

- Completada tecnicamente.

Rama:

- `codex/ux-improvements`

Objetivo:

- Ejecutar las 7 tareas del plan de polish de `Entrenar` y de integridad de datos en la rama actual.
- Mejorar legibilidad y foco del registro por ejercicio sin ampliar el alcance de las specs.
- Evitar que sesiones vacias contaminen historial de stats y limpiar correctamente una sesion sin series completadas.

Fuera de alcance:

- cambios de schema Room
- nuevos flujos fuera de `Entrenar`
- nuevas heuristicas de progreso o cambios de producto no descritos en las specs
- ajustes visuales generales fuera del area tocada

Cambios principales:

- Entrenar:
  - la lista de ejercicios pasa a un acordeon estricto dentro de la sesion activa
  - al completar un ejercicio se colapsa y avanza automaticamente al siguiente pendiente
  - el resumen del header muestra progreso claro por ejercicio y estado completado en colapsado
  - la lista respeta mejor el teclado con `imePadding`
  - las filas de series alinean peso y reps aunque aparezca `Ultima vez` bajo el peso
  - los campos de peso/reps se ajustan para movil con placeholders y anchos mas estables
  - el input de peso acepta y conserva separador decimal con coma
- Rutinas:
  - el editor de rutinas resuelve mejor el teclado con `imePadding` y `contentWindowInsets` a cero
- Alternativas:
  - el dialogo de ejercicios alternativos usa scroll vertical e `imePadding` en modo creacion
- Integridad:
  - finalizar una sesion sin ninguna serie completada la descarta en vez de guardarla como historica
  - stats ignora sesiones finalizadas que no tengan ninguna serie con reps > 0
  - se anade borrado de sesion abierta desde repositorio/DAO para soportar el descarte limpio
- Tests:
  - se amplian tests de parsing/sanitizado de peso
  - se cubre separador decimal con coma en el caso de uso de actualizacion
  - se cubre exclusion de sesiones vacias en stats

Problemas encontrados:

- una primera pasada manual detecto que la etiqueta `Reps` se rompia en vertical en ancho movil; se corrigio sustituyendo labels por placeholders y ajustando tamano de botones
- ejecutar `test` y `build` a la vez provoco un fallo espurio de salidas generadas/KSP; la verificacion estable se mantuvo en secuencia

Decisiones:

- mantener el comportamiento de acordeon estricto tambien dentro de la sesion de entrenamiento para reducir ruido visual
- no guardar sesiones vacias en historial ni contarlas en stats; cuando no hay trabajo real, la sesion se descarta
- aceptar coma y punto al parsear peso, pero normalizar la representacion visible hacia coma en UI

Verificacion:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
bash ./gradlew test --no-daemon --console=plain
bash ./gradlew build --no-daemon --console=plain
```

Resultado:

- Verificacion automatica correcta.
- Pasada manual correcta en emulador para:
  - acordeon de ejercicios y avance visual al siguiente bloque
  - estado colapsado/completado por ejercicio
  - layout movil de filas de peso/reps dentro de `Entrenar`, incluida la alineacion con `Ultima vez`
  - foco usable en campos internos del editor de rutinas sin que el contenido relevante quede inaccesible
  - dialogo de alternativas estable al entrar en modo creacion y foco de campos
- El descarte de sesion vacia y el filtrado de stats quedan verificados por logica y tests; no se forzo un flujo manual adicional fuera del alcance visible.

Pendiente:

- commit limpio en la rama actual

## Iteracion - UX improvements de rutinas y entrenamiento

Estado:

- Completada tecnicamente.

Rama:

- `codex/ux-improvements`

Objetivo:

- Ejecutar las 8 tareas del plan UX en una sola rama.
- Hacer mas seguro el editor de rutinas al navegar y cerrar.
- Mejorar la edicion diaria del entrenamiento con defaults, steppers y feedback mas claros.

Fuera de alcance:

- cambios de schema Room
- sync/cloud/Firebase
- nuevos flujos fuera de las 3 specs acordadas
- ampliar la heuristica de progresion mas alla de `UP/DOWN/NONE`

Cambios principales:

- Rutinas:
  - el editor pasa a acordeon estricto con un unico dia expandido
  - duplicar o anadir dia abre el nuevo bloque y colapsa el anterior
  - se anade deteccion de cambios sin guardar en el borrador
  - cerrar editor, back del sistema y cambio de tab/drawer piden confirmacion antes de descartar
- Shell:
  - `AppShellViewModel` coordina navegacion pendiente y confirmacion posterior al descarte
  - la shell navega solo tras aprobacion explicita cuando una pantalla bloquea salida por cambios sin guardar
- Entrenar:
  - se anade `ProgressionHint` con `GetProgressionHintUseCase`
  - cada serie gana stepper de peso `+/- 2.5` con long press `+/- 5.0` y stepper de reps `+/- 1`
  - los inputs sugieren reps iniciales desde el set completado anterior o desde el minimo del rango objetivo
  - al completar una serie cambia el estilo visual de la fila y se emite evento de haptic
- Tests:
  - se amplian tests del editor de rutinas
  - se crean tests del caso de uso de progresion
  - se crean tests de defaults y steppers de entrenamiento

Problemas encontrados:

- `./gradlew` no era ejecutable en este entorno; la verificacion estable se hizo con `bash ./gradlew ...`
- el entorno Java del shell no apuntaba al JBR de Android Studio; hizo falta fijar `JAVA_HOME` antes de verificar
- lint marco `ContextCastToActivity` en Compose; la solucion limpia fue usar `LocalActivity.current`
- el dataset demo de la sesion `Pull` no tenia suficiente historial cerrado por ejercicio para mostrar un badge de progresion visible

Decisiones:

- Mantener el comportamiento del editor en acordeon estricto por decision explicita del usuario
- No inventar una heuristica nueva para hints cuando no hay historial suficiente; en ese caso se mantiene `NONE`
- No tocar datos demo ni sembrar historial artificial solo para forzar un badge visual en la pasada manual

Verificacion:

```bash
bash ./gradlew test --no-daemon --console=plain
bash ./gradlew build --no-daemon --console=plain
```

Resultado:

- Verificacion automatica correcta.
- Pasada manual correcta en emulador para:
  - acordeon del editor de rutinas
  - descarte de cambios sin guardar al cerrar o cambiar de tab
  - steppers de peso/reps
  - estilo de serie completada tras marcar reps
- La visibilidad del badge de progresion queda validada por tests; el dataset demo actual no activa un hint visible en la sesion probada.

Pendiente:

- push de la rama y cierre del ciclo en remoto
- eventual revision/merge de `codex/ux-improvements`

## Iteracion - Final form sidebar shell y release preview

Estado:

- Completada tecnicamente.

Rama:

- `codex/final-form-sidebar-shell`

Objetivo:

- Llevar a la app el shell principal del diseno final usando menu hamburguesa lateral.
- Sacar `Ajustes` de la bottom bar.
- Dejar visibles en UI las acciones futuras del diseno sin implementar backend falso.
- Preparar distribucion preview desde GitHub Releases.

Fuera de alcance:

- widget real nuevo
- export real de datos
- release firmada
- cambios de dominio, Room o snapshots historicos

Cambios principales:

- Se creo una shell compartida Compose en `core/design/AppShell.kt`.
- Se movio la navegacion principal a una bottom bar de 5 tabs: `Inicio`, `Rutinas`, `Entrenar`, `Historial`, `Datos`.
- `Ajustes` deja de ocupar una tab y pasa a abrirse desde un drawer lateral.
- El drawer integra:
  - acceso real a `Ajustes`
  - selector de tema real
  - selector de unidad `kg/lb` real
  - acciones visibles de futuro: `Widget & atajos` y `Exportar datos`
- Se creo `AppShellViewModel` para coordinar preferencias y feedback breve.
- Se creo `NavigationShellConfig.kt` para fijar configuracion reusable del shell.
- Se anadio `NavigationShellConfigTest` para evitar regresiones estructurales del shell.
- Se actualizo `README.md` con el canal de descarga de APK preview.
- Se anadio `.github/workflows/release-preview.yml` para publicar `FitTrackPlus-preview.apk` al subir tags `v*-preview*`.
- Se anadieron al repo los artefactos de referencia de `docs/Final form Fit track 2/` y se excluyeron del versionado el `.zip` y `.thumbnail` locales.

Problemas encontrados:

- La primera verificacion pesada se lanzo en paralelo y disparo errores intermitentes de cache/KSP; la solucion estable fue ejecutar `test` y `build` en secuencia.
- El release de Gradle sigue saliendo `unsigned`; por eso el flujo publico se monto sobre un preview debug honesto en vez de fingir una release de produccion.

Decisiones:

- Mantener la logica actual de rutinas, entreno, historial y stats intacta.
- Mostrar acciones futuras del drawer solo como superficie visual y feedback de "fase futura".
- No publicar `app-release-unsigned.apk` como descarga publica.
- Usar GitHub Releases para previews y reservar la release firmada para una fase posterior con keystore real.

Verificacion:

```bash
./gradlew test --no-daemon --console=plain
./gradlew build --no-daemon --console=plain
./gradlew assembleDebug --no-daemon --console=plain
```

Resultado:

- Verificacion automatica correcta.

Pendiente:

- Pasada manual en emulador/dispositivo para validar shell, drawer, tabs y ajustes visuales finos.

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

## Bloque 7 - GitHub Actions CI

Estado:

- completado tecnicamente

Rama:

- `codex/v2-mejoras`

Objetivo:

- crear un workflow de GitHub Actions para validar el proyecto en remoto
- ejecutarlo en `push` a `main` y `codex/**`
- ejecutarlo tambien en `pull_request`
- correr `test`, `build` y `detekt` sin mezclar otros cambios de producto

Cambios principales:

- Se crea `.github/workflows/ci.yml`.
- El workflow usa `ubuntu-latest` con:
  - `actions/checkout`
  - `actions/setup-java` con Java 17
  - `android-actions/setup-android`
  - `gradle/actions/setup-gradle`
- Se instala en CI `platforms;android-35` y `build-tools;35.0.0` para alinear el runner con `compileSdk 35`.
- Se fuerza `chmod +x ./gradlew` porque el repo trackea `gradlew` con modo `100644` y en Linux no quedaria ejecutable por defecto.
- Las comprobaciones quedan separadas en pasos distintos para que falle con diagnostico claro:
  - `./gradlew test --no-daemon --console=plain`
  - `./gradlew build --no-daemon --console=plain`
  - `./gradlew detekt --no-daemon --console=plain`

Problemas encontrados:

- El repo no tenia aun carpeta `.github/`, asi que no habia CI previa que extender.
- `local.properties` esta ignorado y no debe existir en GitHub Actions, asi que el workflow necesita preparar Android SDK explicitamente.
- `gradlew` esta versionado sin bit ejecutable (`100644`), lo que en runners Linux obliga a ajustar permisos dentro del workflow.
- La primera verificacion local de `test` encontro un `build/` inconsistente: `compileDebugJavaWithJavac` veia factories de KSP/Hilt pero faltaban las salidas Kotlin en `app/build/tmp/kotlin-classes/debug`.
- Regenerar `:app:compileDebugKotlin --rerun-tasks` recompuso esas clases y despues `test`, `build` y `detekt` volvieron a pasar.

Decisiones:

- Mantener un solo job de calidad por ahora; el objetivo del bloque es robustez basica, no paralelizacion prematura.
- Mantener `test`, `build` y `detekt` como pasos independientes para identificar rapido el punto de rotura en GitHub.
- No tocar intro, splash, `strings.xml` ni codigo funcional del producto en este bloque.

Verificacion:

```powershell
.\gradlew.bat :app:compileDebugKotlin --rerun-tasks --no-daemon --console=plain
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
.\gradlew.bat detekt --no-daemon --console=plain
```

Resultado:

- Tests pasan.
- Build completo pasa.
- Detekt pasa.

Pendiente:

- Observar la primera ejecucion real de GitHub Actions tras el proximo push para confirmar el runner remoto.
- Mantener la validacion manual visual en movil/emulador como paso separado del CI.

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

## Cierre tecnico v1 y apertura Roadmap 2.1

Estado:

- v1 funcional completa cerrada tecnicamente
- Roadmap 2.1 abierto como direccion vigente post-v1

Objetivo:

- convertir la auditoria externa de mejoras en un roadmap oficial dentro de `docs/`
- retirar el backlog antiguo de ideas sueltas
- dejar claro que la validacion manual sigue pendiente y no se marca como completada

Cambios principales:

- se crea `docs/roadmap-2.1.md` como documento canonico de mejoras post-v1
- se elimina `docs/future-improvements.md`
- `README.md` apunta al Roadmap 2.1 y actualiza el estado actual
- `docs/project-plan.md` anade cierre tecnico v1 y la seccion Roadmap 2.1
- `docs/project-progress.md` actualiza siguiente paso y fases post-v1
- `docs/adr/0002-sin-exercise-catalog.md` referencia el nuevo roadmap
- `docs/mejoras-claude.md` queda marcado como historico, no como backlog vigente

Decisiones:

- la v1 queda cerrada tecnicamente, no validada manualmente
- Gate 0 del Roadmap 2.1 es validar intro, navegacion, dark mode, accesibilidad y flujo completo en dispositivo/emulador
- la primera fase de producto nueva pasa a ser Fase 2.1A de estabilidad y fricciones criticas
- Firebase/sync sigue diferido hasta cerrar prioridades locales de V2.1
- `docs/project-methodology/` no se modifica porque no aparecio una regla reusable nueva

Verificacion:

- revision documental y busqueda de referencias obsoletas
- no se ejecutan `test` ni `build` porque solo se modifica documentacion

Pendiente:

- ejecutar Gate 0 en dispositivo/emulador
- abrir Fase 2.1A si Gate 0 no detecta bloqueantes

## Migracion GPT-5.5 para agentes y futuras integraciones

Estado:

- completado a nivel documental

Objetivo:

- revisar si el repo tenia un modelo OpenAI que migrar
- dejar el criterio operativo para usar `gpt-5.5` en futuras sesiones, prompts o integraciones

Cambios principales:

- se confirma con busqueda en el repo que la app Android no tiene llamadas runtime a OpenAI ni IDs de modelo existentes
- `AGENTS.md` anade una seccion OpenAI/GPT-5.5 para futuras integraciones
- `docs/work-methodology/tips-and-skills.md` guarda el aprendizaje local de migracion
- `docs/project-progress.md` registra que no habia superficie de API que cambiar

Decisiones:

- no se toca codigo Android porque no hay integracion OpenAI en `app/src/main/kotlin`
- si aparece OpenAI API en el futuro, partir de `gpt-5.5`, Responses API y `reasoning.effort = medium` para trabajo complejo
- no exponer claves OpenAI en el cliente Android; disenar primero backend o proxy seguro
- `docs/project-methodology/` no se modifica porque esto queda como aprendizaje especifico del repo

Verificacion:

- busqueda amplia con `rg --hidden` de modelos, OpenAI, Responses API y claves relacionadas
- revision documental de las notas editadas
- no se ejecutan `test` ni `build` porque solo se modifico documentacion

Pendiente:

- instalar o reintentar el MCP oficial de OpenAI Docs fuera de este entorno si se quiere usarlo en futuras sesiones; el intento desde Codex fallo por permisos de `codex.exe`

## Gate 0 Roadmap 2.1 - Validacion manual v1 tecnica

Estado:

- ejecutado en telefono fisico conectado
- ajustes bloqueantes/acotados detectados y corregidos en workspace
- verificacion automatica completada tras parar daemons Gradle/Kotlin colgados
- revalidacion manual del usuario completada
- Gate 0 queda cerrado con issues menores delegados a Claude

Objetivo:

- validar que la v1 tecnica funciona como experiencia real antes de abrir Fase 2.1A
- no introducir features nuevas
- aplicar solo ajustes pequenos si aparecian problemas reales

Validado:

- Telefono detectado por `adb devices`: `29211FDH200D4H`.
- Modo oscuro en Inicio, Rutinas, Entrenar, Historial y Datos.
- Navegacion inferior entre tabs principales.
- Creacion de rutina de prueba `Gate0`.
- Activacion de rutina.
- Inicio de entrenamiento desde rutina activa.
- Registro de una serie mediante inputs reales en pantalla.
- Finalizacion de entrenamiento con dialogo de confirmacion.
- Historial muestra la sesion finalizada.
- Detalle historico muestra snapshot de rutina, dia, ejercicio y series.
- Datos incorpora la sesion finalizada en sesiones y volumen.
- Edicion posterior de la rutina activa a `GateEdit0` no cambio el snapshot historico, que siguio mostrando `Gate0`.
- Revalidacion del usuario:
  - inicio/arranque OK
  - Rutinas OK
  - Historial OK
  - Datos OK
  - snapshots historicos OK

Problemas encontrados:

- Back fisico desde detalle de Historial volvia a Inicio en vez de volver al listado.
- En rutinas inactivas, la fila con tres acciones (`Activar`, `Editar`, `Archivar`) partia texto en botones en el ancho real del telefono.
- El dialogo de finalizar entrenamiento mostraba artefactos claros en dark mode.
- La entrada automatica con `adb shell input text` concatena sobre valores existentes; por eso una serie de prueba quedo con volumen inflado. No se considera bug funcional de producto para Gate 0.
- La intro Compose intermedia no se pudo capturar de forma fiable con capturas estaticas; si se quiere cerrar ese punto con rigor, hace falta observacion humana directa o video.
- Revalidacion posterior del usuario detecta issues menores pendientes en Entrenar:
  - al escribir en campos de peso/reps con `0`, el valor no se selecciona ni se reemplaza y puede quedar `010`
  - al enfocar peso/reps aparecen bordes blancos en dark mode
  - el dialogo de finalizar sigue mostrando bordes blancos
  - editar el nombre de una rutina activa no refresca inmediatamente la preview de Entrenar hasta cambiar la rutina activa y volver

Decisiones:

- Cerrar Gate 0 como validado con issues menores delegados.
- Abrir Fase 2.1A sin mezclarla con los ajustes menores de Gate 0.
- Encargar los issues menores a Claude mediante handoff compartido.
- Mantener los cambios limitados a UI/back behavior.
- No tocar Firebase/sync, Room, repositorios ni arquitectura.
- No actualizar `docs/project-methodology/` porque no aparecio una regla general reusable.
- No actualizar `docs/work-methodology/` porque no aparecio un aprendizaje tecnico especifico nuevo que merezca regla local.

Ajustes aplicados:

- `HistoryScreen` gana `BackHandler` cuando hay detalle visible.
- `RoutinesScreen` reorganiza acciones de rutina inactiva: `Activar` queda a ancho completo y `Editar`/`Archivar` comparten una segunda fila.
- `WorkoutScreen` fija `shape` y colores del `AlertDialog` de finalizar entrenamiento para dark mode.

Verificacion realizada:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat --stop
.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain
Stop-Process -Id 8960,14320 -Force
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Los primeros intentos quedaron en timeout por un `GradleDaemon 8.7` y un `KotlinCompileDaemon` colgados.
- Tras parar esos procesos, `test` paso con exit code 0.
- `build` paso con `BUILD SUCCESSFUL in 4m 5s`.
- El build mantiene warnings conocidos de AGP/compileSdk 35 y D8/Kotlin metadata.
- La instalacion del APK actualizado no se pudo ejecutar porque `adb` dejo de listar el telefono.
- El usuario compilo/reviso despues en telefono y confirmo que inicio, Rutinas, Historial, Datos y snapshots quedan OK.

Handoff:

- `docs/coordination/claude-gate0-minor-fixes.md`

Pendiente:

- Claude debe corregir los issues menores delegados de Entrenar/preview.
- Integrar el resultado de Claude solo si mantiene alcance acotado y pasa `test` + `build`.
- Abrir Fase 2.1A como siguiente fase de producto.

## Fase 2.1A - Estabilidad y fricciones criticas

Estado:

- implementada en workspace
- verificacion automatica completada

Objetivo:

- corregir fricciones criticas del Roadmap 2.1 sin abrir features de 2.1B
- mantener arquitectura, Room, Firebase/sync y snapshots historicos intactos
- conservar el WIP de fixes menores de Claude ya presente en Entrenar

Cambios principales:

- Home consume `HomeUiState.message` y muestra feedback mediante snackbar.
- Settings expone mensaje efimero al cambiar unidad `kg/lb` y no muestra confirmacion si se pulsa la unidad ya seleccionada.
- Rutinas gana validacion inline para nombre de rutina, dias, ejercicios, series y reps objetivo.
- Reps objetivo acepta formatos acotados: `8`, `8-12`, `AMRAP`, `RPE 8`; valores absurdos bloquean guardado.
- Entrenar conserva los fixes menores de Gate 0 ya aplicados en workspace:
  - al enfocar campos con `0`, se facilita reemplazo directo
  - inputs usan colores del tema en foco
  - dialogo de finalizar usa `Dialog` + `Surface`
  - preview se refresca cuando cambia la rutina activa editable
- La normalizacion de peso/reps se mantiene en `UpdateWorkoutSetUseCase` y queda cubierta por tests.
- Historial conserva `BackHandler` y anade en detalle:
  - notas de sesion
  - notas por set
  - duracion calculada
  - volumen total
  - mejor set por volumen

Decisiones:

- No tocar Firebase/sync, schema Room ni repositorios por esta fase.
- Calcular metricas de Historial en `HistoryDetailUiState`, no dentro del composable.
- Mantener el editor de Rutinas simple; se anaden mensajes inline sin cambiar flujo ni navegacion.
- No actualizar `docs/project-methodology/` porque no aparecio una regla general reusable.
- No actualizar `docs/work-methodology/` porque no aparecio un aprendizaje especifico nuevo que merezca documentacion local.

Verificacion realizada:

```powershell
.\gradlew.bat :app:compileDebugKotlin --rerun-tasks --no-daemon --console=plain
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Los tests focalizados fallaron primero como se esperaba porque Historial aun no exponia metricas de detalle.
- Un primer `test` completo quedo en timeout sin salida.
- `:app:testDebugUnitTest` encontro un build local inconsistente: Kotlin no pudo leer `R.jar` en `app/build/intermediates`.
- `:app:compileDebugKotlin --rerun-tasks` regenero las salidas y paso.
- `test` paso con `BUILD SUCCESSFUL in 3m 41s`.
- `build` paso con `BUILD SUCCESSFUL in 10m 34s`.
- Se mantienen warnings conocidos de AGP/compileSdk 35 y D8/Kotlin metadata.

Pendiente:

- Validar manualmente pantallas tocadas en dispositivo/emulador.

## Fase 2.1B.1 - Rutinas mas utiles

Estado:

- implementada en workspace
- verificacion automatica completada

Rama:

- `codex/phase-2.1b-routines`

Objetivo:

- reducir friccion al crear rutinas
- acelerar edicion con duplicado y reordenado
- mantener Room, Firebase/sync y snapshots historicos intactos

Cambios principales:

- Se crea un catalogo local de plantillas en `feature/routines`:
  - Push Pull Legs
  - Upper Lower
  - Full Body
- Las plantillas abren el editor como draft editable antes de guardar.
- Rutinas muestra plantillas en la tab de activas como accion rapida.
- El editor permite:
  - duplicar dias
  - subir/bajar dias
  - duplicar ejercicios
  - subir/bajar ejercicios dentro del mismo dia
- Las operaciones actuan sobre `RoutineEditorUiState`; el guardado sigue usando `RoutineDraft` y el orden de listas.
- No se toca schema Room, repositorios, Firebase/sync ni reglas de historial.

Tests anadidos:

- `RoutineTemplatesTest`
- `RoutineEditorOperationsTest`

Verificacion realizada:

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain --tests "com.alvarocervantes.fittrackplus.feature.routines.RoutineTemplatesTest" --tests "com.alvarocervantes.fittrackplus.feature.routines.RoutineEditorOperationsTest" --tests "com.alvarocervantes.fittrackplus.feature.routines.RoutineEditorUiStateTest"
.\gradlew.bat clean test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
.\gradlew.bat test --no-daemon --console=plain
```

Resultado:

- Tests focalizados de Rutinas pasan.
- `clean test` pasa con `BUILD SUCCESSFUL in 6m 40s`.
- `build` pasa con `BUILD SUCCESSFUL in 7m 15s`.
- `test` final pasa con `BUILD SUCCESSFUL in 3m 47s` tras liberar locks locales.
- Se mantienen warnings conocidos de AGP/compileSdk 35, D8/Kotlin metadata y deprecaciones de iconos ya existentes.
- Antes de la verificacion final hubo que parar procesos Java/Kotlin que bloqueaban salidas KSP generadas en Windows.

Pendiente:

- Validar manualmente flujo de plantillas, duplicado/reordenado y preview en Entrenar.
- Revision acotada inicialmente preparada para Claude completada por Codex por decision del usuario; no se envian mas tareas a Claude por ahora.

## Fase 2.1B.2 - Timer de descanso

Estado:

- implementada en workspace
- verificacion automatica completada

Rama:

- `codex/phase-2.1b-timer`

Objetivo:

- anadir un timer local de descanso en Entrenar
- mejorar el uso diario durante una sesion activa
- mantener Room, DataStore, Firebase/sync y snapshots historicos intactos

Cambios principales:

- `WorkoutUiState` gana estado local de timer de descanso.
- `WorkoutViewModel` expone eventos para iniciar, pausar, reanudar, reiniciar, cancelar y activar auto-arranque.
- El countdown vive en el ViewModel con coroutine local y no se persiste.
- Auto-arranque queda desactivado por defecto y solo dispara cuando reps pasan de vacio/0/invalido a valor positivo.
- Auto-arranque no pisa un timer que ya esta corriendo o pausado.
- `WorkoutScreen` muestra una card compacta de descanso solo durante sesion activa:
  - botones rapidos `60s`, `90s`, `120s`
  - pausa/reanudar, reset y cancelar
  - switch `Auto`
  - feedback haptico al terminar
- Al finalizar entrenamiento se cancela el timer.

Tests anadidos:

- `RestTimerStateTest`

Verificacion realizada hasta ahora:

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain --tests "com.alvarocervantes.fittrackplus.feature.workout.RestTimerStateTest"
.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain --tests "com.alvarocervantes.fittrackplus.feature.workout.RestTimerStateTest" --tests "com.alvarocervantes.fittrackplus.domain.usecase.UpdateWorkoutSetUseCaseTest" --tests "com.alvarocervantes.fittrackplus.domain.usecase.StartWorkoutSessionUseCaseTest"
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests RED fallaron primero por simbolos inexistentes del timer.
- Tests focalizados de timer y registro de entrenamiento pasan.
- `test` completo pasa.
- `build` completo pasa con `BUILD SUCCESSFUL in 9m 9s`.
- Se mantienen warnings conocidos de AGP/compileSdk 35 y D8/Kotlin metadata.

Pendiente:

- Validar manualmente en dispositivo/emulador cuando haya `adb` disponible; en esta sesion `adb` no esta en PATH.

## Fase 2.1B.3 - Historial con filtros y orden

Estado:

- implementada en workspace
- verificacion automatica completada

Rama:

- `codex/phase-2.1b-history-filters`

Objetivo:

- hacer mas util el listado de Historial cuando haya muchas sesiones
- anadir filtros por periodo y orden configurable
- mantener Room, Firebase/sync y snapshots historicos intactos

Cambios principales:

- Historial gana filtro de periodo:
  - Todo
  - 4 semanas
  - 12 semanas
- Historial gana orden:
  - Reciente
  - Antiguo
  - Mayor volumen
- `ObserveWorkoutHistoryUseCase` lee sesiones finalizadas con ejercicios y series para calcular resumen enriquecido.
- El resumen del listado incluye volumen total, duracion y numero de series.
- `HistoryViewModel` conserva sesiones completas y expone la lista filtrada/ordenada para Compose.
- Compose solo pinta controles y envia eventos de filtro/orden.
- Se crea `docs/visual-improvements.md` como backlog visual no obligatorio.
- No se toca schema Room, Firebase/sync, repositorios ni reglas de snapshots.

Tests anadidos o ampliados:

- `ObserveWorkoutHistoryUseCaseTest`
- `HistoryFiltersTest`

Verificacion realizada:

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain --tests "com.alvarocervantes.fittrackplus.domain.usecase.ObserveWorkoutHistoryUseCaseTest" --tests "com.alvarocervantes.fittrackplus.feature.history.HistoryFiltersTest"
.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain --tests "com.alvarocervantes.fittrackplus.domain.usecase.ObserveWorkoutHistoryUseCaseTest" --tests "com.alvarocervantes.fittrackplus.feature.history.HistoryFiltersTest" --tests "com.alvarocervantes.fittrackplus.feature.history.HistoryDetailUiStateTest"
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests RED fallaron primero por campos/helpers inexistentes.
- Tests focalizados de Historial pasan.
- Test completo pasa.
- El primer build completo fallo por `detekt` con una linea demasiado larga en `HistoryScreen`; se corrigio formato sin cambiar comportamiento.
- Build completo pasa.
- Se mantienen warnings conocidos de AGP/compileSdk 35, D8/Kotlin metadata y deprecacion de `Icons.Filled.ArrowBack`.

Pendiente:

- Validacion manual conjunta de 2.1B.1, 2.1B.2 y 2.1B.3 queda pendiente para una pasada posterior.

## Fase 2.1B.4 - Datos con periodos y tooltip

Estado:

- implementada en workspace
- verificacion automatica completada

Rama:

- `codex/phase-2.1b-stats-periods`

Objetivo:

- hacer que Datos sea consultable por periodo
- aportar contexto al tocar puntos de la grafica de progreso
- mantener Room, Firebase/sync y snapshots historicos intactos

Cambios principales:

- Datos gana filtro de periodo:
  - Todo
  - 4 semanas
  - 12 semanas
- `ObserveWorkoutStatsUseCase` acepta periodo y `nowMillis`, y filtra sesiones finalizadas antes de calcular estadisticas.
- Resumen, volumen por sesion, progreso por ejercicio y mejores marcas se recalculan dentro del periodo seleccionado.
- `StatsViewModel` conserva el ejercicio seleccionado si sigue existiendo en el nuevo periodo y limpia el punto seleccionado al cambiar periodo o ejercicio.
- `LineChart` permite seleccionar puntos sin mover reglas de datos a Compose.
- La grafica muestra un detalle del punto con fecha, peso maximo, volumen, reps y 1RM estimado.
- `docs/visual-improvements.md` marca periodos y tooltip de Datos como aterrizados.
- No se toca schema Room, Firebase/sync, repositorios ni reglas de snapshots.

Tests anadidos o ampliados:

- `ObserveWorkoutStatsUseCaseTest`
- `StatsUiStateTest`

Verificacion realizada:

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain --tests "com.alvarocervantes.fittrackplus.domain.usecase.ObserveWorkoutStatsUseCaseTest" --tests "com.alvarocervantes.fittrackplus.feature.stats.StatsUiStateTest"
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests RED fallaron primero por simbolos inexistentes de periodo, helpers de estado y puntos enriquecidos.
- Una expectativa del test de 4 semanas se corrigio porque el mejor volumen de set real era `80x12 = 960`, no `90x10 = 900`.
- Tests focalizados de Datos pasan.
- Test completo pasa.
- El primer build completo fallo por `detekt` porque `toWorkoutStats` superaba la longitud permitida; se dividio en helpers pequenos sin cambiar comportamiento.
- Build completo pasa.

Pendiente:

- Validacion manual conjunta de 2.1B.1, 2.1B.2, 2.1B.3 y 2.1B.4 queda pendiente para una pasada posterior.

## Fase 2.1B.5 - Comparativa de sesion + selector de tema

Estado:

- implementada en workspace
- verificacion automatica completada

Rama:

- `codex/phase-2.1b-comparison-theme`

Objetivo:

- anadir una comparativa compacta en el detalle de Historial
- permitir elegir tema `Sistema`, `Claro` u `Oscuro` desde Ajustes
- mantener Room, Firebase/sync y snapshots historicos intactos

Cambios principales:

- `GetWorkoutHistoryDetailUseCase` calcula la sesion anterior comparable usando `routineNameSnapshot` y `dayNameSnapshot`.
- El detalle de Historial expone deltas de volumen, duracion, numero de series y mejor set por volumen.
- Si no hay sesion anterior comparable, la UI muestra `Primera sesion comparable`.
- Ajustes gana selector exclusivo de tema con `Sistema`, `Claro` y `Oscuro`.
- `UserPreferencesRepository` persiste el modo de tema como string simple en DataStore.
- `MainActivity` aplica el tema desde el root con `FitTrackPlusTheme`.
- `Sistema` respeta `isSystemInDarkTheme()`, `Claro` fuerza light y `Oscuro` fuerza dark.
- `docs/visual-improvements.md` marca comparativa de Historial y selector de tema como aterrizados.

Tests anadidos o ampliados:

- `GetWorkoutHistoryDetailUseCaseTest`
- `AppThemeModeTest`
- `SettingsPreferenceMessagesTest`

Verificacion realizada hasta ahora:

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain --tests "com.alvarocervantes.fittrackplus.domain.usecase.GetWorkoutHistoryDetailUseCaseTest" --tests "com.alvarocervantes.fittrackplus.core.design.AppThemeModeTest" --tests "com.alvarocervantes.fittrackplus.feature.settings.SettingsPreferenceMessagesTest"
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests RED fallaron primero por simbolos inexistentes de comparativa, tema y mensaje de Ajustes.
- Tests focalizados pasan.
- Test completo pasa.
- Build completo pasa con warnings conocidos de AGP/compileSdk 35 y D8/Kotlin metadata.

Pendiente:

- Validacion manual conjunta de 2.1B.1 a 2.1B.5 queda pendiente para una pasada posterior.

## Sprint 2.1C.A - Heatmap + PR en vivo + Celebracion

Estado:

- implementada y verificada
- commit: `16d07fc Complete phase 2.1C.A heatmap, PR live detection and celebration`

Rama:

- `codex/phase-2.1c-a-heatmap-pr-celebration`

Objetivo:

- anadir heatmap calendario de actividad en Stats (volumen diario, 5 niveles)
- detectar records personales en vivo durante la sesion activa (PR de peso y de volumen)
- celebrar sesiones con PRs mediante overlay de confetti + haptic doble

Cambios principales:

- `domain/model/StatsModels.kt` - nuevo `data class HeatmapDay(epochDay, totalVolumeKg, intensityLevel)`
- `domain/model/WorkoutModels.kt` - nuevo `enum class PrType { MaxWeight, MaxVolume }`
- `domain/usecase/GetWorkoutHeatmapUseCase.kt` - agrega volumen por dia (365 dias), calcula 5 niveles por percentiles
- `domain/usecase/DetectPersonalRecordUseCase.kt` - compara (exerciseName, weightKg, reps) contra historico finalizado
- `data/local/dao/WorkoutDao.kt` - queries `getMaxWeightForExercise`, `getMaxSetVolumeForExercise`, `getFinishedSessionsSince`
- `core/design/components/HeatmapCalendar.kt` - Canvas custom 7x53, drawRoundRect por celda, colorScale de 5 tonos
- `core/design/components/ConfettiAnimation.kt` - 40 particulas, Animatable 0-1, gravedad simulada, fade en ultimo 25%
- `core/design/Theme.kt` - `heatmapScale: List<Color>` en `FitTrackPlusExtraColors`
- `feature/stats/StatsViewModel.kt` - expone `heatmapDays`, maneja tap en celda con snackbar fecha+volumen
- `feature/stats/StatsScreen.kt` - seccion "Constancia" con `HeatmapCalendar`
- `feature/workout/WorkoutViewModel.kt` - PR detection post-persistSet, haptic channel, `CelebrationData`, `prCount`
- `feature/workout/WorkoutScreen.kt` - badge PR (peso/volumen), haptic doble via `LaunchedEffect`, overlay confetti

Tests anadidos:

- `GetWorkoutHeatmapUseCaseTest`
- `DetectPersonalRecordUseCaseTest`

Verificacion:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests pasan.
- Build pasa.

Pendiente:

- Validacion manual en dispositivo.

## Sprint 2.1C.B - Skeleton loaders + Demo data + Onboarding

Estado:

- implementada
- pendiente: build completo + validacion manual

Rama:

- `codex/phase-2.1c-b-skeletons-demo-onboarding`

Objetivo:

- subir performance percibida con skeleton loaders por pantalla (shimmer)
- facilitar evaluacion del portfolio con demo data on demand en debug
- onboarding minimo de 3 paginas para nuevos usuarios

Cambios principales:

- `core/design/components/Shimmer.kt` - `Modifier.shimmer()` con `composed {}` + `Brush.linearGradient` animado
- `core/design/components/Skeletons.kt` - `SkeletonBlock`, `SkeletonText`, `SkeletonCard`
- `core/design/States.kt` - `FitTrackLoadingCard` marcada `@Deprecated` con `replaceWith`
- `feature/home/HomeScreen.kt` - skeleton en hero card cuando `isLoading`
- `feature/routines/RoutinesScreen.kt` - `RoutineListItemSkeleton` (3x) reemplaza `FitTrackLoadingCard`
- `feature/workout/WorkoutScreen.kt` - `WorkoutLoadingSkeleton` (summary + 2 exercise cards)
- `feature/history/HistoryScreen.kt` - `HistorySessionCardSkeleton` (5x) y `HistoryDetailSummarySkeleton` + `HistoryComparisonSkeleton`
- `feature/stats/StatsScreen.kt` - `StatsLoadingSkeleton` (period controls + grid + heatmap + 4 cards)
- `data/local/dao/WorkoutDao.kt` - `deleteAllSessions()`
- `data/local/dao/RoutineDao.kt` - `deleteAllRoutines()`
- `data/local/seed/DebugDemoDataSeeder.kt` - `reseed()` (wipe + re-seed atomico)
- `data/preferences/UserPreferencesRepository.kt` - `hasSeenOnboarding` Flow + `setHasSeenOnboarding()`
- `feature/settings/SettingsViewModel.kt` - `isDebugBuild`, `reloadDemoData()`
- `feature/settings/SettingsScreen.kt` - seccion "Datos de demostracion" condicional (debug only) con dialog de confirmacion
- `feature/onboarding/OnboardingScreen.kt` - `HorizontalPager` 3 paginas, dots indicator, "Siguiente"/"Empezar"/"Saltar"
- `feature/launch/LaunchIntroScreen.kt` - `FitTrackPlusAppRoot` gate intro -> onboarding -> NavHost
- `MainActivity.kt` - colecta `hasSeenOnboarding`, pasa `onOnboardingComplete` callback
- `CLAUDE.md` - seccion de skills disponibles
- `docs/work-methodology/available-skills.md` - documentacion completa de 12 skills

Tests anadidos:

- `UserPreferencesRepositoryTest` (instrumentado) - `hasSeenOnboarding_defaultFalse`, `setHasSeenOnboarding_persistsTrue`

Verificacion:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Pendiente:

- Validacion manual: shimmer visible con datos vacios, demo data desde Ajustes debug, onboarding en primer arranque.

## Fase 2.1C.C - Shortcuts + Widget + Notificacion

Estado:

- implementada y verificada (test + build pasan)
- pendiente: validacion manual en dispositivo

Rama:

- `codex/phase-2.1c-c-shortcuts-widget-notification`

Commit:

- `ee7649b Complete phase 2.1C.C shortcuts-widget-notification`

Objetivo:

- integracion nativa Android visible desde fuera de la app
- app shortcuts en launcher (long-press)
- widget homescreen 2x1 con racha y sesiones de la semana
- notificacion silenciosa persistente mientras hay sesion activa

Cambios principales:

- `app/src/main/res/xml/shortcuts.xml` - atajos estaticos "Entrenar" y "Stats" con extra `open_tab`
- `AndroidManifest.xml` - shortcuts meta-data, widget receiver, permiso `POST_NOTIFICATIONS`
- `app/src/main/res/values/strings.xml` - etiquetas de shortcuts y descripcion del widget
- `core/navigation/FitTrackPlusNavHost.kt` - parametro `initialTab: AppRoute?` + `LaunchedEffect`
- `feature/launch/LaunchIntroScreen.kt` - `FitTrackPlusAppRoot` acepta `initialTab`
- `MainActivity.kt` - lee extra del intent, solicita permiso `POST_NOTIFICATIONS` tras onboarding
- `gradle/libs.versions.toml` + `build.gradle.kts` - Glance 1.1.0, lifecycle-process, desugar_jdk_libs
- `isCoreLibraryDesugaringEnabled = true` - soporte java.time.* en minSdk 23
- `domain/usecase/GetWorkoutStreakUseCase.kt` - algoritmo de racha consecutiva con deduplicacion por dia
- `app/src/main/res/xml/fittrackplus_widget_info.xml` - metadata del widget 2x1
- `feature/widget/WidgetEntryPoint.kt` - Hilt EntryPoint para acceso al repositorio desde Glance
- `feature/widget/FitTrackPlusWidget.kt` - widget con racha + sesiones semana, tap abre app
- `feature/widget/FitTrackPlusWidgetReceiver.kt` - GlanceAppWidgetReceiver
- `data/local/dao/WorkoutDao.kt` - `observeActiveSession()` Flow para sesion sin finalizar
- `data/repository/WorkoutRepository.kt` + `DefaultWorkoutRepository.kt` - expone `observeActiveSession()`
- `core/notification/ActiveSessionNotificationManager.kt` - canal IMPORTANCE_LOW + show/cancel
- `core/notification/ActiveSessionObserver.kt` - colecta el Flow y llama al manager
- `FitTrackPlusApp.kt` - inyecta y arranca `ActiveSessionObserver` en el lifecycle del proceso
- 7 fakes de tests actualizados con `observeActiveSession()` stub

Tests anadidos:

- `GetWorkoutStreakUseCaseTest` - 7 casos: 0 sesiones, 1 hoy, 1 ayer, 1 hace 2 dias, 7 consecutivos, gap rompe racha, mismo dia cuenta una vez

Verificacion:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Resultado: BUILD SUCCESSFUL (124 tareas), Detekt limpio, Lint limpio, todos los tests pasan.

Pendiente:

- Validacion manual en dispositivo: long-press icono launcher, anadir widget, iniciar sesion (notificacion), finalizar (notificacion desaparece), Android 13+ dialog de permiso.

## Fase 2.1C - Final form visual

Estado:

- implementada y verificada sobre `codex/phase-2.1c-final-form-design`
- fuente visual: `docs/Final form Fit track/`

Objetivo:

- acercar la app nativa al prototipo final sin abrir cambios de arquitectura ni persistencia

Cambios principales:

- paleta clara/oscura, bordes y radios ajustados al lenguaje visual final
- `FitTrackRadialTimer` como indicador radial reutilizable para el descanso
- Home con tira semanal compacta
- Entrenar con timer radial y acciones agrupadas
- Ajustes con selector segmentado de unidad y selector visual exclusivo de tema

Verificacion:

- `.\gradlew.bat test --no-daemon --console=plain`
- `.\gradlew.bat build --no-daemon --console=plain`
- Resultado: BUILD SUCCESSFUL

Pendiente:

- validacion manual conjunta en dispositivo/emulador junto con el resto de 2.1B/2.1C
