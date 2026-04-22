# FitTrackPlus v2 Progress

Este documento resume donde estamos, que se ha hecho y cual es el siguiente paso.

## Estado Actual

- Rama actual: `codex/phase-1-routines`.
- Commit inicial local: `c1b2f31 Initialize FitTrackPlus v2 mobile foundation`.
- Commit de cierre de Fase 1: `Complete phase 1 routines`.
- No hay remoto configurado.
- No se ha subido nada a la nube.
- Fase 1 completada tecnicamente.
- Siguiente fase: `phase-2-workout-logging`.

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

Documentacion creada:

- `README.md`
- `AGENTS.md`
- `docs/development-workflow.md`
- `docs/architecture.md`
- `docs/future-improvements.md`
- `docs/project-plan.md`
- `docs/project-progress.md`
- `docs/phase-log.md`
- `docs/work-methodology/`

Git:

- Se inicializo repositorio Git local.
- Se creo commit inicial en `main`.
- Se creo rama local `codex/phase-0-mobile-foundation`.
- No se configuro remoto.

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

## Verificacion Realizada

Comandos ejecutados:

```powershell
.\gradlew.bat clean test
.\gradlew.bat build --no-daemon
.\gradlew.bat test
.\gradlew.bat build --no-daemon --console=plain
```

Resultado:

- Tests pasan.
- Build completo pasa.

Pendiente:

- Prueba manual en emulador/dispositivo de Fase 1:
  - Crear rutina Push/Pull/Legs.
  - Editar dias y ejercicios.
  - Seleccionar rutina activa.
  - Cerrar y abrir la app para comprobar persistencia.

## Decisiones Importantes

- La v2 vive en `app/src/main/kotlin`.
- `app/src/main/java` se trata como legacy local y queda fuera del nuevo repo.
- `app/google-services.json` queda ignorado.
- XML legacy de `layout`, `menu` y `navigation` queda fuera del repo nuevo.
- Algunas versiones de AndroidX/Hilt se ajustaron para ser compatibles con AGP `8.5.1`.
- Al cerrar cada fase se actualiza tambien `docs/work-methodology/` con aprendizajes reutilizables.

## Siguiente Paso

Empezar Fase 2:

1. Crear rama `codex/phase-2-workout-logging`.
2. Iniciar entrenamiento desde la rutina activa.
3. Crear sesion usando snapshot de rutina.
4. Registrar series con peso y repeticiones.
5. Finalizar sesion.
6. Verificar ciclo de dias y persistencia del historial.
