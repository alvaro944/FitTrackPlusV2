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
