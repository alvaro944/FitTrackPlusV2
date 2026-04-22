# FitTrackPlus v2

FitTrackPlus es una aplicacion Android nativa para crear rutinas de gimnasio, registrar entrenamientos reales y conservar un historial consistente aunque las rutinas cambien con el tiempo.

## Estado Actual

Fase actual: `phase-4-statistics-mvp` completada tecnicamente.
Siguiente fase: `phase-5-ux-polish`.

La app esta arrancando una base nueva en Jetpack Compose. La v2 se compila desde `app/src/main/kotlin`.

Para no perder contexto entre sesiones:

- Plan maestro: `docs/project-plan.md`
- Progreso actual: `docs/project-progress.md`
- Bitacora por fases: `docs/phase-log.md`
- Arquitectura: `docs/architecture.md`
- Workflow: `docs/development-workflow.md`
- Guia de metodologia y estudio: `docs/work-methodology/`

## Decisiones Base

- Plataforma: Android nativo.
- Lenguaje: Kotlin.
- UI: Jetpack Compose + Material 3.
- Persistencia: Room para datos estructurados.
- Preferencias: DataStore.
- Inyeccion de dependencias: Hilt.
- Procesamiento de anotaciones: KSP.
- Sync/Firebase: fuera del MVP inicial.

## Arquitectura

La app sigue una arquitectura sencilla por features:

```text
com.alvarocervantes.fittrackplus
+-- core
+-- data
+-- domain
+-- feature
```

Reglas principales:

- Las pantallas Compose pintan estado y envian eventos.
- Los ViewModels expondran `UiState` con `StateFlow`.
- La logica de negocio vive en repositorios o casos de uso.
- Room es la fuente de verdad local.
- El historial guarda snapshots para no depender de la rutina actual.

## Comandos

En Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat test
```

Cuando existan pruebas instrumentadas:

```powershell
.\gradlew.bat connectedAndroidTest
```

## Roadmap

1. Base movil seria: Compose, Hilt, Room, DataStore, Navigation y docs.
2. Rutinas: crear, editar, archivar y seleccionar rutina activa.
3. Registro: iniciar sesion desde rutina, registrar series y finalizar.
4. Historial: listar sesiones y mostrar detalle historico. Completado tecnicamente.
5. Estadisticas MVP: progreso por ejercicio, volumen y mejores marcas. Completado tecnicamente.
6. Pulido UX: estados, accesibilidad, errores y recorrido de usuario nuevo.
7. Sync futura: evaluar Firebase cuando el nucleo local este estable.

## Metodologia

Este proyecto usa entrega por fases:

- una rama por fase
- alcance pequeno y cerrado
- verificacion automatica
- verificacion manual
- documentacion actualizada
- guia de metodologia actualizada al cerrar fase

La metodologia completa esta en `docs/development-workflow.md`.
