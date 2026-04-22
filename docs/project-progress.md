# FitTrackPlus v2 Progress

Este documento resume donde estamos, que se ha hecho y cual es el siguiente paso.

## Estado Actual

- Rama actual: `codex/phase-0-mobile-foundation`.
- Commit inicial local: `c1b2f31 Initialize FitTrackPlus v2 mobile foundation`.
- No hay remoto configurado.
- No se ha subido nada a la nube.
- Siguiente fase: `phase-1-routines`.

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

Git:

- Se inicializo repositorio Git local.
- Se creo commit inicial en `main`.
- Se creo rama local `codex/phase-0-mobile-foundation`.
- No se configuro remoto.

## Verificacion Realizada

Comandos ejecutados:

```powershell
.\gradlew.bat clean test
.\gradlew.bat build --no-daemon
```

Resultado:

- Tests pasan.
- Build completo pasa.

Pendiente:

- Prueba manual en emulador/dispositivo.

## Decisiones Importantes

- La v2 vive en `app/src/main/kotlin`.
- `app/src/main/java` se trata como legacy local y queda fuera del nuevo repo.
- `app/google-services.json` queda ignorado.
- XML legacy de `layout`, `menu` y `navigation` queda fuera del repo nuevo.
- Algunas versiones de AndroidX/Hilt se ajustaron para ser compatibles con AGP `8.5.1`.

## Siguiente Paso

Empezar Fase 1:

1. Crear rama `codex/phase-1-routines`.
2. Implementar flujo real de rutinas.
3. Crear ViewModel y estados de UI.
4. Guardar y observar rutinas con Room.
5. Seleccionar rutina activa con DataStore.
6. Verificar con tests/build y prueba manual.
