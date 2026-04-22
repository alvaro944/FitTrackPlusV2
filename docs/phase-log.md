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
