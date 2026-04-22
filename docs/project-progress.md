# FitTrackPlus v2 Progress

Este documento resume donde estamos, que se ha hecho y cual es el siguiente paso.

## Estado Actual

- Rama actual: `codex/phase-2-workout-logging`.
- Commit inicial local: `c1b2f31 Initialize FitTrackPlus v2 mobile foundation`.
- Commit de cierre de Fase 1: `9df5a44 Complete phase 1 routines`.
- Commit de cierre de Fase 2: `Complete phase 2 workout logging`.
- No hay remoto configurado.
- No se ha subido nada a la nube.
- Fase 2 completada tecnicamente.
- Siguiente fase: `phase-3-history`.

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
- Validacion basica antes de guardar:
  - nombre de rutina obligatorio
  - al menos un dia
  - al menos un ejercicio por dia
  - nombre, series y reps obligatorios por ejercicio
- `.kotlin/` agregado a `.gitignore` como salida local de Gradle/Kotlin.
- Guia de metodologia creada en `docs/work-methodology/` para estudiar procedimientos, arquitectura practica, colaboracion con agente, tips y skills practicadas.

### Fase 2 - Registro de entrenamiento

Implementado:

- Rama local `codex/phase-2-workout-logging`.
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

## Verificacion Realizada

Comandos ejecutados:

```powershell
.\gradlew.bat test
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests pasan.
- Build completo pasa.

Pendiente:

- Prueba manual en emulador/dispositivo de Fase 2: `adb` no esta disponible en PATH.
  - Crear o seleccionar rutina Push/Pull/Legs.
  - Iniciar Push.
  - Editar series y volver a Entrenar para confirmar reanudacion.
  - Finalizar y comprobar que el siguiente dia es Pull.
  - Completar ciclo hasta volver a Push en semana 2.
  - Editar rutina y confirmar que el historico mantiene snapshots.

## Decisiones Importantes

- La v2 vive en `app/src/main/kotlin`.
- `app/src/main/java` se trata como legacy local y queda fuera del nuevo repo.
- `app/google-services.json` queda ignorado.
- XML legacy de `layout`, `menu` y `navigation` queda fuera del repo nuevo.
- Algunas versiones de AndroidX/Hilt se ajustaron para ser compatibles con AGP `8.5.1`.
- El registro de entrenamiento permite una unica sesion abierta global.
- Una sesion abierta se reanuda desde Entrenar y bloquea nuevas sesiones hasta finalizarla.
- Finalizar una sesion parcial es valido para el MVP.
- Peso se guarda en `weightKg` y la UI muestra kg.
- Firebase sigue fuera del MVP.
- Al cerrar cada fase se actualiza tambien `docs/work-methodology/` con aprendizajes reutilizables.

## Siguiente Paso

Empezar Fase 3:

1. Crear rama `codex/phase-3-history`.
2. Listar sesiones pasadas.
3. Mostrar detalle historico de sesion.
4. Confirmar que editar rutinas no altera entrenamientos antiguos.
